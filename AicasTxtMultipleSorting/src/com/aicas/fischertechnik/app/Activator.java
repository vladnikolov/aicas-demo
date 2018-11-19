package com.aicas.fischertechnik.app;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;

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
    
    HashSet<ObjectWorkerThread> workerSet = new HashSet<ObjectWorkerThread>(); 
    
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
        RealtimeThread outerThread = new RealtimeThread(
                new PriorityParameters(PriorityScheduler.instance().getMaxPriority() - 3), 
                new PeriodicParameters(new RelativeTime(100, 0)))        
        {
            public void run()
            {
                System.out.println("AicasTxtMultipleSorting: waiting for new object ...");
                while (run)
                {
                    try
                    {
//                        // wait until an object crosses the first light barrier
//                        while (driverService.getLightBarrierState(LightBarrier.COLORSENSOR))
//                        {
//                            continue;
//                        }
                        
                        if (!driverService.getLightBarrierState(LightBarrier.COLORSENSOR))
                        {

                            int motorCounter = driverService.getMotorCounter();

                            ObjectWorkerThread objectWorkerThread = new ObjectWorkerThread(driverService, motorCounter);
                            workerSet.add(objectWorkerThread);
                            objectWorkerThread.start();
                        }

                        waitForNextPeriod();
                        // loop waiting for next object
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
        };
        
        outerThread.setName("WorkerThread-" + ++workerThreadCounter);
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
        for (ObjectWorkerThread t : workerSet) {
            t.join();
        }
    }

}
