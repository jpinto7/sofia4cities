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
package com.indracompany.sofia2.config.services.simulation;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.indracompany.sofia2.config.model.ClientPlatform;
import com.indracompany.sofia2.config.model.DeviceSimulation;
import com.indracompany.sofia2.config.model.Ontology;
import com.indracompany.sofia2.config.model.Token;

public interface DeviceSimulationService {

	List<String> getClientsForUser(String userId);

	List<String> getClientTokensIdentification(String clientPlatformId);

	List<String> getClientOntologiesIdentification(String clientPlatformId);

	List<String> getSimulatorTypes();

	List<DeviceSimulation> getAllSimulations();

	DeviceSimulation getSimulatorByIdentification(String identification);

	DeviceSimulation createSimulation(String identification, int interval, String userId, String json)
			throws JsonProcessingException, IOException;

	void save(DeviceSimulation simulation);

	DeviceSimulation getSimulationById(String id);

	List<DeviceSimulation> getSimulationsForUser(String userId);
	
	DeviceSimulation updateSimulation(String identification, int interval, String json, DeviceSimulation simulation) throws JsonProcessingException, IOException;

	DeviceSimulation getSimulationByJobName(String jobName);
}
