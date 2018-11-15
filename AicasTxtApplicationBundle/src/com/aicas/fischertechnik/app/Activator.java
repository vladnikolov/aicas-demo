package com.aicas.fischertechnik.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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

        driverServiceRef = context.getServiceReference(AicasTxtDriverInterface.class);
        driverService = context.getService(driverServiceRef);

        System.out.println("AicasTxtApplication: TXT driver service instantiated\n");

        new Thread()
        {
            public void run()
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));                
                
                while (run)
                {
                    try
                    {
                        System.out.println("AicasTxtApplication: starting a sensor & actuator test");
                        System.out.println("------------------------------------------------------\n");
                        
                        System.out.print("AicasTxtApplication: trying to rotate TXT motor 1: ");
//                        System.out.println("<press enter>");
//                        br.readLine();

                        // this is an example how we should use the motor for the sorting machine
                        // deprecated: driverService.rotateMotor(1, 1, 512, 127);

                        // get the actual Motor Counter value
                        int motorCounter = driverService.getMotorCounter();
                        System.out.print("AicasTxtApplication: motor counter = " + motorCounter);
                        // set some motor target - e.g. move some object from LightBarrier.EJECTION to
                        // the White Valve and Tray
                        int motorTarget = motorCounter + 5;
                        driverService.rotateMotor(1, -1, 512, 0);
                        Thread.sleep(3000);
                        driverService.stopMotor(1);
                        // while (driverService.getMotorCounter() < motorTarget) {/* simply busy wait */}
                        System.out.print("AicasTxtApplication: motor counter = " + motorCounter);
                        
                        // optionally you can then
                        // driverService.resetMotorCounter();

                        System.out.println("AicasTxtApplication: motor rotation ready\n");

                        System.out.print("AicasTxtApplication: rotating TXT motor in opposite direction: ");
                        
                        Thread.sleep(5000);
                        
//                        System.out.println("<press enter>");
//                        
//                        br.readLine();

                        // get the actual Motor Counter value
                        motorCounter = driverService.getMotorCounter();
                        System.out.print("AicasTxtApplication: motor counter = " + motorCounter);
                        motorTarget = motorCounter + 5;
                        driverService.rotateMotor(1, 1, 512, 0);
                        Thread.sleep(3000);
                        driverService.stopMotor(1);
                        // while (driverService.getMotorCounter() < motorTarget) {/* simply busy wait */}
                        System.out.print("AicasTxtApplication: motor counter = " + motorCounter);
                        
                        // optionally you can then
                        driverService.resetMotorCounter();

                        System.out.println("AicasTxtApplication: motor inverse ready\n");

                        System.out.print("AicasTxtApplication: polling light barrier and color-sensor states:");
                        
                        Thread.sleep(5000);
                        
//                        System.out.println("<press enter>");
//                        br.readLine();

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

                        System.out.println(
                                "AicasTxtApplication: photo sensor value = " + driverService.getColorSensorValue());
                        
                        System.out.println("AicasTxtApplication: light barriers and color sensor checks ready\n");

                        System.out.print("AicasTxtApplication: checking compressor and valves: ");
                        
                        Thread.sleep(5000);
                        
//                        System.out.println("<press enter>");
//                        br.readLine();
                        
                        System.out.println(
                                "AicasTxtApplication: activating compressor: " + driverService.activateCompressor());
                        Thread.sleep(1000);
                        System.out.println("AicasTxtApplication: activating WHITE valve:  "
                                + driverService.activateValve(Valve.WHITE));

                        System.out.println(
                                "AicasTxtApplication: activating compressor: " + driverService.activateCompressor());
                        
                        Thread.sleep(1000);
                        
                        System.out.println("AicasTxtApplication: activating RED   valve:  "
                                + driverService.activateValve(Valve.RED));                        

                        System.out.println(
                                "AicasTxtApplication: activating compressor: " + driverService.activateCompressor());
                        
                        Thread.sleep(1000);
                        
                        System.out.println("AicasTxtApplication: activating BLUE  valve:  "
                                + driverService.activateValve(Valve.BLUE));
                        
//                        System.out.println("AicasTxtApplication: TXT sensors and actuators test done.");
//                        System.out.println("<press enter to repeat the tests>");
//                        br.readLine();
                        
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
