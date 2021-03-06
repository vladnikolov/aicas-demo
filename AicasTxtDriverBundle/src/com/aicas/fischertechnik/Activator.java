package com.aicas.fischertechnik;

import javax.realtime.PeriodicParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	AicasTxtDriverInterface driver = new AicasTxtCommonJNIDriver();

	static BundleContext getContext() {
		return context;
	}
	
	boolean run = true;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		driver.initTxt();
		
		context.registerService(AicasTxtDriverInterface.class, driver, null);
		
        RealtimeThread counterThread = new RealtimeThread(
                new PriorityParameters(PriorityScheduler.MAX_PRIORITY -1),
                new PeriodicParameters(new RelativeTime(40, 0)))
        {

            int last_cnt = 0;

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
                            // System.err.println(AicasTxtCommonJNIDriver.globalMotorCounter);
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
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		run = false;
		driver.uninitTxt();
	}

}
