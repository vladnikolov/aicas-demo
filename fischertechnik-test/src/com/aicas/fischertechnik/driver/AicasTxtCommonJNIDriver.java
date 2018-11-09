package com.aicas.fischertechnik.driver;

public class AicasTxtCommonJNIDriver
{
    /**
     * initializes the native TXT libraries
     */
    public native void initialize();
    
    /**
     * uninitializes the native TXT libraries
     */    
    public native void uninitialize();
    
    /***
     * Rotates a TXT motor with <code>id</code> for a certain <code>distance</code> 
     * and with a certain <code>speed</code>.
     * 
     * @param id - the motor ID (e.g. M1) as specified on the TXT controller
     * @param direction - a positive or a negative number determines the direction
     * @param speed - motor speed can be a value between 0 .. 512
     * @param distance - given in cycles with 1x cycle = 63, 2x cycles = 127, etc.
     */
    public native void rotateMotor(int id, int direction, int speed, int distance);
    
}
