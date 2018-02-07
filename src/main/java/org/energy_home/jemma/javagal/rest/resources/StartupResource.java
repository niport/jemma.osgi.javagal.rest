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
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.StartupAttributeInfo;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * Resource file used to manage the API GET:readStartupAttributeSet.
 * POST:configureStartupAttributeSet, startGatewayDeviceSync,
 * startGatewayDevice. DELETE:stopNetworkSync, stopNetwork
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class StartupResource extends CommonResource {

	private GatewayInterface gatewayInterface;

	@Get
	public void represent() {

		long index = getLongParameter(Resources.URI_PARAM_INDEX);

		try {
			// Gal Manager check
			gatewayInterface = getGatewayInterface();
			// ReadStartupAttributeSet
			StartupAttributeInfo sai = gatewayInterface.readStartupAttributeSet((short) index);

			Info.Detail details = new Info.Detail();
			details.setStartupAttributeInfo(sai);

			sendResult(details);
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}

	@Post
	public void processPost(String body) {

		// Uri parameters check
		String startString = null;
		String uriListener = null;

		Parameter startParam = getParameter(Resources.URI_PARAM_START);
		if (startParam == null) {

			generalError("Error: mandatory start parameter missing.");
			return;
		} else {
			startString = startParam.getValue();
			if (!(startString.equals("true") || startString.equals("false"))) {
				generalError(
						"Error: mandatory '" + Resources.URI_PARAM_START + "' parameter's value invalid. You provided: " + startString);
				return;
			}
		}

		long timeout;
		try {
			timeout = getLongParameter(Resources.URI_PARAM_TIMEOUT, true, -1);
		} catch (IllegalArgumentException e) {
			generalError(e.getMessage());
			return;
		}

		Parameter uriListenerParam = getParameter(Resources.URI_PARAM_URILISTENER);

		// Actual Gal call
		if (uriListenerParam == null) {
			// Sync call because uriListener not present.
			if (startString.equals("true")) {
				// Real Startup (start=true)
				StartupAttributeInfo sai;
				try {
					sai = Util.unmarshal(body, StartupAttributeInfo.class);
					// Gal Manager check
					gatewayInterface = getGatewayInterface();
					// StartGatewayDevice synch
					Status status = gatewayInterface.startGatewayDeviceSync(timeout, sai);
					Info info = new Info();
					info.setStatus(status);
					getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
					return;

				} catch (Exception e) {
					generalError(e.getMessage());
					return;
				}
			} else {
				// ConfigureStartupAttributeInfo (start=false)
				StartupAttributeInfo sai;
				try {
					sai = Util.unmarshal(body, StartupAttributeInfo.class);
					// Gal Manager check
					gatewayInterface = getGatewayInterface();
					// ConfigureStartupAttributeSet
					gatewayInterface.configureStartupAttributeSet(sai);
					sendSuccess();
				} catch (Exception e) {
					generalError(e.getMessage());
				}
			}
		} else {
			// Async call
			// We know here that uriListenerParam is not null...
			uriListener = uriListenerParam.getValue();
			// Process async. If uriListener equals "", don't send the result
			// but wait that the IPHA polls for it using the request
			// identifier. Async is possible only if start=true
			if (startString.equals("true")) {
				// Real Startup
				StartupAttributeInfo sai;
				try {
					sai = Util.unmarshal(body, StartupAttributeInfo.class);
					// Gal Manager check
					ClientResources client = getClientResources(uriListener);
					gatewayInterface = client.getGatewayInterface();
					if (!uriListener.equals("")) {
						client.getClientEventListener().setStartGatewayDestination(uriListener);
					}
					gatewayInterface.startGatewayDevice(timeout, sai);
					sendSuccess();
				} catch (Exception e) {
					generalError(e.getMessage());
				}
			} else {
				generalError("Error: asynch call with start= false. You cannot make a ConfigureStartupAttributeInfo asynchronously.");
			}
		}
	}

	@Delete
	public void stopMethod() {

		String uriListener = null;

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
				gatewayInterface = getGatewayInterface();
				Status status = gatewayInterface.stopNetworkSync(timeout);
				Info info = new Info();
				info.setStatus(status);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			} else {
				// Async call
				// We know here that uriListenerParam is not null...
				uriListener = uriListenerParam.getValue();

				ClientResources client = getClientResources(uriListener);
				gatewayInterface = client.getGatewayInterface();
				client.getClientEventListener().setGatewayStopDestination(uriListener);
				gatewayInterface.stopNetwork(timeout);

				sendSuccess();
			}
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}