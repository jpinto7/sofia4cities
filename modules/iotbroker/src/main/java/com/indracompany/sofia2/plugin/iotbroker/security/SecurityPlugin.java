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
package com.indracompany.sofia2.plugin.iotbroker.security;

import com.indracompany.sofia2.common.exception.AuthenticationException;
import com.indracompany.sofia2.common.exception.AuthorizationException;
import com.indracompany.sofia2.ssap.SSAPMessage;
import com.indracompany.sofia2.ssap.SSAPMessageTypes;
import com.indracompany.sofia2.ssap.body.parent.SSAPBodyMessage;

public interface SecurityPlugin {
	public String authenticate(SSAPMessage<? extends SSAPBodyMessage> message) throws AuthenticationException;

	public void closeSession(String sessionKey) throws AuthorizationException;

	public void checkSessionKeyActive(String sessionKey) throws AuthorizationException;

	public void checkAuthorization(SSAPMessageTypes messageType, String ontology, String sessionKey) throws AuthorizationException;
}