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
import org.energy_home.jemma.zgd.jaxb.JoiningInfo;
import org.restlet.resource.Post;

/**
 * Resource file used to manage the API POST:permitJoinAll
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class PermitJoinAllResource extends CommonResource {

	private GatewayInterface gatewayInterface;

	@Post
	public void processPost(String body) {

		long timeout = getLongParameter(Resources.URI_PARAM_TIMEOUT, -1);
		String uriListener = this.getStringParameter(Resources.URI_PARAM_URILISTENER, null);

		JoiningInfo joiningInfo;
		try {
			joiningInfo = Util.unmarshal(body, JoiningInfo.class);

			if (uriListener == null) {
				gatewayInterface = getGatewayInterface();
				gatewayInterface.permitJoinAll(timeout, joiningInfo.getPermitDuration());

				Info.Detail details = new Info.Detail();
				sendResult(details);
			} else {
				// Async call. We know here that uriListenerParam is not null...
				// Process async. If uriListener equals "", don't send the
				// result but wait that the IPHA polls for it using the request
				// identifier.
				ClientResources client = getClientResources(uriListener);

				gatewayInterface = client.getGatewayInterface();
				client.getClientEventListener().setPermitJoinDestination(uriListener);
				gatewayInterface.permitJoinAll(timeout, joiningInfo.getPermitDuration());

				sendSuccess();
			}
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}