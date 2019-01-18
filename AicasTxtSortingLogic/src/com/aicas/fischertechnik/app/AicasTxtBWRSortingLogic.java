package com.aicas.fischertechnik.app;

import com.aicas.fischertechnik.app.sorting.AicasTxtSortingLogic;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.Valve;

public class AicasTxtBWRSortingLogic implements AicasTxtSortingLogic
{
    AicasTxtDriverInterface driverService;    
    
    @Override
    public void doSort(DetectedColor color, int motorCounter) throws InterruptedException
    {
        driverService = Activator.driverServiceTracker.getService();
        
        if (driverService == null) { 
            System.out.println("AicasTxtStandardSortingLogic: no driver service found");
            return;
        }        
        
        switch (color)
        {
        case BLUE:
            while (driverService.getMotorCounter() < motorCounter + 1) {Thread.sleep(Activator.sleepDuration);};
            driverService.activateValve(Valve.WHITE);
            break;
        case WHITE:
            while (driverService.getMotorCounter() < motorCounter + 6) {Thread.sleep(Activator.sleepDuration);};
            driverService.activateValve(Valve.RED);
            break;
        case RED:
            while (driverService.getMotorCounter() < motorCounter + 11) {Thread.sleep(Activator.sleepDuration);};
            driverService.activateValve(Valve.BLUE);
            break;
        case NONE:
            System.err.println("impossible");
        };
    }
}
