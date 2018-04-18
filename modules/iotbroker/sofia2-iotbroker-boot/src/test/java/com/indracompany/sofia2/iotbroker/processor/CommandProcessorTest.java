/**
 * Copyright Indra Sistemas, S.A.
 * 2013-2018 SPAIN
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.indracompany.sofia2.iotbroker.processor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indracompany.sofia2.audit.bean.Sofia2AuditEvent.EventType;
import com.indracompany.sofia2.audit.bean.Sofia2AuditEvent.Module;
import com.indracompany.sofia2.iotbroker.audit.aop.IotBrokerAuditableAspect;
import com.indracompany.sofia2.iotbroker.audit.bean.IotBrokerAuditEvent;
import com.indracompany.sofia2.iotbroker.mock.pojo.PojoGenerator;
import com.indracompany.sofia2.iotbroker.mock.ssap.SSAPMessageGenerator;
import com.indracompany.sofia2.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.indracompany.sofia2.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.indracompany.sofia2.iotbroker.plugable.interfaces.security.IoTSession;
import com.indracompany.sofia2.ssap.SSAPMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyCommandMessage;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class CommandProcessorTest {
	private MockMvc mockMvc;
	@Autowired
	private WebApplicationContext wac;
	private ResultActions resultAction;
	private final String URL_COMMAND_PATH  = "/commandAsync";
	@Autowired
	ObjectMapper mapper;

	@Autowired
	GatewayNotifier notifier;

	@MockBean
	SecurityPluginManager securityPluginManager;

	IoTSession session;

	SSAPMessage<SSAPBodyCommandMessage> ssapCommand;

	CompletableFuture<SSAPMessage<SSAPBodyCommandMessage>> completableFutureCommand;

	@MockBean
	DeviceManager deviceManager;

	@MockBean
	IotBrokerAuditableAspect iotBrokerAuditableAspect;

	private void auditMocks() {
		doNothing().when(iotBrokerAuditableAspect).afterReturningExecution(any(), any(), any());
		doNothing().when(iotBrokerAuditableAspect).beforeExecution(any(), any());
		doNothing().when(iotBrokerAuditableAspect).doRecoveryActions(any(),any(),any());

		final IotBrokerAuditEvent evt = new IotBrokerAuditEvent("", UUID.randomUUID().toString(), EventType.IOTBROKER, 10l,"formatedTimeStamp", "user", "ontology", "operationType", Module.IOTBROKER, null, "otherType", "remoteAddress", new IoTSession(), new GatewayInfo(), "query", "data", "clientPlatform", "clientPlatformInstance");
		when(iotBrokerAuditableAspect.getEvent(any(), any())).thenReturn(evt);
	}



	private void securityMocks() {
		session = PojoGenerator.generateSession();
		when(deviceManager.registerActivity(any(), any(), any(), any())).thenReturn(true);

		when(securityPluginManager.getSession(anyString())).thenReturn(Optional.of(session));
		when(securityPluginManager.checkSessionKeyActive(anyString())).thenReturn(true);
		when(securityPluginManager.checkAuthorization(any(), any(), any())).thenReturn(true);
	}

	@Before
	public void setUp() throws IOException, Exception {
		completableFutureCommand = new CompletableFuture<>();
		//		repositoy.deleteByOntologyName(Person.class.getSimpleName());
		securityMocks();
		auditMocks();
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

		ssapCommand = SSAPMessageGenerator.generateCommandMessage(session.getSessionKey());

	}

	@Test
	public void given_AGateway_When_ACommandArrivesInPlatform_Then_TheGatewayReceivesIt() throws Exception {
		completableFutureCommand = new CompletableFuture<>();
		notifier.addCommandListener("test_gateway", (c) -> {
			completableFutureCommand.complete(c);
			return new SSAPMessage<>();
		});

		final StringBuilder url = new StringBuilder(URL_COMMAND_PATH);
		url.append("/test_command/?sessionKey="+session.getSessionKey());

		resultAction = mockMvc.perform(MockMvcRequestBuilders.post(url.toString())
				.accept(org.springframework.http.MediaType.APPLICATION_JSON)
				.content("{}")
				.contentType(org.springframework.http.MediaType.APPLICATION_JSON));

		resultAction.andExpect(status().is2xxSuccessful());
		//		final Boolean result = mapper.readValue(resultAction.andReturn().getResponse().getContentAsString(),
		//				Boolean.class);

		final SSAPMessage<SSAPBodyCommandMessage> response = completableFutureCommand.get(10, TimeUnit.SECONDS);
		Assert.assertNotNull(response);
	}


}
