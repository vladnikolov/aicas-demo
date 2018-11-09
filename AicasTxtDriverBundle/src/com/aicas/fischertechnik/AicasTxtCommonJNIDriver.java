package com.aicas.fischertechnik;

import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;

public class AicasTxtCommonJNIDriver implements AicasTxtDriverInterface
{
    static {
        System.loadLibrary("AicasTxtCommonJNIDriver");
    }
    
    @Override
    public native int initTxt();
      
    @Override
    public native int uninitTxt();
    
    @Override
    public native int rotateMotor(int id, int direction, int speed, int distance);
    
}
