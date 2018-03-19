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
package com.indracompany.sofia2.controlpanel.controller.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.indracompany.sofia2.config.model.DeviceSimulation;
import com.indracompany.sofia2.config.model.Role;
import com.indracompany.sofia2.config.services.deletion.EntityDeletionService;
import com.indracompany.sofia2.config.services.ontology.OntologyService;
import com.indracompany.sofia2.config.services.simulation.DeviceSimulationService;
import com.indracompany.sofia2.controlpanel.services.simulation.SimulationService;
import com.indracompany.sofia2.controlpanel.utils.AppWebUtils;

@Controller
@RequestMapping("devicesimulation")
public class DeviceSimulatorController {

	@Autowired
	private DeviceSimulationService deviceSimulationService;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private SimulationService simulationService;
	@Autowired
	private EntityDeletionService entityDeletionService;

	@GetMapping("list")
	public String List(Model model) {

		List<DeviceSimulation> simulations = new ArrayList<DeviceSimulation>();
		if (this.utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			simulations = this.deviceSimulationService.getAllSimulations();
		else
			simulations = this.deviceSimulationService.getSimulationsForUser(this.utils.getUserId());

		model.addAttribute("simulations", simulations);
		return "simulator/list";
	}

	@GetMapping("create")
	public String createForm(Model model) {
		List<String> clients = this.deviceSimulationService.getClientsForUser(this.utils.getUserId());
		List<String> simulators = this.deviceSimulationService.getSimulatorTypes();
		model.addAttribute("platformClients", clients);
		model.addAttribute("simulators", simulators);
		model.addAttribute("simulation", new DeviceSimulation());
		return "simulator/create";
	}

	@GetMapping("update/{id}")
	public String updateForm(Model model, @PathVariable("id") String id) {
		List<String> clients = this.deviceSimulationService.getClientsForUser(this.utils.getUserId());
		List<String> simulators = this.deviceSimulationService.getSimulatorTypes();
		DeviceSimulation simulation = this.deviceSimulationService.getSimulationById(id);
		model.addAttribute("platformClient", simulation.getClientPlatform());
		model.addAttribute("ontology", simulation.getOntology());
		model.addAttribute("token", simulation.getToken());
		model.addAttribute("platformClients", clients);
		model.addAttribute("simulators", simulators);
		model.addAttribute("simulation", simulation);
		model.addAttribute("ontologies", this.deviceSimulationService
				.getClientOntologiesIdentification(simulation.getClientPlatform().getIdentification()));
		model.addAttribute("tokens", this.deviceSimulationService
				.getClientTokensIdentification(simulation.getClientPlatform().getIdentification()));
		return "simulator/create";
	}

	@PostMapping("create")
	public String create(Model model, @RequestParam String identification, @RequestParam String jsonMap,
			@RequestParam String ontology, @RequestParam String clientPlatform, @RequestParam String token,
			@RequestParam int interval) throws JsonProcessingException, IOException {

		this.simulationService.createSimulation(identification, interval, utils.getUserId(),
				this.simulationService.getDeviceSimulationJson(identification, clientPlatform, token, ontology, jsonMap));
		return "redirect:/devicesimulation/list";
	}

	@PostMapping("ontologiesandtokens")
	public String getOntologiesAndTokens(Model model, @RequestParam String clientPlatformId) {

		model.addAttribute("ontologies",
				this.deviceSimulationService.getClientOntologiesIdentification(clientPlatformId));
		model.addAttribute("tokens", this.deviceSimulationService.getClientTokensIdentification(clientPlatformId));

		return "simulator/create :: ontologiesAndTokens";
	}

	@PostMapping("ontologyfields")
	public String getOntologyfields(Model model, @RequestParam String ontologyIdentification)
			throws JsonProcessingException, IOException {

		model.addAttribute("fields",
				this.ontologyService.getOntologyFields(ontologyIdentification, this.utils.getUserId()));
		model.addAttribute("simulators", this.deviceSimulationService.getSimulatorTypes());
		return "simulator/create :: ontologyFields";
	}

	@PostMapping("startstop")
	public String startStop(Model model, @RequestParam String id) {
		DeviceSimulation simulation = this.deviceSimulationService.getSimulationById(id);
		List<DeviceSimulation> simulations = new ArrayList<DeviceSimulation>();
		if (simulation != null) {
			if (simulation.isActive())
				this.simulationService.unscheduleSimulation(simulation);
			else
				this.simulationService.scheduleSimulation(simulation);
		}
		if (this.utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.name()))
			simulations = this.deviceSimulationService.getAllSimulations();
		else
			simulations = this.deviceSimulationService.getSimulationsForUser(this.utils.getUserId());
		model.addAttribute("simulations", simulations);
		return "simulator/list :: simulations";

	}

	@PutMapping("update/{id}")
	public String update(Model model, @PathVariable("id") String id, @RequestParam String identification,
			@RequestParam String jsonMap, @RequestParam String ontology, @RequestParam String clientPlatform,
			@RequestParam String token, @RequestParam int interval, RedirectAttributes redirect)
			throws JsonProcessingException, IOException {

		DeviceSimulation simulation = this.deviceSimulationService.getSimulationById(id);
		if (simulation != null) {
			if (!simulation.isActive()) {
				this.simulationService.updateSimulation(identification, interval,
						this.simulationService.getDeviceSimulationJson(identification, clientPlatform, token, ontology, jsonMap),
						simulation);
				return "redirect:/devicesimulation/list";
			} else {
				this.utils.addRedirectMessage("simulation.update.isactive", redirect);
				return "redirect:/devicesimulation/update/" + id;
			}

		} else {
			this.utils.addRedirectMessage("simulation.update.error", redirect);
			return "redirect:/devicesimulation/update/" + id;
		}

	}

	@DeleteMapping("{id}")
	public @ResponseBody String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		DeviceSimulation simulation = this.deviceSimulationService.getSimulationById(id);
		if (simulation != null) {
			try {
				this.entityDeletionService.deleteDeviceSimulation(simulation);
			} catch (Exception e) {
				this.utils.addRedirectException(e, redirect);
				return "error";
			}
			return "ok";
		} else {
			this.utils.addRedirectMessage("simulation.exists.false", redirect);
			return "error";
		}
	}
}
