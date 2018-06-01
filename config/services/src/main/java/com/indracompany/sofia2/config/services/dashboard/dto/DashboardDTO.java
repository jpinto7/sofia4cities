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

package com.indracompany.sofia2.config.services.dashboard.dto;

import com.indracompany.sofia2.config.model.User;
import com.indracompany.sofia2.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

public class DashboardDTO extends AuditableEntityWithUUID {

	@Getter
	@Setter
	private String identification;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private User user;

	@Getter
	@Setter
	private String jsoni18n;

	@Getter
	@Setter
	private String customcss;

	@Getter
	@Setter
	private String customjs;

	@Getter
	@Setter
	private boolean isPublic;

	@Getter
	@Setter
	private String model;

	@Getter
	@Setter
	private String userAccessType;

	@Getter
	@Setter
	private byte[] image;

	@Getter
	@Setter
	private Boolean hasImage;
}