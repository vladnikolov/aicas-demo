package com.aicas.fischertechnik.app;

import com.aicas.fischertechnik.app.monitoring.StatusConnector;
import com.aicas.fischertechnik.app.sorting.AicasTxtSortingLogic;
import com.aicas.fischertechnik.app.sorting.AicasTxtSortingLogic.DetectedColor;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.LightBarrier;

// TODO: set-up RR scheduling for the worker threads
// TODO: measure the pure cpu costs of the workers and their wall execution time 
//       use costs for feasibility analysis - how much workers are possible in parallel at all ?
// TODO: bind each thread to a processing group, with replenishing period 500 ms and interpolated costs for that.

public class ObjectWorkerRunnable implements Runnable
{
    public int initialMotorCounter;
    public String name;
    public AicasTxtDriverInterface driverService;
    public StatusConnector statusConnector;
    
    int motorCounter;

    static volatile boolean motorStarted = false;
    static volatile boolean compressorActivated = false;
    static volatile int activeWorkers = 0;
    
    static final int DISTANCE_SAMPLING_REGION_START = 8;
    static final int DISTANCE_SAMPLING_REGION_END = 9;
    
    static final int DISTANCE_LIGHT_BARRIER_EJECTION= 14;
    static final int DISTANCE_VALVE_WHITE = 18;
    static final int DISTANCE_VALVE_RED = 24;
    static final int DISTANCE_VALVE_BLUE = 29;
    

    static final int COLOR_THRESHOLD_WHITE = 1100; // 1390
    static final int COLOR_THRESHOLD_RED = 1500;   // 1600
    static final int COLOR_THRESHOLD_BLUE = 1800;

    static final double SMOOTH_FACTOR = 0.5;
    
    AicasTxtSortingLogic sortingLogic;

    @Override
    public void run()
    {
        activeWorkers++;
        statusConnector.sendStatus(StatusConnector.OBJECT_ON_TRACK, activeWorkers);
        
        System.out.println(String.format("AicasRealtimeSorting: %s new object detected!", name));
        System.out.println(String.format("AicasRealtimeSorting: %s initial motor counter is = %d", name,
                initialMotorCounter)); 
        
        sortingLogic = Activator.sortingServiceTracker.getService();
        
        // activate the motor of the supply line
        if (!motorStarted)
        {
            driverService.rotateMotor(1, 1, 512, 0);
            motorStarted = true;
            statusConnector.sendStatus(StatusConnector.STATUS_MOTOR, 0);
        }

        // wait until the object left out of the first light barrier!
        // Note: here we use periodic check and sleep ... 
        while (!driverService.getLightBarrierState(LightBarrier.COLORSENSOR))
        {
            try
            {
                // give up control for some time               
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            continue;
        }
        
        statusConnector.sendStatus(StatusConnector.STATUS_LIGHT_BARRIER_COLORSENSOR, 1);
        
        System.out.println(
                String.format("AicasRealtimeSorting: %s left first light barrier with motor counter = %d",
                        name, driverService.getMotorCounter()));

        int colorSensorValue = 0;
        DetectedColor detectedColor = DetectedColor.NONE;

        System.out.println(String.format("AicasRealtimeSorting: %s sampling color value", name));

        int colorSampleRegionIn = initialMotorCounter + DISTANCE_SAMPLING_REGION_START;
        int colorSampleRegionOut = initialMotorCounter + DISTANCE_SAMPLING_REGION_END;
        
        int cnt;

        // periodic check and sleep. same story as above but with 40 ms (motor counter update frequency) 
        while ((cnt = driverService.getMotorCounter()) < colorSampleRegionIn)
        {
            try
            {                
                Thread.sleep(40);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            continue;
        }      

        // we are in the color sampling region - measure an exponentially smoothed object color value

        // initialize the history with the first sampled value
        colorSensorValue = driverService.getColorSensorValue();

        int out_cnt = 0;

        // sample and smooth to approximate the color value
        while (driverService.getMotorCounter() < colorSampleRegionOut)
        {
            int val = driverService.getColorSensorValue();
            System.out.println(String.format("%s color val = %d", name, val));            
            colorSensorValue = (int) (val * SMOOTH_FACTOR + colorSensorValue * (1 - SMOOTH_FACTOR));
            System.out.println(String.format("%s colorSensorValue = %d", name, colorSensorValue));
        }
        System.out.println();
        System.out.println(String.format("AicasRealtimeSorting: %s sampled color value %d", name,
                colorSensorValue));

        statusConnector.sendStatus(StatusConnector.DETECTED_COLOR, colorSensorValue);
        
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
                String.format("AicasRealtimeSorting: %s detected object color %s", name, detectedColor));

        // wait until the according object proceeds to proximity of the ejection light barrier
        while ((cnt = driverService.getMotorCounter()) < initialMotorCounter + DISTANCE_LIGHT_BARRIER_EJECTION)
        {
            try
            {
                Thread.sleep(40);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            } 
            continue;
        }       
        
        // periodic check and sleep - wait until the object crosses the ejection light barrier 
        while (driverService.getLightBarrierState(LightBarrier.EJECTION))
        {
            try
            {
                Thread.sleep(100);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            continue;
        }
        
//        System.out.println(String.format("AicasRealtimeSorting: %s crossed EJECTION barrier with motor counter = %d",
//                name, driverService.getMotorCounter()));
        
        statusConnector.sendStatus(StatusConnector.STATUS_LIGHT_BARRIER_EJECTION, 0);

        System.out.println(String.format("AicasRealtimeSorting: %s preparing ejection for %s", name, detectedColor));
        
        // in the mean time activate the compressor
        if (!compressorActivated)
        {
            driverService.activateCompressor();
            compressorActivated = true;
            statusConnector.sendStatus(StatusConnector.STATUS_COMPRESSOR, 1);
        }

        // wait until the object left out of the eject light barrier
        while (!driverService.getLightBarrierState(LightBarrier.EJECTION))
        {
            try
            {
                // here we sample twice as often - in proximity of the ejection valves we have to be more precise
                Thread.sleep(50);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            continue;
        }
        
        statusConnector.sendStatus(StatusConnector.STATUS_LIGHT_BARRIER_EJECTION, 0);

        // get the actual motor counter to compute the distance to the according ejection valve
        motorCounter = driverService.getMotorCounter();

        System.out.println(String.format("AicasRealtimeSorting: %s activating valve %s ", name, detectedColor));         
        
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

        activeWorkers--;
        
        statusConnector.sendStatus(StatusConnector.OBJECT_ON_TRACK, activeWorkers);
        
        // from here on we do not have to be time-lined

        // if this was the last object switch off compressor and motor
        if (activeWorkers == 0)
        {
            // stop the compressor
            if (compressorActivated)
            {
                driverService.stopCompressor();
                compressorActivated = false;
                statusConnector.sendStatus(StatusConnector.STATUS_COMPRESSOR, 0);
            }

            // stop the motor
            if (motorStarted)
            {
                driverService.stopMotor(1);
                motorStarted = false;
                statusConnector.sendStatus(StatusConnector.STATUS_MOTOR, 0);
            }
        }
        
        System.out.println(String.format("AicasParallelSorting: %s ready !\n\n\n", name));
    }
}
