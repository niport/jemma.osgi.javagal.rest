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

import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Aliases;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.restlet.resource.Get;

/**
 * Resource file used to manage the API GET:listAddresses
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class ListAddressesResource extends CommonResource {

	private GatewayInterface proxyGalInterface = null;

	@Get
	public void represent() {
		try {
			proxyGalInterface = getGatewayInterface();
			Aliases aliases = proxyGalInterface.listAddresses();
			Detail details = new Info.Detail();
			details.setAliases(aliases);

			sendResult(details);
		} catch (NullPointerException e) {
			generalError(e.getMessage());
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}