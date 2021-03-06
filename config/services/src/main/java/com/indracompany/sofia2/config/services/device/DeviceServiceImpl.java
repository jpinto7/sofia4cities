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
package com.indracompany.sofia2.config.services.device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.indracompany.sofia2.config.components.LogOntology;
import com.indracompany.sofia2.config.model.ClientPlatform;
import com.indracompany.sofia2.config.model.Device;
import com.indracompany.sofia2.config.repository.ClientPlatformRepository;
import com.indracompany.sofia2.config.repository.DeviceRepository;

@Service
public class DeviceServiceImpl implements DeviceService {

	@Autowired
	DeviceRepository deviceRepository;
	@Autowired
	ClientPlatformRepository clientPlatformRepository;
	@Autowired
	ObjectMapper mapper;

	@Override
	public List<Device> getAll() {
		return deviceRepository.findAll();
	}

	@Override
	public List<Device> getByClientPlatformId(ClientPlatform clientPlatform) {

		final List<Device> devices = deviceRepository.findByClientPlatform(clientPlatform);

		return devices;
	}

	@Override
	public void createDevice(Device device) {
		deviceRepository.save(device);
	}

	@Override
	public void updateDevice(Device device) {
		deviceRepository.save(device);
	}

	@Override
	@Transactional
	public int updateDeviceStatusAndDisableWhenUpdatedAtLessThanDate(boolean status, boolean disabled, Date date) {
		return deviceRepository.updateDeviceStatusByUpdatedAt(status, disabled, date);
	}

	@Override
	public List<Device> getByClientPlatformIdAndIdentification(ClientPlatform clientPlatform, String identification) {
		return deviceRepository.findByClientPlatformAndIdentification(clientPlatform, identification);
	}

	@Override
	public Device getById(String id) {
		return this.deviceRepository.findById(id);
	}

	@Override
	public void patchDevice(String deviceId, String tags) {
		Device device = this.deviceRepository.findById(deviceId);
		device.setTags(tags);
		this.deviceRepository.save(device);

	}

	@Override
	public List<LogOntology> getLogInstances(String resultFromQueryTool) throws IOException {
		try {
			ArrayNode arrayResult = (ArrayNode) mapper.readTree(resultFromQueryTool);
			ArrayNode newArray = mapper.createArrayNode();
			for (JsonNode node : arrayResult) {
				newArray.add(node.get("value").get("DeviceLog"));
			}

			return mapper.readValue(newArray.toString(), new TypeReference<List<LogOntology>>() {
			});
		} catch (RuntimeException re) {
			return null;
		}

	}

	@Override
	public List<String> getDeviceCommands(Device device) {
		List<String> commandActions = new ArrayList<>();
		if (!StringUtils.isEmpty(device.getJsonActions())) {
			try {
				JsonNode commands = mapper.readTree(device.getJsonActions());
				if (!commands.isArray()) {
					Iterator<String> fields = commands.fieldNames();
					while (fields.hasNext()) {
						commandActions.add(fields.next());
					}
				} else {
					for (JsonNode command : commands) {
						Iterator<String> fields = command.fieldNames();
						while (fields.hasNext()) {
							commandActions.add(fields.next());
						}
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return commandActions;
	}

}
