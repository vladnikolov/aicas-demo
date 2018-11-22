package com.aicas.fischertechnik.app.sorting;

public interface AicasTxtSortingLogic
{
    enum DetectedColor
    {
        WHITE, RED, BLUE, NONE
    }
    
    void doSort(DetectedColor color, int motorCounter) throws InterruptedException;
}
