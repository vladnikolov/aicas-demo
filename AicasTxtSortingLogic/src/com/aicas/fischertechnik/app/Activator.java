package com.aicas.fischertechnik.app;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.aicas.fischertechnik.app.monitoring.StatusConnector;
import com.aicas.fischertechnik.app.sorting.AicasTxtSortingLogic;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;

public class Activator implements BundleActivator {

	private static BundleContext context;

	// set up a sleep duration for the worker, while waiting for the object to approach to its ejection valve
	// Note: since we sync against the motor counter, which is sampled with 25 Hz (i.e. 40 ms period),
	// we need to detect each motor counter change

	static final int sleepDuration = 40;

	static ServiceTracker<AicasTxtDriverInterface, AicasTxtDriverInterface> driverServiceTracker;
	static ServiceTracker<StatusConnector,StatusConnector> statusConnectorTracker;

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

		driverServiceTracker = new ServiceTracker<AicasTxtDriverInterface, AicasTxtDriverInterface>
		    (bundleContext, AicasTxtDriverInterface.class, null);

		driverServiceTracker.open();

		statusConnectorTracker = new ServiceTracker<StatusConnector, StatusConnector>
            (bundleContext, StatusConnector.class, null);

		statusConnectorTracker.open();

		// context.registerService(AicasTxtSortingLogic.class, new AicasTxtStandardSortingLogic(), null);
		// context.registerService(AicasTxtSortingLogic.class, new AicasTxtBWRSortingLogic(), null);
		context.registerService(AicasTxtSortingLogic.class, new AicasTxtRWBSortingLogic(), null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
  public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}
}
