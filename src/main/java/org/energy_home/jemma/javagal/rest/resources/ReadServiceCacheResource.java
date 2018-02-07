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
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.NodeServicesList;
import org.energy_home.jemma.zgd.jaxb.WSNNodeList;
import org.restlet.data.Parameter;
import org.restlet.resource.Get;

/**
 * Resource file used to manage the API GET:readServicesCache, readNodeCache
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class ReadServiceCacheResource extends CommonResource {

	private GatewayInterface gatewayInterface;

	@Get
	public void readServiceCacheGetmethod() {

		String modeString = null;
		Parameter modeParam = getParameter(Resources.URI_PARAM_MODE);
		if (modeParam == null) {
			generalError("Error: mandatory '" + Resources.URI_PARAM_MODE + "' parameter missing.");
			return;
		} else {
			modeString = modeParam.getValue().trim();
			if (modeString.equals("cache")) {

				try {
					gatewayInterface = getGatewayInterface();
					NodeServicesList _result = gatewayInterface.readServicesCache();

					Info.Detail details = new Info.Detail();
					details.setNodeServicesList(_result);

					sendResult(details);
				} catch (Exception e) {
					generalError(e.getMessage());
					return;
				}
			} else {
				try {
					gatewayInterface = getGatewayInterface();

					WSNNodeList nodeList = gatewayInterface.readNodeCache();

					Info.Detail detail = new Info.Detail();
					detail.setWSNNodes(nodeList);

					sendResult(detail);
				} catch (NullPointerException e) {
					generalError(e.getMessage());
				} catch (Exception e) {
					generalError(e.getMessage());
				}
			}
		}
	}
}