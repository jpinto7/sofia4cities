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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
		return "/ontologies/list";
	}

	@PostMapping("/getNamesForAutocomplete")
	public @ResponseBody List<String> getNamesForAutocomplete() {
		return this.ontologyService.getAllIdentifications();
	}

	@GetMapping(value = "/create")
	public String create(Model model) {
		model.addAttribute("ontology", new Ontology());
		this.populateForm(model);
		return "/ontologies/create";
	}

	
	@GetMapping(value = "/createwizard", produces = "text/html")
	public String createWizard(Model model) {
		model.addAttribute("ontology", new Ontology());
		this.populateForm(model);
		return "/ontologies/createwizard";
	}
	
	@PostMapping(value = {"/create","/createwizard"})
	public String createOntology(Model model,
			@Valid Ontology ontology, BindingResult bindingResult,
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
			if (!this.utils.getUserId().equals(ontology.getUser().getUserId()) && !utils.isAdministrator())
				return "/error/403";
			model.addAttribute("ontology", ontology);
			this.populateForm(model);
			return "/ontologies/createwizard";
		} else
			return "/ontologies/create";

	}

	@PutMapping(value = "/update/{id}", produces = "text/html")
	public String updateOntology(Model model, @PathVariable("id") String id, @Valid Ontology ontology,
			BindingResult bindingResult, RedirectAttributes redirect) {

		if (bindingResult.hasErrors()) {
			log.debug("Some ontology properties missing");
			utils.addRedirectMessage("ontology.validation.error", redirect);
			return "redirect:/ontologies/update/" + id;
		}
		if (!this.ontologyService.hasUserPermissionForInsert(this.utils.getUserId(), ontology.getIdentification())
				&& !utils.isAdministrator())
			return "/error/403";
		try {
			ontology.setUser(this.userService.getUser(this.utils.getUserId()));
			this.ontologyService.updateOntology(ontology);
		} catch (OntologyServiceException e) {
			log.debug("Cannot update ontology");
			utils.addRedirectMessage("ontology.update.error", redirect);
			return "redirect:/ontologies/create";
		}

		utils.addRedirectMessage("ontology.update.success", redirect);
		return "redirect:/ontologies/show/" + id;
	}

	@DeleteMapping("/{id}")
	public String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {

		if (!this.ontologyService.getOntologyById(id).getUser().getUserId().equals(this.utils.getUserId())
				&& !this.utils.isAdministrator())
			return "/error/403";
		try {
			this.entityDeletionService.deleteOntology(id);
			// TODO ON DELETE CASCADE
		} catch (Exception e) {
			utils.addRedirectMessage("ontology.delete.error", redirect);
			return "redirect:/ontologies/list";
		}

		return "redirect:/ontologies/list";
	}

	@GetMapping("/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		Ontology ontology = this.ontologyService.getOntologyById(id);

		if(ontology != null) {
			if(ontology.getUser().getUserId().equals(this.utils.getUserId()) && !this.utils.isAdministrator()) {
				return "/error/403";
			}
			List<OntologyUserAccess> authorizations = this.ontologyService.getOntologyUserAccesses(ontology.getId());
			
			model.addAttribute("ontology",ontology);
			model.addAttribute("authorizations", authorizations);
			
			return "/ontologies/show";
		} else {
			this.utils.addRedirectMessage("ontology.notfound.error", redirect);
			return "redirect:/ontologies/list";
		}

	}

	private void populateForm(Model model) {
		model.addAttribute("dataModels", this.ontologyService.getAllDataModels());
		model.addAttribute("dataModelTypes", this.ontologyService.getAllDataModelTypes());
	}
	
	
	@PostMapping(value="/authorization")
	public String createAuthorization(
			Model model,
			@Valid OntologyUserAccess ontologyUserAccess,
			BindingResult bindingResult,
			RedirectAttributes redirect) {
		
		if(bindingResult.hasErrors()) {
			log.debug("Some ontologyUserAccess properties missing");
			utils.addRedirectMessage("ontology.validation.error", redirect);
			return "/ontology/list";
		}else {
			Ontology ontology = ontologyService.getOntologyById(ontologyUserAccess.getOntology().getId());
			if (ontology.getUser().getUserId().equals(this.utils.getUserId())) {
				ontologyService.createUserAccess(ontology, ontologyUserAccess);
			} else {
				return "/error/403";
			}
		}
		return "/ontology/authorizations/list";
	}

}
