package com.aicas.fischertechnik;

import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;

public class AicasTxtCommonJNIDriver implements AicasTxtDriverInterface
{
    static {
        System.loadLibrary("AicasTxtCommonJNIDriver");
    }
    
    static volatile int globalMotorCounter = 0;
    
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
        boolean ret = true;
        int val = -1;
        switch(lightBarrier) {
        case COLORSENSOR:
            if ((val = readInput(1)) == 0)
                ret = false;
            else
                ret = true;
            break;
        case EJECTION:
            if ((val = readInput(3)) == 0)
                ret = false;
            else
                ret = true;
            break;
        case WHITE:
            if ((val = readInput(6)) == 0)
                ret = false;
            else
                ret = true;
            break;
        case RED:
            if ((val = readInput(7)) == 0)
                ret = false;
            else
                ret = true;
            break;
        case BLUE:
            if ((val = readInput(8)) == 0)
                ret = false;
            else
                ret = true;
            break;
        }
        
        return ret;
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
        return globalMotorCounter;
    }
    
    @Override
    public void resetMotorCounter()
    {
        resetImpulseSamplerCounter();
    }
    
    public native int readImpulseSamplerCounter();
    
    public native void resetImpulseSamplerCounter();

    @Override
    public boolean activateValve(Valve valve)
    {
        try
        {
            switch (valve)
            {
            case WHITE:
                writeOutput(5, 512);
                Thread.sleep(500);
                writeOutput(5, 0);
                break;
            case RED:
                writeOutput(6, 512);
                Thread.sleep(500);
                writeOutput(6, 0);
                break;
            case BLUE:
                writeOutput(7, 512);
                Thread.sleep(500);
                writeOutput(7, 0);
                break;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean activateCompressor()
    {
        return writeOutput(8, 512);
    }

    @Override
    public boolean stopCompressor()
    {
        
        return writeOutput(8, 0);
    }
}
