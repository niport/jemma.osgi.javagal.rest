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
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.restlet.data.Parameter;
import org.restlet.resource.Get;

/**
 * Resource file used to manage the API GET:frequencyAgilitySync,
 * frequencyAgility
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class FrequenceAgilityResource extends CommonResource {

	private GatewayInterface proxyGalInterface;

	@Get
	public void processGet(String body) {
		String uriListener = null;

		long timeout = getLongParameter(Resources.URI_PARAM_TIMEOUT, -1);
		long scanChannel = getLongParameter(Resources.URI_SCANCHANNEL, 0);
		long scanDuration = getLongParameter(Resources.URI_SCANDURATION, 0xFE);

		try {
			Parameter uriListenerParam = getParameter(Resources.URI_PARAM_URILISTENER);

			if (uriListenerParam != null) {
				uriListener = uriListenerParam.getValue();
				ClientResources client = getClientResources(uriListener);
				proxyGalInterface = client.getGatewayInterface();

				client.getClientEventListener().setFrequencyAgilityResultDestination(uriListener);
				proxyGalInterface.frequencyAgility(timeout, (short) scanChannel, (short) scanDuration);

				sendSuccess();
			} else {
				proxyGalInterface = getGatewayInterface();
				Status status = proxyGalInterface.frequencyAgilitySync(timeout, (short) scanChannel, (short) scanDuration);

				sendStatus(status);
			}
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}