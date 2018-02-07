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
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.energy_home.jemma.zgd.jaxb.LQIInformation;
import org.restlet.resource.Get;

/**
 * Resource file used to manage the API getLQIInformation.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class AllLqiInformationClass extends CommonResource {

	private GatewayInterface proxyGalInterface;

	@Get
	public void processGet() {
		try {
			proxyGalInterface = getGatewayInterface();
			LQIInformation lqi = proxyGalInterface.getLQIInformation();
			Detail details = new Detail();
			details.getLQIInformation().add(lqi);

			sendResult(details);
		} catch (Exception e) {
			generalError(e.getMessage());
		}
	}
}