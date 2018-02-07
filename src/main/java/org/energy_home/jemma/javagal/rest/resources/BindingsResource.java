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
import org.energy_home.jemma.javagal.rest.util.Util;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Binding;
import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * Resource file used to manage the APIs GET:getNodeBindingsSync,
 * getNodeBindings. POST:addBindingSync,addBinding
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class BindingsResource extends CommonResource {
	private GatewayInterface proxyGalInterface;

	@Get
	public void processGet(String body) {

		// TODO how to use the address read address? There is probably a mistake
		// to correct in this resource!

		long timeout = this.getLongParameter(Resources.URI_PARAM_TIMEOUT, INTERNAL_TIMEOUT);
		Address address = this.getAddressAttribute("addr");
		String uriListener = this.getStringParameter(Resources.URI_PARAM_URILISTENER, null);
		long index = this.getLongParameter(Resources.URI_PARAM_INDEX, -1);

		try {
			if (uriListener == null) {
				/*
				 * Sync call because uriListener not present. Gal Manager check
				 */
				proxyGalInterface = getGatewayInterface();
				BindingList bindingList = null;

				if (index > 0) {
					bindingList = proxyGalInterface.getNodeBindingsSync(timeout, address, (short) index);
				} else {
					bindingList = proxyGalInterface.getNodeBindingsSync(timeout, address);
				}

				Info.Detail details = new Info.Detail();
				details.setBindings(bindingList);
				sendResult(details);
			} else {
				/*
				 * Process async. If uriListener equals "", don't send the result but
				 * wait that the IPHA polls for it using the request identifier.
				 */

				ClientResources client = getClientResources(uriListener);

				proxyGalInterface = client.getGatewayInterface();

				client.getClientEventListener().setNodeBindingDestination(uriListener);

				if (index > 0) {
					proxyGalInterface.getNodeBindings(timeout, address, (short) index);
				} else {
					proxyGalInterface.getNodeBindings(timeout, address);
				}

				sendSuccess();
			}
		} catch (NullPointerException e) {
			generalError(e.getMessage());
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}

	@Post
	public void processPost(String body) {

		// TODO how to use the address read address? There is probably a mistake
		// to correct in this resource!

		Binding binding = null;

		try {
			binding = Util.unmarshal(body, Binding.class);
		} catch (Exception e) {
			generalError(e.getMessage());
			return;
		}

		if ((binding.getDeviceDestination() == null) || binding.getDeviceDestination().size() != 1) {
			generalError("DeviceDestination must contain exactly one element.");
			return;
		}

		// FIXME: why it reads the address?

		long timeout = this.getLongParameter(Resources.URI_PARAM_TIMEOUT, INTERNAL_TIMEOUT);
		Address address = this.getAddressAttribute("addr");
		String uriListener = this.getStringParameter(Resources.URI_PARAM_URILISTENER, null);

		try {

			if (uriListener == null) {
				proxyGalInterface = getGatewayInterface();
				Status status = proxyGalInterface.addBindingSync(timeout, binding);

				sendStatus(status);
			} else {
				ClientResources client = getClientResources(uriListener);

				proxyGalInterface = client.getGatewayInterface();

				client.getClientEventListener().setBindingDestination(uriListener);
				proxyGalInterface.addBinding(timeout, binding);

				sendSuccess();
			}
		} catch (NullPointerException e) {
			generalError(e.getMessage());
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}
