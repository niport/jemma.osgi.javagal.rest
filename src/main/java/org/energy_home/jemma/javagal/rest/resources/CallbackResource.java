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

import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.restlet.representation.AppendableRepresentation;
import org.restlet.resource.Delete;

/**
 * Resource file used to manage the API POST:deleteCallback.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class CallbackResource extends CommonResource {

	private GatewayInterface proxyGalInterface;

	@Delete
	public void deleteCallback() {

		String idString = (String) getRequest().getAttributes().get(Resources.PARAMETER_ID);
		Long id = -1l;

		if (idString == null) {
			generalError("Error: mandatory callback id parameter missing. (Unsigned32)");
			return;
		} else {
			try {
				id = Long.decode(Resources.HEX_PREFIX + idString);
			} catch (NumberFormatException e) {
				generalError("Error: mandatory callback id parameter incorrect (Unsigned32). You provided: " + idString);
				return;
			}
		}

		if (!Util.isUnsigned32(id)) {
			generalError("Error1: mandatory id parameter's value invalid (Unsigned32). You provided: " + id);
			return;
		}

		AppendableRepresentation toReturn = new AppendableRepresentation();
		try {
			// Check for Gal Interface
			proxyGalInterface = getGatewayInterface();

			// Delete Callback
			proxyGalInterface.deleteCallback(id);
			Info.Detail details = new Info.Detail();
			sendResult(details);
		} catch (NullPointerException e) {
			generalError(e.getMessage());
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}