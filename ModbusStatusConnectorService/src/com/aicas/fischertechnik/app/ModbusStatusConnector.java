package com.aicas.fischertechnik.app;

import com.aicas.fischertechnik.app.monitoring.StatusConnector;

public class ModbusStatusConnector implements StatusConnector
{

    @Override
    public boolean sendStatus(int id, int value)
    {
        return false;
    }

}
