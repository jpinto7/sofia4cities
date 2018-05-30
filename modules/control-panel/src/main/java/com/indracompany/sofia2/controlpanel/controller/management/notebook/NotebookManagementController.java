package com.indracompany.sofia2.controlpanel.controller.management.notebook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.indracompany.sofia2.config.services.notebook.NotebookService;
import com.indracompany.sofia2.config.services.oauth.JWTService;
import com.indracompany.sofia2.controlpanel.controller.management.NotebookOpsRestServices;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "Notebook Ops")
@RestController
public class NotebookManagementController extends NotebookOpsRestServices {

	@Autowired
	private JWTService jwtService;
	@Autowired
	private NotebookService notebookService;

	@ApiOperation(value = "Runs paragraph synchronously")
	@PostMapping(value = "/run/notebook/{notebookZepId}/paragraph/{paragraphId}")
	public ResponseEntity<?> runParagraph(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookZepId") String notebookZepId,
			@ApiParam(value = "Paragraph Id", required = true) @PathVariable(name = "paragraphId") String paragraphId,
			@RequestHeader("Authorization") String authorization) {

		String userId = this.jwtService.getAuthentication(authorization.split(" ")[1]).getName();
		boolean authorized = this.notebookService.hasUserPermissionForNotebook(notebookZepId, userId);

		if (authorized) {
			try {
				return this.notebookService.runParagraph(notebookZepId, paragraphId);
			} catch (Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Runs all paragraphs synchronously")
	@PostMapping(value = "/run/notebook/{notebookZepId}")
	public ResponseEntity<?> runAllParagraphs(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookZepId") String notebookZepId,
			@RequestHeader("Authorization") String authorization) {

		String userId = this.jwtService.getAuthentication(authorization.split(" ")[1]).getName();
		boolean authorized = this.notebookService.hasUserPermissionForNotebook(notebookZepId, userId);

		if (authorized) {
			try {
				return this.notebookService.runAllParagraphs(notebookZepId);
			} catch (Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}

	@ApiOperation(value = "Get the results of a paragraph")
	@GetMapping(value = "/result/notebook/{notebookZepId}/paragraph/{paragraphId}")
	public ResponseEntity<?> getParagraphResult(
			@ApiParam(value = "Notebook Zeppelin Id", required = true) @PathVariable("notebookZepId") String notebookZepId,
			@ApiParam(value = "Paragraph Id", required = true) @PathVariable(name = "paragraphId") String paragraphId,
			@RequestHeader("Authorization") String authorization) {

		String userId = this.jwtService.getAuthentication(authorization.split(" ")[1]).getName();
		boolean authorized = this.notebookService.hasUserPermissionForNotebook(notebookZepId, userId);

		if (authorized) {
			try {
				return this.notebookService.getParagraphResult(notebookZepId, paragraphId);
			} catch (Exception e) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

	}
}
