package com.supermap.desktop.frame;


import com.supermap.desktop.Application;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.swing.*;

public class FrameActivator implements BundleActivator {

	ServiceRegistration<?> registration;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		System.out.println("Hello SuperMap  ===  Frame !!");
		
		Application.getActiveApplication().getPluginManager().addPlugin("SuperMap.Desktop.Frame", bundleContext.getBundle());	
		
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("Goodbye SuperMap === Frame!!");
		
		this.registration.unregister();
	}

}