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
package com.indracompany.sofia2.controlpanel.controller.ontology;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.indracompany.sofia2.config.model.Ontology;
import com.indracompany.sofia2.config.model.OntologyUserAccess;
import com.indracompany.sofia2.config.model.User;
import com.indracompany.sofia2.config.services.deletion.EntityDeletionService;
import com.indracompany.sofia2.config.services.exceptions.OntologyServiceException;
import com.indracompany.sofia2.config.services.ontology.OntologyService;
import com.indracompany.sofia2.config.services.user.UserService;
import com.indracompany.sofia2.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/ontologies")
@Slf4j
public class OntologyController {

	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private EntityDeletionService entityDeletionService;
	@Autowired
	private UserService userService;

	@Autowired
	private AppWebUtils utils;

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {

		// Scaping "" string values for parameters
		if (identification != null) {
			if (identification.equals(""))
				identification = null;
		}
		if (description != null) {
			if (description.equals(""))
				description = null;
		}
		List<Ontology> ontologies = this.ontologyService
				.getOntologiesWithDescriptionAndIdentification(utils.getUserId(), identification, description);
		model.addAttribute("ontologies", ontologies);
		return "ontologies/list";
	}

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return this.ontologyService.getAllIdentifications();
	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		model.addAttribute("ontology", new Ontology());
		this.populateForm(model);
		return "ontologies/create";
	}

	@GetMapping(value = "/createwizard", produces = "text/html")
	public String createWizard(Model model, @Valid Ontology ontology, BindingResult bindingResult) {

		if (bindingResult.hasErrors())
			model.addAttribute("ontology", new Ontology());
		this.populateForm(model);
		return "ontologies/createwizard";
	}

	@PostMapping(value = { "/create", "/createwizard" })
	public String createOntology(Model model, @Valid Ontology ontology, BindingResult bindingResult,
			RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			log.debug("Some ontology properties missing");
			utils.addRedirectMessage("ontology.validation.error", redirect);
			return "redirect:/ontologies/create";
		}
		try {
			ontology.setUser(this.userService.getUser(this.utils.getUserId()));
			this.ontologyService.createOntology(ontology);

		} catch (OntologyServiceException e) {
			log.error("Cannot create ontology because of:" + e.getMessage());
			utils.addRedirectException(e, redirect);
			return "redirect:/ontologies/createwizard";
		}
		utils.addRedirectMessage("ontology.create.success", redirect);
		return "redirect:/ontologies/list";
	}

	@GetMapping(value = "/update/{id}", produces = "text/html")
	public String update(Model model, @PathVariable("id") String id) {
		Ontology ontology = this.ontologyService.getOntologyById(id);
		if (ontology != null) {
			if (isUserOwnerOrAdministrator(ontology.getUser().getUserId())) {
				List<OntologyUserAccess> authorizations = ontologyService.getOntologyUserAccesses(ontology.getId());
				List<OntologyUserAccessDTO> authorizationsDTO = new ArrayList<OntologyUserAccessDTO>();

				for (OntologyUserAccess authorization : authorizations) {
					authorizationsDTO.add(new OntologyUserAccessDTO(authorization));
				}

				List<User> users = userService.getAllUsers();

				model.addAttribute("authorizations", authorizationsDTO);
				model.addAttribute("ontology", ontology);
				model.addAttribute("users", users);

				this.populateForm(model);
				return "ontologies/createwizard";
			} else {
				return "error/403";
			}
		} else
			return "ontologies/create";

	}

	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateOntology(Model model, @PathVariable("id") String id, @Valid Ontology ontology,
			BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some ontology properties missing");
			utils.addRedirectMessage("ontology.validation.error", redirect);
			return "redirect:/ontologies/update/" + id;
		}
		
		Ontology ontologyDB = ontologyService.getOntologyById(id);
		
		if (isUserOwnerOrAdministrator(ontologyDB.getId())) {
			try {
				this.ontologyService.updateOntology(ontology);
			} catch (OntologyServiceException e) {
				log.debug("Cannot update ontology");
				utils.addRedirectMessage("ontology.update.error", redirect);
				return "redirect:/ontologies/create";
			}
			utils.addRedirectMessage("ontology.update.success", redirect);
			return "redirect:/ontologies/show/" + id;
		} else {
			return "error/403";
		}
	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		Ontology ontology = ontologyService.getOntologyById(id);
		if (ontology != null) {
			if (isUserOwnerOrAdministrator(ontology.getUser().getUserId())) {
				try {
					this.entityDeletionService.deleteOntology(id);
					// TODO ON DELETE CASCADE
				} catch (Exception e) {
					utils.addRedirectMessage("ontology.delete.error", redirect);
					return "redirect:/ontologies/list";
				}
	
				return "redirect:/ontologies/list";
			} else {
				return "error/403";
			}
		} else {
			return "redirect:/ontologies/list";
		}
	}

	@GetMapping("/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		Ontology ontology = ontologyService.getOntologyById(id);

		if (ontology != null) {

			if (isUserOwnerOrAdministrator(ontology.getUser().getUserId())) {
				List<OntologyUserAccess> authorizations = ontologyService.getOntologyUserAccesses(ontology.getId());
				List<OntologyUserAccessDTO> authorizationsDTO = new ArrayList<OntologyUserAccessDTO>();

				for (OntologyUserAccess authorization : authorizations) {
					authorizationsDTO.add(new OntologyUserAccessDTO(authorization));
				}

				List<User> users = userService.getAllUsers();

				model.addAttribute("ontology", ontology);
				model.addAttribute("authorizations", authorizationsDTO);
				model.addAttribute("users", users);

				return "ontologies/show";
			} else {
				return "error/403";
			}

		} else {
			utils.addRedirectMessage("ontology.notfound.error", redirect);
			return "redirect:/ontologies/list";
		}

	}

	private void populateForm(Model model) {
		model.addAttribute("dataModels", this.ontologyService.getAllDataModels());
		model.addAttribute("dataModelTypes", this.ontologyService.getAllDataModelTypes());
	}

	@PostMapping(value = "/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<OntologyUserAccessDTO> createAuthorization(@RequestParam String accesstype,
			@RequestParam String ontology, @RequestParam String user) {

		Ontology ontologyDB = ontologyService.getOntologyById(ontology);

		if (isUserOwnerOrAdministrator(ontologyDB.getUser().getUserId())) {
			ontologyService.createUserAccess(ontologyDB, user, accesstype);
			OntologyUserAccess ontologyUserAccessCreated = ontologyService
					.getOntologyUserAccessByOntologyIdAndUserId(ontologyDB.getId(), user);
			OntologyUserAccessDTO ontologyUserAccessDTO = new OntologyUserAccessDTO(ontologyUserAccessCreated);
			return new ResponseEntity<OntologyUserAccessDTO>(ontologyUserAccessDTO, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<OntologyUserAccessDTO>(HttpStatus.FORBIDDEN);
		}

	}

	@PostMapping(value = "/authorization/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> deleteAuthorization(@RequestParam String id) {

		OntologyUserAccess access = ontologyService.getOntologyUserAccessById(id);

		if (isUserOwnerOrAdministrator(access.getOntology().getUser().getUserId())) {
			ontologyService.deleteOntologyUserAccess(id);
			return new ResponseEntity<String>("{\"status\" : \"ok\"}", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
		}
	}

	@PostMapping(value = "/authorization/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<OntologyUserAccessDTO> updateAuthorization(@RequestParam String id,
			@RequestParam String accesstype) {

		OntologyUserAccess access = ontologyService.getOntologyUserAccessById(id);

		if (isUserOwnerOrAdministrator(access.getOntology().getUser().getUserId())) {
			ontologyService.updateOntologyUserAccess(id, accesstype);
			OntologyUserAccess ontologyUserAccessCreated = ontologyService.getOntologyUserAccessById(id);
			OntologyUserAccessDTO ontologyUserAccessDTO = new OntologyUserAccessDTO(ontologyUserAccessCreated);
			return new ResponseEntity<OntologyUserAccessDTO>(ontologyUserAccessDTO, HttpStatus.OK);
		} else {
			return new ResponseEntity<OntologyUserAccessDTO>(HttpStatus.FORBIDDEN);
		}
	}

	@GetMapping(value = "/authorization", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody ResponseEntity<List<OntologyUserAccessDTO>> getAuthorizations(@RequestParam("id") String id) {

		Ontology ontology = ontologyService.getOntologyById(id);
		if (isUserOwnerOrAdministrator(ontology.getUser().getUserId())) {
			List<OntologyUserAccess> authorizations = ontologyService.getOntologyUserAccesses(ontology.getId());
			List<OntologyUserAccessDTO> authorizationsDTO = new ArrayList<OntologyUserAccessDTO>();
			for (OntologyUserAccess authorization : authorizations) {
				authorizationsDTO.add(new OntologyUserAccessDTO(authorization));
			}
			return new ResponseEntity<List<OntologyUserAccessDTO>>(authorizationsDTO, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<OntologyUserAccessDTO>>(HttpStatus.FORBIDDEN);
		}
	}

	private boolean isUserOwnerOrAdministrator(String userId) {
		return userId.equals(utils.getUserId()) || utils.isAdministrator();
	}
}
