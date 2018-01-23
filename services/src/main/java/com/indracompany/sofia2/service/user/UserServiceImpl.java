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
package com.indracompany.sofia2.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.indracompany.sofia2.config.model.UserCDB;
import com.indracompany.sofia2.config.repository.RoleTypeRepository;
import com.indracompany.sofia2.config.repository.UserCDBRepository;

@Service
public class UserServiceImpl implements UserService {
	
	
	@Autowired
	UserCDBRepository userCDBRepository;
	@Autowired
	RoleTypeRepository roleTypeRepository;
	
	
	
	
	
	public UserCDB findUser(String userId)
	{
		return userCDBRepository.findByUserId(userId);
	}
	
	public void populateFormData(Model model)
	{
		model.addAttribute("roleTypes",this.roleTypeRepository.findAll());
	}

}