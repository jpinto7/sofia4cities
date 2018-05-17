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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indracompany.sofia2.config.model.IoTSession;
import com.indracompany.sofia2.iotbroker.common.MessageException;
import com.indracompany.sofia2.iotbroker.common.exception.BaseException;
import com.indracompany.sofia2.iotbroker.common.exception.OntologySchemaException;
import com.indracompany.sofia2.iotbroker.common.exception.SSAPProcessorException;
import com.indracompany.sofia2.iotbroker.common.util.SSAPUtils;
import com.indracompany.sofia2.iotbroker.plugable.impl.security.SecurityPluginManager;
import com.indracompany.sofia2.iotbroker.processor.MessageTypeProcessor;
import com.indracompany.sofia2.router.service.app.model.NotificationModel;
import com.indracompany.sofia2.router.service.app.model.OperationModel;
import com.indracompany.sofia2.router.service.app.model.OperationModel.OperationType;
import com.indracompany.sofia2.router.service.app.model.OperationModel.QueryType;
import com.indracompany.sofia2.router.service.app.model.OperationModel.Source;
import com.indracompany.sofia2.router.service.app.model.OperationResultModel;
import com.indracompany.sofia2.router.service.app.service.RouterService;
import com.indracompany.sofia2.ssap.SSAPMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyReturnMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyUpdateByIdMessage;
import com.indracompany.sofia2.ssap.body.SSAPBodyUpdateMessage;
import com.indracompany.sofia2.ssap.body.parent.SSAPBodyMessage;
import com.indracompany.sofia2.ssap.enums.SSAPErrorCode;
import com.indracompany.sofia2.ssap.enums.SSAPMessageTypes;

@Component
public class UpdateProcessor implements MessageTypeProcessor {

	@Autowired
	private RouterService routerService;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	SecurityPluginManager securityPluginManager;

