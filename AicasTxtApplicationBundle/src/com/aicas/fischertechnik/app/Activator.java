package com.aicas.fischertechnik.app;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.aicas.fischertechnik.driver.AicasTxtDriverInterface;

public class Activator implements BundleActivator
{

    private static BundleContext context;
    
    ServiceReference<AicasTxtDriverInterface> driverServiceRef;
    
    AicasTxtDriverInterface driverService;

    static BundleContext getContext()
    {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception
    {
        Activator.context = bundleContext;
        
        System.out.println("AicasTxtApplication: starting");
        
        System.out.println("AicasTxtApplication: querying TXT driver service");
        
        Thread.sleep(3000);
        
        driverServiceRef = context.getServiceReference(AicasTxtDriverInterface.class);
        driverService = context.getService(driverServiceRef);
        
        System.out.println("AicasTxtApplication: TXT driver service instantiated");
        
        Thread.sleep(3000);
        
        System.out.println("AicasTxtApplication: trying to rotate TXT motor 1");
        
        Thread.sleep(3000);

        driverService.rotateMotor(1, 1, 512, 127);
        
        System.out.println("AicasTxtApplication: ready");        
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception
    {
        Activator.context = null;
    }

}
