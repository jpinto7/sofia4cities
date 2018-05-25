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
package com.indracompany.sofia2.config.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.indracompany.sofia2.config.model.Ontology;
import com.indracompany.sofia2.config.model.OntologyUserAccess;
import com.indracompany.sofia2.config.model.OntologyUserAccessType;
import com.indracompany.sofia2.config.model.User;

public interface OntologyUserAccessRepository extends JpaRepository<OntologyUserAccess, String> {

	OntologyUserAccess findByOntologyAndUser(Ontology ontology, User user);

	List<OntologyUserAccess> findByUser(User user);

	List<OntologyUserAccess> findByUserAndOntologyUserAccessType(User user,
			OntologyUserAccessType ontologyUserAccessType);

	OntologyUserAccess findById(String id);

	List<OntologyUserAccess> findByOntologyUserAccessType(OntologyUserAccessType ontologyUserAccessType);

	List<OntologyUserAccess> findByOntology(Ontology ontology);

	@Modifying
	@Transactional
	void deleteByOntology(Ontology ontology);

	

}
