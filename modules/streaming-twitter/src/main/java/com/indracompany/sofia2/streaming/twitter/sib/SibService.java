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
package com.indracompany.sofia2.streaming.twitter.sib;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.indracompany.sofia2.common.exception.AuthenticationException;
import com.indracompany.sofia2.iotbroker.common.exception.SSAPComplianceException;
import com.indracompany.sofia2.ssap.SSAPMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyReturnMessage;

public interface SibService {

	String getSessionKey(String token) throws SSAPComplianceException, AuthenticationException;
	void inserOntologyInstanceToMongo(String instance, String ontology, String clientPlatform, String clientPlatformInstance, String user) throws JsonProcessingException, IOException;
	SSAPMessage<SSAPBodyReturnMessage> disconnect(String sessionKey);
	boolean insertOntologyInstance(String instance, String sessionKey,
			String ontology, String clientPlatform, String clientPlatformInstance) throws JsonProcessingException, IOException;
}