	@Override
	public SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<? extends SSAPBodyMessage> message)
			throws BaseException {

		if (SSAPMessageTypes.UPDATE.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyUpdateMessage> processUpdate = (SSAPMessage<SSAPBodyUpdateMessage>) message;
			return processUpdate(processUpdate);
		}

		if (SSAPMessageTypes.UPDATE_BY_ID.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyUpdateByIdMessage> processUpdate = (SSAPMessage<SSAPBodyUpdateByIdMessage>) message;
			return processUpdateById(processUpdate);
		}

		SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();
		responseMessage = SSAPUtils.generateErrorMessage(message, SSAPErrorCode.PROCESSOR, "Mesage not supported");

		return responseMessage;

	}

	private SSAPMessage<SSAPBodyReturnMessage> processUpdate(SSAPMessage<SSAPBodyUpdateMessage> updateMessage) {

		SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();
		responseMessage.setBody(new SSAPBodyReturnMessage());
		responseMessage.getBody().setOk(true);
		final Optional<IoTSession> session = securityPluginManager.getSession(updateMessage.getSessionKey());

		String user = null;
		String deviceTemplate = null;

		if (session.isPresent()) {
			user = session.get().getUserID();
			deviceTemplate = session.get().getClientPlatform();
		}

		final OperationModel model = OperationModel
				.builder(updateMessage.getBody().getOntology(), OperationType.PUT, user, Source.IOTBROKER)
				.deviceTemplate(deviceTemplate).queryType(QueryType.NATIVE).body(updateMessage.getBody().getQuery())
				.build();

		final NotificationModel modelNotification = new NotificationModel();
		modelNotification.setOperationModel(model);

		OperationResultModel result;
		String responseStr = null;
		String messageStr = null;
		try {
			result = routerService.update(modelNotification);
			messageStr = result.getMessage();
			responseStr = result.getResult();

			final String response = String.format("{\"nModified\":%s}", responseStr);
			responseMessage.getBody().setData(objectMapper.readTree(response));

		} catch (final Exception e) {
			// TODO: LOG

			final String error = MessageException.ERR_DATABASE;
			responseMessage = SSAPUtils.generateErrorMessage(updateMessage, SSAPErrorCode.PROCESSOR, error);
			if (messageStr != null) {
				responseMessage.getBody().setError(messageStr);
			}
		}

		return responseMessage;
	}

	private SSAPMessage<SSAPBodyReturnMessage> processUpdateById(SSAPMessage<SSAPBodyUpdateByIdMessage> updateMessage) {

		SSAPMessage<SSAPBodyReturnMessage> responseMessage = new SSAPMessage<>();
		responseMessage.setBody(new SSAPBodyReturnMessage());
		responseMessage.getBody().setOk(true);
		final Optional<IoTSession> session = securityPluginManager.getSession(updateMessage.getSessionKey());

		String user = null;
		String deviceTemplate = null;

		if (session.isPresent()) {
			user = session.get().getUserID();
			deviceTemplate = session.get().getClientPlatform();
		}

		final OperationModel model = OperationModel
				.builder(updateMessage.getBody().getOntology(), OperationType.PUT, user, Source.IOTBROKER)
				.objectId(updateMessage.getBody().getId()).queryType(QueryType.NATIVE)
				.body(updateMessage.getBody().getData().toString()).deviceTemplate(deviceTemplate).build();

		final NotificationModel modelNotification = new NotificationModel();
		modelNotification.setOperationModel(model);

		OperationResultModel result;
		String responseStr = null;
		String messageStr = null;
		try {
			result = routerService.update(modelNotification);
			responseStr = result.getResult();
			messageStr = result.getMessage();
			responseMessage.getBody().setData(objectMapper.readTree(responseStr));

		} catch (final Exception e) {
			// TODO LOG

			final String error = MessageException.ERR_DATABASE;
			responseMessage = SSAPUtils.generateErrorMessage(updateMessage, SSAPErrorCode.PROCESSOR, error);
			if (messageStr != null) {
				responseMessage.getBody().setError(messageStr);
			}

		}

		return responseMessage;
	}

	@Override
	public boolean validateMessage(SSAPMessage<? extends SSAPBodyMessage> message)
			throws OntologySchemaException, BaseException, Exception {

		if (SSAPMessageTypes.UPDATE.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyUpdateMessage> updateMessage = (SSAPMessage<SSAPBodyUpdateMessage>) message;
			return validateMessageUpdate(updateMessage);
		}

		if (SSAPMessageTypes.UPDATE_BY_ID.equals(message.getMessageType())) {
			final SSAPMessage<SSAPBodyUpdateByIdMessage> updateMessage = (SSAPMessage<SSAPBodyUpdateByIdMessage>) message;
			return validateMessageUpdateById(updateMessage);
		}

		return false;
	}

	private boolean validateMessageUpdate(SSAPMessage<SSAPBodyUpdateMessage> updateMessage)
			throws SSAPProcessorException {
		if (StringUtils.isEmpty(updateMessage.getBody().getQuery())) {
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY, "quey",
					updateMessage.getMessageType().name()));
		}
		return true;
	}

	private boolean validateMessageUpdateById(SSAPMessage<SSAPBodyUpdateByIdMessage> updateMessage)
			throws SSAPProcessorException {
		if (StringUtils.isEmpty(updateMessage.getBody().getId())) {
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY, "id",
					updateMessage.getMessageType().name()));
		}
		if (updateMessage.getBody().getData() == null || updateMessage.getBody().getData().isNull()) {
			throw new SSAPProcessorException(String.format(MessageException.ERR_FIELD_IS_MANDATORY, "data",
					updateMessage.getMessageType().name()));
		}
		return true;
	}

	@Override
	public List<SSAPMessageTypes> getMessageTypes() {
		final List<SSAPMessageTypes> types = new ArrayList<>();
		types.add(SSAPMessageTypes.UPDATE);
		types.add(SSAPMessageTypes.UPDATE_BY_ID);
		return types;
	}

}
