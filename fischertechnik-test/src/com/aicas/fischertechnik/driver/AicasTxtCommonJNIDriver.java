package com.aicas.fischertechnik.driver;

public class AicasTxtCommonJNIDriver
{
    public native void rotateMotor(int id, int direction, int speed, int duration);
    
}
