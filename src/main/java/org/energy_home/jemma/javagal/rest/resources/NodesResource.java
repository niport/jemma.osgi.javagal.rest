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
import org.energy_home.jemma.javagal.rest.util.ResourcePathURIs;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.WSNNodeList;
import org.restlet.data.Parameter;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * Resource file used to manage the API GET:readNodeCache, subscribeNodeRemoval,
 * startNodeDiscovery.
 *
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class NodesResource extends CommonResource {
	private GatewayInterface gatewayInterface = null;

	@Get
	public void processGet() throws ResourceException {
		// Uri parameters check
		String modeString = null;

		Parameter modeParam = getParameter(Resources.URI_PARAM_MODE);
		if (modeParam != null) {
			modeString = modeParam.getValue();
		}

		if (modeString != null) {
			// Mode parameter is present, it's a Read Node Cache
			// Control mode parameter's validity
			if (!modeString.equals(Resources.URI_PARAM_CACHE)) {
				generalError("Error: optional '" + Resources.URI_PARAM_MODE + "' parameter's value invalid. You provided: " + modeString);
				return;
			}

			try {
				gatewayInterface = getGatewayInterface();
				// ReadNodeCache
				WSNNodeList _cache = gatewayInterface.readNodeCache();
				Info.Detail details = new Info.Detail();
				details.setWSNNodes(_cache);

				sendResult(details);
				return;

			} catch (NullPointerException e) {
				generalError(e.getMessage());
				return;
			} catch (Exception e) {
				generalError(e.getMessage());
				return;
			}
		} else {
			/*
			 * Mode parameter not present, it's a StartNodeDiscovery or a
			 * SubscribeNodeRemoval
			 */

			long timeout = this.getLongParameter(Resources.URI_PARAM_TIMEOUT, INTERNAL_TIMEOUT);
			String uriListener = this.getStringParameter(Resources.URI_PARAM_URILISTENER);

			// ReportOnExistingNodes parameter
			Parameter reportOnExistingNodesParam = getParameter(ResourcePathURIs.DISCOVERY_INQUIRY);

			// ReportOnExistingNodes is no longer implemented
			if (reportOnExistingNodesParam != null) {
				generalError("Error: optional '" + ResourcePathURIs.DISCOVERY_INQUIRY + "' parameter's is no longer implemented.");
				return;
			}

			/* TODO: not clear which have to be the type of these parameters */
			Parameter reportAnnouncementsParam = getParameter(ResourcePathURIs.DISCOVERY_ANNOUNCEMENTS);
			Parameter lqiParam = getParameter(ResourcePathURIs.DISCOVERY_LQI);
			Parameter reportLeaveParam = getParameter(ResourcePathURIs.DISCOVERY_LEAVE);
			Parameter freshnessParam = getParameter(ResourcePathURIs.DISCOVERY_FRESHNESS);

			// Calculating the discovery mask
			int discoveryMask = -1;
			if (reportAnnouncementsParam != null) {
				if (discoveryMask == -1) {
					discoveryMask = 0;
				}
				discoveryMask = discoveryMask | GatewayConstants.DISCOVERY_ANNOUNCEMENTS;
			}

			if (lqiParam != null) {
				if (discoveryMask == -1) {
					discoveryMask = 0;
				}
				discoveryMask = discoveryMask | GatewayConstants.DISCOVERY_LQI;
			}

			// Calculating the freshness mask
			int freshnessMask = -1;
			if (freshnessParam != null) {
				if (freshnessMask == -1) {
					freshnessMask = 0;
				}
				freshnessMask = freshnessMask | GatewayConstants.DISCOVERY_FRESHNESS;
			}
			if (reportLeaveParam != null) {
				if (freshnessMask == -1) {
					freshnessMask = 0;
				}
				freshnessMask = freshnessMask | GatewayConstants.DISCOVERY_LEAVE;
			}

			// Control if it's a Start Node Discovery or a SubscribeNodeRemoval
			// or a "Stop" request
			if (discoveryMask == -1 && freshnessMask == -1) {
				// It's a "Stop" request
				try {
					gatewayInterface = getGatewayInterface();

					if (uriListener.toLowerCase().contains("nodediscovered"))
						gatewayInterface.startNodeDiscovery(timeout, 0);
					if (uriListener.toLowerCase().contains("noderemoved"))
						gatewayInterface.subscribeNodeRemoval(timeout, 0);

					sendSuccess();

					return;
				} catch (NullPointerException e) {
					generalError(e.getMessage());
					return;
				} catch (Exception e) {
					generalError(e.getMessage());
					return;
				}
			} else if (discoveryMask != -1 && freshnessMask != -1) {
				/*
				 * Error: you cannot ask for both a Start Node Discovery and
				 * SubscribeNodeRemoval in the same request
				 */
				generalError("Error: you cannot ask for both a Start Node Discovery and SubscribeNodeRemoval in the same request");
			} else if (discoveryMask != -1 && freshnessMask == -1) {
				// It's a StartNodeDiscovery
				try {
					// Obtaining the listener
					ClientResources client = getClientResources(uriListener);

					gatewayInterface = client.getGatewayInterface();

					client.getClientEventListener().setNodeDiscoveredDestination(uriListener);

					gatewayInterface.startNodeDiscovery(timeout, discoveryMask);

					sendSuccess();
				} catch (NullPointerException e) {
					generalError(e.getMessage());
				} catch (Exception e) {
					generalError(e.getMessage());
				}
			} else if (discoveryMask == -1 && freshnessMask != -1) {

				// It's a SubscribeNodeRemoval
				try {
					// Obtaining the listener
					ClientResources client = getClientResources(uriListener);

					gatewayInterface = client.getGatewayInterface();
					// Setting the urlilistener to the listener
					client.getClientEventListener().setNodeRemovedDestination(uriListener);

					// SubscribeNodeRemoval
					gatewayInterface.subscribeNodeRemoval(timeout, freshnessMask);

					sendSuccess();
				} catch (NullPointerException e) {
					generalError(e.getMessage());
				} catch (Exception e) {
					generalError(e.getMessage());
				}
			}
		}
	}
}