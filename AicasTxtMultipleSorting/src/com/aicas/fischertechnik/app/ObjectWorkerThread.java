package com.aicas.fischertechnik.app;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.osgi.framework.ServiceReference;

import com.aicas.fischertechnik.app.sorting.AicasTxtSortingLogic;
import com.aicas.fischertechnik.app.sorting.AicasTxtSortingLogic.DetectedColor;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.LightBarrier;

// public class ObjectWorkerThread extends RealtimeThread
// public class ObjectWorkerThread extends Thread
public class ObjectWorkerThread implements Runnable
{
    public int initialMotorCounter;
    public String name;
    public AicasTxtDriverInterface driverService;
    
    int motorCounter;

    static volatile boolean motorStarted = false;
    static volatile boolean compressorActivated = false;
    static volatile int activeWorkers = 0;

//    private enum DetectedColor
//    {
//        WHITE, RED, BLUE, NONE
//    }

//    static final int DISTANCE_SAMPLING_REGION_START = 8;
//    static final int DISTANCE_SAMPLING_REGION_END = 9;
    
    static final int DISTANCE_SAMPLING_REGION_START = 8;
    static final int DISTANCE_SAMPLING_REGION_END = 9;
    
    static final int DISTANCE_LIGHT_BARRIER_EJECTION= 14;
    static final int DISTANCE_VALVE_WHITE = 18;
    static final int DISTANCE_VALVE_RED = 24;
    static final int DISTANCE_VALVE_BLUE = 29;
    

    static final int COLOR_THRESHOLD_WHITE = 1100; // 1390
    static final int COLOR_THRESHOLD_RED = 1500;   // 1600
    static final int COLOR_THRESHOLD_BLUE = 1800;

    // static final double SMOOTH_FACTOR = 0.25;
    static final double SMOOTH_FACTOR = 0.5;
    
    AicasTxtSortingLogic sortingLogic;

    // public ObjectWorkerThread(AicasTxtDriverInterface driverService, int initialMotorCounter)
    public ObjectWorkerThread()
    {
//        this.initialMotorCounter = initialMotorCounter;
//        this.driverService = driverService;
//        this.setSchedulingParameters(new PriorityParameters(PriorityScheduler.instance().getMaxPriority() - 2));
//        this.setReleaseParameters(new AperiodicParameters());
    }

