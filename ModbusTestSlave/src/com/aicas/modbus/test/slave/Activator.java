package com.aicas.modbus.test.slave;

import java.util.Random;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.aicas.fischertechnik.app.monitoring.StatusConnector;

public class Activator implements BundleActivator {

  private static BundleContext context;

  static BundleContext getContext() {
    return context;
  }

  StatusConnector statusConnector;

  boolean run = true;

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext bundleContext) throws Exception {
    Activator.context = bundleContext;

    ServiceTracker<StatusConnector, StatusConnector> statusConnectorServiceTracker =
        new ServiceTracker<StatusConnector, StatusConnector>(bundleContext, StatusConnector.class, null);

    statusConnectorServiceTracker.open();

    new Thread() {
      @Override
      public void run() {
        Random random = new Random(4711);

        while (run) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          if (statusConnector == null) {
            statusConnector = statusConnectorServiceTracker.getService();
            continue;
          }

          statusConnector.sendStatus(StatusConnector.ERROR, random.nextInt());
          statusConnector.sendStatus(StatusConnector.OBJECTS_ON_TRACK, random.nextInt());
          statusConnector.sendStatus(StatusConnector.STATUS_COMPRESSOR, random.nextInt());
          statusConnector.sendStatus(StatusConnector.STATUS_MOTOR, random.nextInt());
          statusConnector.sendStatus(StatusConnector.STATUS_LIGHT_BARRIER_COLORSENSOR, random.nextInt());
          statusConnector.sendStatus(StatusConnector.STATUS_LIGHT_BARRIER_EJECTION, random.nextInt());
          statusConnector.sendStatus(StatusConnector.MOTOR_COUNTER, random.nextInt());
          statusConnector.sendStatus(StatusConnector.DETECTED_COLOR, random.nextInt());
          statusConnector.sendStatus(StatusConnector.STATUS_VALVE_WHITE, random.nextInt());
          statusConnector.sendStatus(StatusConnector.STATUS_VALVE_RED, random.nextInt());
          statusConnector.sendStatus(StatusConnector.STATUS_VALVE_BLUE, random.nextInt());
        }
      };
    }.start();
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    Activator.context = null;
    run = false;
  }

}
