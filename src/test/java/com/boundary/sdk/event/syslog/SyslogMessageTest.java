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
package com.boundary.sdk.event.syslog;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.Ignore;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringTestSupport;

import com.boundary.sdk.event.syslog.SyslogMessageGenerator;

/**
 * 
 */
public class SyslogMessageTest extends CamelSpringTestSupport {

	private final String SYSLOG_OUT_URI = "mock:syslog-test";
	private final String QUEUE_OUT_URI = "mock:queue-test";
	private final int SYSLOG_ONLY_PORT = 2514;
	private final int SYSLOG_AND_QUEUE_PORT = 3514;
	private final int DELAY = 10;
	private final int DEFAULT_MESSAGE_COUNT = 64;

	@Test
	public void syslogOnlyMessageTest() throws Exception {
		MockEndpoint syslogOut = getMockEndpoint(SYSLOG_OUT_URI);
		int expectedMessageCount = DEFAULT_MESSAGE_COUNT;
		syslogOut.setExpectedMessageCount(expectedMessageCount);
		SyslogMessageGenerator generator = new SyslogMessageGenerator();
		
		generator.setPort(SYSLOG_ONLY_PORT);
		generator.sendMessages(expectedMessageCount, DELAY);

		assertMockEndpointsSatisfied();
	}
	
	@Ignore("Disable for this release")
	@Test
	public void syslogAndQueueMessageTest() throws Exception {
		MockEndpoint queueOut = getMockEndpoint(QUEUE_OUT_URI);
		//queueOut.setMinimumResultWaitTime(10000);
 
		int expectedMessageCount = DEFAULT_MESSAGE_COUNT;
		queueOut.setExpectedMessageCount(expectedMessageCount);
		//queueOut.expectedMinimumMessageCount(expectedMessageCount);
		SyslogMessageGenerator generator = new SyslogMessageGenerator();
		
		generator.setPort(SYSLOG_AND_QUEUE_PORT);
		generator.sendMessages(expectedMessageCount, DELAY);
		
		
		queueOut.await(60000,TimeUnit.SECONDS);

		assertMockEndpointsSatisfied();
	}


	@Override
	protected AbstractApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext(
				"META-INF/syslog-message-test.xml");
	}

}
