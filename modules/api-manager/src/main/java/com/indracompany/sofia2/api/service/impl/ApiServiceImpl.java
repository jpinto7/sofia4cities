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
package com.indracompany.sofia2.api.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.jeasy.rules.api.Facts;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.codahale.metrics.annotation.Timed;
import com.indracompany.sofia2.api.rule.DefaultRuleBase.ReasonType;
import com.indracompany.sofia2.api.rule.RuleManager;
import com.indracompany.sofia2.api.service.ApiServiceInterface;
import com.indracompany.sofia2.api.service.api.ApiManagerService;
import com.indracompany.sofia2.config.model.Api;
import com.indracompany.sofia2.config.model.ApiOperation;
import com.indracompany.sofia2.config.model.Ontology;
import com.indracompany.sofia2.config.model.User;
import com.indracompany.sofia2.router.service.app.model.NotificationModel;
import com.indracompany.sofia2.router.service.app.model.OperationModel;
import com.indracompany.sofia2.router.service.app.model.OperationModel.OperationType;
import com.indracompany.sofia2.router.service.app.model.OperationModel.QueryType;
import com.indracompany.sofia2.router.service.app.model.OperationResultModel;

import io.prometheus.client.spring.web.PrometheusTimeMethod;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiServiceImpl extends ApiManagerService implements ApiServiceInterface, Processor {
	
	@Autowired
	RuleManager ruleManager;
		
	@Autowired
	private RouterOperationsServiceFacade facade;
		
	 
	@SuppressWarnings("unchecked")
	@Override
	@PrometheusTimeMethod(name = "ApiServiceImplEntryPointCamel", help = "ApiServiceImpl doGET")
	@Timed
	public void process(Exchange exchange) throws Exception {
		
		HttpServletRequest request = exchange.getIn().getHeader(Exchange.HTTP_SERVLET_REQUEST, HttpServletRequest.class);
		HttpServletResponse response = exchange.getIn().getHeader(Exchange.HTTP_SERVLET_RESPONSE, HttpServletResponse.class);
	
		Facts facts = new Facts();
		facts.put(RuleManager.REQUEST, request);
		facts.put(RuleManager.RESPONSE, response);
		
		Map<String,Object> dataFact=new HashMap<String,Object>();
		dataFact.put(ApiServiceInterface.BODY, exchange.getIn().getBody());
		
		facts.put(RuleManager.FACTS, dataFact);
		ruleManager.fire(facts);

		Map<String,Object> data = (Map<String,Object>)facts.get(RuleManager.FACTS);
		Boolean stopped = (Boolean)facts.get(RuleManager.STOP_STATE);
		String REASON="";
		String REASON_TYPE;
		
		if (stopped!=null && stopped==true) {
			REASON=((String)facts.get(RuleManager.REASON));
			REASON_TYPE=((String)facts.get(RuleManager.REASON_TYPE));
			
			if (REASON_TYPE.equals(ReasonType.API_LIMIT.name())) {
				exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 429);
			}
			else if (REASON_TYPE.equals(ReasonType.SECURITY.name())){
				exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 403);
			}
			else {
				exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
			}
			
			exchange.getIn().setBody("[\"STOPPED EXECUTION BY "+REASON_TYPE+"\",\""+REASON+"\"]");
			exchange.getIn().setHeader("content-type", "text/plain");
			exchange.getIn().setHeader("STATUS", "STOP");
			exchange.getIn().setHeader("REASON", REASON);
		}
		else {
			exchange.getIn().setHeader("STATUS", "FOLLOW");
			exchange.getIn().setBody(data);
		}
	}
	
	@PrometheusTimeMethod(name = "processQuery", help = "processQuery")
	@Timed
	public Map<String,Object> processQuery(Map<String,Object> data, Exchange exchange) throws Exception {
		
		Ontology ontology = (Ontology) data.get(ApiServiceInterface.ONTOLOGY);
		String METHOD = (String) data.get(ApiServiceInterface.METHOD);
		String BODY = (String) data.get(ApiServiceInterface.BODY);
		String QUERY_TYPE = (String) data.get(ApiServiceInterface.QUERY_TYPE);
		String QUERY = (String) data.get(ApiServiceInterface.QUERY);
		String TARGET_DB_PARAM = (String) data.get(ApiServiceInterface.TARGET_DB_PARAM);
		String OBJECT_ID = (String) data.get(ApiServiceInterface.OBJECT_ID);
		String CACHEABLE = (String) data.get(ApiServiceInterface.CACHEABLE);
		
		User user = (User) data.get(ApiServiceInterface.USER);
		
		String body = BODY;
		OperationType operationType = null;
		
		if (METHOD.equalsIgnoreCase(ApiOperation.Type.GET.name())) {
			body = QUERY;
			operationType = OperationType.QUERY;
		} else if (METHOD.equalsIgnoreCase(ApiOperation.Type.POST.name())) {
			operationType = OperationType.INSERT;
		} else if (METHOD.equalsIgnoreCase(ApiOperation.Type.PUT.name())) {
			operationType = OperationType.UPDATE;
		} else if (METHOD.equalsIgnoreCase(ApiOperation.Type.DELETE.name())) {
			operationType = OperationType.DELETE;
		}
		else operationType=OperationType.QUERY;
		
		
		OperationModel model = OperationModel.builder(
				ontology.getIdentification(), 
				OperationType.valueOf(operationType.name()),
				user.getUserId(), 
				OperationModel.Source.APIMANAGER)
				.body(body)
				.queryType(QueryType.valueOf(QUERY_TYPE))
				.objectId(OBJECT_ID)
				.clientPlatformId("")
				.cacheable("true".equalsIgnoreCase(CACHEABLE) ? true : false)
				.build();
		
		NotificationModel modelNotification= new NotificationModel();
		modelNotification.setOperationModel(model);
		
		String OUTPUT = "";
		OperationResultModel result =facade.query(modelNotification);
		
		if (result != null) {
			if ("ERROR".equals(result.getResult())){
				throw new ApiServiceException("Error inserting data: " + result.getMessage());
			}
			OUTPUT= result.getResult();
		}

		data.put(ApiServiceInterface.OUTPUT, OUTPUT);
		exchange.getIn().setBody(data);
		return data;
	}
	
	
	
	@PrometheusTimeMethod(name = "processOutput", help = "processOutput")
	@Timed
	public Map<String,Object> processOutput(Map<String,Object> data, Exchange exchange) throws Exception {
		
		String FORMAT_RESULT = (String) data.get(ApiServiceInterface.FORMAT_RESULT);
		String OUTPUT = (String) data.get(ApiServiceInterface.OUTPUT);
		String CONTENT_TYPE="text/plain";
		
		if (OUTPUT==null || OUTPUT.equalsIgnoreCase("")) {
			OUTPUT="{\"RESULT\":\"NO_DATA\"}";
		}
		
		if (FORMAT_RESULT.equals("")) {
			CONTENT_TYPE = (String)data.get(ApiServiceInterface.CONTENT_TYPE_OUTPUT);
		}
		
		JSONObject jsonObj = toJSONObject(OUTPUT);
		JSONArray jsonArray = toJSONArray(OUTPUT);
		
		String xmlOrCsv=OUTPUT;
		
		if (FORMAT_RESULT.equalsIgnoreCase("JSON") || CONTENT_TYPE.equalsIgnoreCase("application/json") ) {
			data.put(ApiServiceInterface.CONTENT_TYPE, "application/json");
			CONTENT_TYPE = "application/json";
		}
		else if (FORMAT_RESULT.equalsIgnoreCase("XML") || CONTENT_TYPE.equalsIgnoreCase("application/atom+xml")) {
			data.put(ApiServiceInterface.CONTENT_TYPE, "application/atom+xml");
			
			if (jsonObj!=null) xmlOrCsv = XML.toString(jsonObj);
			if (jsonArray!=null) xmlOrCsv = XML.toString(jsonArray);
			CONTENT_TYPE = "application/atom+xml";
		}	
		else if (FORMAT_RESULT.equalsIgnoreCase("CSV") ) {
			data.put(ApiServiceInterface.CONTENT_TYPE, "text/plain");
			
			if (jsonObj!=null) xmlOrCsv = CDL.toString(new JSONArray("[" + jsonObj + "]"));
			if (jsonArray!=null) xmlOrCsv = CDL.toString(jsonArray);
			CONTENT_TYPE = "text/plain";
		}
		
		
		data.put(ApiServiceInterface.OUTPUT, xmlOrCsv);
		exchange.getIn().setHeader(ApiServiceInterface.CONTENT_TYPE, CONTENT_TYPE);
		return data;
		
	}
	
	
	private JSONObject toJSONObject(String input) {
		JSONObject jsonObj =null;
		try {
			jsonObj = new JSONObject(input);
		} catch (JSONException e) {
			return null;
		}
		return jsonObj;
	}
	
	private JSONArray toJSONArray(String input) {
		JSONArray jsonObj =null;
		try {
			jsonObj = new JSONArray(input);
		} catch (JSONException e) {
			return null;
		}
		return jsonObj;
	}
	
	
	
	@Override
	@PrometheusTimeMethod(name = "ApiServiceImplEntryPoint", help = "ApiServiceImpl")
	@Timed
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		Facts facts = new Facts();
		facts.put(RuleManager.REQUEST, request);
		facts.put(RuleManager.ACTION, "GET");
		Map<String,Object> dataFact=new HashMap<String,Object>();
		facts.put(RuleManager.FACTS, dataFact);
		ruleManager.fire(facts);

		Map<String,Object> data = (Map<String,Object>)facts.get(RuleManager.FACTS);
		Boolean stopped = (Boolean)facts.get(RuleManager.STOP_STATE);
		String REASON="";
		String  REASON_TYPE="";
		if (stopped!=null && stopped==true) {
			REASON=((String)facts.get(RuleManager.REASON));
			REASON_TYPE=((String)facts.get(RuleManager.REASON_TYPE));
		}
		log.debug(hashPP(data));
				
		sendResponse(response, HttpServletResponse.SC_OK, hashPP(data)+"\n"+REASON,null,null);

	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
		doGet(request,response);

	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws Exception {
		doGet(request,response);

	}

	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws Exception {
		doGet(request,response);

	}
	

	private void sendResponse(HttpServletResponse response, int status, String message, String formatResult,
			String query) throws IOException {

		String infoJSON = null;
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setStatus(status);
		response.setCharacterEncoding("UTF-8");

		ByteArrayOutputStream byteFichero = null;
		Locale locale = LocaleContextHolder.getLocale();

		try {
			response.setContentType("text/plain");
			// message=message.replace("\n", " ");
			response.getWriter().write(message);
		} catch (IOException e) {
			throw new IOException(e);
		}
		return;
	}

	private static JSONObject getJsonFromMap(Map<String, Object> map) throws JSONException {
		JSONObject jsonData = new JSONObject();
		for (String key : map.keySet()) {
			Object value = map.get(key);
			if (value instanceof Map<?, ?>) {
				value = getJsonFromMap((Map<String, Object>) value);
			}
			jsonData.put(key, value);
		}
		return jsonData;
	}

	
	private static String hashPP(final Map<String, Object> m, String... offset) {
		String retval = "";
		String delta = offset.length == 0 ? "" : offset[0];
		for (Map.Entry<String, Object> e : m.entrySet()) {
			retval += delta + "[" + e.getKey() + "] -> ";
			Object value = e.getValue();
			if (value instanceof Map) {
				retval += "(Hash)\n" + hashPP((Map<String, Object>) value, delta + "  ");
			} else if (value instanceof List) {
				retval += "{";
				for (Object element : (List) value) {
					retval += element + ", ";
				}
				retval += "}\n";
			} else {
				retval += "[" + value.toString() + "]\n";
			}
		}
		return retval + "\n";
	}



	

}
