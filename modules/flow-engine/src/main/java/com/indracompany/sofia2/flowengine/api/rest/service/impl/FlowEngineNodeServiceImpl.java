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
package com.indracompany.sofia2.flowengine.api.rest.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indracompany.sofia2.config.model.ClientPlatform;
import com.indracompany.sofia2.config.model.Flow;
import com.indracompany.sofia2.config.model.FlowDomain;
import com.indracompany.sofia2.config.model.FlowNode;
import com.indracompany.sofia2.config.model.FlowNode.MessageType;
import com.indracompany.sofia2.config.model.FlowNode.Type;
import com.indracompany.sofia2.config.model.Ontology;
import com.indracompany.sofia2.config.model.User;
import com.indracompany.sofia2.config.services.client.ClientPlatformService;
import com.indracompany.sofia2.config.services.flow.FlowService;
import com.indracompany.sofia2.config.services.flowdomain.FlowDomainService;
import com.indracompany.sofia2.config.services.flownode.FlowNodeService;
import com.indracompany.sofia2.config.services.ontology.OntologyService;
import com.indracompany.sofia2.flowengine.api.rest.pojo.DecodedAuthentication;
import com.indracompany.sofia2.flowengine.api.rest.pojo.DeployRequestRecord;
import com.indracompany.sofia2.flowengine.api.rest.pojo.UserDomainValidationRequest;
import com.indracompany.sofia2.flowengine.api.rest.service.FlowEngineNodeService;
import com.indracompany.sofia2.flowengine.api.rest.service.FlowEngineValidationNodeService;
import com.indracompany.sofia2.flowengine.audit.aop.FlowEngineAuditable;
import com.indracompany.sofia2.flowengine.exception.NodeRedAdminServiceException;
import com.indracompany.sofia2.flowengine.exception.NotAllowedException;
import com.indracompany.sofia2.flowengine.exception.NotAuthorizedException;
import com.indracompany.sofia2.flowengine.exception.ResourceNotFoundException;
import com.indracompany.sofia2.persistence.exceptions.DBPersistenceException;
import com.indracompany.sofia2.router.service.app.model.NotificationModel;
import com.indracompany.sofia2.router.service.app.model.OperationModel;
import com.indracompany.sofia2.router.service.app.model.OperationModel.OperationType;
import com.indracompany.sofia2.router.service.app.model.OperationModel.QueryType;
import com.indracompany.sofia2.router.service.app.model.OperationResultModel;
import com.indracompany.sofia2.router.service.app.service.RouterService;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FlowEngineNodeServiceImpl implements FlowEngineNodeService {

	/*
	 * @Autowired
	 * 
	 * @Qualifier("MongoBasicOpsDBRepository") private BasicOpsDBRepository
	 * basicRDBRepository;
	 * 
	 * @Autowired private QueryToolService queryToolService;
	 */

	@Autowired(required = false)
	private RouterService routerService;

	@Autowired
	private FlowDomainService domainService;

	@Autowired
	private FlowService flowService;

	@Autowired
	private FlowNodeService nodeService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private ClientPlatformService clientPlatformService;

	@Autowired
	private FlowEngineValidationNodeService flowEngineValidationNodeService;

	@Override
	public String deploymentNotification(String json) {
		// TODO CHECH EXCEPTION HANDLING AND 500 SERVER ERROR HANDLING ON CLIENT
		final ObjectMapper mapper = new ObjectMapper();
		FlowDomain domain = null;
		List<DeployRequestRecord> deployRecords = new ArrayList<>();

		try {
			deployRecords = mapper.readValue(json, new TypeReference<List<DeployRequestRecord>>() {
			});
			for (final DeployRequestRecord record : deployRecords) {
				if (record != null) {
					if (record.getDomain() != null) {

						log.info("Deployment info from domain = {}", record.getDomain());
						domain = domainService.getFlowDomainByIdentification(record.getDomain());
						domainService.deleteFlowDomainFlows(record.getDomain());
					} else {
						log.debug("Deployment record = {}", record.toString());
						if (record.getType() != null) {
							if (record.getType().equals("tab")) {
								// it is a FLOW
								final Flow newFlow = new Flow();
								newFlow.setIdentification(record.getLabel());
								newFlow.setNodeRedFlowId(record.getId());
								newFlow.setActive(true);
								newFlow.setFlowDomain(domain);
								flowService.createFlow(newFlow);
							} else {
								// It is a node
								if (record.getType().equals(Type.HTTP_NOTIFIER.getName())) {
									final FlowNode node = new FlowNode();
									final Flow flow = flowService.getFlowByNodeRedFlowId(record.getZ());
									node.setIdentification(record.getName());
									node.setNodeRedNodeId(record.getId());
									node.setFlow(flow);
									node.setFlowNodeType(Type.HTTP_NOTIFIER);
									node.setMessageType(MessageType.valueOf(record.getMeassageType()));
									node.setOntology(ontologyService.getOntologyByIdentification(record.getOntology(),
											domain.getUser().getUserId()));
									node.setPartialUrl(record.getUrl());
									nodeService.createFlowNode(node);
								}
							}
						} else {
							log.warn("Undefined type for NodeRed element. Record will be skipped : {}",
									record.toString());
						}
					}
				}
			}
		} catch (final IOException e) {
			// TODO Change return statement
			log.error("Unable to save deployment info from NodeRed into CDB. Cause = {}, message = {}", e.getCause(),
					e.getMessage());
			return "NOTOK";
		}
		return "ok";
	}

	@Override
	public List<String> getOntologyByUser(String authentication)
			throws ResourceNotFoundException, NotAuthorizedException {

		final List<String> response = new ArrayList<>();
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		final User sofia2User = flowEngineValidationNodeService.validateUserCredentials(decodedAuth.getUserId(),
				decodedAuth.getPassword());

		List<Ontology> ontologies = null;
		switch (sofia2User.getRole().getId()) {
		case "ROLE_ADMINISTRATOR":
			ontologies = ontologyService.getAllOntologies(sofia2User.getUserId());
			break;
		default:
			// TODO check default criteria. Public ontologies should be included
			ontologies = ontologyService.getOntologiesByUserId(sofia2User.getUserId());
			break;
		}
		for (final Ontology ontology : ontologies) {
			response.add(ontology.getIdentification());
		}
		Collections.sort(response);
		return response;
	}

	@Override
	public List<String> getClientPlatformByUser(String authentication)
			throws ResourceNotFoundException, NotAuthorizedException {

		final List<String> response = new ArrayList<>();

		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		final User sofia2User = flowEngineValidationNodeService.validateUserCredentials(decodedAuth.getUserId(),
				decodedAuth.getPassword());

		List<ClientPlatform> clientPlatforms = null;
		switch (sofia2User.getRole().getId()) {
		case "ROLE_ADMINISTRATOR":
			clientPlatforms = clientPlatformService.getAllClientPlatforms();
			break;
		default:
			// TODO check default criteria
			clientPlatforms = clientPlatformService.getclientPlatformsByUser(sofia2User);
			break;
		}
		for (final ClientPlatform clientPlatform : clientPlatforms) {
			response.add(clientPlatform.getIdentification());
		}
		Collections.sort(response);
		return response;
	}

	@Override
	public String validateUserDomain(UserDomainValidationRequest request)
			throws ResourceNotFoundException, NotAuthorizedException, NotAllowedException, IllegalArgumentException {

		String response = null;
		final DecodedAuthentication decodedAuth = flowEngineValidationNodeService
				.decodeAuth(request.getAuthentication());
		final User sofia2User = flowEngineValidationNodeService.validateUserCredentials(decodedAuth.getUserId(),
				decodedAuth.getPassword());

		if (request.getDomainId() == null) {
			throw new IllegalArgumentException("DomainId must be specified.");
		}

		final FlowDomain domain = domainService.getFlowDomainByIdentification(request.getDomainId());

		if (domain == null) {
			throw new ResourceNotFoundException(
					"Domain with identification " + request.getDomainId() + " could not be found.");
		}

		switch (sofia2User.getRole().getName()) {
		case "ROLE_ADMINISTRATOR":
			response = "OK"; // Has permission over all domains
			break;
		default:
			if (!domain.getUser().getUserId().equals(sofia2User.getUserId())) {
				throw new NotAllowedException("User " + decodedAuth.getUserId()
						+ " has no permissions over specified domain " + request.getDomainId());
			}
			response = "OK";
			break;
		}
		return response;
	}

	@Override
	@FlowEngineAuditable
	public String submitQuery(String ontology, String queryType, String query, String authentication)
			throws ResourceNotFoundException, NotAuthorizedException, NotFoundException, JsonProcessingException,
			DBPersistenceException {

		DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		User sofia2User = flowEngineValidationNodeService.validateUserCredentials(decodedAuth.getUserId(),
				decodedAuth.getPassword());

		QueryType type;

		if ("sql".equals(queryType.toLowerCase())) {
			type = QueryType.SQLLIKE;
		} else if ("native".equals(queryType)) {
			type = QueryType.NATIVE;
		} else {
			log.error("Invalid value {} for queryType. Possible values are: SQL, NATIVE.", queryType);
			throw new IllegalArgumentException(
					"Invalid value " + queryType + " for queryType. Possible values are: SQL, NATIVE.");
		}

		OperationModel operationModel = OperationModel
				.builder(ontology, OperationType.QUERY, sofia2User.getUserId(), OperationModel.Source.FLOWENGINE)
				.body(query).queryType(type).build();

		OperationResultModel result = null;
		try {
			NotificationModel notificationModel = new NotificationModel();
			notificationModel.setOperationModel(operationModel);
			result = routerService.query(notificationModel);
		} catch (Exception e) {

			log.error("Error executing query. Ontology={}, QueryType ={}, Query = {}. Cause = {}, Message = {}.",
					ontology, queryType, query, e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException(
					"Error executing query. Ontology=" + ontology + ", QueryType =" + queryType + ", Query = " + query
							+ ". Cause = " + e.getCause() + ", Message = " + e.getMessage() + ".");
		}
		// TODO: Check response state
		return result.getResult();
	}

	@Override
	@FlowEngineAuditable
	public String submitInsert(String ontology, String data, String authentication)
			throws ResourceNotFoundException, NotAuthorizedException, JsonProcessingException, NotFoundException {

		DecodedAuthentication decodedAuth = flowEngineValidationNodeService.decodeAuth(authentication);
		User sofia2User = flowEngineValidationNodeService.validateUserCredentials(decodedAuth.getUserId(),
				decodedAuth.getPassword());

		OperationModel operationModel = OperationModel
				.builder(ontology, OperationType.INSERT, sofia2User.getUserId(), OperationModel.Source.FLOWENGINE)
				.body(data).build();

		OperationResultModel result = null;

		try {
			NotificationModel notificationModel = new NotificationModel();
			notificationModel.setOperationModel(operationModel);
			result = routerService.insert(notificationModel);
		} catch (Exception e) {
			log.error("Error inserting data. Ontology={}, Data = {}. Cause = {}, Message = {}.", ontology, data,
					e.getCause(), e.getMessage());
			throw new NodeRedAdminServiceException("Error executing query. Ontology=" + ontology + ", Data = " + data
					+ ". Cause = " + e.getCause() + ", Message = " + e.getMessage() + ".");
		}
		return result.getResult();
	}
}
