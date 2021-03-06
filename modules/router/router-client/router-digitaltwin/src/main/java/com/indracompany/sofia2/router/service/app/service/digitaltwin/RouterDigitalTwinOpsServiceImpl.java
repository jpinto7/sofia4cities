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
package com.indracompany.sofia2.router.service.app.service.digitaltwin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indracompany.sofia2.persistence.mongodb.MongoBasicOpsDBRepository;
import com.indracompany.sofia2.router.service.app.model.DigitalTwinCompositeModel;
import com.indracompany.sofia2.router.service.app.model.DigitalTwinModel;
import com.indracompany.sofia2.router.service.app.model.OperationResultModel;
import com.indracompany.sofia2.router.service.app.service.RouterDigitalTwinService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RouterDigitalTwinOpsServiceImpl implements RouterDigitalTwinService {

	final static String EVENTS_COLLECTION = "TwinEvents";
	final static String LOG_COLLECTION = "TwinLogs";
	final static String PROPERTIES_COLLECTION = "TwinProperties";
	final static String ACTIONS_COLLECTION = "TwinActions";

	DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	@Autowired
	private MongoBasicOpsDBRepository mongoRepo;

	@Override
	public OperationResultModel insertEvent(DigitalTwinCompositeModel compositeModel) {
		log.info("Router Digital Twin Service Operation " + compositeModel.getDigitalTwinModel().toString());
		OperationResultModel result = new OperationResultModel();
		DigitalTwinModel model = compositeModel.getDigitalTwinModel();

		final String event = model.getEvent().name();

		JSONObject instance = new JSONObject();

		JSONObject timestamp = new JSONObject();
		timestamp.put("$date", LocalDateTime.now().format(timestampFormatter));

		instance.put("deviceId", model.getDeviceId());
		instance.put("type", model.getType());
		instance.put("timestamp", timestamp);
		instance.put("event", event);

		if (model.getEventName() != null) {
			instance.put("eventName", model.getEventName());
		}

		Optional<JSONObject> optionalContent = buildEventContent(model);
		if (optionalContent.isPresent()) {
			instance.put("content", optionalContent.get());
		}

		String output = "";

		result.setMessage("OK");
		result.setStatus(true);

		try {

			output = mongoRepo.insert(EVENTS_COLLECTION, null, instance.toString());

		} catch (final Exception e) {
			result.setResult(output);
			result.setStatus(false);
			result.setMessage(e.getMessage());
		}

		result.setResult(output);
		result.setOperation(event);
		return result;
	}

	@Override
	public OperationResultModel insertLog(DigitalTwinCompositeModel compositeModel) {
		log.info("Router Digital Twin Service Operation " + compositeModel.getDigitalTwinModel().toString());
		OperationResultModel result = new OperationResultModel();
		DigitalTwinModel model = compositeModel.getDigitalTwinModel();

		String event = model.getEvent().name();

		JSONObject instance = new JSONObject();

		JSONObject timestamp = new JSONObject();
		timestamp.put("$date", LocalDateTime.now().format(timestampFormatter));

		instance.put("trace", model.getLog());
		instance.put("deviceId", model.getDeviceId());
		instance.put("type", model.getType());
		instance.put("timestamp", timestamp);

		String output = "";

		result.setMessage("OK");
		result.setStatus(true);

		try {

			output = mongoRepo.insert(LOG_COLLECTION, null, instance.toString());

		} catch (final Exception e) {
			result.setResult(output);
			result.setStatus(false);
			result.setMessage(e.getMessage());
		}

		result.setResult(output);
		result.setOperation(event);
		return result;
	}

	@Override
	public OperationResultModel updateShadow(DigitalTwinCompositeModel compositeModel) {
		log.info("Router Digital Twin Service Operation " + compositeModel.getDigitalTwinModel().toString());
		OperationResultModel result = new OperationResultModel();

		DigitalTwinModel model = compositeModel.getDigitalTwinModel();

		String event = model.getEvent().name();

		JSONObject timestamp = new JSONObject();
		timestamp.put("$date", LocalDateTime.now().format(timestampFormatter));

		JSONObject instance = new JSONObject();
		instance.put("deviceId", model.getDeviceId());
		instance.put("type", model.getType());
		instance.put("timestamp", timestamp);
		instance.put("status", new JSONObject(model.getStatus()));

		String output = "";

		result.setMessage("OK");
		result.setStatus(true);

		try {

			output = mongoRepo.insert(PROPERTIES_COLLECTION + model.getType().substring(0, 1).toUpperCase()
					+ model.getType().substring(1), null, instance.toString());

		} catch (final Exception e) {
			result.setResult(output);
			result.setStatus(false);
			result.setMessage(e.getMessage());
		}

		result.setResult(output);
		result.setOperation(event);
		return result;
	}

	public OperationResultModel insertAction(DigitalTwinCompositeModel compositeModel) {
		log.info("Router Digital Twin Service Operation " + compositeModel.getDigitalTwinModel().toString());
		OperationResultModel result = new OperationResultModel();

		DigitalTwinModel model = compositeModel.getDigitalTwinModel();

		String action = model.getActionName();

		JSONObject timestamp = new JSONObject();
		timestamp.put("$date", LocalDateTime.now().format(timestampFormatter));

		JSONObject instance = new JSONObject();
		instance.put("deviceId", model.getDeviceId());
		instance.put("type", model.getType());
		instance.put("timestamp", timestamp);
		instance.put("action", action);

		String output = "";
		result.setMessage("OK");
		result.setStatus(true);

		try {

			output = mongoRepo.insert(
					ACTIONS_COLLECTION + model.getType().substring(0, 1).toUpperCase() + model.getType().substring(1),
					null, instance.toString());

		} catch (final Exception e) {
			result.setResult(output);
			result.setStatus(false);
			result.setMessage(e.getMessage());
		}

		result.setResult(output);
		result.setOperation(action);
		return result;
	}

	private Optional<JSONObject> buildEventContent(DigitalTwinModel model) {
		JSONObject content = new JSONObject();

		switch (model.getEvent()) {
		case REGISTER:
			content.put("endpoint", model.getEndpoint());
			return Optional.of(content);

		default:
			return Optional.empty();
		}

	}
}
