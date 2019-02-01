package com.aicas.fischertechnik.app;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.aicas.fischertechnik.app.monitoring.StatusConnector;


/**
 *  static final int STATUS_LIGHT_BARRIER_COLORSENSOR = 1;
    static final int STATUS_LIGHT_BARRIER_EJECTION = 2;
    static final int STATUS_MOTOR = 3;
    static final int STATUS_COMPRESSOR = 4;
    static final int OBJECT_ON_TRACK = 5;
    static final int ERROR = 6;
 */
public class Activator implements BundleActivator {

    private static BundleContext context;

    ModbusStatusConnector connector = null;
    
    boolean modbusInitialized = false;

    static BundleContext getContext() {
        return context;
    }

    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        
        connector = new ModbusStatusConnector();
        if (!connector.initialize()) {
            throw new RuntimeException("could not initialize modbus");
        }
        modbusInitialized = true;
        context.registerService(StatusConnector.class, new ModbusStatusConnector(), null);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        if(modbusInitialized) {
            connector.uninitialize();
        }
        Activator.context = null;
    }
}
