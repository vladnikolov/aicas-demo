package com.aicas.fischertechnik;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.aicas.fischertechnik.app.monitoring.StatusConnector;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;

public class Activator implements BundleActivator {

  private static BundleContext context;

  AicasTxtDriverInterface driver = new AicasTxtCommonJNIDriver();

  static ServiceTracker<StatusConnector,StatusConnector> statusConnectorTracker;

  static BundleContext getContext() {
    return context;
  }

  boolean run = true;
  StatusConnector statusConnector;

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext bundleContext) throws Exception {
    Activator.context = bundleContext;

    driver.initTxt();

    context.registerService(AicasTxtDriverInterface.class, driver, null);

    statusConnectorTracker = new ServiceTracker<StatusConnector, StatusConnector>
      (bundleContext, StatusConnector.class, null);
    statusConnectorTracker.open();

    statusConnector = statusConnectorTracker.getService();

    RealtimeThread counterThread = new RealtimeThread(
        new PriorityParameters(PriorityScheduler.MAX_PRIORITY -1),
        new PeriodicParameters(new RelativeTime(40, 0)))
    {

      int last_cnt = 0;

      @Override
      public void run()
      {
        while (run)
        {
          int cnt = ((AicasTxtCommonJNIDriver) driver).readImpulseSamplerCounter();
          // System.err.println("ftX1in.cnt_in[0] = " + cnt);

          if (cnt == 0)
          {
            if (last_cnt == 1)
            {
              AicasTxtCommonJNIDriver.globalMotorCounter++;
              if (statusConnector != null) {
                statusConnector.sendStatus(StatusConnector.MOTOR_COUNTER, AicasTxtCommonJNIDriver.globalMotorCounter);
              }

              last_cnt = 0;
            }
          } else
          {
            if (last_cnt == 0)
            {
              last_cnt = 1;
            }
          }

          waitForNextPeriod();
        }
      }
    };

    counterThread.start();
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    Activator.context = null;
    run = false;
    driver.uninitTxt();
  }

}
