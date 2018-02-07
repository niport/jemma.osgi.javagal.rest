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

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.energy_home.jemma.javagal.rest.util.ClientKey;
import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.osgi.service.component.ComponentFactory;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rest manager for javagal Rest package. This class creates and stops the Rest
 * server and maintains a client's map useful to associate a client to its
 * identification parameters (address and port).
 * <p>
 * Please note that a Rest server provides a number of resources each identified
 * by its uri. Then every resource is called in parallel by a number of
 * different clients. The Rest server identify each client by its unique address
 * and ip port.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class RestManager {

	private static final Logger LOG = LoggerFactory.getLogger("REST-XML");

	private final ConcurrentHashMap<ClientKey, ClientResources> clientsMap = new ConcurrentHashMap<ClientKey, ClientResources>();

	private boolean proxyActive = false;

	private ComponentFactory componentFactory;

	private Component component;

	private PropertiesManager configuration = null;

	/**
	 * Gets the properties manager.
	 * 
	 * @return the properties manager.
	 */
	public PropertiesManager getPropertiesManager() {
		return configuration;
	}

	/**
	 * Creates a new instance with a given properties manager and gal extender
	 * proxy factory.
	 * 
	 * @param __PropertiesManager
	 *          the properties manager.
	 * @param componentFactory
	 *          the gal extender proxy factory.
	 */
	public RestManager(PropertiesManager configuration, ComponentFactory componentFactory) {
		this.proxyActive = true;
		this.componentFactory = componentFactory;
		this.configuration = configuration;

		/* restlet container for servers */
		component = new Component();

		Server server = new Server(Protocol.HTTP, configuration.getIPPort());

		component.getServers().add(server);

		// Attach the sample application.
		GalManagerRestApplication gmra = new GalManagerRestApplication(this);
		component.getDefaultHost().attach("", gmra);
	}

	/**
	 * Starts the server
	 */
	public void start() {
		synchronized (this) {
			try {
				component.start();
			} catch (Exception e) {
				LOG.error("Error starting Rest: ", e);
			}
		}
	}

	/**
	 * Stops the server.
	 */
	public void stop() {
		synchronized (this) {
			try {
				setProxyActive(false);
				deleteFactory();
				component.stop();
			} catch (Exception e) {
				LOG.error("Error stopping rest server component:", e);
			}
		}
	}

	/**
	 * Tells if the proxy is active or not.
	 * 
	 * @return {@code true} if proxy is active, {@code false} otherwise.
	 */
	public synchronized boolean isProxyActive() {
		return this.proxyActive;
	}

	/**
	 * Sets the proxy active parameter.
	 * 
	 * @param proxyActive
	 *          the proxy active parameter.
	 */
	public synchronized void setProxyActive(boolean proxyActive) {
		this.proxyActive = proxyActive;
	}

	/**
	 * Gets the gal extender proxy factory.
	 * 
	 * @return the gal extender proxy factory.
	 */
	public ComponentFactory getFactory() {
		return componentFactory;
	}

	/**
	 * Deletes the local factory reference.
	 */
	public void deleteFactory() {
		componentFactory = null;
	}

	/**
	 * Retrieves the right {@code ClientResources} object for a client. Every
	 * client is identified by its address and its port. This method looks on the
	 * client's map for the right resources object. If it's present, then gives it
	 * back to the caller, otherwise creates a new one, put it on the map and then
	 * returns it to the caller.
	 * 
	 * @param port
	 *          the client's port.
	 * @param address
	 *          the client's address.
	 * @return the right resource object for that client.
	 * @throws Exception
	 *           if the factory fails to create a new gateway interface object,
	 *           due to some internal error.
	 */
	synchronized public ClientResources getClientObjectKey(int port, String address) throws Exception {
		if (!isProxyActive())
			return null;

		ClientKey clientKey = new ClientKey();

		// ...looking for the correspondence on the map
		ClientResources client = null;

		synchronized (clientsMap) {
			// Creating the key to look for on the map...
			clientKey.setPort(port);
			clientKey.setAddress(address);
			client = clientsMap.get(clientKey);
			if (client == null && clientKey.getPort() > -1) {
				clientKey.setPort(-1);
				client = clientsMap.get(clientKey);
				clientKey.setPort(port);
				if (client != null) {
					clientsMap.remove(clientKey);
					clientKey.setPort(port);
					clientsMap.put(clientKey, client);
				}

			} else if (client == null && clientKey.getPort() == -1) {
				for (Iterator<Entry<ClientKey, ClientResources>> it = clientsMap.entrySet().iterator(); it.hasNext();) {
					ClientKey p = (ClientKey) it.next().getKey();
					if (p.getAddress().equals(clientKey.getAddress())) {
						client = clientsMap.get(p);
						break;
					}
				}
			}
		}

		if (client != null) {
			if (clientKey.getPort() > -1)
				client.setGatewayEventListener();
			if (getPropertiesManager().getDebugEnabled()) {
				LOG.debug("Get proxy client: Port: " + clientKey.getPort() + " Address: " + clientKey.getAddress());
			}

		} else {
			client = new ClientResources(getPropertiesManager(), (GatewayInterface) getFactory().newInstance(null).getInstance(),
					clientKey, this);
			if (clientKey.getPort() > -1)
				client.setGatewayEventListener();
			synchronized (clientsMap) {
				clientsMap.put(clientKey, client);
			}

			if (getPropertiesManager().getDebugEnabled()) {
				LOG.debug("New Gal proxy client created. Port: " + clientKey.getPort() + "Address: " + clientKey.getAddress());
			}

		}
		return client;
	}

	/**
	 * Removes the client key from the map.
	 * 
	 * @param key
	 *          the client key to remove.
	 * 
	 * @throws Exception
	 *           if an error occurs.
	 */
	synchronized public void removeClientObjectKey(ClientKey key) throws Exception {
		if (clientsMap.containsKey(key)) {
			clientsMap.remove(key);
		}
	}
}
