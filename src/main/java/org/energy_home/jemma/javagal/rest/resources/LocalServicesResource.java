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

import org.energy_home.jemma.javagal.rest.util.ResourcePathURIs;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.SimpleDescriptor;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.restlet.data.MediaType;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * Resource file used to manage the API GET:getLocalServices.
 * POST:configureEndpoint. DELETE:ClearEndPoint
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class LocalServicesResource extends CommonResource {

	private GatewayInterface proxyGalInterface;
	private Long timeout = (long) INTERNAL_TIMEOUT;

	@Get
	public void processGet() {
		String epString = (String) getRequest().getAttributes().get(Resources.PARAMETER_EP);

		if (epString == null) {

			// GetLocalServices (GET method with /{ep} not present)
			NodeServices services = null;
			try {
				proxyGalInterface = getGatewayInterface();
				services = proxyGalInterface.getLocalServices();
				Detail details = new Detail();
				details.setNodeServices(services);

				sendResult(details);
			} catch (NullPointerException e) {
				generalError(e.getMessage());
			} catch (Exception e) {
				generalError(e.getMessage());
			}
		} else {
			Detail details = new Detail();
			details.getValue().add(ResourcePathURIs.WSNCONNECTION);

			sendResult(details);
		}
	}

	@Post
	public void processPost(String body) {

		String epString = (String) getRequest().getAttributes().get(Resources.PARAMETER_EP);

		if (epString == null) {

			long timeout;
			try {
				timeout = getLongParameter(Resources.URI_PARAM_TIMEOUT, -1);
			} catch (IllegalArgumentException e) {
				generalError(e.getMessage());
				return;
			}

			SimpleDescriptor simpleDescriptor;
			try {
				simpleDescriptor = Util.unmarshal(body, SimpleDescriptor.class);
			} catch (Exception e) {
				generalError("Malformed SimpleDesriptor in request");
				return;
			}

			// Actual Gal call

			try {
				// Gal Manager check
				proxyGalInterface = getGatewayInterface();

				// ConfigureEndpoint
				short endPoint = proxyGalInterface.configureEndpoint(timeout, simpleDescriptor);
				if (endPoint > 0) {

					Info.Detail details = new Info.Detail();
					details.setEndpoint(endPoint);

					sendResult(details);
				} else {
					generalError("Error in creating end point. Not created.");
					return;
				}
			} catch (NullPointerException e) {
				generalError(e.getMessage());
			} catch (Exception e) {
				generalError(e.getMessage());
			}
		}
	}

	@Delete
	public void precessDelete() {

		try {
			String epString = "";
			epString = (String) getRequest().getAttributes().get(Resources.PARAMETER_EP);

			proxyGalInterface = getGatewayInterface();
			Short endpoint = Short.parseShort(epString, 16);

			// ClearEndpoint
			proxyGalInterface.clearEndpoint(endpoint);

			Info i = new Info();
			Status st = new Status();
			st.setCode((short) GatewayConstants.SUCCESS);
			i.setStatus(st);
			getResponse().setEntity(Util.marshal(i), MediaType.APPLICATION_XML);
		} catch (NullPointerException e) {
			generalError(e.getMessage());
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}