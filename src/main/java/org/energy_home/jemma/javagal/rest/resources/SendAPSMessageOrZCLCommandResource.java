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
import org.energy_home.jemma.zgd.jaxb.APSMessage;
import org.energy_home.jemma.zgd.jaxb.APSMessageResult;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.ZCLCommand;
import org.restlet.resource.Post;

/**
 * Resource file used to manage the API POST:sendAPSMessage, sendZCLCommand
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class SendAPSMessageOrZCLCommandResource extends CommonResource {

	private GatewayInterface gatewayInterface;

	@Post
	public void processPost(String body) {

		long timeout = getLongParameter(Resources.URI_PARAM_TIMEOUT, -1);
		short service = getShortAttribute(Resources.PARAMETER_SERVICE);
		String uriListener = this.getStringParameter(Resources.URI_PARAM_URILISTENER, null);

		APSMessage apsMessage = null;
		ZCLCommand zclCommand = null;

		try {
			apsMessage = Util.unmarshal(body, APSMessage.class);
		} catch (Exception je) {

		}

		try {
			zclCommand = Util.unmarshal(body, ZCLCommand.class);
		} catch (Exception je) {

		}

		if (apsMessage != null) {
			// It's a Send APSMessage invocation
			try {
				if (uriListener == null) {
					// Sync call because uriListener not present.
					// Only Asynch is admitted.
					gatewayInterface = getGatewayInterface();
					int txTime = Util.currentTimeMillis();
					gatewayInterface.sendAPSMessage(apsMessage);

					Info.Detail details = new Info.Detail();
					APSMessageResult apsMessageResult = new APSMessageResult();
					apsMessageResult.setConfirmStatus(0);
					apsMessageResult.setTxTime(txTime);
					details.setAPSMessageResult(apsMessageResult);

					sendResult(details);
				} else {
					/*
					 * Process async. If uriListener equals "", don't send the result but
					 * wait that the IPHA polls for it using the request identifier.
					 */
					gatewayInterface = getGatewayInterface();

					// TODO control if it's correct this invocation/result
					gatewayInterface.sendAPSMessage(timeout, apsMessage);
					sendSuccess();
					return;
				}
			} catch (Exception e) {
				generalError(e.getMessage());
				return;
			}
		}

		else if (zclCommand != null) {

			// It's a Send ZCLCommand invocation
			try {
				// Gal Manager check
				gatewayInterface = getGatewayInterface();

				if (uriListener == null) {
					// Only Asynch is admitted.
					generalError("No Urilistener, Only Asynch is admitted");
				} else {
					/*
					 * Process async. If uriListener equals "", don't send the result but
					 * wait that the IPHA polls for it using the request identifier.
					 */

					ClientResources client = getClientResources(uriListener);
					gatewayInterface = client.getGatewayInterface();
					client.getClientEventListener().setZclCommandDestination(uriListener);
					gatewayInterface.sendZCLCommand(timeout, zclCommand);

					sendSuccess();
				}
			} catch (Exception e) {
				generalError(e.getMessage());
			}
		} else {
			generalError("Wrong xml");
		}
	}
}