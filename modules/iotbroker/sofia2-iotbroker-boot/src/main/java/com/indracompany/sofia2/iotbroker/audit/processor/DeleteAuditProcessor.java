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
package com.indracompany.sofia2.iotbroker.audit.processor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.indracompany.sofia2.config.model.IoTSession;
import com.indracompany.sofia2.iotbroker.audit.aop.MessageAuditProcessor;
import com.indracompany.sofia2.iotbroker.audit.bean.IotBrokerAuditEvent;
import com.indracompany.sofia2.iotbroker.audit.bean.IotBrokerAuditEventFactory;
import com.indracompany.sofia2.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.indracompany.sofia2.ssap.SSAPMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyDeleteByIdMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyDeleteMessage;
import com.indracompany.sofia2.ssap.body.parent.SSAPBodyMessage;
import com.indracompany.sofia2.ssap.enums.SSAPMessageTypes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DeleteAuditProcessor implements MessageAuditProcessor {

	@Override
	public IotBrokerAuditEvent process(SSAPMessage<? extends SSAPBodyMessage> message, IoTSession session,
			GatewayInfo info) {

		log.debug("Processing delete message");
		IotBrokerAuditEvent event = null;

		if (SSAPMessageTypes.DELETE.equals(message.getMessageType())) {

			final SSAPBodyDeleteMessage deletetMessage = (SSAPBodyDeleteMessage) message.getBody();
			deletetMessage.getOntology();
			deletetMessage.getQuery();

			String deleteMessageText = "Delete ontology " + deletetMessage.getOntology();

			if (session != null) {
				deleteMessageText += " by user " + session.getUserID();
			}

			event = IotBrokerAuditEventFactory.builder().build().createIotBrokerAuditEvent(deletetMessage,
					deleteMessageText, session, info);

		} else if (SSAPMessageTypes.DELETE_BY_ID.equals(message.getMessageType())) {

			SSAPBodyDeleteByIdMessage deleteMessage = (SSAPBodyDeleteByIdMessage) message.getBody();

			String deleteMessageText = "Delete ontology " + deleteMessage.getOntology() + " by id  "
					+ deleteMessage.getId();

			if (session != null) {
				deleteMessageText += "  and user  " + session.getUserID();
			}

			event = IotBrokerAuditEventFactory.builder().build().createIotBrokerAuditEvent(deleteMessage,
					deleteMessageText, session, info);
		}

		return event;
	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		final List<SSAPMessageTypes> types = new ArrayList<>();
		types.add(SSAPMessageTypes.DELETE);
		types.add(SSAPMessageTypes.DELETE_BY_ID);
		return types;
	}

}