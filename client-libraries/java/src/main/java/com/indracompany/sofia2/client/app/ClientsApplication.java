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
package com.indracompany.sofia2.client.app;

import java.io.IOException;

import com.indracompany.sofia2.client.MQTTClient;
import com.indracompany.sofia2.client.MQTTClient.QUERY_TYPE;
import com.indracompany.sofia2.client.SubscriptionListener;

public class ClientsApplication {

	public static void main(String[] args) throws InterruptedException, IOException {

		MQTTClient client = new MQTTClient("tcp://localhost:1883");
		String token = "e7ef0742d09d4de5a3687f0cfdf7f626";
		String clientPlatform = "Ticketing App";
		String clientPlatformInstance = clientPlatform + ":MQTT";
		String ontology = "Ticket";
		int timeout = 50;
		String sessionKey = client.connect(token, clientPlatform, clientPlatformInstance, timeout);
		String jsonData = "{\"Ticket\":{ \"Temp\":28.6}}";
		// client.publish(ontology, jsonData, timeout);
		String subsId = client.subscribe(ontology, "SELECT * FROM Ticket", QUERY_TYPE.SQL, timeout,
				new SubscriptionListener() {

					@Override
					public void onMessageArrived(String message) {
						System.out.println(message);

					}

				});
		client.unsubscribe(subsId);

		//
		// while(true);
		client.disconnect();
		System.exit(0);
		// RestClient restClient = new RestClient("http://localhost:8081/iotbroker");
		// String sessionKey = restClient.connect(token, clientPlatform,
		// clientPlatformInstance);
		// String instance = "{\"BinaryOnt\":{
		// \"Name\":\"string\",\"Image\":{\"data\":\"string\",\"media\":{\"name\":\"fichero.pdf\",\"storageArea\":\"SERIALIZED\",\"binaryEncoding\":\"Base64\",\"mime\":\"application/pdf\"}}}}";
		// restClient.insertInstance(ontology, instance);
		// restClient.disconnect();
		//
	}
}
