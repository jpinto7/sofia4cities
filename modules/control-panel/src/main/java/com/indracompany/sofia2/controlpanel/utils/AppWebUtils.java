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
package com.indracompany.sofia2.controlpanel.utils;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.indracompany.sofia2.config.model.Role;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AppWebUtils {

	@Autowired
	private MessageSource messageSource;

	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	public String getUserId() {
		Authentication auth = getAuthentication();
		if (auth == null)
			return null;
		return auth.getName();
	}

	public String getRole() {
		Authentication auth = getAuthentication();
		if (auth == null)
			return null;
		return auth.getAuthorities().toArray()[0].toString();
	}

	public boolean isAdministrator() {
		if (getRole().equals(Role.Type.ROLE_ADMINISTRATOR.toString()))
			return true;
		return false;
	}

	public boolean isAuthenticated() {
		Authentication auth = getAuthentication();
		if (auth == null) {
			return false;
		}
		return true;
	}

	public boolean isUser() {
		if (getRole().equals(Role.Type.ROLE_USER.toString()))
			return true;
		return false;
	}

	public boolean isDataViewer() {
		if (getRole().equals(Role.Type.ROLE_DATAVIEWER.toString()))
			return true;
		return false;
	}

	public void addRedirectMessage(String messageKey, RedirectAttributes redirect) {
		String message = getMessage(messageKey, "Error processing request");
		redirect.addFlashAttribute("message", message);

	}

	public void addRedirectException(Exception exception, RedirectAttributes redirect) {
		redirect.addFlashAttribute("message", exception.getMessage());

	}

	public String getMessage(String key, String valueDefault) {
		try {
			return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
		} catch (Exception e) {
			log.debug("Key:" + key + " not found. Returns:" + valueDefault);
			return valueDefault;
		}
	}

	public void setSessionAttribute(HttpServletRequest request, String name, Object o) {
		WebUtils.setSessionAttribute(request, name, o);
	}

	public String validateAndReturnJson(String json) {
		ObjectMapper objectMapper = new ObjectMapper();
		String formattedJson = null;
		try {
			JsonNode tree = objectMapper.readValue(json, JsonNode.class);
			formattedJson = tree.toString();
		} catch (Exception e) {
			log.error("Error reading JSON by:" + e.getMessage(), e);
		}
		return formattedJson;
	}

	public boolean paswordValidation(String data) {

		Pattern pattern = Pattern.compile("(?=^.{7,20}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$");
		Matcher matcher = pattern.matcher(data);
		return matcher.find();

	}

	public String beautifyJson(String json) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		String result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
		return result;
	}

	public Object getAsObject(String json) throws JsonProcessingException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Object oJson = mapper.readValue(json, Object.class);
			return oJson;
			// return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(oJson);
		} catch (Exception e) {
			log.error("Impossible to convert to Object, returning the same");
			return json;
		}

	}

	public String encodeUrlPathSegment(final String pathSegment, final HttpServletRequest httpServletRequest) {
		String enc = httpServletRequest.getCharacterEncoding();
		String pathSegmentEncode = "";
		if (enc == null) {
			enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
		}
		try {
			pathSegmentEncode = UriUtils.encodePathSegment(pathSegment, enc);
		} catch (UnsupportedEncodingException uee) {
			log.warn("Error encoding path segment " + uee.getMessage());
		}
		return pathSegmentEncode;
	}

}
