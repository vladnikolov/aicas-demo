package com.aicas.fischertechnik.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.LightBarrier;

public class Activator implements BundleActivator
{

    private static BundleContext context;

    ServiceReference<AicasTxtDriverInterface> driverServiceRef;

    AicasTxtDriverInterface driverService;
    
    // HashSet<ObjectWorkerThread> workerSet = new HashSet<ObjectWorkerThread>(); 
    
    ExecutorService executorService = Executors.newFixedThreadPool(7, new ThreadFactory()
    {
        
        @Override
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread();
            t.setPriority(Thread.MAX_PRIORITY);
            return t;
        }
    });
    
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

        // TODO: make as real-time thread
//        RealtimeThread outerThread = new RealtimeThread(
//                new PriorityParameters(PriorityScheduler.instance().getMaxPriority() - 3), 
//                new PeriodicParameters(new RelativeTime(250, 0)))        
        Thread outerThread = new Thread("outer-loop-thread")
        {
            public void run()
            {
                System.out.println("AicasTxtMultipleSorting: waiting for new object ...");
                while (run)
                {
                    try
                    {
                        // wait until an object crosses the first light barrier
                        int motorCounter;
                        
                        while (driverService.getLightBarrierState(LightBarrier.COLORSENSOR)) {
                            continue;
                        }

                        motorCounter = driverService.getMotorCounter();
                        ObjectWorkerThread workerRunnable = new ObjectWorkerThread();
                        workerRunnable.initialMotorCounter = motorCounter;
                        workerRunnable.driverService = driverService;
                        workerRunnable.name = "WorkerThread-" + ++workerThreadCounter;
                        
                        executorService.execute(workerRunnable);

//                        ObjectWorkerThread objectWorkerThread = new ObjectWorkerThread(driverService, motorCounter);
//                        workerSet.add(objectWorkerThread);
//                        objectWorkerThread.setName("WorkerThread-" + ++workerThreadCounter);
//                        objectWorkerThread.start();
                        
                        while(driverService.getMotorCounter() < motorCounter + 2) {
                            /** wait until object leaves the first light barrier **/
                        }
                        
                        // wait for the object to leave the sensor region, otherwise we create several worker threads
                        while (!driverService.getLightBarrierState(LightBarrier.COLORSENSOR)) {
                            continue;
                        }

                        // waitForNextPeriod();
                        // loop waiting for next object
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
//        for (ObjectWorkerThread t : workerSet) {
//            t.join();
//        }
    }

}
