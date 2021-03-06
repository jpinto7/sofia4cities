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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.indracompany.sofia2.config.model.Dashboard;
import com.indracompany.sofia2.config.model.User;

public interface DashboardRepository extends JpaRepository<Dashboard, String> {

	Dashboard findById(String id);

	List<Dashboard> findByUser(User user);

	List<Dashboard> findByIdentification(String identification);

	List<Dashboard> findByDescription(String description);

	List<Dashboard> findByIdentificationContainingAndDescriptionContaining(String identification, String description);

	List<Dashboard> findByIdentificationContaining(String identification);

	List<Dashboard> findByDescriptionContaining(String description);

	List<Dashboard> findByUserAndIdentificationContainingAndDescriptionContaining(User user, String identification,
			String description);

	List<Dashboard> findByUserAndIdentificationContaining(User user, String identification);

	List<Dashboard> findByUserAndDescriptionContaining(User user, String description);

	List<Dashboard> findAllByOrderByIdentificationAsc();

	List<Dashboard> findByIdentificationAndDescriptionAndUser(String identification, String description, User user);

	List<Dashboard> findByIdentificationAndDescription(String identification, String description);

	@Query("SELECT o " + "FROM Dashboard AS o " + "WHERE (o.isPublic=TRUE OR " + "o.user=:user OR "
			+ "o.id IN (SELECT uo.dashboard.id " + "FROM DashboardUserAccess AS uo " + "WHERE uo.user=:user)) AND "
			+ "(o.identification like %:identification% AND o.description like %:description%) ORDER BY o.identification ASC")
	List<Dashboard> findByUserAndPermissionsANDIdentificationContainingAndDescriptionContaining(
			@Param("user") User user, @Param("identification") String identification,
			@Param("description") String description);

	long countByIdentification(String identification);
}
