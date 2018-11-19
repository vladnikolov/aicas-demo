package com.aicas.fischertechnik.app;

import javax.realtime.AperiodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RealtimeThread;

import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.LightBarrier;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.Valve;

public class ObjectWorkerThread extends RealtimeThread
{
    int initialMotorCounter;
    int motorCounter;
    AicasTxtDriverInterface driverService;
    
    static volatile boolean motorStarted = false;
    static volatile boolean compressorActivated = false;
    static volatile int activeWorkers = 0;
    
    private enum DetectedColor
    {
        WHITE, RED, BLUE, NONE,
    }
    
    static final int SAMPLING_REGION_START = 8;
    static final int SAMPLING_REGION_END = 9;

    static final int COLOR_THRESHOLD_WHITE = 1000;
    static final int COLOR_THRESHOLD_RED = 1400;
    static final int COLOR_THRESHOLD_BLUE = 1800;
    
    static final int EJECTION_DISTANCE = 14;
    
    public ObjectWorkerThread(AicasTxtDriverInterface driverService, int initialMotorCounter)
    {
        this.initialMotorCounter = initialMotorCounter;
        this.driverService = driverService;
        this.setSchedulingParameters(new PriorityParameters(PriorityScheduler.instance().getMaxPriority() - 2));
        this.setReleaseParameters(new AperiodicParameters());
    }
    
    @Override
    public void run()
    {
        activeWorkers++;
        System.out.println(String.format("AicasTxtMultipleSorting: %s new object detected!", this.getName()));
        // System.out.println("AicasTxtMultipleSorting: motor counter is: " +
        // motorCounter);

        // activate the motor of the supply line
        if(!motorStarted) {
            driverService.rotateMotor(1, 1, 512, 0);
            motorStarted = true;
        }

        // wait until the object left out of the first light barrier
        while (!driverService.getLightBarrierState(LightBarrier.COLORSENSOR))
        {
            continue;
        }

        int colorSensorValue = 0;
        DetectedColor detectedColor = DetectedColor.NONE;

        System.out.println(String.format("AicasTxtMultipleSorting: %s sampling color value", this.getName()));
        

        int colorSampleRegionIn = initialMotorCounter + SAMPLING_REGION_START;
        int colorSampleRegionOut = initialMotorCounter + SAMPLING_REGION_END;

        while (driverService.getMotorCounter() < colorSampleRegionIn)
        {
            continue;
        }

        // measure an exponentially smoothed object color value

        // get first sample as history
        colorSensorValue = driverService.getColorSensorValue();

        int out_cnt = 0;

        // sample and smooth to approximate color value
        while (driverService.getMotorCounter() < colorSampleRegionOut)
        {
            int val = driverService.getColorSensorValue();
            // System.out.println("color val = " + val);
            if ((out_cnt % 10) == 0)
                System.out.println(". ");
            // System.out.print(". ");
            // smooth factor is = 0.35
            colorSensorValue = (int) (driverService.getColorSensorValue() * 0.35
                    + colorSensorValue * 0.65);
            // System.out.println("AicasTxtMultipleSorting: colorSensorValue = " +
            // colorSensorValue);
            out_cnt++;
        }
        System.out.println();
        System.out.println(String.format("AicasTxtMultipleSorting: %s approximated color value %d", 
                this.getName(), colorSensorValue));

        // decide whether the is object white, blue or red
        // if (colorSensorValue < 1390) {
        if (colorSensorValue < COLOR_THRESHOLD_WHITE)
        {
            detectedColor = DetectedColor.WHITE;
            // } else if (colorSensorValue < 1600) {
        } else if (colorSensorValue < COLOR_THRESHOLD_RED)
        {
            detectedColor = DetectedColor.RED;
        } else
        {
            detectedColor = DetectedColor.BLUE;
        }

        System.out.println(String.format("AicasTxtMultipleSorting: %s detected object color %s", 
                this.getName(), detectedColor));
        

        // wait until the object crosses the light barrier of the eject part
        while ((driverService.getLightBarrierState(LightBarrier.EJECTION) && 
                (driverService.getMotorCounter() < initialMotorCounter + EJECTION_DISTANCE)))
        {
            continue;
        }

        System.out.println("AicasTxtMultipleSorting: preparing ejection for " + detectedColor);

        // in the mean time activate the compressor
        if (!compressorActivated) {
            driverService.activateCompressor();
            compressorActivated = true;
        }

        // wait until the object left out of the eject light barrier
        while (!driverService.getLightBarrierState(LightBarrier.EJECTION))
        {
            continue;
        }

        // get the actual motor counter to compute the distance to the according
        // ejection valve
        motorCounter = driverService.getMotorCounter();

        System.out.println("AicasTxtMultipleSorting: activating valve " + detectedColor);

        switch (detectedColor)
        {
        case WHITE:
            while (driverService.getMotorCounter() < motorCounter + 1)
                ;
            driverService.activateValve(Valve.WHITE);
            break;
        case RED:
            while (driverService.getMotorCounter() < motorCounter + 6)
                ;
            driverService.activateValve(Valve.RED);
            break;
        case BLUE:
            while (driverService.getMotorCounter() < motorCounter + 11)
                ;
            driverService.activateValve(Valve.BLUE);
            break;
        case NONE:
            System.err.println("impossible");
        }
        ;

        System.out.println("AicasTxtMultipleSorting: ready!\n\n\n");

        activeWorkers--;
        
        if (activeWorkers == 0) {
            // stop the compressor
            if (compressorActivated) {
                driverService.stopCompressor();
                compressorActivated = false;
            }

            // stop the motor
            if (motorStarted) {
                driverService.stopMotor(1);
                motorStarted = false;
            }
        }
    }
}
