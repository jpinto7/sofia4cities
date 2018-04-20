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
package com.indracompany.sofia2.persistence.elasticsearch;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.indracompany.sofia2.persistence.elasticsearch.api.ESBaseApi;
import com.indracompany.sofia2.persistence.elasticsearch.api.ESDeleteService;
import com.indracompany.sofia2.persistence.exceptions.DBPersistenceException;
import com.indracompany.sofia2.persistence.interfaces.ManageDBRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component("ElasticSearchManageDBRepository")
@Scope("prototype")
@Lazy
@Slf4j
public class ElasticSearchManageDBRepository implements ManageDBRepository {
	
	private static final String NOT_IMPLEMENTED_ALREADY = "Not Implemented Already";

	@Autowired
	ESBaseApi connector;
	
	@Autowired
	private ESDeleteService eSDeleteService;
	
	
	@Override
	public Map<String, Boolean> getStatusDatabase() throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public String createTable4Ontology(String ontology, String schema) throws DBPersistenceException {
		ontology=ontology.toLowerCase();
		try {
			String res =   connector.createIndex(ontology);
			log.info("Index result :  "+res);
		} catch (Exception e) {
			log.info("Resource already exists ");
		}
		
		if (schema.equals("")) schema="{}";
		if (schema.equals("{}")) {
			log.info("No schema is declared");
		}
		
		else {
			connector.createType(ontology, ontology, schema);
		}
		return ontology;
	}

	@Override
	public List<String> getListOfTables() throws DBPersistenceException {
		
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public List<String> getListOfTables4Ontology(String ontology) throws DBPersistenceException {
		ontology=ontology.toLowerCase();
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public void removeTable4Ontology(String ontology) throws DBPersistenceException {
		ontology=ontology.toLowerCase();
		eSDeleteService.deleteAll(ontology, ontology);

	}

	@Override
	public void createIndex(String ontology, String attribute) throws DBPersistenceException {
		ontology=ontology.toLowerCase();
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

	@Override
	public void createIndex(String ontology, String nameIndex, String attribute) throws DBPersistenceException {
		ontology=ontology.toLowerCase();
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

	@Override
	public void createIndex(String sentence) throws DBPersistenceException {
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public void dropIndex(String ontology, String indexName) throws DBPersistenceException {
		ontology=ontology.toLowerCase();
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

	@Override
	public List<String> getListIndexes(String ontology) throws DBPersistenceException {
		ontology=ontology.toLowerCase();
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public String getIndexes(String ontology) throws DBPersistenceException {
		ontology=ontology.toLowerCase();
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);
	}

	@Override
	public void validateIndexes(String ontology, String schema) throws DBPersistenceException {
		ontology=ontology.toLowerCase();
		throw new DBPersistenceException(NOT_IMPLEMENTED_ALREADY);

	}

}
