package com.aicas.fischertechnik.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.Register;

public class Activator implements BundleActivator {

  private static BundleContext context;

  private static final int RETRY_INTERVAL = 5000;
  private static final int POLL_INTERVAL = 1000;
  private static final int MODBUS_UNIT_ID = 15;
  private static final String MODBUS_SLAVE_IP = "127.0.0.1";
  private static final int MODBUS_SLAVE_PORT = 12345;
  private static final int MODBUS_TIMEOUT = 3000;
  private static final boolean MODBUS_RECONNECT = true;
  private static final int REGISTER_REFERENCE_OFFSET = 0;
  private static final int REGISTER_READ_COUNT = 11;
  private static final int REGISTER_INDEX = 0;

  private ModbusTCPMaster master;

  boolean run = true;

  static BundleContext getContext() {
    return context;
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext bundleContext) throws Exception {
    Activator.context = bundleContext;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    executor.execute(new Runnable()
    {
      @Override
      public void run()
      {
        boolean reconnect = true;

        while (reconnect) {
          try {
            master = new ModbusTCPMaster(MODBUS_SLAVE_IP, MODBUS_SLAVE_PORT,
                MODBUS_TIMEOUT, MODBUS_RECONNECT);

            master.connect();

            reconnect = false;

          } catch (Exception e) {
            e.printStackTrace();
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e1) { e1.printStackTrace(); }
          }
        }

        while (run)
        {
          try
          {
            Register[] regs = master.readMultipleRegisters(MODBUS_UNIT_ID,
                REGISTER_REFERENCE_OFFSET,
                REGISTER_READ_COUNT);

            System.out.print("ModbusTestMaster: ");

            for (int i= 0; i < REGISTER_READ_COUNT; i++)
            {
              System.out.print(String.format("%d ", regs[i].getValue()));
            }

            System.out.println();

            Thread.sleep(1000);

          }
          catch (ModbusException | InterruptedException e)
          {
            e.printStackTrace();
          }
        }
      }
    });
  }

  /*
   * (non-Javadoc)
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    Activator.context = null;

    run = false;

    Thread.sleep(500);

    master.disconnect();
  }

}
