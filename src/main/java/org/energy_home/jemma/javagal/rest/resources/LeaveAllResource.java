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
import org.energy_home.jemma.javagal.rest.util.ResourcePathURIs;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

/**
 * Resource file used to manage the API GET:URL menu. DELETE:leaveAllSync,
 * leaveAll
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class LeaveAllResource extends CommonResource {

	private GatewayInterface gatewayInterface;

	@Get
	public void represent() {
		Detail details = new Detail();
		details.getValue().add(ResourcePathURIs.SERVICES);
		details.getValue().add(ResourcePathURIs.PERMIT_JOIN);
		details.getValue().add(ResourcePathURIs.LQIINFORMATION);

		sendResult(details);
		return;
	}

	@Delete
	public void processDelete(String body) {

		long timeout = getLongParameter(Resources.URI_PARAM_TIMEOUT, -1);
		String uriListener = getStringParameter(Resources.URI_PARAM_URILISTENER, null);

		try {
			if (uriListener == null) {
				// Sync call because uriListener not present.
				gatewayInterface = getGatewayInterface();
				Status status = gatewayInterface.leaveAllSync();
				sendStatus(status);
			} else {
				/* Async call. We know here that uriListenerParam is not null... */
				ClientResources client = getClientResources(uriListener);
				gatewayInterface = client.getGatewayInterface();

				/*
				 * Process async. If uriListener equals "", don't send the result but
				 * wait that the IPHA polls for it using the request identifier.
				 */
				client.getClientEventListener().setPermitJoinDestination(uriListener);

				gatewayInterface.leaveAll();
				sendSuccess();
			}
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}