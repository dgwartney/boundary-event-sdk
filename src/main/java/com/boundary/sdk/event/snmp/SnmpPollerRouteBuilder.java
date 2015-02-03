// Copyright 2014-2015 Boundary, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.boundary.sdk.event.snmp;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.mp.SnmpConstants;

import static org.apache.camel.LoggingLevel.*;

public class SnmpPollerRouteBuilder extends SNMPRouteBuilder {
	
	public final static String BOUNDARY_HOSTNAME="boundary.hostname";
	
	private static Logger LOG = LoggerFactory.getLogger(SnmpPollerRouteBuilder.class);
	
	private String communityRead;
	private String oids;
	private int delay;
	private List<SnmpPollerConfiguration> configuration;
	
	public SnmpPollerRouteBuilder() {
		communityRead="public";
		delay = 5;
		setPort(161);
		setToUri("seda:metric-translate");
		configuration = new ArrayList<SnmpPollerConfiguration>();
	}
	
	public SnmpPollerRouteBuilder(List<SnmpPollerConfiguration> configuration) {
		this.configuration = configuration;
	}
	
	private void load() {
		
	}

	public void setOids(String oids) {
		this.oids = oids;
	}
	public String getOids() {
		return this.oids;
	}

	public void setCommunityRead(String communityRead) {
		this.communityRead = communityRead;
	}
	public String getCommunityRead() {
		return this.communityRead;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	private String getUri(
			String host,
			long port,
			String oids,
			String community,
			long delay) {
		StringBuffer sb = new StringBuffer();
		sb.append("snmp:");
		sb.append(host);
		sb.append(":");
		sb.append(port);
		sb.append("?protocol=udp");
		sb.append("&type=POLL");
		sb.append("&oids=" + oids);
		sb.append("&snmpVersion=" + SnmpConstants.version2c);
		sb.append("&snmpCommunity=" + community);
		sb.append("&delay="+delay);
		LOG.debug("from: " + sb.toString());
		return sb.toString();
	}
	
	/**
	 * Configuration for the SNMP route 
	 */
	@Override
	public void configure() {
		int startUpOrder = this.getStartUpOrder();
		DataFormat jaxb = new JaxbDataFormat("com.boundary.sdk.event.snmp");

		for (SnmpPollerConfiguration config : this.configuration) {
			
			String fromUri = getUri(
					config.getHost(),
					config.getPort(),
					config.getOidsAsString(),
					config.getCommunity(),
					config.getDelay());

			RouteDefinition routeDefinition = from(fromUri)
					.routeId(this.routeId)
					.setHeader(BOUNDARY_HOSTNAME,
							constant(this.getBindAddress()))
					.log(DEBUG, "body: ${body}")
					.unmarshal(jaxb)
					.marshal().serialization()
					.to(this.getToUri());

			// Setup startup order only if it had been configured
			if (this.getStartUpOrder() != 0) {
				routeDefinition.startupOrder(this.getStartUpOrder());
			}
		}
	}
}
