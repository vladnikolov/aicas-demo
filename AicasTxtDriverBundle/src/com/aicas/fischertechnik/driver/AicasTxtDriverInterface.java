package com.aicas.fischertechnik.driver;

public interface AicasTxtDriverInterface
{
    /**
     * Initialize the native TXT libraries.
     * @return 
     */
    public int initTxt();
    
    /**
     * Uninitialize the native TXT libraries.
     * @return
     */  
    public int uninitTxt();
    
    /**
     * Read input with <code>id</code> directly.
     * @param id - the input id as specified on the TXT
     * @return the actual value of the input.
     */
    public int readInput(int id);
    
    /**
     * Write directly to output <code>id</code>
     * @param id - the output id
     * @param value - the value to be written
     * @return 
     */
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
    
    /**
     * Light barrier enumeration for identification.
     */
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
    
    /**
     * Get the actual measured value of the color sensor.
     * @return - an integer representation of the color value as RGB.
     */
    int getColorSensorValue();
    
    /**
     * Gets the actual impulse sampler value attached to motor 01.
     * @return
     */
    int getImpulseSamplerValue();
    
    /**
     * Valve enumeration for identification.
     */
    public enum Valve {
      WHITE, RED, BLUE  
    };
    
    /**
     * Activates a certain valve.
     * @param valve
     * @return true on success, otherwise false.
     */
    boolean activateValve(Valve valve);
    
    /**
     * Activates the compressor.
     * @return
     */
    boolean activateCompressor();    
}
