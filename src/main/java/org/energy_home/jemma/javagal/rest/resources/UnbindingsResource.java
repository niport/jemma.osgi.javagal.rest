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

import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Binding;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.restlet.resource.Post;

/**
 * Resource file used to manage the API POST:removeBindingSync, removeBinding
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class UnbindingsResource extends CommonResource {
	private GatewayInterface proxyGalInterface;

	@Post
	public void processPost(String body) {

		Binding binding = null;
		try {
			binding = Util.unmarshal(body, Binding.class);
		} catch (Exception e) {
			generalError("Malformed Binding in request");
			return;
		}

		if ((binding.getDeviceDestination() == null) || binding.getDeviceDestination().size() != 1) {
			generalError("DeviceDestination must contain exactly one element.");
			return;
		}

		long timeout = getLongParameter(Resources.URI_PARAM_TIMEOUT, -1);
		Address address = this.getAddressAttribute("addr");
		String uriListener = this.getStringParameter(Resources.URI_PARAM_URILISTENER, null);

		try {

			if (uriListener == null) {
				proxyGalInterface = getGatewayInterface();
				Status status = proxyGalInterface.removeBindingSync(timeout, binding);

				sendStatus(status);
			} else {
				ClientResources client = getClientResources(uriListener);

				proxyGalInterface = client.getGatewayInterface();

				client.getClientEventListener().setUnbindingDestination(uriListener);
				proxyGalInterface.removeBinding(timeout, binding);
				sendSuccess();
			}
		} catch (NullPointerException e) {
			generalError(e.getMessage());
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}
