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
package com.indracompany.sofia2.controlpanel.controller.graph;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indracompany.sofia2.config.model.ClientPlatform;
import com.indracompany.sofia2.config.model.ClientPlatformOntology;
import com.indracompany.sofia2.config.model.Ontology;
import com.indracompany.sofia2.config.model.Role;
import com.indracompany.sofia2.config.model.User;
import com.indracompany.sofia2.config.repository.ClientPlatformRepository;
import com.indracompany.sofia2.config.repository.DashboardRepository;
import com.indracompany.sofia2.config.repository.GadgetRepository;
import com.indracompany.sofia2.config.repository.OntologyRepository;
import com.indracompany.sofia2.config.services.user.UserService;
import com.indracompany.sofia2.controlpanel.utils.AppWebUtils;

@Component
public class GraphUtil {

	private String urlClientPlatform;
	private String urlDashboard;
	private String urlGadget;
	private String urlOntology;
	private final String genericUserName = "USER";
	private User user;
	@Autowired
	private OntologyRepository ontologyRepository;
	@Autowired
	private ClientPlatformRepository clientPlatformRepository;
	@Autowired
	private GadgetRepository gadgetRepository;
	@Autowired
	private DashboardRepository dashboardRepository;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private UserService userService;
	@Value("${sofia2.urls.iotbroker}")
	private String url;

	@PostConstruct
	public void init() {
		// initialize URLS
		this.urlClientPlatform = this.url + "/controlpanel/devices/show/";
		this.urlGadget = this.url + "/controlpanel/gadgets/";
		this.urlDashboard = this.url + "/controlpanel/dashboards/";
		this.urlOntology = this.url + "/controlpanel/ontologies/";

	}

	public List<GraphDTO> constructGraphWithOntologies() {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.ontologies", "ONTOLOGIES");
		final String description = utils.getMessage("tooltip_ontologies", null);
		// carga de nodo ontologia con link a crear y con titulo
		arrayLinks.add(new GraphDTO(genericUserName, name, null, urlOntology + "list", genericUserName, name,
				utils.getUserId(), name, "suit", description, urlOntology + "create"));
		List<Ontology> ontologies;
		if (utils.getRole().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			ontologies = ontologyRepository.findAll();
		} else {
			ontologies = ontologyRepository
					.findByUserAndOntologyUserAccessAndAllPermissions(this.userService.getUser(utils.getUserId()));
		}
		for (final Ontology ont : ontologies) {
			arrayLinks.add(new GraphDTO(name, ont.getId(), urlOntology + "list", urlOntology + "show/" + ont.getId(),
					name, "ontology", name, ont.getIdentification(), "licensing"));
		}
		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithClientPlatforms() {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name.clients", "PLATFORM CLIENTS");
		final String description = utils.getMessage("tooltip_clients", null);

		// carga de nodo clientPlatform
		arrayLinks.add(new GraphDTO(genericUserName, name, null, urlClientPlatform + "list", genericUserName, name,
				utils.getUserId(), name, "suit", description, urlClientPlatform + "create"));

		final List<ClientPlatform> clientPlatforms = clientPlatformRepository
				.findByUser(this.userService.getUser(utils.getUserId()));

		for (final ClientPlatform clientPlatform : clientPlatforms) {
			// Creación de enlaces
			arrayLinks.add(new GraphDTO(name, clientPlatform.getId(), urlClientPlatform + "list",
					urlClientPlatform + clientPlatform.getIdentification(), name, "clientplatform", name,
					clientPlatform.getIdentification(), "licensing"));

			if (clientPlatform.getClientPlatformOntologies() != null) {
				final List<ClientPlatformOntology> clientPlatformOntologies = new LinkedList<>(
						clientPlatform.getClientPlatformOntologies());
				for (final ClientPlatformOntology clientPlatformOntology : clientPlatformOntologies) {
					final Ontology ontology = clientPlatformOntology.getOntology();
					// Crea link entre ontologia y clientPlatform
					arrayLinks.add(new GraphDTO(ontology.getId(), clientPlatform.getId(),
							urlOntology + ontology.getId(), urlClientPlatform + clientPlatform.getIdentification(),
							"ontology", "clientplatform", ontology.getIdentification(),
							clientPlatform.getIdentification(), "licensing"));
				}
			}
		}
		return arrayLinks;
	}

	private List<GraphDTO> constructGraphWithGadgets(String visualizationId, String visualizationName) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		/*
		 * String name = utils.getMessage("name.gadgets", "GADGETS");
		 * 
		 * // carga de nodo gadget dependiente de visualizacion arrayLinks.add(new
		 * GraphDTO(visualizationId, name, null, urlGadget + "list", visualizationId,
		 * name, visualizationName, name, "suit", null, urlGadget + "selectWizard"));
		 * 
		 * List<Gadget> gadgets =
		 * gadgetRepository.findByUser(this.userService.getUser(utils.getUserId()));
		 * 
		 * if (gadgets != null) { for (Gadget gadget : gadgets) { // Creación de enlaces
		 * arrayLinks.add(new GraphDTO(name, gadget.getId(), urlGadget + "list",
		 * urlDashboard + gadget.getId(), name, "gadget", name,
		 * gadget.getIdentification(), "licensing"));
		 * 
		 * } gadgets.clear(); }
		 */
		return arrayLinks;
	}

