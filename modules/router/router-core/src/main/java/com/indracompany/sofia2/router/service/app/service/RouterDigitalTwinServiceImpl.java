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
package com.indracompany.sofia2.router.service.app.service;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indracompany.sofia2.router.service.app.model.DigitalTwinCompositeModel;
import com.indracompany.sofia2.router.service.app.model.OperationResultModel;

@Service("routerDigitalTwinServiceImpl")
public class RouterDigitalTwinServiceImpl implements RouterDigitalTwinService {

	@Autowired
	CamelContext camelContext;

	private String defaultStartupRoute = "direct:start-digitaltwin-broker-flow";

	@Override
	public OperationResultModel insertEvent(DigitalTwinCompositeModel model) {
		ProducerTemplate t = camelContext.createProducerTemplate();
		OperationResultModel result = (OperationResultModel) t.requestBody(defaultStartupRoute, model);
		return result;
	}

	@Override
	public OperationResultModel insertLog(DigitalTwinCompositeModel model) {
		ProducerTemplate t = camelContext.createProducerTemplate();
		OperationResultModel result = (OperationResultModel) t.requestBody(defaultStartupRoute, model);
		return result;
	}

	@Override
	public OperationResultModel updateShadow(DigitalTwinCompositeModel model) {
		ProducerTemplate t = camelContext.createProducerTemplate();
		OperationResultModel result = (OperationResultModel) t.requestBody(defaultStartupRoute, model);
		return result;
	}

	@Override
	public OperationResultModel insertAction(DigitalTwinCompositeModel model) {
		ProducerTemplate t = camelContext.createProducerTemplate();
		OperationResultModel result = (OperationResultModel) t.requestBody(defaultStartupRoute, model);
		return result;
	}

}
