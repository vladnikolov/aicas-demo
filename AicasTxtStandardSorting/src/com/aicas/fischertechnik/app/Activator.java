package com.aicas.fischertechnik.app;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
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
    
    static final int SAMPLING_REGION_START = 8;
    static final int SAMPLING_REGION_END = 9;
    
    static final int COLOR_THRESHOLD_WHITE = 1100;
    static final int COLOR_THRESHOLD_RED = 1500;
    static final int COLOR_THRESHOLD_BLUE = 1800;
    
    static final double SMOOTH_FACTOR = 0.5;

    private boolean run = true;

	static BundleContext getContext() {
		return context;
	}

	private enum DetectedColor  {
	    WHITE, RED, BLUE, NONE,
	}
	
    static MultiUserChat multiUserChat;
    AbstractXMPPConnection connection;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
	    Activator.context = bundleContext;
	    
        connection = XMPPClient.connect("colorsortingguisender", "password");
        MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
        multiUserChat = multiUserChatManager.getMultiUserChat("muc@conference.es-0226.aicas.burg");
        multiUserChat.createOrJoin("sender");

        System.out.println("AicasTxtStandardSorting: starting");

        System.out.println("AicasTxtStandardSorting: querying TXT driver service");

        driverServiceRef = context.getServiceReference(AicasTxtDriverInterface.class);
        driverService = context.getService(driverServiceRef);

        System.out.println("AicasTxtStandardSorting: TXT driver service instantiated\n");
        
        driverService.stopMotor(1);
        driverService.stopCompressor();

        // TODO: make as real-time thread
        new Thread()
        {
            public void run()
            {
                while (run)
                {
                    try
                    {
                        System.out.println("-----------------------------------------------");
                        System.out.println("-             aicas standard sorting          -");
                        System.out.println("-----------------------------------------------");
                        System.out.println();
                        
                        System.out.println("AicasTxtStandardSorting: waiting for new object ...");
                        
                        // wait until an object crosses the first light barrier
                        while (driverService.getLightBarrierState(LightBarrier.COLORSENSOR)) {
                            Thread.sleep(10);
                            continue;
                        }
                        
                        int motorCounter = driverService.getMotorCounter();
                        
                        System.out.println("AicasTxtStandardSorting: new object detected!");
                        // System.out.println("AicasTxtStandardSorting: motor counter is: " + motorCounter);
                        
                        // activate the motor of the supply line
                        driverService.rotateMotor(1, 1, 512, 0);
                        
                        // wait until the object left out of the first light barrier
                        while (!driverService.getLightBarrierState(LightBarrier.COLORSENSOR)) {
                            Thread.sleep(10);
                            continue;
                        }            
                        
                        Activator.multiUserChat.sendMessage("Motor.Rotating : true");
                        Activator.multiUserChat.sendMessage("Motor.Direction : 1");
                        Activator.multiUserChat.sendMessage("Motor.Speed : 512");
                        Activator.multiUserChat.sendMessage("Motor.Counter : " + motorCounter);
                      
                        int colorSensorValue = 0;
                        DetectedColor detectedColor = DetectedColor.NONE;
                        
                        System.out.println("AicasTxtStandardSorting: sampling color value ");
                        
                        int colorSampleRegionIn = motorCounter + SAMPLING_REGION_START;
                        int colorSampleRegionOut = motorCounter + SAMPLING_REGION_END;
                        
                        while (driverService.getMotorCounter() < colorSampleRegionIn) {
                            Thread.sleep(10);
                            continue;
                        }
                                                
                        // measure an exponentially smoothed object color value
                        
                        // get first sample as history
                        colorSensorValue = driverService.getColorSensorValue();
                        
                        // sample and smooth to approximate color value
                        while (driverService.getMotorCounter() < colorSampleRegionOut) {
                            int val = driverService.getColorSensorValue();
                            System.out.println("sampled color value = " + val);
                            colorSensorValue = (int) (driverService.getColorSensorValue() * SMOOTH_FACTOR + colorSensorValue * (1 - SMOOTH_FACTOR));
                            System.out.println("smoothed colorSensorValue = " + colorSensorValue);
                        }
                        System.out.println();
                        System.out.println("AicasTxtStandardSorting: approximated color value " + colorSensorValue);
                        
                        // decide whether the is object white, blue or red
                        // if (colorSensorValue < 1390) {
                        if (colorSensorValue < COLOR_THRESHOLD_WHITE) {
                            detectedColor = DetectedColor.WHITE;
                        // } else if (colorSensorValue < 1600) {
                        } else if (colorSensorValue < COLOR_THRESHOLD_RED) {
                            detectedColor = DetectedColor.RED;
                        } else {
                            detectedColor = DetectedColor.BLUE;
                        }
                                                
                        System.out.println("AicasTxtStandardSorting: detected object color " + detectedColor);
                        
                        // wait until the object crosses the light barrier of the eject part
                        while (driverService.getLightBarrierState(LightBarrier.EJECTION)) {
                            Thread.sleep(10);
                            continue;
                        }
                        
                        System.out.println("AicasTxtStandardSorting: preparing ejection for " + detectedColor);
                        
                        // in the mean time activate the compressor
                        driverService.activateCompressor();
                        
                        // wait until the object left out of the eject light barrier
                        while (!driverService.getLightBarrierState(LightBarrier.EJECTION)) {
                            Thread.sleep(10);
                            continue;
                        }   
                        
                        // get the actual motor counter to compute the distance to the according ejection valve
                        motorCounter = driverService.getMotorCounter();
                        
                        System.out.println("AicasTxtStandardSorting: activating valve " + detectedColor);
                        
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
                        
                        System.out.println("AicasTxtStandardSorting: ready!\n\n\n");
                        
                        // stop the compressor                         
                        driverService.stopCompressor();
                        
                        // stop the motor
                        driverService.stopMotor(1);
                        
                        multiUserChat.sendMessage("Motor.Rotating : false");
                        multiUserChat.sendMessage("Motor.Direction : 0");
                        multiUserChat.sendMessage("Motor.Speed : 0");
                        multiUserChat.sendMessage("Motor.Counter : " + driverService.getMotorCounter());
                        
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
		connection.disconnect();
	}

}
