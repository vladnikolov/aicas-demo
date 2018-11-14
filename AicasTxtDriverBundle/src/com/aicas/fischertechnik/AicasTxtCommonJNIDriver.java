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
    
    @Override
    public native void stopMotor(int id); 

    @Override
    public boolean getLightBarrierState(LightBarrier lightBarrier)
    {
        switch(lightBarrier) {
        case COLORSENSOR:
            return (readInput(1) != 0) ? true : false; 
        case EJECTION:
            return (readInput(3) != 0) ? true : false;
        case WHITE:
            return (readInput(4) != 0) ? true : false;
        case RED:
            return (readInput(5) != 0) ? true : false;
        case BLUE:
            return (readInput(6) != 0) ? true : false;
        }
        
        return false;
    }
    
    @Override
    public native int readInput(int id);
    
    @Override
    public native boolean writeOutput(int id, int value);

    @Override
    public int getColorSensorValue()
    {
        return readInput(2);
    }

    @Override
    public int getMotorCounter()
    {
        return readImpulseSamplerCounter();
    }
    
    @Override
    public void resetMotorCounter()
    {
        
    }
    
    private native int readImpulseSamplerCounter();
    
    private native void resetImpulseSamplerCounter();

    @Override
    public boolean activateValve(Valve valve)
    {
        switch(valve) {
        case WHITE:
            return writeOutput(5, 1);
        case RED:
            return writeOutput(6, 1);
        case BLUE:
            return writeOutput(7, 1);
        }        
        return false;
    }

    @Override
    public boolean activateCompressor()
    {
        return writeOutput(8, 1);
    }
}
