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
package com.indracompany.sofia2.controlpanel.helper.digitaltwin.device;

import java.util.HashMap;
import java.util.Map;

public class GeneratorJavaTypesMapper {
	
	
	private static Map<String, String> types=new HashMap<String, String>();
	
	static {
		types.put("string", "String");
		types.put("int", "Integer");
		types.put("object", "Object");
		types.put("double", "Double");
		types.put("boolean", "Boolean");
	}
	

	public static String mapPropertyName(String type) {
		return types.get(type);
	}
	
	
	
}
