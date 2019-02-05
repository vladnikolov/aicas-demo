package com.aicas.fischertechnik.app;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.aicas.fischertechnik.app.monitoring.StatusConnector;

public class Activator implements BundleActivator {

    private static BundleContext context;

    ModbusStatusConnector connector = null;

    boolean modbusInitialized = false;

    static BundleContext getContext() {
        return context;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;

        connector = new ModbusStatusConnector();
        if (!connector.initialize()) {
            throw new RuntimeException("could not initialize modbus");
        }
        modbusInitialized = true;
        context.registerService(StatusConnector.class, connector, null);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if(modbusInitialized) {
            connector.uninitialize();
            modbusInitialized = false;
        }
        Activator.context = null;
    }
}
