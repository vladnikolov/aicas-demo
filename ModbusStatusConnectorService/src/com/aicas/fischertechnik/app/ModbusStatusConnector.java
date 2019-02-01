package com.aicas.fischertechnik.app;

import com.aicas.fischertechnik.app.monitoring.StatusConnector;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;

public class ModbusStatusConnector implements StatusConnector
{
    ModbusSlave slave;
    SimpleProcessImage spi;

    static final int SLEEP_TIMER = 1000;
    static final int MODBUS_UNIT_ID = 15;
    static final int MODBUS_SLAVE_PORT = 12345;
    static final int MODBUS_SLAVE_POOL_SIZE = 5;

    public boolean initialize() {
        try
        {
            spi = new SimpleProcessImage(MODBUS_UNIT_ID);

            spi.addRegister(new SimpleRegister(0)); // STATUS_LIGHT_BARRIER_COLORSENSOR
            spi.addRegister(new SimpleRegister(1)); // STATUS_LIGHT_BARRIER_EJECTION
            spi.addRegister(new SimpleRegister(2)); // STATUS_MOTOR
            spi.addRegister(new SimpleRegister(3)); // STATUS_COMPRESSOR
            spi.addRegister(new SimpleRegister(4)); // OBJECT_ON_TRACK
            spi.addRegister(new SimpleRegister(5)); // ERROR
            spi.addRegister(new SimpleRegister(6)); // MOTOR_COUNTER
            spi.addRegister(new SimpleRegister(7)); // DETECTED_COLOR
            spi.addRegister(new SimpleRegister(8)); // STATUS_VALVE_WHITE
            spi.addRegister(new SimpleRegister(9)); // STATUS_VALVE_RED
            spi.addRegister(new SimpleRegister(10)); // STATUS_VALVE_BLUE

            System.out.println("Modbus Slave listening: " + MODBUS_SLAVE_PORT);
            slave = ModbusSlaveFactory.createTCPSlave(MODBUS_SLAVE_PORT,
                    MODBUS_SLAVE_POOL_SIZE);
            slave.addProcessImage(MODBUS_UNIT_ID, spi);
            slave.open();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void uninitialize() {
        slave.close();
    }

    @Override
    public synchronized boolean sendStatus(int id, int value)
    {
        slave.getProcessImage(MODBUS_UNIT_ID).getRegister(id).setValue(value);
        return true;
    }

}
