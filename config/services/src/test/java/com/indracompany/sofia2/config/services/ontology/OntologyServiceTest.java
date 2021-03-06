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
package com.indracompany.sofia2.config.services.ontology;

import static org.mockito.Mockito.*;

import java.util.ArrayList;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.indracompany.sofia2.config.model.Ontology;
import com.indracompany.sofia2.config.model.OntologyUserAccess;
import com.indracompany.sofia2.config.model.Role;
import com.indracompany.sofia2.config.model.User;
import com.indracompany.sofia2.config.repository.OntologyRepository;
import com.indracompany.sofia2.config.repository.OntologyUserAccessRepository;

@RunWith(MockitoJUnitRunner.class)
public class OntologyServiceTest {

	@Mock
	private OntologyRepository ontologyRepository;
	@Mock
	private OntologyUserAccessRepository ontologyUserAccessRepository;
	
	@InjectMocks
	OntologyServiceImpl service;
	
	@Test
	public void given_OneOntologyWithNullUserAccesses_When_IsRequestedIfItHasAnyUserAccess_Then_FalseIsReturned() {
		String id = "1";
		Ontology ontology = new Ontology();
		ontology.setId(id);
		when(ontologyRepository.findById(id)).thenReturn(ontology);
		when(ontologyUserAccessRepository.findByOntology(ontology)).thenReturn(null);
		assertFalse(service.hasOntologyUsersAuthorized("1"));
	}
	
	@Test
	public void given_OneOntologyWithEmptyListOfUserAccesses_When_IsRequestedIfItHasAnyUserAccess_Then_FalseIsReturned() {
		String id = "1";
		Ontology ontology = new Ontology();
		ontology.setId(id);
		when(ontologyRepository.findById(id)).thenReturn(ontology);
		when(ontologyUserAccessRepository.findByOntology(ontology)).thenReturn(new ArrayList<OntologyUserAccess>(1));
		assertFalse(service.hasOntologyUsersAuthorized("1"));
	}
	
	@Test
	public void given_OneOntologyWithOneUserAccesses_When_IsRequestedIfItHasAnyUserAccess_Then_TrueIsReturned() {
		String id = "1";
		Ontology ontology = new Ontology();
		ontology.setId(id);
		OntologyUserAccess ontologyUserAccess = new OntologyUserAccess();
		ontologyUserAccess.setId("1");
		ArrayList<OntologyUserAccess> authorizies = new ArrayList<OntologyUserAccess>(1);
		authorizies.add(ontologyUserAccess);
		when(ontologyRepository.findById(id)).thenReturn(ontology);
		when(ontologyUserAccessRepository.findByOntology(ontology)).thenReturn(authorizies);
		assertTrue(service.hasOntologyUsersAuthorized("1"));
	}
	
	@Test
	public void given_OneOntologyIsPublic_When_AnyUserAsksForQueryAccess_Then_TrueItIsReturned() {
		String id = "1";
		User ontologyUser = createUser("owner", "normal");	
		Ontology ontology = new Ontology();
		ontology.setId(id);
		ontology.setPublic(true);
		ontology.setUser(ontologyUser);
		
		User sessionUser = createUser("any", "any");
		
		assertTrue("Any user should have query access to a public ontology", service.hasUserPermissionForQuery(sessionUser, ontology));
	}
	
	private User createUser(String userId, String roleId) {
		Role role = new Role();
		role.setId(roleId);
		
		User user = new User();
		user.setUserId(userId);
		user.setRole(role);
		
		return user;
	}
	
}
