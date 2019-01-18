package com.aicas.fischertechnik.app;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.realtime.AperiodicParameters;
import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.ProcessingGroupParameters;
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

    // ExecutorService executorService = Executors.newFixedThreadPool(7);

    //    ExecutorService executorService = Executors.newFixedThreadPool(7, new ThreadFactory()
    //    {        
    //        @Override
    //        public Thread newThread(Runnable r)
    //        {
    //            PriorityParameters priority = new PriorityParameters(PriorityScheduler.instance().getMaxPriority() - 3);
    //            AperiodicParameters aperiodic = new AperiodicParameters(); //can be customized with cost, deadline and handlers
    //            RealtimeThread rt = new RealtimeThread(priority, aperiodic, null, null, null, r);
    //            return rt;
    //        }
    //    });    

    // TODO: implement a custom ThreadPoolExecutor for RT object worker threads 
    // https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ThreadPoolExecutor.html

    public class AperiodicWorkerExecutor extends ThreadPoolExecutor {

        private static final int poolCoreSize = 7;
        private static final int poolMaxSize = 7;
        private static final int timeoutInMin = 10;

        /**
         * General (initial) resource (CPU time) model:
         * 10 % for Framework
         * 10 % outer periodic thread
         * 10 % non RT tasks
         * 70 % for RT Workers controlling the line
         * 
         * Each RT worker is encapsulated within a (Deferrable) server (a PGP) with period a (in ms) and a cost (in ms),
         * i.e. a flat task:server = 1:1 model.
         * This ensures temporal isolation at one hand, but also preemption of eager workers at least with PGP period rate.
         * The PGP period value can be adjusted to a smaller number of workers do not react timely on sensors or actuators. 
         * However, we expect that workers will occasionally block preserving a fifo exec order on the shared priority level.
         * Note that cost < period is assumed, otherwise one server could over-utilize the whole system CPU resources.
         * All worker threads execute at the same FIFO priority level, while the outer thread operates one level above.
         */

        // capacity for the RT workers  
        short rtCapacityInPercent = 70;

        // a common server period for all workers
        int pgpPeriod = 500;

        // the overall available CPU capacity for the workers
        int overalCapacity = (pgpPeriod * rtCapacityInPercent) / 100 ; 
        
        // the RT workers budget is equally divided between the maximum allowed parallel workers in the system 
        int pgpCosts = overalCapacity / poolMaxSize;

        int workersPriority = PriorityScheduler.instance().getMaxPriority() - 3;

        public AperiodicWorkerExecutor()
        {
            super(poolCoreSize , poolMaxSize , timeoutInMin, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

            ThreadFactory threadFactory = new ThreadFactory()
            {        
                @Override
                public Thread newThread(Runnable r)
                {
                    PriorityParameters priority = new PriorityParameters(workersPriority);
                    // aperiodic can be further customized with cost, deadline and handlers
                    AperiodicParameters aperiodic = new AperiodicParameters();                    
                    // assign a separate PGP instancefor each worker with proper budget and replenishment period 
                    ProcessingGroupParameters pgp = 
                            new ProcessingGroupParameters(null, new RelativeTime(pgpPeriod, 0), new RelativeTime() , null, null, null);
                    RealtimeThread rt = new RealtimeThread(priority, aperiodic, null, null, pgp, r);
                    return rt;
                }
            };

            setThreadFactory(threadFactory);

            prestartAllCoreThreads();
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r)
        {
            super.beforeExecute(t, r);
            System.out.println("starting " + r);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t)
        {
            super.afterExecute(r, t);
            System.out.println("terminating " + r);
        }
    };

    AperiodicWorkerExecutor aperiodicExecutor;

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

        System.out.println("AicasRealtimeSorting: starting");

        aperiodicExecutor = new AperiodicWorkerExecutor();

        System.out.println("AicasRealtimeSorting: querying TXT driver service");

        driverServiceRef = context.getServiceReference(AicasTxtDriverInterface.class);

        driverService = context.getService(driverServiceRef);

        System.out.println("AicasRealtimeSorting: TXT driver service instantiated\n");

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
                System.out.println("AicasRealtimeSorting: waiting for new object ...");

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
                                newObjectDetected = true;

                                // Activator.multiUserChat.sendMessage("LightBarrier.ColorSensor : true");

                                motorCounter = driverService.getMotorCounter();
                                ObjectWorkerThread workerRunnable = new ObjectWorkerThread();
                                workerRunnable.initialMotorCounter = motorCounter;
                                // TODO: all workers use the same service instance - is it thread safe?  
                                workerRunnable.driverService = driverService;
                                workerRunnable.name = "WorkerThread-" + ++workerThreadCounter;

                                aperiodicExecutor.execute(workerRunnable);

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
        if (aperiodicExecutor != null) {
            aperiodicExecutor.shutdown();
            aperiodicExecutor.awaitTermination(7, TimeUnit.SECONDS);
        }
        // sortingServiceTracker.close();
        connection.disconnect();
    }

}
