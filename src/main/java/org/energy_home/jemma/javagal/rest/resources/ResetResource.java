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
import org.energy_home.jemma.zgd.jaxb.Status;
import org.restlet.resource.Get;

/**
 * Resource file used to manage the API GET:resetDongleSync, resetDongle
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class ResetResource extends CommonResource {

	private GatewayInterface proxyGalInterface;

	@Get
	public void resetMethod(String body) {

		String uriListener = getStringParameter(Resources.URI_PARAM_URILISTENER, null);
		long timeout = getLongParameter(Resources.URI_PARAM_TIMEOUT, INTERNAL_TIMEOUT);
		long warmStart = getLongParameter(Resources.URI_PARAM_START_MODE_RESET);

		if (uriListener == null) {
			try {
				proxyGalInterface = getGatewayInterface();

				Status result = proxyGalInterface.resetDongleSync(timeout, (short) warmStart);

				sendStatus(result);
			} catch (Exception e) {
				generalError(e.getMessage());
				return;
			}
		} else {
			try {
				ClientResources client = getClientResources(uriListener);
				proxyGalInterface = client.getGatewayInterface();
				if (!uriListener.equals("")) {
					client.getClientEventListener().setResetDestination(uriListener);
				}
				proxyGalInterface.resetDongle(timeout, (short) warmStart);

				sendSuccess();
			} catch (Exception e) {
				generalError("The warm start value is mandatory");
			}
		}
	}
}