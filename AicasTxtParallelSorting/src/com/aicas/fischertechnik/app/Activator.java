package com.aicas.fischertechnik.app;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;

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

    // actually we do not need a service tracker customizer

    //        ServiceTrackerCustomizer<AicasTxtSortingLogic, AicasTxtSortingLogic> sortingServiceCustomizer = 
    //                new ServiceTrackerCustomizer<AicasTxtSortingLogic, AicasTxtSortingLogic>() {
    //    
    //                    @Override
    //                    public AicasTxtSortingLogic addingService(ServiceReference<AicasTxtSortingLogic> reference)
    //                    {
    //                        @SuppressWarnings("unchecked")
    //                        AicasTxtSortingLogic result = (AicasTxtSortingLogic) context.getService(reference);
    //                        return result;
    //                    }
    //    
    //                    @Override
    //                    public void modifiedService(ServiceReference<AicasTxtSortingLogic> reference,
    //                            AicasTxtSortingLogic service)
    //                    {
    //                        // TODO Auto-generated method stub
    //                        
    //                    }
    //    
    //                    @Override
    //                    public void removedService(ServiceReference<AicasTxtSortingLogic> reference,
    //                            AicasTxtSortingLogic service)
    //                    {
    //                        context.ungetService(reference);
    //                    }
    //                    
    //                };   

    // Provide a custom ThreadPoolExecutor for RT object worker threads 
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
         * 70 % for RT Workers controlling the line - handled here
         * 
         * Each RT worker is encapsulated within a (Deferrable) server (a PGP) with period (in ms) and a cost (in ms),
         * i.e. a flat task:server = 1:1 model.
         * This ensures temporal isolation at one hand, but also preemption of eager workers at least with PGP period rate.
         * The PGP period value can be adjusted to a smaller number if workers do not react timely on sensors or actuators. 
         * However, we expect that workers will occasionally block preserving a fifo exec order on the shared priority level.
         * Note that cost < period is assumed, otherwise one server could over-utilize the whole system CPU resources.
         * All worker threads execute at the same FIFO priority level, while the outer thread operates one level above.
         * 
         * Priority Distribution:
         * Max - 1: Driver / Motor Counter Periodic Thread
         * Max - 2: outer thread / sampling for incoming objects
         * Max - 3: all Worker Threads
         */

        int workersPriority = PriorityScheduler.instance().getMinPriority();

        public AperiodicWorkerExecutor()
        {
            super(poolCoreSize , poolMaxSize , timeoutInMin, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

            ThreadFactory threadFactory = new ThreadFactory()
            {        
                @Override
                public Thread newThread(Runnable r)
                {
                    Thread t = new Thread(r);
                    t.setPriority(workersPriority);
                    return t;
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

        sortingServiceTracker = new ServiceTracker<AicasTxtSortingLogic, AicasTxtSortingLogic>
        (bundleContext, AicasTxtSortingLogic.class, null);

        sortingServiceTracker.open();

        System.out.println("-----------------------------------------------------------");
        System.out.println("-             aicas multiple sorting application          -");
        System.out.println("-----------------------------------------------------------");
        System.out.println();

        System.out.println("AicasRealtimeSorting: starting");

        // start the executor service early, because we pre-start all core threads
        aperiodicExecutor = new AperiodicWorkerExecutor();

        System.out.println("AicasRealtimeSorting: querying TXT driver service");

        driverServiceRef = context.getServiceReference(AicasTxtDriverInterface.class);

        driverService = context.getService(driverServiceRef);

        System.out.println("AicasRealtimeSorting: TXT driver service instantiated\n");

        driverService.stopMotor(1);

        driverService.stopCompressor();
        
        // TODO: add a server for the outer thread with 10 % bandwidth 

        /**
         * The outer thread basically samples the first light barrier for incoming objects.
         * If a new object is detected, then the outer thread uses the executor to trigger further processing.
         * The outer thread then waits until the object leaves the barrier before accepting other new objects. 
         */
        RealtimeThread outerThread = new RealtimeThread(
                // the outer thread priority must be higher as of the workers, since it creates workers
                new PriorityParameters(PriorityScheduler.instance().getMaxPriority() - 2), 
                // set a proper outer thread period (4-time a second might be sufficient)
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

                        // if the light barrier is on (not crossed)
                        if (driverService.getLightBarrierState(LightBarrier.COLORSENSOR)) {
                            // if an object was already detected 
                            if(newObjectDetected) {
                                // it just left the color sensor barrier
                                newObjectDetected = false;
                            } else {
                                // there is no object, just skip any action
                            }
                        }
                        // if the light barrier is off (crossed)
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

                                // if we reached poolMaxSize of objects we can not process this object
                                if(aperiodicExecutor.getActiveCount() == aperiodicExecutor.getMaximumPoolSize()) {
                                    System.err.println("AicasRealtimeSorting: object detected but cannot be processed: no workers!");
                                } else {
                                    aperiodicExecutor.execute(workerRunnable);
                                }

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
        // connection.disconnect();
    }

}
