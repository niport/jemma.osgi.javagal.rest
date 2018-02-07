/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2013 Telecom Italia (http://www.telecomitalia.it)
 *
 * JEMMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) version 3
 * or later as published by the Free Software Foundation, which accompanies
 * this distribution and is available at http://www.gnu.org/licenses/lgpl.html
 *
 * JEMMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License (LGPL) for more details.
 */
package org.energy_home.jemma.javagal.rest;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestXmlComponent {

	private final Logger LOG = LoggerFactory.getLogger("REST-XML");

	private final static String configFilePath = "config.properties";

	private ComponentFactory componentFactory;

	private RestManager restManager;

	private PropertiesManager configuration;

	protected void activate(BundleContext bc) {
		synchronized (this) {
			LOG.debug("REST Service started");
		}
	}

	protected void deactivate() {
		synchronized (this) {
			LOG.debug("Dectivated");
		}
	}

	protected void bindComponentFactory(ComponentFactory s) {
		synchronized (this) {
			if (configuration == null) {
				BundleContext bc = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
				configuration = new PropertiesManager(bc.getBundle().getResource(configFilePath));
			}
			this.componentFactory = s;
			this.restManager = new RestManager(configuration, s);
			this.restManager.start();
		}
	}

	protected void unbindComponentFactory(ComponentFactory s) {
		synchronized (this) {
			if (this.componentFactory == s) {
				if (restManager != null) {
					this.restManager.stop();
					this.restManager = null;
				}
				this.componentFactory = null;
			}
		}
	}
}
