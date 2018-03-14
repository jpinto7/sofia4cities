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
package com.indracompany.sofia2.iotbroker.mock.ssap;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.indracompany.sofia2.ssap.SSAPMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyCommandMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyDeleteByIdMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyDeleteMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyInsertMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyJoinMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyQueryMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodySubscribeMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyUpdateByIdMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyUpdateMessage;
import com.indracompany.sofia2.ssap.enums.SSAPMessageDirection;
import com.indracompany.sofia2.ssap.enums.SSAPMessageTypes;
import com.indracompany.sofia2.ssap.enums.SSAPQueryType;

public final class SSAPMessageGenerator {

	private static final Faker faker = new Faker();
	private static final ObjectMapper mapper = new ObjectMapper();

	public static SSAPMessage<SSAPBodyJoinMessage> generateJoinMessageWithToken() {
		final SSAPMessage<SSAPBodyJoinMessage> ssapMessage = new SSAPMessage<>();
		final SSAPBodyJoinMessage body = new SSAPBodyJoinMessage();
		ssapMessage.setDirection(SSAPMessageDirection.REQUEST);
		ssapMessage.setMessageId(UUID.randomUUID().toString());
		ssapMessage.setMessageType(SSAPMessageTypes.JOIN);
		body.setClientPlatform(faker.name().firstName());
		body.setClientPlatformInstance(UUID.randomUUID().toString());
		body.setToken(UUID.randomUUID().toString());

		ssapMessage.setBody(body);

		return ssapMessage;
	}

	public static SSAPMessage<SSAPBodyInsertMessage> generateInsertMessage(String ontology, Object value) throws Exception, IOException {

		final SSAPMessage<SSAPBodyInsertMessage> message = new SSAPMessage<>();
		message.setSessionKey(UUID.randomUUID().toString());

		final SSAPBodyInsertMessage body = new SSAPBodyInsertMessage();
		final JsonNode jsonValue = mapper.readTree(mapper.writeValueAsBytes(value));
		body.setData(jsonValue);
		body.setOntology(ontology);
		message.setBody(body);
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.setMessageType(SSAPMessageTypes.INSERT);
		return message;
	}

	public static SSAPMessage<SSAPBodyUpdateMessage> generateUpdateMessage(String ontology, String query) throws Exception, IOException {

		final SSAPMessage<SSAPBodyUpdateMessage> message = new SSAPMessage<>();
		message.setSessionKey(UUID.randomUUID().toString());

		final SSAPBodyUpdateMessage body = new SSAPBodyUpdateMessage();
		body.setQuery(query);
		body.setOntology(ontology);
		message.setBody(body);
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.setMessageType(SSAPMessageTypes.UPDATE);

		return message;
	}

	public static SSAPMessage<SSAPBodyUpdateByIdMessage> generateUpdateByIdtMessage(String ontology, JsonNode data) {
		final SSAPMessage<SSAPBodyUpdateByIdMessage> message = new SSAPMessage<>();
		message.setSessionKey(UUID.randomUUID().toString());

		final SSAPBodyUpdateByIdMessage body = new SSAPBodyUpdateByIdMessage();
		body.setData(data);
		body.setOntology(ontology);
		message.setBody(body);
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.setMessageType(SSAPMessageTypes.UPDATE_BY_ID);

		return message;
	}

	public static SSAPMessage<SSAPBodyDeleteMessage> generateDeleteMessage(String ontology, String query) {
		final SSAPMessage<SSAPBodyDeleteMessage> message = new SSAPMessage<>();
		message.setSessionKey(UUID.randomUUID().toString());

		final SSAPBodyDeleteMessage body = new SSAPBodyDeleteMessage();
		body.setOntology(ontology);
		body.setQuery(query);
		message.setBody(body);
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.setMessageType(SSAPMessageTypes.DELETE);

		return message;
	}

	public static SSAPMessage<SSAPBodyDeleteByIdMessage> generateDeleteByIdMessage(String ontology, String id) {
		final SSAPMessage<SSAPBodyDeleteByIdMessage> message = new SSAPMessage<>();
		message.setSessionKey(UUID.randomUUID().toString());

		final SSAPBodyDeleteByIdMessage body = new SSAPBodyDeleteByIdMessage();
		body.setOntology(ontology);
		body.setId(id);
		message.setBody(body);
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.setMessageType(SSAPMessageTypes.DELETE_BY_ID);

		return message;
	}

	public static SSAPMessage<SSAPBodyQueryMessage> generateQueryMessage(String ontology, SSAPQueryType type, String query) {
		final SSAPMessage<SSAPBodyQueryMessage> message = new SSAPMessage<>();
		message.setSessionKey(UUID.randomUUID().toString());

		final SSAPBodyQueryMessage body = new SSAPBodyQueryMessage();
		body.setOntology(ontology);
		body.setQueryType(type);
		body.setQuery(query);
		message.setBody(body);
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.setMessageType(SSAPMessageTypes.QUERY);

		return message;
	}

	public static SSAPMessage<SSAPBodySubscribeMessage> generateSubscriptionMessage(String ontology, String sessionKey,SSAPQueryType queryType, String query) {
		final SSAPMessage<SSAPBodySubscribeMessage> message = new SSAPMessage<>();
		message.setSessionKey(sessionKey);

		final SSAPBodySubscribeMessage body = new SSAPBodySubscribeMessage();
		body.setOntology(ontology);
		body.setQueryType(queryType);
		body.setQuery(query);
		message.setBody(body);
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.setMessageType(SSAPMessageTypes.SUBSCRIBE);

		return message;
	}

	public static SSAPMessage<SSAPBodyCommandMessage> generateCommandMessage(String sessionKey) throws JsonProcessingException, IOException {
		final SSAPMessage<SSAPBodyCommandMessage> message = new SSAPMessage<>();
		message.setSessionKey(sessionKey);

		final SSAPBodyCommandMessage body = new SSAPBodyCommandMessage();
		body.setCommand("COMMAND");
		body.setCommandId(UUID.randomUUID().toString());
		body.setParams(mapper.readTree("{}"));
		message.setBody(body);
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.setMessageType(SSAPMessageTypes.COMMAND);

		return message;
	}

	//	public static SSAPMessage<SSAPBodyLeaveMessage> generateLeaveMessage() {
	//		final SSAPMessage<SSAPBodyLeaveMessage> message = new SSAPMessage<>();
	//
	//	}
}
