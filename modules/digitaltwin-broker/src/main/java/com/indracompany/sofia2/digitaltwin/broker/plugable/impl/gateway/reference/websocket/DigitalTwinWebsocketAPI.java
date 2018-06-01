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
package com.indracompany.sofia2.digitaltwin.broker.plugable.impl.gateway.reference.websocket;

import org.json.JSONObject;
import org.springframework.messaging.MessageHeaders;

public interface DigitalTwinWebsocketAPI {

	public void sendAction(String message, MessageHeaders messageHeaders);

	public void notifyShadowMessage(JSONObject message);

	public void notifyCustomMessage(JSONObject message);

	public void notifyActionMessage(JSONObject message);

}