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
package org.energy_home.jemma.javagal.rest;

import org.energy_home.jemma.javagal.rest.util.Util;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.service.ConverterService;
import org.restlet.service.StatusService;

public class RestXmlStatusService extends StatusService {

	private ConverterService converter;

	public RestXmlStatusService() {
		converter = new ConverterService();
	}

	@Override
	public Representation getRepresentation(Status status, Request request, Response response) {
		Throwable t = status.getThrowable();

		Info info = new Info();
		org.energy_home.jemma.zgd.jaxb.Status s = new org.energy_home.jemma.zgd.jaxb.Status();
		s.setCode((short) GatewayConstants.GENERAL_ERROR);
		s.setMessage(t.getMessage());
		info.setStatus(s);
		Info.Detail detail = new Info.Detail();
		info.setDetail(detail);

		Representation result = converter.toRepresentation(Util.marshal(info), new Variant(MediaType.APPLICATION_XML), null);
		return result;
	}
}