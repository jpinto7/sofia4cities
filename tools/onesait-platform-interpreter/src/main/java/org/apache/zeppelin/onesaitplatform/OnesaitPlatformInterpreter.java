/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zeppelin.onesaitplatform;

import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.zeppelin.interpreter.Interpreter;
import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.interpreter.InterpreterResult;
import org.apache.zeppelin.interpreter.InterpreterResult.Code;
import org.apache.zeppelin.scheduler.Scheduler;
import org.apache.zeppelin.scheduler.SchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Onesait Platform interpreter
 *
 */
public class OnesaitPlatformInterpreter extends Interpreter {

  private static final Logger LOGGER = LoggerFactory.getLogger(OnesaitPlatformInterpreter.class);

  private long commandTimeout = 60000;

  private OnesaitPlatformConnection osp = null;

  public OnesaitPlatformInterpreter(Properties property) {
    super(property);
  }

  @Override
  public void open() {
    commandTimeout = Long.parseLong(getProperty("onesaitplatform.execution.timeout"));
    int port = Integer.parseInt(getProperty("onesaitplatform.server.port"));
    osp = new OnesaitPlatformConnection(getProperty("onesaitplatform.server.host"), port, getProperty("onesaitplatform.server.restpath"), commandTimeout);
  }

  @Override
  public void close() {
    osp.doLeave();
  }

  @Override
  public FormType getFormType() {
    return FormType.SIMPLE;
  }

  @Override
  public InterpreterResult interpret(String script, InterpreterContext context) {
    LOGGER.debug("Run onesait platform query: {}", script);
    
    InterpreterResult result = null;
    if (StringUtils.isEmpty(script)) {
      return new InterpreterResult(Code.SUCCESS);
    }
    else{
      List<String> resultQuery; 
      try{
        resultQuery = OnesaitPlatformParser.parseAndExecute(context, osp, script);
        result = new InterpreterResult(InterpreterResult.Code.SUCCESS);
        result.add(formatOutputZeppelin(resultQuery));
      }
      catch(Exception e){
        result = new InterpreterResult(InterpreterResult.Code.ERROR);
        result.add("Error in onesait platform query: " + e.getMessage());
      }
      
      
    }

    return result;
  }

  private String formatOutputZeppelin(List<String> alist){
    return String.join("\n", alist);
  }

  @Override
  public int getProgress(InterpreterContext context) {
    return 0;
  }

  @Override
  public void cancel(InterpreterContext context) {
  }

  @Override
  public Scheduler getScheduler() {
    return SchedulerFactory.singleton().createOrGetParallelScheduler("onesaitplatform", 10);
  }
  
}
