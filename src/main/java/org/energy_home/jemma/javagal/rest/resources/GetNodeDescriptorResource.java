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
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.restlet.resource.Get;

/**
 * Resource file used to manage the API GET:getNodeDescriptorSync,
 * getNodeDescriptor
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class GetNodeDescriptorResource extends CommonResource {
	private GatewayInterface gatewayInterface;

	@Get
	public void processGet(String body) {

		long timeout = this.getLongParameter(Resources.URI_PARAM_TIMEOUT, INTERNAL_TIMEOUT);
		Address address = this.getAddressAttribute("addr");
		String uriListener = this.getStringParameter(Resources.URI_PARAM_URILISTENER, null);

		try {
			if (uriListener == null) {
				/* Sync call because uriListener not present. */

				/* Gal Manager check */
				gatewayInterface = getGatewayInterface();
				NodeDescriptor nd = gatewayInterface.getNodeDescriptorSync(timeout, address);

				Detail details = new Detail();
				details.setNodeDescriptor(nd);
				sendResult(details);
			} else {

				/*
				 * Process async. If uriListener equals "", don't send the result but
				 * wait that the IPHA polls for it using the request identifier.
				 */

				ClientResources client = getClientResources(uriListener);
				gatewayInterface = client.getGatewayInterface();

				client.getClientEventListener().setNodeDescriptorDestination(uriListener);
				gatewayInterface.getNodeDescriptor(timeout, address);
				sendSuccess();
			}
		} catch (NullPointerException e) {
			generalError(e.getMessage());
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}
