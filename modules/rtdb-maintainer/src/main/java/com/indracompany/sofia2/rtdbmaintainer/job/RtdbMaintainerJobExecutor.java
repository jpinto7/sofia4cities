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
package com.indracompany.sofia2.rtdbmaintainer.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indracompany.sofia2.scheduler.job.BatchGenericExecutor;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RtdbMaintainerJobExecutor implements BatchGenericExecutor {

	@Autowired
	RtdbMaintainerJob rtdbMaintainerJob;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			this.rtdbMaintainerJob.execute(context);
			log.info("Executed");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
