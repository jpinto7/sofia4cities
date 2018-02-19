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
package com.indracompany.sofia2.iotbroker.processor.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.indracompany.sofia2.common.exception.AuthenticationException;
import com.indracompany.sofia2.iotbroker.common.MessageException;
import com.indracompany.sofia2.iotbroker.common.exception.SSAPComplianceException;
import com.indracompany.sofia2.iotbroker.processor.MessageTypeProcessor;
import com.indracompany.sofia2.plugin.iotbroker.security.SecurityPluginManager;
import com.indracompany.sofia2.ssap.SSAPMessage;
import com.indracompany.sofia2.ssap.SSAPMessageDirection;
import com.indracompany.sofia2.ssap.SSAPMessageTypes;
import com.indracompany.sofia2.ssap.body.SSAPBodyJoinMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyReturnMessage;
import com.indracompany.sofia2.ssap.body.parent.SSAPBodyMessage;

@Component
public class JoinProcessor implements MessageTypeProcessor {

	@Autowired
	SecurityPluginManager securityManager;

	@SuppressWarnings("unchecked")
	@Override
	public SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<? extends SSAPBodyMessage> message)
			throws SSAPComplianceException, AuthenticationException {
		SSAPMessage<SSAPBodyJoinMessage> join = (SSAPMessage<SSAPBodyJoinMessage>) message;
		SSAPMessage<SSAPBodyReturnMessage> response = new SSAPMessage<>();

		if (StringUtils.isEmpty(join.getBody().getToken())) {
			throw new SSAPComplianceException(MessageException.ERR_TOKEN_IS_MANDATORY);
		}

		String sessionKey = securityManager.authenticate(message);

		if (StringUtils.isEmpty(sessionKey)) {
			response.setDirection(SSAPMessageDirection.RESPONSE);
			response.setMessageId(join.getMessageId());
			response.setMessageType(SSAPMessageTypes.JOIN);
			response.setSessionKey(sessionKey);
		} else {
			throw new AuthenticationException(MessageException.ERR_SESSIONKEY_NOT_ASSINGED);
		}

		return response;
	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		return Collections.singletonList(SSAPMessageTypes.JOIN);
	}

	@Override
	public void validateMessage(SSAPMessage<? extends SSAPBodyMessage> message) {
	}

}