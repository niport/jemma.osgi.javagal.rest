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
package org.energy_home.jemma.javagal.rest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.energy_home.jemma.zgd.GatewayEventListenerExtended;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.InterPANMessageEvent;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.WSNNode;
import org.energy_home.jemma.zgd.jaxb.ZCLMessage;
import org.energy_home.jemma.zgd.jaxb.ZDPMessage;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@code GatewayEventListenerExtended} for the Rest server.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or
 *         <marco.niedducv@gmail.com> from Consoft Sistemi
 *         S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity
 *         SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class RestClientManagerAndListener implements GatewayEventListenerExtended {

	private static final Logger LOG = LoggerFactory.getLogger(RestClientManagerAndListener.class);

	private ExecutorService executor = null;

	private String bindingDestination;
	private String gatewayStopDestination;
	private String leaveResultDestination;
	private String nodeBindingDestination;
	private String nodeDescriptorDestination;
	private String nodeDiscoveredDestination;
	private String nodeRemovedDestination;
	private String nodeServicesDestination;
	private String permitJoinDestination;
	private String resetDestination;
	private String serviceDescriptorDestination;
	private String startGatewayDestination;
	private String unbindingDestination;
	private String zclCommandDestination;
	private String zdpCommandDestination;
	private String interPANCommandDestination;
	private String frequencyAgilityResultDestination;
	private final Context context;

	private PropertiesManager configuration;
	private ClientResources clientResource;

	public RestClientManagerAndListener(PropertiesManager configuration, ClientResources _clientResorce) {
		this.configuration = configuration;
		this.clientResource = _clientResorce;
		this.context = new Context();

		context.getParameters().add("socketTimeout", ((Integer) (configuration.getHttpOptTimeout() * 1000)).toString());

		executor = Executors.newFixedThreadPool(configuration.getNumberOfThreadForAnyPool(), new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "THPool-RestClientManagerAndListener");
			}
		});

		if (executor instanceof ThreadPoolExecutor) {
			((ThreadPoolExecutor) executor).setKeepAliveTime(configuration.getKeepAliveThread(), TimeUnit.MINUTES);
			((ThreadPoolExecutor) executor).allowCoreThreadTimeOut(true);
		}
	}

	private boolean isEmpty(String s) {
		return s.equals("");
	}

	protected void postResource(ClientResource resource, Info.Detail detail) {
		this.postResource(resource, detail, null);
	}

	protected void postResource(ClientResource resource, Status status) {
		this.postResource(resource, null, status);
	}

	protected void postResource(ClientResource resource, Info.Detail detail, Status status) {
		Info info = new Info();
		if (status != null) {
			info.setStatus(status);
		}

		if (detail == null) {
			detail = new Info.Detail();
		}

		info.setDetail(detail);
		String xml = Util.marshal(info);
		if (configuration.getDebugEnabled()) {
			LOG.debug("Marshaled: " + xml);
		}
		resource.post(xml, MediaType.TEXT_XML);
	}

	synchronized public void gatewayStartResult(final Status status) {

		if (isEmpty(startGatewayDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled()) {
						LOG.debug("Connecting to:" + startGatewayDestination);
					}

					ClientResource resource = new ClientResource(context, startGatewayDestination);

					postResource(resource, status);
					resource.release();

					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled()) {
						LOG.error("Exception on gatewayStartResult", e);
					}
					clientResource.addToCounterException();
				}
			}
		});

	}

	public void nodeDiscovered(final Status status, final WSNNode node) {

		if (isEmpty(nodeDiscoveredDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled())
						LOG.debug("Connecting to:" + nodeDiscoveredDestination);

					ClientResource resource = new ClientResource(context, nodeDiscoveredDestination);

					Info.Detail detail = new Info.Detail();
					detail.setWSNNode(node);

					postResource(resource, detail, status);

					resource.release();

					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled())
						LOG.error("Exception on ??", e);

					clientResource.addToCounterException();
				}
			}
		});
	}

	public void nodeRemoved(final Status status, final WSNNode node) {
		if (isEmpty(nodeRemovedDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled())
						LOG.debug("Connecting to:" + nodeRemovedDestination);

					ClientResource resource = new ClientResource(context, nodeRemovedDestination);

					Info.Detail detail = new Info.Detail();
					detail.setWSNNode(node);

					postResource(resource, detail, status);

					resource.release();
					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled()) {
						LOG.error("Exception on ??", e);
					}
					clientResource.addToCounterException();
				}
			}
		});
	}

	public void servicesDiscovered(final Status status, final NodeServices services) {

		if (isEmpty(nodeServicesDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled()) {
						LOG.debug("Connecting to:" + nodeServicesDestination);
					}
					ClientResource resource = new ClientResource(context, nodeServicesDestination);

					Info.Detail detail = new Info.Detail();
					detail.setNodeServices(services);
					postResource(resource, detail, status);

					resource.release();

					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled()) {
						LOG.error("Exception on ??", e);
					}

					clientResource.addToCounterException();
				}
			}
		});
	}

	public void serviceDescriptorRetrieved(final Status status, final ServiceDescriptor service) {

		if (isEmpty(serviceDescriptorDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled()) {
						LOG.debug("Connecting to:" + serviceDescriptorDestination);
					}

					ClientResource resource = new ClientResource(context, serviceDescriptorDestination);

					Info.Detail detail = new Info.Detail();
					detail.setServiceDescriptor(service);

					postResource(resource, detail, status);

					resource.release();

					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled()) {
						LOG.error("Exception on ??", e);
					}
					clientResource.addToCounterException();
				}
			}
		});

	}

	public void dongleResetResult(final Status status) {

		if (isEmpty(resetDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled())
						LOG.debug("Connecting to:" + resetDestination);

					ClientResource resource = new ClientResource(context, resetDestination);

					postResource(resource, status);
					resource.release();

					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled())
						LOG.error("Exception on ??", e);

					clientResource.addToCounterException();
				}
			}
		});
	}

	public void bindingResult(final Status status) {

		if (isEmpty(bindingDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled()) {
						LOG.debug("Connecting to:" + bindingDestination);
					}

					ClientResource resource = new ClientResource(context, bindingDestination);

					postResource(resource, status);

					resource.release();

					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled())
						LOG.error("Exception on ??", e);

					clientResource.addToCounterException();
				}
			}
		});
	}

	public void unbindingResult(final Status status) {

		if (isEmpty(unbindingDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled()) {
						LOG.debug("Connecting to:" + unbindingDestination);
					}

					ClientResource resource = new ClientResource(context, unbindingDestination);

					postResource(resource, status);

					resource.release();
					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled()) {
						LOG.error("Exception on ??", e);
					}

					clientResource.addToCounterException();
				}
			}
		});
	}

	public void nodeBindingsRetrieved(final Status status, final BindingList bindings) {

		if (isEmpty(nodeBindingDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled()) {
						LOG.debug("Connecting to:" + nodeBindingDestination);
					}

					ClientResource resource = new ClientResource(context, nodeBindingDestination);

					Info.Detail detail = new Info.Detail();
					detail.setBindings(bindings);

					postResource(resource, detail, status);

					resource.release();

					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled()) {
						LOG.error("Exception on ??", e);
					}
					clientResource.addToCounterException();
				}
			}
		});
	}

	public void leaveResult(final Status status) {

		if (isEmpty(leaveResultDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled())
						LOG.debug("Connecting to:" + leaveResultDestination);

					ClientResource resource = new ClientResource(context, leaveResultDestination);
					postResource(resource, status);
					resource.release();

					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled()) {
						LOG.error("Exception on ??", e);
					}

					clientResource.addToCounterException();
				}
			}
		});
	}

	public void permitJoinResult(final Status status) {

		if (isEmpty(permitJoinDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled())
						LOG.debug("Connecting to:" + permitJoinDestination);

					ClientResource resource = new ClientResource(context, permitJoinDestination);
					postResource(resource, status);
					resource.release();
					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled()) {
						LOG.error("Exception on ??", e);
					}

					clientResource.addToCounterException();
				}
			}
		});
	}

	public void nodeDescriptorRetrievedExtended(final Status status, final NodeDescriptor node, final Address addressOfInterest) {

		if (isEmpty(nodeDescriptorDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled()) {
						LOG.debug("Connecting to:" + nodeDescriptorDestination);
					}

					ClientResource resource = new ClientResource(context, nodeDescriptorDestination);

					Info.Detail detail = new Info.Detail();
					detail.setNodeDescriptor(node);
					WSNNode n = new WSNNode();
					n.setAddress(addressOfInterest);
					detail.setWSNNode(n);

					postResource(resource, detail, status);

					resource.release();

					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled())
						LOG.error("Exception on ??", e);

					clientResource.addToCounterException();
				}
			}
		});
	}

	@Deprecated
	public void nodeDescriptorRetrieved(final Status status, final NodeDescriptor node) {

		if (isEmpty(nodeDescriptorDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled()) {
						LOG.debug("Connecting to:" + nodeDescriptorDestination);
					}

					ClientResource resource = new ClientResource(context, nodeDescriptorDestination);

					Info.Detail detail = new Info.Detail();
					detail.setNodeDescriptor(node);

					postResource(resource, detail, status);

					resource.release();
					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled()) {
						LOG.error("Exception on ??", e);
					}
					clientResource.addToCounterException();
				}
			}
		});
	}

	public void gatewayStopResult(final Status status) {

		if (isEmpty(gatewayStopDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled())
						LOG.debug("Connecting to:" + gatewayStopDestination);

					ClientResource resource = new ClientResource(context, gatewayStopDestination);

					postResource(resource, status);
					resource.release();

					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled())
						LOG.error("Exception on ??", e);

					clientResource.addToCounterException();
				}
			}
		});
	}

	public void leaveResultExtended(final Status status, final Address addressOfInterest) {

		if (isEmpty(leaveResultDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled()) {
						LOG.debug("Connecting to:" + leaveResultDestination);
					}

					ClientResource resource = new ClientResource(context, leaveResultDestination);

					Info.Detail detail = new Info.Detail();
					WSNNode node = new WSNNode();
					node.setAddress(addressOfInterest);
					detail.setWSNNode(node);

					postResource(resource, detail, status);

					resource.release();
					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled())
						LOG.error("Exception on ??", e);

					clientResource.addToCounterException();
				}
			}
		});
	}

	public void notifyZDPCommand(final ZDPMessage message) {

		if (isEmpty(zdpCommandDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled())
						LOG.debug("Connecting to:" + zdpCommandDestination);

					ClientResource resource = new ClientResource(context, zdpCommandDestination);

					Info.Detail detail = new Info.Detail();
					detail.setZDPMessage(message);

					postResource(resource, detail);
					resource.release();

					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled())
						LOG.error("Exception on ??", e);

					clientResource.addToCounterException();
				}
			}
		});
	}

	public void notifyInterPANCommand(final InterPANMessageEvent message) {

		if (isEmpty(interPANCommandDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled())
						LOG.info("Connecting to:" + interPANCommandDestination);

					ClientResource resource = new ClientResource(context, interPANCommandDestination);

					Info.Detail detail = new Info.Detail();
					detail.setInterPANMessageEvent(message);

					postResource(resource, detail);

					resource.release();
					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled())
						LOG.error(e.getMessage(), e);

					clientResource.addToCounterException();
				}
			}
		});
	}

	public void notifyZCLCommand(final ZCLMessage message) {

		if (isEmpty(zclCommandDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled())
						LOG.debug("Connecting to:" + zclCommandDestination);

					ClientResource resource = new ClientResource(context, zclCommandDestination);

					Info.Detail detail = new Info.Detail();
					detail.setZCLMessage(message);
					postResource(resource, detail);

					resource.release();

					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled())
						LOG.error("Exception on ??", e);

					clientResource.addToCounterException();
				}
			}
		});
	}

	public void frequencyAgilityResponse(final Status status) {

		if (isEmpty(frequencyAgilityResultDestination)) {
			return;
		}

		executor.execute(new Runnable() {
			public void run() {
				try {
					if (configuration.getDebugEnabled()) {
						LOG.debug("Connecting to:" + frequencyAgilityResultDestination);
					}

					ClientResource resource = new ClientResource(context, frequencyAgilityResultDestination);
					postResource(resource, status);

					resource.release();
					clientResource.resetCounter();
				} catch (Exception e) {
					if (configuration.getDebugEnabled()) {
						LOG.error("Exception on ??", e);
					}
					clientResource.addToCounterException();
				}
			}
		});
	}

	public String getGatewayStopDestination() {
		return gatewayStopDestination;
	}

	public void setGatewayStopDestination(String gatewayStopDestination) {
		this.gatewayStopDestination = gatewayStopDestination;
	}

	public String getLeaveResultDestination() {
		return leaveResultDestination;
	}

	public void setLeaveResultDestination(String leaveResultDestination) {
		this.leaveResultDestination = leaveResultDestination;
	}

	public String getNodeDescriptorDestination() {
		return nodeDescriptorDestination;
	}

	public void setNodeDescriptorDestination(String nodeDescriptorDestination) {
		this.nodeDescriptorDestination = nodeDescriptorDestination;
	}

	public String getNodeDiscoveredDestination() {
		return nodeDiscoveredDestination;
	}

	public void setNodeDiscoveredDestination(String nodeDiscoveredDestination) {
		this.nodeDiscoveredDestination = nodeDiscoveredDestination;
	}

	public String getNodeRemovedDestination() {
		return nodeRemovedDestination;
	}

	public void setNodeRemovedDestination(String nodeRemovedDestination) {
		this.nodeRemovedDestination = nodeRemovedDestination;
	}

	public String getNodeServicesDestination() {
		return nodeServicesDestination;
	}

	public void setNodeServicesDestination(String nodeServicesDestination) {
		this.nodeServicesDestination = nodeServicesDestination;
	}

	public String getPermitJoinDestination() {
		return permitJoinDestination;
	}

	public void setPermitJoinDestination(String permitJoinDestination) {
		this.permitJoinDestination = permitJoinDestination;
	}

	public String getResetDestination() {
		return resetDestination;
	}

	public void setResetDestination(String resetDestination) {
		this.resetDestination = resetDestination;
	}

	public String getServiceDescriptorDestination() {
		return serviceDescriptorDestination;
	}

	public void setServiceDescriptorDestination(String serviceDescriptorDestination) {
		this.serviceDescriptorDestination = serviceDescriptorDestination;
	}

	public String getStartGatewayDestination() {
		return startGatewayDestination;
	}

	public void setStartGatewayDestination(String startGatewayDestination) {
		this.startGatewayDestination = startGatewayDestination;
	}

	public String getZclCommandDestination() {
		return zclCommandDestination;
	}

	public void setZclCommandDestination(String zclCommandDestination) {
		this.zclCommandDestination = zclCommandDestination;
	}

	public String getFrequencyAgilityResultDestination() {
		return frequencyAgilityResultDestination;
	}

	public void setFrequencyAgilityResultDestination(String _frequencyAgilityResultDestination) {
		this.frequencyAgilityResultDestination = _frequencyAgilityResultDestination;
	}

	public String getZdpCommandDestination() {
		return zdpCommandDestination;
	}

	public String getinterPANCommandDestination() {
		return interPANCommandDestination;
	}

	public void setZdpCommandDestination(String zdpCommandDestination) {
		this.zdpCommandDestination = zdpCommandDestination;
	}

	public void setInterPANCommandDestination(String interPANCommandDestination) {
		this.interPANCommandDestination = interPANCommandDestination;
	}

	public String getBindingDestination() {
		return bindingDestination;
	}

	public void setBindingDestination(String bindingDestination) {
		this.bindingDestination = bindingDestination;
	}

	public String getUnbindingDestination() {
		return unbindingDestination;
	}

	public void setUnbindingDestination(String unbindingDestination) {
		this.unbindingDestination = unbindingDestination;
	}

	public String getNodeBindingDestination() {
		return nodeBindingDestination;
	}

	public void setNodeBindingDestination(String nodeBindingDestination) {
		this.nodeBindingDestination = nodeBindingDestination;
	}
}
