package com.aicas.fischertechnik.app.sorting;

import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;

public interface AicasTxtSortingLogic
{
    enum DetectedColor
    {
        WHITE, RED, BLUE, NONE
    }
    
    void doSort(DetectedColor color, int motorCounter, AicasTxtDriverInterface driverService);
}
