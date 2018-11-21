package com.aicas.fischertechnik.app;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.aicas.fischertechnik.app.sorting.AicasTxtSortingLogic;
import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	static ServiceTracker<AicasTxtDriverInterface, AicasTxtDriverInterface> driverServiceTracker;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		driverServiceTracker = new ServiceTracker<AicasTxtDriverInterface, AicasTxtDriverInterface>
		    (bundleContext, AicasTxtDriverInterface.class, null);
		
		driverServiceTracker.open();
		
		context.registerService(AicasTxtSortingLogic.class, new AicasTxtStandardSortingLogic(), null);
		
		// get the driverService instead of assigning it
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
