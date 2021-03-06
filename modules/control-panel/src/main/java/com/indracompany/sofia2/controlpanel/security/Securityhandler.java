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
package com.indracompany.sofia2.controlpanel.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import com.indracompany.sofia2.controlpanel.controller.management.login.LoginManagementController;
import com.indracompany.sofia2.controlpanel.controller.management.login.model.RequestLogin;

@Component
public class Securityhandler implements AuthenticationSuccessHandler {

	private final String BLOCK_PRIOR_LOGIN = "block_prior_login";
	private final String URI_CONTROLPANEL = "/controlpanel";
	private final String URI_MAIN = "/main";

	@Autowired
	private LoginManagementController controller;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {

		HttpSession session = request.getSession();
		if (session != null) {
			this.generateTokenOauth2ForControlPanel(request);
			String redirectUrl = (String) session.getAttribute(BLOCK_PRIOR_LOGIN);
			if (redirectUrl != null) {
				// we do not forget to clean this attribute from session
				session.removeAttribute(BLOCK_PRIOR_LOGIN);
				// then we redirect
				response.sendRedirect(request.getContextPath() + redirectUrl.replace(URI_CONTROLPANEL, ""));
			} else {
				response.sendRedirect(request.getContextPath() + URI_MAIN);
			}
		} else {
			response.sendRedirect(request.getContextPath() + URI_MAIN);
		}

	}

	private void generateTokenOauth2ForControlPanel(HttpServletRequest request) {
		String password = request.getParameter("password");
		String username = request.getParameter("username");
		if (!StringUtils.isEmpty(password) && !StringUtils.isEmpty(username)) {
			RequestLogin oauthRequest = new RequestLogin();
			oauthRequest.setPassword(password);
			oauthRequest.setUsername(username);
			try {
				request.getSession().setAttribute("oauthToken",
						this.controller.postLoginOauth2(oauthRequest).getBody());
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}

}
