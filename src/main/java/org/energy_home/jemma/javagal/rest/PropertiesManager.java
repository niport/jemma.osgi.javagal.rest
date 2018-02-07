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
 *
 */
package org.energy_home.jemma.javagal.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties manager class.
 * <p>
 * Loads/saves from/to a ".properties" file the desired values for the JavaGal
 * execution. It's THE way to control a number of parameters at startup.
 * 
 * TODO: remove the PropertiesManager and replace it with the configuration admin.
 */
/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class PropertiesManager {
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesManager.class);
	public Properties props;

	public PropertiesManager(URL url) {

		LOG.debug("PropertiesManager - Costructor - Loading configuration file...");

		InputStream in = null;

		try {
			in = url.openStream();
			props = new Properties();
			props.load(in);
			LOG.debug("PropertiesManager - Costructor - Configuration file loaded!");
		} catch (IOException e) {
			LOG.error("Exception", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Get the debug messages enabled status
	 */
	public boolean getDebugEnabled() {
		String value = props.getProperty("debugEnabled");
		return (value.equalsIgnoreCase("0")) ? false : true;
	}

	/**
	 * Decide if the Network Root URI can be obtained by appending the
	 * net/default' suffix (SELECT 1), or by appending the net/<ExtendedPANId>'
	 * suffix (SELECT 0)
	 */
	public int getUseDefaultNWKRootURI() {
		String value = props.getProperty("UseDefaultNWKRootURI");
		return Integer.parseInt(value);
	}

	/**
	 * HTTP option application timeout (in seconds) - Note: for remote connection
	 * between GW and IPHA insert a value higher than 1. Set to zero to completely
	 * disable caching
	 */
	public int getHttpOptTimeout() {
		String value = props.getProperty("httpOptTimeout");
		return Integer.parseInt(value);
	}

	public int getnumberOfConnectionFail() {
		String value = props.getProperty("numberOfConnectionFail");
		return Integer.parseInt(value);
	}

	/**
	 * Gets NumberOfThreadForAnyPool property.
	 * 
	 * @return the NumberOfThreadForAnyPool value.
	 */
	public int getNumberOfThreadForAnyPool() {
		String value = props.getProperty("NumberOfThreadForAnyPool");
		return Integer.parseInt(value);

	}

	/**
	 * Gets KeepAliveThread property.
	 * 
	 * @return the KeepAliveThread value.
	 */
	public int getKeepAliveThread() {
		String value = props.getProperty("KeepAliveThread");
		return Integer.parseInt(value);
	}

	public void setDebugEnabled(Boolean _debug) {
		props.setProperty("debugEnabled", _debug.toString());
	}

	/* Debug */
	public int getIPPort() {
		String value = props.getProperty("serverPorts");
		return Integer.parseInt(value);
	}
}
