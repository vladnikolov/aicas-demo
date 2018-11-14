package com.aicas.fischertechnik.app;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.LightBarrier;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.Valve;

public class Activator implements BundleActivator
{

    private static BundleContext context;

    ServiceReference<AicasTxtDriverInterface> driverServiceRef;

    AicasTxtDriverInterface driverService;
    
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

        System.out.println("AicasTxtApplication: starting");

        System.out.println("AicasTxtApplication: querying TXT driver service");

        Thread.sleep(3000);

        driverServiceRef = context.getServiceReference(AicasTxtDriverInterface.class);
        driverService = context.getService(driverServiceRef);

        System.out.println("AicasTxtApplication: TXT driver service instantiated");
        
        new Thread()
        {
            public void run()
            {
                while (run)
                {

                    System.out.println("AicasTxtApplication: trying to rotate TXT motor 1");

                    // this is an example how we should use the motor for the sorting machine
                    // deprecated: driverService.rotateMotor(1, 1, 512, 127);
                    
                    
                    // get the actual Motor Counter value
                    int motorCounter = driverService.getMotorCounter();
                    // set some motor target - e.g. move some object from LightBarrier.EJECTION to the White Valve and Tray
                    int motorTarget = motorCounter + 5; 
                    driverService.rotateMotor(1, 1, 512, 0);
                    while (driverService.getMotorCounter() < motorTarget) {/* simply busy wait */}
                    // optionally you can then 
                    driverService.resetMotorCounter();
                    
                    System.out.println("AicasTxtApplication: motor rotation ready");                
                    
                    try
                    {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1)
                    {
                        e1.printStackTrace();
                    }
                    
                    System.out.println("AicasTxtApplication: rotating TXT motor in opposite direction");
                    
                    // get the actual Motor Counter value
                    motorCounter = driverService.getMotorCounter();
                    motorTarget = motorCounter + 5; 
                    driverService.rotateMotor(1, -1, 512, 0);
                    while (driverService.getMotorCounter() < motorTarget) {/* simply busy wait */}
                    // optionally you can then 
                    driverService.resetMotorCounter();

                    System.out.println("AicasTxtApplication: motor inverse ready");

                    try
                    {
                        Thread.sleep(1000);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    System.out.println("AicasTxtApplication: polling ligh barrier states:");
                    
                    System.out.println("AicasTxtApplication: Getting state of LightBarrier @ ColorSensor: "
                            + driverService.getLightBarrierState(LightBarrier.COLORSENSOR));
                    System.out.println("AicasTxtApplication: Getting state of LightBarrier @ Ejection   : "
                            + driverService.getLightBarrierState(LightBarrier.EJECTION));
                    System.out.println("AicasTxtApplication: Getting state of LightBarrier @ White      : "
                            + driverService.getLightBarrierState(LightBarrier.WHITE));
                    System.out.println("AicasTxtApplication: Getting state of LightBarrier @ Red        : "
                            + driverService.getLightBarrierState(LightBarrier.RED));
                    System.out.println("AicasTxtApplication: Getting state of LightBarrier @ Blue       : "
                            + driverService.getLightBarrierState(LightBarrier.BLUE));

                    System.out.println("AicasTxtApplication: light barrier states ready");
                    
                    try
                    {
                        Thread.sleep(1000);
                    } 
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    
                    System.out.println("AicasTxtApplication: photo sensor value = " + driverService.getColorSensorValue());
                    
                    System.out.println("AicasTxtApplication: activating compressor: " + driverService.activateCompressor());
                    System.out.println("AicasTxtApplication: activating WHITE valve:  " + driverService.activateValve(Valve.WHITE));
                    
                    System.out.println("AicasTxtApplication: activating compressor: " + driverService.activateCompressor());
                    System.out.println("AicasTxtApplication: activating RED   valve:  " + driverService.activateValve(Valve.RED));
                    
                    System.out.println("AicasTxtApplication: activating compressor: " + driverService.activateCompressor());
                    System.out.println("AicasTxtApplication: activating BLUE  valve:  " + driverService.activateValve(Valve.BLUE));
                }
            };
        }.start();     
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
    }

}
