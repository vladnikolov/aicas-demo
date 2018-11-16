package com.aicas.fischertechnik.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.LightBarrier;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.Valve;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
    ServiceReference<AicasTxtDriverInterface> driverServiceRef;

    AicasTxtDriverInterface driverService;

    private boolean run = true;

	static BundleContext getContext() {
		return context;
	}

	private enum DetectedColor  {
	    WHITE, RED, BLUE, NONE,
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
	    Activator.context = bundleContext;

        System.out.println("AicasTxtParallelSorting: starting");

        System.out.println("AicasTxtParallelSorting: querying TXT driver service");

        driverServiceRef = context.getServiceReference(AicasTxtDriverInterface.class);
        driverService = context.getService(driverServiceRef);

        System.out.println("AicasTxtParallelSorting: TXT driver service instantiated\n");
        
        driverService.stopMotor(1);

        // TODO: make as real-time thread
        new Thread()
        {
            public void run()
            {
                while (run)
                {
                    try
                    {
                        System.out.println("AicasTxtParallelSorting: starting a sensor & actuator test");
                        System.out.println("------------------------------------------------------\n");                       
                        
                        while (driverService.getLightBarrierState(LightBarrier.COLORSENSOR)) {
                            continue;
                        }
                        
                        driverService.rotateMotor(1, 1, 512, 0);
                        
                        while (!driverService.getLightBarrierState(LightBarrier.COLORSENSOR)) {
                            continue;
                        }
                        
                        // driverService.stopMotor(1);
                      
                        int colorSensorValue = 0; 
                                
                        for (colorSensorValue = driverService.getColorSensorValue(); 
                                (colorSensorValue > 0) && ((colorSensorValue < 3000));
                                colorSensorValue = driverService.getColorSensorValue()) {
                            Thread.sleep(50);
                        }
                        
                        DetectedColor detectedColor = DetectedColor.NONE;
                        
                        if (colorSensorValue < 1390) {
                            detectedColor = DetectedColor.WHITE;
                        } else if (colorSensorValue < 1600) {
                            detectedColor = DetectedColor.RED;
                        } else {
                            detectedColor = DetectedColor.BLUE;
                        }
                                                
                        System.out.println("AicasTxtParallelSorting: detected color " + detectedColor);
                        
                        while (driverService.getLightBarrierState(LightBarrier.EJECTION)) {}
                        
                        driverService.activateCompressor();
                        
                        int motorCounter = driverService.getMotorCounter();
                        
                        switch (detectedColor) {
                        case WHITE:
                            while (driverService.getMotorCounter() < motorCounter + 3);
                            driverService.activateValve(Valve.WHITE);
                            break;
                        case RED:
                            while (driverService.getMotorCounter() < motorCounter + 7);
                            driverService.activateValve(Valve.WHITE);
                            break;
                        case BLUE:
                            while (driverService.getMotorCounter() < motorCounter + 12);
                            driverService.activateValve(Valve.WHITE);
                            break;
                        case NONE:
                            System.err.println("impossible");
                        };
                        
                        
                        driverService.stopCompressor();
                        
                        driverService.stopMotor(1);
                        
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
        }.start();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		run = false;
	}

}
