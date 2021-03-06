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
package com.indracompany.sofia2.audit.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventSenderImpl implements EventRouter {

	@Autowired
	private HazelcastInstance instance;

	@Override
	public void notify(String event) {
		log.info("EventSenderImpl :: thread '{}' handling '{}' Notify to Router The Event: ", Thread.currentThread(),
				event);
		instance.getQueue("audit").offer(event);

	}
	/*
	 * @Override public void notify(Sofia2AuditEvent event) { log.
	 * info("EventSenderImpl :: thread '{}' handling '{}' Notify to Router The Event: "
	 * , Thread.currentThread(), event.getMessage());
	 * instance.getQueue("audit").offer(event.toJson());
	 * 
	 * }
	 */

}
