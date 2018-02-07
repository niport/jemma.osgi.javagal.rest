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
 */
package org.energy_home.jemma.javagal.rest.resources;

import java.math.BigInteger;

import org.energy_home.jemma.javagal.rest.GalManagerRestApplication;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.resource.ServerResource;

public class CommonResource extends ServerResource {

	protected void sendErrorReply(int errorCode, String errorMessage) {
		Info info = new Info();
		Status _st = new Status();
		_st.setCode((short) errorCode);
		_st.setMessage(errorMessage);
		info.setStatus(_st);
		Info.Detail detail = new Info.Detail();
		info.setDetail(detail);
		getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
	}

	/**
	 * Send a GatewayConstants.SUCCESS response.
	 */
	protected void sendSuccess() {
		Info.Detail detail = new Info.Detail();
		Info infoToReturn = new Info();
		Status status = new Status();
		status.setCode((short) GatewayConstants.SUCCESS);
		infoToReturn.setStatus(status);
		infoToReturn.setRequestIdentifier(Util.getRequestIdentifier());
		infoToReturn.setDetail(detail);

		getResponse().setEntity(Util.marshal(infoToReturn), MediaType.TEXT_XML);
	}

	/**
	 * Send back a result with the specified details
	 * 
	 * @param details
	 *          An Info.Detail object containing the details of the result.
	 */
	protected void sendResult(Info.Detail details) {
		Info info = new Info();
		Status status = new Status();
		status.setCode((short) GatewayConstants.SUCCESS);
		info.setStatus(status);
		info.setDetail(details);
		getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
	}

	protected void sendStatus(Status status) {
		Info info = new Info();
		info.setStatus(status);
		Info.Detail detail = new Info.Detail();
		info.setDetail(detail);
		getResponse().setEntity(Util.marshal(info), MediaType.TEXT_XML);
	}

	protected void generalError(String errorMessage) {
		this.sendErrorReply(GatewayConstants.GENERAL_ERROR, errorMessage);
	}

	protected Parameter getParameter(String name) {
		return getRequest().getResourceRef().getQueryAsForm().getFirst(name);
	}

	/**
	 * Read a long parameter from the URI.
	 * 
	 * FIXME: add ranges (min e max value expected).
	 * 
	 * @param name
	 *          The name of the parameter.
	 * @param isMandatory
	 *          Set to true if this parameter is mandatory.
	 * 
	 * @param defaultValue
	 *          If the isMandatory parameter is false, then this argument is used
	 *          to return a defaultValue in case the parameter is missing.
	 * @return The read parameter or default if missing.
	 */
	protected long getLongParameter(String name, boolean isMandatory, long defaultValue) {

		Parameter param = getParameter(name);

		long value = defaultValue;

		if (param != null) {
			String timeoutString = param.getSecond();
			try {
				value = Long.decode(Resources.HEX_PREFIX + timeoutString);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(name + " parameter must be a number");
			}

			// if (!Util.isUnsigned32(value)) {
			// throw new IllegalArgumentException(parameterName + " parameter must be
			// in the range [0, 0xffff]");
			// }
		} else if (isMandatory) {
			throw new IllegalArgumentException("Missing mandatory " + name + " parameter.");
		}

		return value;
	}

	protected String getStringParameter(String parameterName, String defaultValue) {

		Parameter param = getParameter(parameterName);

		if (param == null) {
			return defaultValue;
		}

		return param.getSecond();
	}

	protected String getStringParameter(String name) {

		Parameter param = getParameter(name);
		if (param == null) {
			throw new IllegalArgumentException("Missing mandatory " + name + " parameter.");
		}
		return param.getSecond();
	}

	protected long getLongParameter(String parameterName) {
		return this.getLongParameter(parameterName, true, 0);
	}

	protected long getLongParameter(String parameterName, long defaultValue) {
		return this.getLongParameter(parameterName, false, defaultValue);
	}

	/**
	 * Interpret the URI argument as a ZigBee address. If can be both a 4 or 16
	 * hex digit address. In the first case it will be saved in the returned
	 * Address class instance as a short address, in the latter as a IEEE address.
	 * 
	 * @param name
	 *          The name of the attribute.
	 * @return An instance of the Address class.
	 */
	protected Address getAddressAttribute(String name) {
		String s = (String) getRequest().getAttributes().get(name);

		if (s == null) {
			throw new IllegalArgumentException("Missing mandatory " + name + " attribute.");
		}

		Address address = new Address();
		if (s.length() == 16) {
			BigInteger addressBigInteger = BigInteger.valueOf(Long.parseLong(s, 16));
			address.setIeeeAddress(addressBigInteger);
		} else if (s.length() == 4) {
			Integer addressInteger = Integer.parseInt(s, 16);
			address.setNetworkAddress(addressInteger);
		} else {
			throw new IllegalArgumentException("Wrong address parameter: " + s);
		}
		return address;
	}

	/**
	 * @param attributeName
	 * @return
	 */
	protected short getShortAttribute(String attributeName) {

		String s = (String) getRequest().getAttributes().get(attributeName);

		short result;

		try {
			result = Short.parseShort(s, 16);
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("Error: mandatory '" + attributeName + "' attribute value invalid. You provided: " + s);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Error: mandatory '" + attributeName + "' attribute value invalid. You provided: " + s);
		}

		return result;
	}

	protected ClientResources getClientResources(String s) throws Exception {
		return getRestManager().getClientObjectKey(Util.getPortFromUriListener(s), getClientInfo().getAddress());
	}

	protected GatewayInterface getGatewayInterface() throws Exception {
		return getRestManager().getClientObjectKey(-1, getClientInfo().getAddress()).getGatewayInterface();
	}

	/**
	 * Gets the RestManager.
	 * 
	 * @return the RestManager.
	 */
	protected RestManager getRestManager() {
		return ((GalManagerRestApplication) getApplication()).getRestManager();
	}
}
