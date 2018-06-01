/**
 * Copyright Indra Sistemas, S.A.
 * 2013-2018 SPAIN
 *
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
package com.indracompany.sofia2.rtdbmaintainer.audit.bean;

import java.util.Map;

import com.indracompany.sofia2.audit.bean.Sofia2AuditRemoteEvent;

import lombok.Builder;

public class RtdbMaintainerAuditEvent extends Sofia2AuditRemoteEvent {

	private static final long serialVersionUID = 1L;

	@Builder
	private RtdbMaintainerAuditEvent(String message, String id, EventType type, long timeStamp,
			String formatedTimeStamp, String user, String ontology, String operationType, Module module,
			Map<String, Object> extraData, String otherType, String remoteAddress, ResultOperationType resultOperation,
			String query, String data) {
		super(message, id, type, timeStamp, formatedTimeStamp, user, ontology, operationType, module, extraData,
				otherType, remoteAddress, resultOperation);
	}

}