/*
 * Copyright 2012-2014 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.client.document;

import com.marklogic.client.util.RequestParameters;

import java.util.List;
import java.util.Map;

/**
 * ServerTransform specifies the invocation of a transform on the server
 * including both the name of the transform and the parameters passed
 * to the transform.
 */
public class ServerTransform extends RequestParameters {
	private String name;

	public ServerTransform(String name) {
		super();
    	this.name = name;
	}

	public String getName() {
		return name;
	}

	public RequestParameters merge(RequestParameters currentParams) {
		RequestParameters params = (currentParams != null) ?
				currentParams : new RequestParameters();
		params.put("transform", getName());

		for (Map.Entry<String, List<String>> entry: entrySet()) {
			params.put("trans:"+entry.getKey(), entry.getValue());
		}

		return params;
	}
}
