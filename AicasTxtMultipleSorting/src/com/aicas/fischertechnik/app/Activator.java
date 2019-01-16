package com.aicas.fischertechnik.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.aicas.fischertechnik.app.sorting.AicasTxtSortingLogic;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.LightBarrier;

public class Activator implements BundleActivator
{

    static BundleContext context;

    ServiceReference<AicasTxtDriverInterface> driverServiceRef;

    AicasTxtDriverInterface driverService;
    
    static ServiceTracker<AicasTxtSortingLogic, AicasTxtSortingLogic> sortingServiceTracker;
    
    static MultiUserChat multiUserChat;
    AbstractXMPPConnection connection;
//    
//    ServiceTrackerCustomizer<AicasTxtSortingLogic, AicasTxtSortingLogic> sortingServiceCustomizer = 
//            new ServiceTrackerCustomizer<AicasTxtSortingLogic, AicasTxtSortingLogic>() {
//
//                @Override
//                public AicasTxtSortingLogic addingService(ServiceReference<AicasTxtSortingLogic> reference)
//                {
//                    @SuppressWarnings("unchecked")
//                    AicasTxtSortingLogic result = (AicasTxtSortingLogic) context.getService(reference);
//                    return result;
//                }
//
//                @Override
//                public void modifiedService(ServiceReference<AicasTxtSortingLogic> reference,
//                        AicasTxtSortingLogic service)
//                {
//                    // TODO Auto-generated method stub
//                    
//                }
//
//                @Override
//                public void removedService(ServiceReference<AicasTxtSortingLogic> reference,
//                        AicasTxtSortingLogic service)
//                {
//                    context.ungetService(reference);
//                }
//                
//            };
    
//    ExecutorService executorService = Executors.newFixedThreadPool(7, new ThreadFactory()
//    {
//        
//        @Override
//        public Thread newThread(Runnable r)
//        {
//            Thread t = new Thread();
//            t.setPriority(Thread.MAX_PRIORITY);
//            return t;
//        }
//    });
    
    // TODO: implement a custom ThreadPoolExecutor for RT object worker threads 
    // https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ThreadPoolExecutor.html
    
    ExecutorService executorService = Executors.newFixedThreadPool(7);
    
    int workerThreadCounter = 0;

    private boolean run = true;

    static BundleContext getContext()
    {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception
    {
        Activator.context = bundleContext;
        
        connection = XMPPClient.connect("colorsortingguisender", "password");
        MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
        multiUserChat = multiUserChatManager.getMultiUserChat("muc@conference.es-0226.aicas.burg");
        multiUserChat.createOrJoin("sender");
        
        sortingServiceTracker = new ServiceTracker<AicasTxtSortingLogic, AicasTxtSortingLogic>
            (bundleContext, AicasTxtSortingLogic.class, null);
    
        sortingServiceTracker.open();

        System.out.println("-----------------------------------------------------------");
        System.out.println("-             aicas multiple sorting application          -");
        System.out.println("-----------------------------------------------------------");
        System.out.println();

        System.out.println("AicasTxtMultipleSorting: starting");

        System.out.println("AicasTxtMultipleSorting: querying TXT driver service");

        driverServiceRef = context.getServiceReference(AicasTxtDriverInterface.class);
        
        driverService = context.getService(driverServiceRef);

        System.out.println("AicasTxtMultipleSorting: TXT driver service instantiated\n");

        driverService.stopMotor(1);
        
        driverService.stopCompressor();
        
        RealtimeThread outerThread = new RealtimeThread(
                // TODO: set a proper outer thread priority (higher as workers, since this one creates workers)
                new PriorityParameters(PriorityScheduler.instance().getMaxPriority() - 3), 
                // TODO: set a proper outer thread period (4-time a second might be sufficient)
                new PeriodicParameters(new RelativeTime(250, 0))) {
            
            boolean newObjectDetected = false;
            
            public void run()
            {
                System.out.println("AicasTxtMultipleSorting: waiting for new object ...");
                
                while (run)
                {
                    try
                    {
                        int motorCounter;
                        
                        // wait until an object crosses the first light barrier
                        
                        // if the light barrier is on (not crossed)
                        if (driverService.getLightBarrierState(LightBarrier.COLORSENSOR)) {
                            // if an object was already detected 
                            if(newObjectDetected) {
                                // it just left the color sensor barrier
                                newObjectDetected = false;
                            } else {
                                // otherwise there is no object, just skip any action
                            }
                        }
                        // if light barrier is off (crossed)
                        else if (!driverService.getLightBarrierState(LightBarrier.COLORSENSOR)) {
                            if (newObjectDetected) {
                                // object already detected but still intercepts the barrier
                                // do nothing, wait until it moves forward
                            } else {
                                // we just detected a new object
                                
//                              Activator.multiUserChat.sendMessage("LightBarrier.ColorSensor : true");

                                motorCounter = driverService.getMotorCounter();
                                ObjectWorkerThread workerRunnable = new ObjectWorkerThread();
                                workerRunnable.initialMotorCounter = motorCounter;
                                // TODO: all workers use the same service instance - is it thread safe?  
                                workerRunnable.driverService = driverService;
                                workerRunnable.name = "WorkerThread-" + ++workerThreadCounter;
                                
                                executorService.execute(workerRunnable);
                                
                                // TODO: profile cost of this worst case path
                                //       use pure cpu costs measurement with real-time clocks (linux)
                                //       plan the task cost and period accordingly
                            }
                        }
                        
                        // suspend until next period
                        waitForNextPeriod();                        
                    } 
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
        };
        
        outerThread.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception
    {
        Activator.context = null;
        run = false;
        executorService.shutdown();
        executorService.awaitTermination(7, TimeUnit.SECONDS);
        // sortingServiceTracker.close();
        connection.disconnect();
    }

}
