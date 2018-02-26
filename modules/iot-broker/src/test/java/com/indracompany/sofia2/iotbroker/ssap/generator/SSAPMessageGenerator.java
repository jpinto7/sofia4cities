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
package com.indracompany.sofia2.iotbroker.ssap.generator;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.indracompany.sofia2.ssap.SSAPMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyInsertMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyJoinMessage;
import com.indracompany.sofia2.ssap.enums.SSAPMessageDirection;
import com.indracompany.sofia2.ssap.enums.SSAPMessageTypes;

public final class SSAPMessageGenerator {

	private static final Faker faker = new Faker();
	private static final ObjectMapper mapper = new ObjectMapper();

	public static SSAPMessage<SSAPBodyJoinMessage> generateJoinMessageWithToken() {
		final SSAPMessage<SSAPBodyJoinMessage> ssapMessage = new SSAPMessage<>();
		final SSAPBodyJoinMessage body = new SSAPBodyJoinMessage();
		ssapMessage.setDirection(SSAPMessageDirection.REQUEST);
		ssapMessage.setMessageId(UUID.randomUUID().toString());
		ssapMessage.setMessageType(SSAPMessageTypes.JOIN);
		//		ssapMessage.setOntology(ontology);
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
		message.setBody(body);
		message.setDirection(SSAPMessageDirection.REQUEST);
		message.setMessageType(SSAPMessageTypes.INSERT);
		message.setOntology(ontology);
		return message;
	}


}
