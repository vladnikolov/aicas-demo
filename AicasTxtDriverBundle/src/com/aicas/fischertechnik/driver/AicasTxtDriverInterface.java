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
    
    public int readInput(int id);
    
    public boolean writeOutput(int id, int value);
    
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
    
    public enum LightBarrier {
        COLORSENSOR, EJECTION, WHITE, RED, BLUE 
    };
    
    /**
     * Gets the actual state of a light barrier.
     * 
     * @param an instance of type {@link LightBarrier}
     * @return a boolean value with, true = on and false = off
     */    
    boolean getLightBarrierState(LightBarrier lightBarrier);
    
    int getColorSensorValue();
    
    int getImpulseSamplerValue();
    
    public enum Valve {
      WHITE, RED, BLUE  
    };
    
    boolean activateValve(Valve valve);
    
    boolean activateCompressor();    
}
