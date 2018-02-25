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

import java.math.BigInteger;

import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.ResourcePathURIs;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.energy_home.jemma.zgd.jaxb.InterPANMessage;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.ZDPCommand;
import org.restlet.data.Parameter;
import org.restlet.engine.header.Header;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.util.Series;

/**
 * Resource file used to manage the API GET:URL menu. POST:sendZDPCommand.
 * DELETE:deleteCallBack
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class SendZdpAndInterPANAndLeaveResource extends CommonResource {

	private GatewayInterface proxyGalInterface;

	@Options
	public void doOptions() {
		Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
		if (responseHeaders == null) {
			responseHeaders = new Series<Header>(Header.class);
			getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
		}
		responseHeaders.add("Access-Control-Allow-Origin", "*");
		responseHeaders.add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
		responseHeaders.add("Access-Control-Allow-Headers", "Content-Type");
		responseHeaders.add("Access-Control-Max-Age", "60");
	}

	@Get
	public void processGet() {
		Detail details = new Detail();
		details.getValue().add(ResourcePathURIs.BINDINGS);
		details.getValue().add(ResourcePathURIs.UNBINDINGS);
		details.getValue().add(ResourcePathURIs.NODEDESCRIPTOR);
		details.getValue().add(ResourcePathURIs.SERVICES);
		details.getValue().add(ResourcePathURIs.PERMIT_JOIN);
		details.getValue().add(ResourcePathURIs.LQIINFORMATION);

		sendResult(details);
	}

	@Post
	public void processPost(String body) {

		long timeout = getLongParameter(Resources.URI_PARAM_TIMEOUT, true, -1);
		Address address = this.getAddressAttribute("addr");
		String uriListener = this.getStringParameter(Resources.URI_PARAM_URILISTENER, null);

		Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
		if (responseHeaders == null) {
			responseHeaders = new Series<Header>(Header.class);
			getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
		}
		// allow request from other origins
		responseHeaders.add(new Header("Access-Control-Allow-Origin", "*"));

		ZDPCommand zdpCommand = null;

		InterPANMessage interPANMessage = null;

		try {
			zdpCommand = Util.unmarshal(body, ZDPCommand.class);
		} catch (Exception je) {

		}

		try {
			interPANMessage = Util.unmarshal(body, InterPANMessage.class);
		} catch (Exception je) {

		}

		if (interPANMessage != null) {
			// It's a Send InterPan message invocation
			try {
				if (uriListener == null) {
					generalError("Sync call because uriListener not present. Not implemented. Only asynch is admitted");
				} else {
					ClientResources client = getClientResources(uriListener);

					proxyGalInterface = client.getGatewayInterface();
					if (client.getClientEventListener() != null)
						client.getClientEventListener().setInterPANCommandDestination(uriListener);
					proxyGalInterface.sendInterPANMessage(timeout, interPANMessage);

					sendSuccess();
				}
			} catch (Exception e) {
				generalError(e.getMessage());
			}
		} else if (zdpCommand != null) {

			// It's a Send Zdp Message message invocation
			try {
				if (uriListener == null) {
					generalError("Sync call because uriListener not present. Not implemented. Only asynch is admitted");
					return;
				} else {
					ClientResources client = getClientResources(uriListener);
					proxyGalInterface = client.getGatewayInterface();
					if (client.getClientEventListener() != null) {
						client.getClientEventListener().setZdpCommandDestination(uriListener);
					}
					proxyGalInterface.sendZDPCommand(timeout, zdpCommand);

					sendSuccess();
				}
			} catch (Exception e) {
				generalError(e.getMessage());
			}
		} else {
			generalError("Wrong xml");
		}
	}

	@Delete
	public void processDelete() {
		Address address = new Address();

		String uriListener = null;

		// addrString parameters check
		String addrString = (String) getRequest().getAttributes().get(Resources.PARAMETER_ADDR);
		if (addrString != null) {
			if (addrString.length() > 4) {
				// IEEEAddress
				BigInteger ieee = new BigInteger(addrString, 16);
				address.setIeeeAddress(ieee);
			} else {
				// ShortAddress
				Integer shortAddress = new Integer(Integer.parseInt(addrString, 16));
				address.setNetworkAddress(shortAddress);
			}
		} else {
			generalError("Error: mandatory '" + Resources.PARAMETER_ADDR + "' parameter's value invalid. You provided: " + addrString);
			return;
		}

		long timeout;
		try {
			timeout = getLongParameter(Resources.URI_PARAM_TIMEOUT, true, -1);
		} catch (IllegalArgumentException e) {
			generalError(e.getMessage());
			return;
		}

		Parameter uriListenerParam = getParameter(Resources.URI_PARAM_URILISTENER);

		try {
			if (uriListenerParam == null) {
				// Sync call because uriListener not present.

				// Check for Gal Interface
				proxyGalInterface = getGatewayInterface();

				// TODO exists also leaveSync(timeout, addrOfInterest, mask)
				// Leave
				Status status = proxyGalInterface.leaveSync(timeout, address, 0);

				sendStatus(status);
			} else {
				// Async call
				// We know here that uriListenerParam is not null...
				uriListener = uriListenerParam.getValue();
				// Process async. If uriListener equals "", don't send the
				// result
				// but wait that the IPHA polls for it using the request
				// identifier. Async is possible only if start=true

				ClientResources client = getClientResources(uriListener);
				proxyGalInterface = client.getGatewayInterface();

				client.getClientEventListener().setLeaveResultDestination(uriListener);
				proxyGalInterface.leave(timeout, address);

				sendSuccess();
				return;
			}
		} catch (NullPointerException e) {
			generalError(e.getMessage());
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}