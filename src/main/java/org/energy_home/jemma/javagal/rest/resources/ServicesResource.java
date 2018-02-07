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
package org.energy_home.jemma.javagal.rest.resources;

import static org.energy_home.jemma.javagal.rest.util.Util.INTERNAL_TIMEOUT;

import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.restlet.resource.Get;

/**
 * Resource file used to manage the API GET:startServiceDiscoverySync,
 * startServiceDiscovery
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class ServicesResource extends CommonResource {

	private GatewayInterface proxyGalInterface;

	@Get
	public void processGet() {

		Address address = this.getAddressAttribute("addr");
		long timeout = this.getLongParameter(Resources.URI_PARAM_TIMEOUT, INTERNAL_TIMEOUT);
		String uriListener = this.getStringParameter(Resources.URI_PARAM_URILISTENER, null);

		try {
			if (uriListener == null) {
				proxyGalInterface = getGatewayInterface();
				NodeServices node = proxyGalInterface.startServiceDiscoverySync(timeout, address);

				Info.Detail details = new Info.Detail();
				details.setNodeServices(node);

				sendResult(details);
			} else {
				ClientResources client = getClientResources(uriListener);
				proxyGalInterface = client.getGatewayInterface();
				if (!uriListener.equals("")) {
					client.getClientEventListener().setNodeServicesDestination(uriListener);
				}
				proxyGalInterface.startServiceDiscovery(timeout, address);
				sendSuccess();
			}
		} catch (NullPointerException e) {
			generalError(e.getMessage());
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}