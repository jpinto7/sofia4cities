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
package com.indracompany.sofia2.persistence.services.util;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class MustacheUtil {

    private static final MustacheFactory mf = new DefaultMustacheFactory();
    
    public static MustacheFactory getMustacheFactory(){
        return mf;
    }
    
    public static String executeTemplate(Mustache m, Map<String, Object> context) throws IOException {
        StringWriter writer = new StringWriter();
        m.execute(writer, context).flush();
        return writer.toString();
    }
    
    public static String executeTemplate(String templateName,  Map<String, Object> context) throws IOException 
    {
    	 Mustache m = MustacheUtil.getMustacheFactory().compile(templateName);
         return executeTemplate(m, context);
    }
    
}