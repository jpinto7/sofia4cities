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
package com.indracompany.sofia2.config.services.webproject;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.indracompany.sofia2.config.model.WebProject;

public interface WebProjectService {

	List<WebProject> getWebProjectsWithDescriptionAndIdentification(String userId, String identification,
			String description);

	List<String> getWebProjectsIdentifications(String userId);

	List<String> getAllIdentifications();

	void createWebProject(WebProject webProject, String userId);
	
	void updateWebProject(WebProject webProject, String userId);

	WebProject getWebProjectById(String webProjectId, String userId);

	void deleteWebProjectById(String webProjectId, String userId);
	
	void uploadZip(MultipartFile file, String userId);

	void deleteWebProject(WebProject webProject, String userId);

	boolean webProjectExists(String identification);

}