    @Override
    public void run()
    {
        activeWorkers++;
        System.out.println(String.format("AicasTxtMultipleSorting: %s new object detected!", name));
        System.out.println(String.format("AicasTxtMultipleSorting: %s initial motor counter is = %d", name,
                initialMotorCounter));

//        ServiceReference<AicasTxtSortingLogic> sortingLogicRef; 
//        int query_cnt = 0;
//        for (sortingLogicRef = Activator.context.getServiceReference(AicasTxtSortingLogic.class) ;
//                (sortingLogicRef == null) && (query_cnt < 5);
//                sortingLogicRef = Activator.context.getServiceReference(AicasTxtSortingLogic.class), query_cnt++); 
        
        sortingLogic = Activator.sortingServiceTracker.getService();
        
        // activate the motor of the supply line
        if (!motorStarted)
        {
            driverService.rotateMotor(1, 1, 512, 0);
            motorStarted = true;
            try
            {
                Activator.multiUserChat.sendMessage("Motor.Rotating : true");
                Activator.multiUserChat.sendMessage("Motor.Direction : 1");
                Activator.multiUserChat.sendMessage("Motor.Speed : 512");
                Activator.multiUserChat.sendMessage("Motor.Counter : " + initialMotorCounter);
            } catch (NotConnectedException e)
            {
            }
        }

        // wait until the object left out of the first light barrier
        while (!driverService.getLightBarrierState(LightBarrier.COLORSENSOR))
        {
            try
            {
                Thread.sleep(10);
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            continue;
        }

//        try
//        {
//            Activator.multiUserChat.sendMessage("LightBarrier.ColorSensor : false");
//        } catch (NotConnectedException e1)
//        {
//            e1.printStackTrace();
//        }
        
        System.out.println(
                String.format("AicasTxtMultipleSorting: %s left first light barrier with motor counter = %d",
                        name, driverService.getMotorCounter()));

        int colorSensorValue = 0;
        DetectedColor detectedColor = DetectedColor.NONE;

        System.out.println(String.format("AicasTxtMultipleSorting: %s sampling color value", name));

        int colorSampleRegionIn = initialMotorCounter + DISTANCE_SAMPLING_REGION_START;
        int colorSampleRegionOut = initialMotorCounter + DISTANCE_SAMPLING_REGION_END;
        
        int cnt;

        while ((cnt = driverService.getMotorCounter()) < colorSampleRegionIn)
        {
            try
            {                
                Thread.sleep(10);
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
            
            continue;
        }
        
//        try
//        {
//            Activator.multiUserChat.sendMessage("Motor.Counter : " + cnt);
//        } catch (NotConnectedException e2)
//        {
//            // TODO Auto-generated catch block
//            e2.printStackTrace();
//        }

        // measure an exponentially smoothed object color value

        // initialize history with first sample value
        colorSensorValue = driverService.getColorSensorValue();

        int out_cnt = 0;

        // sample and smooth to approximate color value
        while (driverService.getMotorCounter() < colorSampleRegionOut)
        {
            int val = driverService.getColorSensorValue();
            System.out.println(String.format("%s color val = %d", name, val));            
//            if ((out_cnt % 10) == 0)
//                System.out.print(". ");
            colorSensorValue = (int) (val * SMOOTH_FACTOR + colorSensorValue * (1 - SMOOTH_FACTOR));
            System.out.println(String.format("%s colorSensorValue = %d", name, colorSensorValue));
            // out_cnt++;
        }
        System.out.println();
        System.out.println(String.format("AicasTxtMultipleSorting: %s sampled color value %d", name,
                colorSensorValue));

        // decide whether the object is white, red or blue        
        if (colorSensorValue < COLOR_THRESHOLD_WHITE)
        {
            detectedColor = DetectedColor.WHITE;
        } 
        else if (colorSensorValue < COLOR_THRESHOLD_RED)
        {
            detectedColor = DetectedColor.RED;
        } 
        else
        {
            detectedColor = DetectedColor.BLUE;
        }

        System.out.println(
                String.format("AicasTxtMultipleSorting: %s detected object color %s", name, detectedColor));

        // wait until the according object proceeds to proximity of the ejection light barrier
        while ((cnt = driverService.getMotorCounter()) < initialMotorCounter + DISTANCE_LIGHT_BARRIER_EJECTION)
        {
            try
            {
                Thread.sleep(10);
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
            continue;
        }
        
//        try
//        {
//            Activator.multiUserChat.sendMessage("Motor.Counter : " + cnt);
//        } catch (NotConnectedException e2)
//        {
//            // TODO Auto-generated catch block
//            e2.printStackTrace();
//        }
        
        // wait until the object crosses the ejection light barrier 
        while (driverService.getLightBarrierState(LightBarrier.EJECTION))
        {
            try
            {
                Thread.sleep(10);
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            continue;
        }

//        try
//        {
//            Activator.multiUserChat.sendMessage("LightBarrier.Ejection : true");
//        } catch (NotConnectedException e1)
//        {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
        
//        System.out.println(String.format("AicasTxtMultipleSorting: %s crossed EJECTION barrier with motor counter = %d",
//                name, driverService.getMotorCounter()));

        System.out.println(String.format("AicasTxtMultipleSorting: %s preparing ejection for %s", name, detectedColor));
        
        // in the mean time activate the compressor
        if (!compressorActivated)
        {
            driverService.activateCompressor();
            compressorActivated = true;
        }

        // wait until the object left out of the eject light barrier
        while (!driverService.getLightBarrierState(LightBarrier.EJECTION))
        {
            try
            {
                Thread.sleep(10);
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            continue;
        }
        
//        try
//        {
//            Activator.multiUserChat.sendMessage("LightBarrier.Ejection : false");
//        } catch (NotConnectedException e1)
//        {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }

        // get the actual motor counter to compute the distance to the according
        // ejection valve
        motorCounter = driverService.getMotorCounter();

        System.out.println(String.format("AicasTxtMultipleSorting: %s activating valve %s ", name, detectedColor));         
        
        if(sortingLogic == null) {
            sortingLogic = Activator.sortingServiceTracker.getService();
        }
        
        if (sortingLogic != null) {
            try
            {
                sortingLogic.doSort(detectedColor, motorCounter);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        } else {
            System.out.println(String.format("AicasTxtStandardSortingLogic: %s no sorting service found", name));
            System.out.println(String.format("AicasTxtStandardSortingLogic: %s skipping sorting !", name));
        }
        
        // sortingLogic = Activator.sortingServiceTracker.getService();
        
//        switch (detectedColor)
//        {
//        case WHITE:
//            while (driverService.getMotorCounter() < motorCounter + 1);
//            driverService.activateValve(Valve.WHITE);
//            break;
//        case RED:
//            while (driverService.getMotorCounter() < motorCounter + 6);
//            driverService.activateValve(Valve.RED);
//            break;
//        case BLUE:
//            while (driverService.getMotorCounter() < motorCounter + 11);
//            driverService.activateValve(Valve.BLUE);
//            break;
//        case NONE:
//            System.err.println("impossible");
//        };
        
        // obviously our motor counter gets a small additive drift.
        // therefore better use the code above, since it syncs on the EJECTION light barrier
//        switch (detectedColor)
//        {
//        case WHITE:
//            while (driverService.getMotorCounter() < initialMotorCounter + DISTANCE_VALVE_WHITE);
//            driverService.activateValve(Valve.WHITE);
//            break;
//        case RED:
//            while (driverService.getMotorCounter() < initialMotorCounter + DISTANCE_VALVE_RED);
//            driverService.activateValve(Valve.RED);
//            break;
//        case BLUE:
//            while (driverService.getMotorCounter() < initialMotorCounter + DISTANCE_VALVE_BLUE);
//            driverService.activateValve(Valve.BLUE);
//            break;
//        case NONE:
//            System.err.println("impossible");
//        };

        activeWorkers--;

        // if this was the last object switch off compressor and motor
        if (activeWorkers == 0)
        {
            // stop the compressor
            if (compressorActivated)
            {
                driverService.stopCompressor();
                compressorActivated = false;
            }

            // stop the motor
            if (motorStarted)
            {
                driverService.stopMotor(1);
                motorStarted = false;
                try
                {
                    Activator.multiUserChat.sendMessage("Motor.Rotating : false");
                    Activator.multiUserChat.sendMessage("Motor.Direction : 0");
                    Activator.multiUserChat.sendMessage("Motor.Speed : 0");
                    Activator.multiUserChat.sendMessage("Motor.Counter : " + driverService.getMotorCounter());
                } catch (NotConnectedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println(String.format("AicasTxtMultipleSorting: %s ready !\n\n\n", name));
    }
}
