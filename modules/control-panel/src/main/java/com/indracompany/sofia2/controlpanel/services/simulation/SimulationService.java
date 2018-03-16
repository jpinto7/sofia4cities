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
package com.indracompany.sofia2.controlpanel.services.simulation;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.indracompany.sofia2.config.model.DeviceSimulation;

public interface SimulationService {

	void createSimulation(String identification, int interval, String userId, String json) throws JsonProcessingException, IOException;

	String getDeviceSimulationJson(String identification, String clientPlatform, String token, String ontology, String jsonMap)
			throws JsonProcessingException, IOException;

	void scheduleSimulation(DeviceSimulation deviceSimulation);

	void unscheduleSimulation(DeviceSimulation deviceSimulation);
	
	void updateSimulation(String identification, int interval, String json, DeviceSimulation simulation) throws JsonProcessingException, IOException;
}
