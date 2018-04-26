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
/*******************************************************************************
 * © Indra Sistemas, S.A.
 * 2013 - 2018  SPAIN
 *
 * All rights reserved
 ******************************************************************************/

package com.indracompany.sofia2.config.repository;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.indracompany.sofia2.commons.testing.IntegrationTest;
import com.indracompany.sofia2.config.model.OntologyUserAccess;
import com.indracompany.sofia2.config.model.User;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
public class OntologyUserAccessRepositoryIntegrationTest {

	@Autowired
	OntologyUserAccessTypeRepository ouatRep;
	@Autowired
	OntologyUserAccessRepository repository;
	@Autowired
	OntologyRepository ontRep;

	@Autowired
	UserRepository userRepository;

	private User getUserDeveloper() {
		return this.userRepository.findByUserId("developer");
	}

	@Before
	public void setUp() {
		List<OntologyUserAccess> users = this.repository.findAll();
		if (users.isEmpty()) {
			log.info("No OntologyUserAccess found...adding");
			OntologyUserAccess user = new OntologyUserAccess();
			user.setUser(getUserDeveloper());
			user.setOntology(ontRep.findAll().get(0));
			user.setOntologyUserAccessType(ouatRep.findAll().get(0));
			this.repository.save(user);
		}
	}

	@Test
	public void given_SomeOntologyUsersAccessExist_When_ItIsSearchedById_Then_TheCorrectObjectIsObtained() {
		OntologyUserAccess user = this.repository.findAll().get(0);
		Assert.assertTrue(this.repository.findByUser(user.getUser()).size() > 0);
	}
}
