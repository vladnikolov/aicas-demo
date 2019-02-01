package com.aicas.fischertechnik.app;

import com.aicas.fischertechnik.app.monitoring.StatusConnector;
import com.aicas.fischertechnik.app.sorting.AicasTxtSortingLogic;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface.Valve;

public class AicasTxtRWBSortingLogic implements AicasTxtSortingLogic
{
    AicasTxtDriverInterface driverService;
    StatusConnector statusConnector;

    @Override
    public void doSort(DetectedColor color, int motorCounter) throws InterruptedException
    {
        driverService = Activator.driverServiceTracker.getService();
        statusConnector = Activator.statusConnectorTracker.getService();

        if (driverService == null) {
            System.out.println("AicasTxtStandardSortingLogic: no driver service found");
            return;
        }

        switch (color)
        {
        case RED:
            while (driverService.getMotorCounter() < motorCounter + 1) {Thread.sleep(Activator.sleepDuration);};
            if (statusConnector != null) statusConnector.sendStatus(StatusConnector.STATUS_VALVE_WHITE, 1);
            driverService.activateValve(Valve.WHITE);
            Thread.sleep(500);
            if (statusConnector != null) statusConnector.sendStatus(StatusConnector.STATUS_VALVE_WHITE, 0);
            break;
        case WHITE:
            while (driverService.getMotorCounter() < motorCounter + 6) {Thread.sleep(Activator.sleepDuration);};
            if (statusConnector != null) statusConnector.sendStatus(StatusConnector.STATUS_VALVE_RED, 1);
            driverService.activateValve(Valve.RED);
            Thread.sleep(500);
            if (statusConnector != null) statusConnector.sendStatus(StatusConnector.STATUS_VALVE_RED, 0);
            break;
        case BLUE:
            while (driverService.getMotorCounter() < motorCounter + 11){Thread.sleep(Activator.sleepDuration);};
            if (statusConnector != null) statusConnector.sendStatus(StatusConnector.STATUS_VALVE_BLUE, 1);
            driverService.activateValve(Valve.BLUE);
            Thread.sleep(500);
            if (statusConnector != null) statusConnector.sendStatus(StatusConnector.STATUS_VALVE_BLUE, 0);
            break;
        case NONE:
            System.err.println("impossible");
        };
    }
}
