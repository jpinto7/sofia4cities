/*******************************************************************************
 * Â© Indra Sistemas, S.A.
 * 2013 - 2018  SPAIN
 * 
 * All rights reserved
 ******************************************************************************/
package com.indracompany.sofia2.persistence;

import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;
import com.indracompany.sofia2.persistence.util.CalendarAdapter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class ContextData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Getter @Setter private String user;
	@Getter @Setter private String clientPatform;
	@Getter @Setter private String clientConnection;
	@Getter @Setter private String clientSession;
	@Getter @Setter private String timezoneId;
	
	public ContextData() {}
	
	public ContextData(JsonNode node) {
		try {
			this.user = node.findValue("user").asText();
			this.clientPatform = node.findValue("clientPatform").asText();
			this.clientConnection = node.findValue("clientConnection").asText();
			this.clientSession = node.findValue("clientSession").asText();
		} catch (Exception e) {
			// We are processing a minimal context data
			this.user = "";
			this.clientPatform= "";
			this.clientConnection = "";
			this.clientSession = "";
		}
		
		JsonNode timezoneId = node.findValue("timezoneId");
		if (timezoneId != null) {
			this.timezoneId = timezoneId.asText();
		} else {
			this.timezoneId = CalendarAdapter.getServerTimezoneId();
		}
	}
	
	public ContextData(ContextData other) {
		this.user = other.user;
		this.clientPatform = other.clientPatform;
		this.clientConnection = other.clientConnection;
		this.clientSession = other.clientSession;
		this.timezoneId = other.timezoneId;
	}

	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ContextData)) {
			return false;
		}
		ContextData contextData = (ContextData) other;
		return this.clientPatform.equals(contextData.clientPatform) && this.clientConnection.equals(contextData.clientConnection)
				&& this.clientSession.equals(contextData.clientSession) && this.timezoneId.equals(contextData.timezoneId)
				&& this.user.equals(contextData.user);
	}
}