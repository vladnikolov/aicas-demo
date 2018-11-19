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
                        System.out.println("AicasTxtParallelSorting: starting a new sorting loop");
                        System.out.println("------------------------------------------------------\n");                       
                        
                        System.out.println("AicasTxtParallelSorting: waiting for a new object ...");
                        
                        // wait until an object crosses the first light barrier
                        while (driverService.getLightBarrierState(LightBarrier.COLORSENSOR)) {
                            continue;
                        }
                        
                        int motorCounter = driverService.getMotorCounter();
                        
                        System.out.println("AicasTxtParallelSorting: new object detected!");
                        System.out.println("AicasTxtParallelSorting: motor counter is: " + motorCounter);
                        
                        // activate the motor of the supply line
                        driverService.rotateMotor(1, 1, 512, 0);
                        
                        // wait until the object left out of the first light barrier
                        while (!driverService.getLightBarrierState(LightBarrier.COLORSENSOR)) {
                            continue;
                        }            
                      
                        int colorSensorValue = 0;
                        DetectedColor detectedColor = DetectedColor.NONE;
                        
                        System.out.println("AicasTxtParallelSorting: sampling color value");
                        
                        int colorSampleRegionIn = motorCounter + 8;
                        int colorSampleRegionOut = motorCounter + 9;
                        
                        while (driverService.getMotorCounter() < colorSampleRegionIn) {
                            continue;
                        }
                        
                        // measure an exponentially smoothed object color value
                        while (driverService.getMotorCounter() < colorSampleRegionOut) {
                            int val = driverService.getColorSensorValue();
                            System.out.println("color val = " + val);
                            colorSensorValue = (int) (driverService.getColorSensorValue() * 0.25 + colorSensorValue * 0.75);
                            System.out.println("AicasTxtParallelSorting: colorSensorValue = " + colorSensorValue);
                        }                       
                        
                        System.out.println("AicasTxtParallelSorting: final color sensor value " + colorSensorValue);
                        
                        // decide whether the is object white, blue or red
                        // if (colorSensorValue < 1390) {
                        if (colorSensorValue < 1000) {
                            detectedColor = DetectedColor.WHITE;
                        // } else if (colorSensorValue < 1600) {
                        } else if (colorSensorValue < 1400) {
                            detectedColor = DetectedColor.RED;
                        } else {
                            detectedColor = DetectedColor.BLUE;
                        }
                                                
                        System.out.println("AicasTxtParallelSorting: detected object color " + detectedColor);
                        
                        // wait until the object crosses the light barrier of the eject part
                        while (driverService.getLightBarrierState(LightBarrier.EJECTION)) {
                            continue;
                        }
                        
                        // in the mean time activate the compressor
                        driverService.activateCompressor();
                        
                        // wait until the object left out of the eject light barrier
                        while (!driverService.getLightBarrierState(LightBarrier.EJECTION)) {
                            continue;
                        }   
                        
                        // get the actual motor counter to compute the distance to the according ejection valve
                        motorCounter = driverService.getMotorCounter();
                        
                        switch (detectedColor) {
                        case WHITE:
                            while (driverService.getMotorCounter() < motorCounter + 1);
                            driverService.activateValve(Valve.WHITE);
                            break;
                        case RED:
                            while (driverService.getMotorCounter() < motorCounter + 6);
                            driverService.activateValve(Valve.RED);
                            break;
                        case BLUE:
                            while (driverService.getMotorCounter() < motorCounter + 11);
                            driverService.activateValve(Valve.BLUE);
                            break;                            
                        case NONE:
                            System.err.println("impossible");
                        };
                        
                        // stop the compressor                         
                        driverService.stopCompressor();
                        
                        // stop the motor
                        driverService.stopMotor(1);
                        
                        // loop waiting for next object
                    } 
                    catch (Exception e)
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