	private List<GraphDTO> constructGraphWithDashboard(String visualizationId, String visualizationName) {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		/*
		 * String name = utils.getMessage("name.dashboards", "DASHBOARDS");
		 * 
		 * arrayLinks.add(new GraphDTO(visualizationId, name, null, urlDashboard +
		 * "list", visualizationId, name, visualizationName, name, "suit", null,
		 * urlDashboard + "creategroup?"));
		 * 
		 * // dashboardTipo---> son los dashboard List<DashboardType> dashboardTypes =
		 * dashboardTypeRepository
		 * .findByUser(this.userService.getUser(utils.getUserId())); for (DashboardType
		 * dashboardType : dashboardTypes) { // Ahora hay que buscar la relacion entre
		 * dashboard y gadget. Eso nos lo da el // dashboard List<Dashboard> dashboards
		 * = dashboardRepository.findByDashboardType(dashboardType); arrayLinks.add(new
		 * GraphDTO(name, Integer.toString(dashboardType.getId()), urlDashboard +
		 * "list", urlDashboard + Integer.toString(dashboardType.getId()), name,
		 * "dashboard", null, dashboardType.getType(), "licensing"));
		 * 
		 * for (Dashboard dashboard : dashboards) { try { List<String> gadgetIds =
		 * this.getGadgetIdsFromModel(dashboard.getModel()); for (String gadget :
		 * gadgetIds) { arrayLinks.add(new GraphDTO(gadget,
		 * Integer.toString(dashboardType.getId()), urlDashboard + gadget, urlDashboard
		 * + dashboardType.getId(), "gadget", "dashboard", null,
		 * dashboardType.getType(), "licensing")); } } catch (Exception e) {
		 * 
		 * } } dashboards.clear(); }
		 */

		return arrayLinks;
	}

	public List<GraphDTO> constructGraphWithVisualization() {

		final List<GraphDTO> arrayLinks = new LinkedList<>();
		final String name = utils.getMessage("name_visualization", "VISUALIZATION");
		final String description = utils.getMessage("tooltip_visualization", null);
		// carga de nodo gadget
		arrayLinks.add(new GraphDTO(genericUserName, name, null, null, genericUserName, name, utils.getUserId(), name,
				"suit", description, null));

		arrayLinks.addAll(constructGraphWithGadgets(name, name));

		arrayLinks.addAll(constructGraphWithDashboard(name, name));

		return arrayLinks;
	}

	public List<String> getGadgetIdsFromModel(String modelJson) throws JsonProcessingException, IOException {
		final List<String> gadgetIds = new LinkedList<>();
		final ObjectMapper objectMapper = new ObjectMapper();
		final JsonNode jsonNode = objectMapper.readTree(modelJson);
		final int rows = jsonNode.path("rows").size();
		for (int i = 0; i < rows; i++) {
			final int columns = jsonNode.path("rows").path(i).path("columns").size();
			for (int j = 0; j < columns; j++) {
				final int widgets = jsonNode.path("rows").path(i).path("columns").path(j).path("widgets").size();
				for (int k = 0; k < widgets; k++) {
					String gadgetId = jsonNode.path("rows").path(i).path("columns").path(j).path("widgets").path(k)
							.path("config").get("gadgetId").asText();
					gadgetId = gadgetId.split("_")[0];
					gadgetIds.add(gadgetId);
				}
			}

		}
		return gadgetIds;

	}

}