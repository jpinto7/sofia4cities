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
package com.indracompany.sofia2.simulator.scheduler.listener;

import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import com.indracompany.sofia2.scheduler.SchedulerType;
import com.indracompany.sofia2.scheduler.scheduler.service.BatchSchedulerFactory;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class SimulatorSchedulerListenerConfig implements ApplicationListener<ContextRefreshedEvent> {


	@Autowired
	@Qualifier("simulation-scheduler-factory")
	private SchedulerFactoryBean schedulerFactory;

	@Autowired
	private BatchSchedulerFactory batchSchedulerFactory;

	@Autowired
	private SchedulerListener simulatorSchedulerListener;

	@Override

	public void onApplicationEvent(ContextRefreshedEvent event) {

		try {

			batchSchedulerFactory.getScheduler(SchedulerType.Simulation).getListenerManager().

					addSchedulerListener(simulatorSchedulerListener);

		} catch (SchedulerException | NotFoundException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		}

		// event.getApplicationContext()

		log.info("*******init scheduler listener*************");

		schedulerFactory.setSchedulerListeners(simulatorSchedulerListener);

	}
}
