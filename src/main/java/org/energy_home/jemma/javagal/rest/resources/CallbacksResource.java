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

import org.energy_home.jemma.javagal.rest.RestMessageListener;
import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Callback;
import org.energy_home.jemma.zgd.jaxb.CallbackIdentifierList;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * Resource file used to manage the API GET:getlistCallbacks.
 * POST:createCallback
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class CallbacksResource extends CommonResource {

	private GatewayInterface gatewayInterface = null;

	@Get
	public void represent() {

		// ListCallbacks
		try {
			gatewayInterface = getGatewayInterface();
			CallbackIdentifierList callbacksList = gatewayInterface.getlistCallbacks();

			Info.Detail details = new Info.Detail();
			details.setCallbacks(callbacksList);

			sendResult(details);
		} catch (NullPointerException e) {
			generalError(e.getMessage());
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}

	@Post
	public void processPost(String body) {

		Callback callback = null;

		try {
			callback = Util.unmarshal(body, Callback.class);
		} catch (Exception jbe) {
			generalError("Malformed Callback in request");
			return;
		}

		String uriListener = this.getStringParameter(Resources.URI_PARAM_URILISTENER);

		try {
			ClientResources client = getClientResources(uriListener);
			gatewayInterface = client.getGatewayInterface();

			RestMessageListener listener = new RestMessageListener(callback, uriListener, client,
					getRestManager().getPropertiesManager());

			Long id = gatewayInterface.createCallback(callback, listener);

			if (id >= 0) {
				listener.setCallBackId(id);
				client.getCallbacksEventListeners().put(id, listener);

				Info.Detail details = new Info.Detail();
				details.setCallbackIdentifier(id);

				sendResult(details);
			} else {
				generalError("Error in creating callback. Not created.");
			}
		} catch (NullPointerException e) {
			generalError(e.getMessage());
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}