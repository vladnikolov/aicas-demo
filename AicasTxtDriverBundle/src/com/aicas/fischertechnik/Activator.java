package com.aicas.fischertechnik;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	AicasTxtDriverInterface driver = new AicasTxtCommonJNIDriver();

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		driver.initTxt();
		
		context.registerService(AicasTxtDriverInterface.class, driver, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		driver.uninitTxt();
	}

}
