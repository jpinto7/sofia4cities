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
package com.indracompany.sofia2.persistence.hadoop.util;

import org.springframework.stereotype.Service;

@Service
public class HadoopQueryProcessor {

	public String parse(String query) {

		String parsedQuery = query.toLowerCase();

		parsedQuery = parsedQuery.replace(".coordinates.0", HiveFieldType.LATITUDE_FIELD);
		parsedQuery = parsedQuery.replace(".coordinates.1", HiveFieldType.LONGITUDE_FIELD);
		// parsedQuery = parsedQuery.replace("geometry",
		// HiveFieldType.LATITUDE_FIELD + ", " + HiveFieldType.LONGITUDE_FIELD);

		if (parsedQuery.endsWith("as c")) {
			parsedQuery = parsedQuery.replace("as c", "");
		}

		return parsedQuery;
	}

}
