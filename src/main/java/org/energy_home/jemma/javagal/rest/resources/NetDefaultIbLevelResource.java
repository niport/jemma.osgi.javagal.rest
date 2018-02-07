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

import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.restlet.resource.Get;

/**
 * Resource file used to manage the API GET:URL menu.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class NetDefaultIbLevelResource extends CommonResource {

	@Get
	public void represent() {
		Detail details = new Detail();
		details.getValue()
				.add("a1 - nwkSecurityMaterialSet (The two entries for the security material set, does not includes incoming counters.)");
		details.getValue().add("c3 - apsChannelMask (Mask of channels to form/join)");
		details.getValue().add("c4 - apsUseExtendedPANID (Extended PAN ID)");
		details.getValue().add("c8 - apsUseInsecureJoin (Use secure or insecure join)");
		details.getValue().add("80 - nwkPanId (The PAN Identifier for the PAN of which the device is amember.)");
		details.getValue().add("9A - nwkExtendedPANID (The Extended PAN Identifier for the PAN of which the device is a member.)");
		details.getValue().add("A0 - nwkSecurityLevel");
		details.getValue().add("96 - nwkShortAddress");
		details.getValue().add("DA - nwkDeviceType");
		details.getValue().add("DB - nwkSoftwareVersion");
		details.getValue().add("E6 - SASNwkKey");
		// _det.getValue().add("85 - MacKey");

		sendResult(details);
	}
}