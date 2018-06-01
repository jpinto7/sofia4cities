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
 * 2013 - 2014  SPAIN
 *
 * All rights reserved
 ******************************************************************************/
package com.indracompany.sofia2.config.model;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;

import com.indracompany.sofia2.config.model.base.AuditableEntityWithUUID;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "API")
public class Api extends AuditableEntityWithUUID {

	private static final long serialVersionUID = 1L;

	public static enum ApiStates {
		CREATED, PUBLISHED, DELETED, DEPRECATED, DEVELOPMENT;
	}

	public static enum ApiCategories {
		ALL, ADVERTISING, BUSINESS, COMMUNICATION, EDUCATION, ENTERTAINMENT, MEDIA, MEDICAL, OTHER, SOCIAL, SPORTS, TOOLS, TRAVEL;
	}

	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ONTOLOGY_ID", referencedColumnName = "ID")
	@Getter
	@Setter
	private Ontology ontology;

	@ManyToOne
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	@Getter
	@Setter
	private User user;

	@Basic(fetch = FetchType.LAZY)
	@Column(name = "IMAGE", length = 100000)
	@Lob
	@Type(type = "org.hibernate.type.BinaryType")
	@Getter
	@Setter
	private byte[] image;

	@Column(name = "SSL_CERTIFICATE")
	@NotNull
	@Getter
	@Setter
	private boolean ssl_certificate;

	@OneToMany(mappedBy = "api", cascade = CascadeType.ALL)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Set<ApiComment> comments;

	@OneToMany(mappedBy = "api", cascade = CascadeType.ALL)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@Getter
	@Setter
	private Set<ApiUserAssessment> userAssessments;

	@Column(name = "IDENTIFICATION", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String identification;

	@Column(name = "NUM_VERSION")
	@Getter
	@Setter
	private Integer numversion;

	@Column(name = "DESCRIPTION", length = 512, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String description;

	@Column(name = "CATEGORY", length = 255)
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private ApiCategories category;

	@Column(name = "ENDPOINT", length = 512)
	@Getter
	@Setter
	private String endpoint;

	@Column(name = "ENDPOINT_EXT", length = 512)
	@Getter
	@Setter
	private String endpointExt;

	@Column(name = "STATE", length = 20, nullable = false)
	@NotNull
	@Getter
	@Setter
	@Enumerated(EnumType.STRING)
	private ApiStates state;

	@Column(name = "META_INF", length = 512)
	@Getter
	@Setter
	private String metaInf;

	@Column(name = "IMAGE_TYPE", length = 20)
	@Getter
	@Setter
	private String imageType;

	@Column(name = "IS_PUBLIC", nullable = false)
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

	@Column(name = "CACHE_TIMEOUT")
	@Getter
	@Setter
	private Integer cachetimeout;

	@Column(name = "API_LIMIT")
	@Getter
	@Setter
	private Integer apilimit;

	@Column(name = "API_TYPE", length = 50)
	@Getter
	@Setter
	private String apiType;

	@Column(name = "ASSESSMENT", precision = 10)
	@Getter
	@Setter
	private Double assessment;

}
