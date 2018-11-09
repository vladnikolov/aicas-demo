package com.aicas.fischertechnik.driver;

public interface AicasTxtDriverInterface
{
    /**
     * initializes the native TXT libraries
     */
    public int initTxt();
    
    /**
     * uninitializes the native TXT libraries
     */  
    public int uninitTxt();
    
    /***
     * Rotates a TXT motor with <code>id</code> for a certain <code>distance</code> 
     * and with a certain <code>speed</code>.
     * 
     * @param id - the motor ID (e.g. M1) as specified on the TXT controller
     * @param direction - a positive or a negative number determines the direction
     * @param speed - motor speed can be a value between 0 .. 512
     * @param distance - given in cycles with 1x cycle = 63, 2x cycles = 127, etc.
     * @return - the number of steps performed by the motor
     */
    public int rotateMotor(int id, int direction, int speed, int distance);
}
