/*
 * Copyright 2012 MarkLogic Corporation
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
package com.marklogic.client.impl;

import com.marklogic.client.admin.MethodType;
import com.marklogic.client.util.RequestLogger;
import com.marklogic.client.util.RequestParameters;
import com.marklogic.client.extensions.ResourceServices;
import com.marklogic.client.Transaction;
import com.marklogic.client.io.marker.AbstractReadHandle;
import com.marklogic.client.io.marker.AbstractWriteHandle;


@SuppressWarnings({"unchecked", "rawtypes"})
class ResourceServicesImpl
    extends AbstractLoggingManager
    implements ResourceServices
{
	private String       resourceName;
	private RESTServices services;

	ResourceServicesImpl(RESTServices services, String resourceName) {
		super();
		this.services     = services;
		this.resourceName = resourceName;
	}

	@Override
	public String getResourceName() {
		return resourceName;
	}
	private String getResourcePath() {
		return "resources/"+getResourceName();
	}

	@Override
	public <R extends AbstractReadHandle> R get(RequestParameters params, R output) {
		return get(params, null, output);
	}
	@Override
	public <R extends AbstractReadHandle> R get(RequestParameters params, Transaction transaction, R output) {
		return makeResult(
				execMethod(MethodType.GET, prepareParams(params, transaction), output),
				output
				);
	}
	@Override
	public ServiceResultIterator get(RequestParameters params, String... outputMimetypes) {
		return get(params, null, outputMimetypes);
	}
	@Override
	public ServiceResultIterator get(RequestParameters params, Transaction transaction, String... outputMimetypes) {
		return execMethod(MethodType.GET, prepareParams(params, transaction), outputMimetypes);
	}

	@Override
	public <R extends AbstractReadHandle> R put(RequestParameters params, AbstractWriteHandle input, R output) {
		return put(params, input, null, output);
	}
	@Override
	public <R extends AbstractReadHandle> R put(RequestParameters params, AbstractWriteHandle input, Transaction transaction, R output) {
		return makeResult(
				execMethod(MethodType.PUT, prepareParams(params, transaction), input, output),
				output
				);
	}
	@Override
	public <R extends AbstractReadHandle, W extends AbstractWriteHandle> R put(RequestParameters params, W[] input, R output) {
		return put(params, input, null, output);
	}
	@Override
	public <R extends AbstractReadHandle, W extends AbstractWriteHandle> R put(RequestParameters params, W[] input, Transaction transaction, R output) {
		return makeResult(
				execMethod(MethodType.PUT, prepareParams(params, transaction), input, output),
				output
				);
	}

	@Override
	public <R extends AbstractReadHandle> R post(RequestParameters params, AbstractWriteHandle input, R output) {
		return post(params, input, null, output);
	}
	@Override
	public <R extends AbstractReadHandle> R post(RequestParameters params, AbstractWriteHandle input, Transaction transaction, R output) {
		return makeResult(
				execMethod(MethodType.POST, prepareParams(params, transaction), input, output),
				output
				);
	}
	@Override
	public ServiceResultIterator post(RequestParameters params, AbstractWriteHandle input, String... outputMimetypes) {
		return post(params, input, null, outputMimetypes);
	}
	@Override
	public ServiceResultIterator post(RequestParameters params, AbstractWriteHandle input, Transaction transaction, String... outputMimetypes) {
		return execMethod(MethodType.POST, prepareParams(params, transaction), input, outputMimetypes);
	}
	@Override
	public <R extends AbstractReadHandle, W extends AbstractWriteHandle> R post(RequestParameters params, W[] input, R output) {
		return post(params, input, null, output);
	}
	@Override
	public <R extends AbstractReadHandle, W extends AbstractWriteHandle> R post(RequestParameters params, W[] input, Transaction transaction, R output) {
		return makeResult(
				execMethod(MethodType.POST, prepareParams(params, transaction), input, output),
				output
				);
	}
	@Override
	public <W extends AbstractWriteHandle> ServiceResultIterator post(RequestParameters params, W[] input, String... outputMimetypes) {
		return post(params, input, null, outputMimetypes);
	}
	@Override
	public <W extends AbstractWriteHandle> ServiceResultIterator post(RequestParameters params, W[] input, Transaction transaction, String... outputMimetypes) {
		return execMethod(MethodType.POST, prepareParams(params, transaction), input, outputMimetypes);
	}

	@Override
	public <R extends AbstractReadHandle> R delete(RequestParameters params, R output) {
		return delete(params, null, output);
	}
	@Override
	public <R extends AbstractReadHandle> R delete(RequestParameters params, Transaction transaction, R output) {
		return makeResult(
				execMethod(MethodType.DELETE, prepareParams(params, transaction), output),
				output
				);
	}

	@Override
    public RequestLogger getRequestLogger() {
    	return requestLogger;
    }

	private RequestParameters prepareParams(RequestParameters params, Transaction transaction) {
		if (params == null && transaction == null)
			return null;
		if (transaction == null)
			return params.copy("rs");

		RequestParameters requestParams =
			(params != null) ? params.copy("rs") : new RequestParameters();
		requestParams.add("txid", transaction.getTransactionId());

		return requestParams;
	}

	private <R extends AbstractReadHandle> Object execMethod(MethodType method, RequestParameters params, R output) {
		HandleImplementation outputBase = HandleAccessor.checkHandle(output, "read");

		String outputMimetype = outputBase.getMimetype();
		Class  as             = outputBase.receiveAs();

		switch (method) {
		case GET:
			return services.getResource(requestLogger, getResourcePath(), params, outputMimetype, as);
		case DELETE:
			return services.deleteResource(requestLogger, getResourcePath(), params, outputMimetype, as);
		default:
			throw new IllegalArgumentException("unknown method type: "+method.name());
		}
	}
	private ServiceResultIterator execMethod(MethodType method, RequestParameters params, String... outputMimetypes) {
		switch (method) {
		case GET:
			return services.getResource(requestLogger, getResourcePath(), params, outputMimetypes);
		default:
			throw new IllegalArgumentException("unknown method type: "+method.name());
		}
	}
	private <R extends AbstractReadHandle> Object execMethod(MethodType method, RequestParameters params, AbstractWriteHandle input, R output) {
		HandleImplementation inputBase  = HandleAccessor.checkHandle(input,  "write");
		HandleImplementation outputBase = HandleAccessor.checkHandle(output, "read");

		Object  value          = inputBase.sendContent();
		String  inputMimetype  = inputBase.getMimetype();
		String  outputMimetype = outputBase.getMimetype();
		boolean isStreaming    = !inputBase.isResendable();
		Class   as             = outputBase.receiveAs();

		switch (method) {
		case PUT:
			return services.putResource(requestLogger, getResourcePath(), params,
					inputMimetype, value, outputMimetype, isStreaming, as);
		case POST:
			return services.postResource(requestLogger, getResourcePath(), params,
					inputMimetype, value, outputMimetype, isStreaming, as);
		default:
			throw new IllegalArgumentException("unknown method type: "+method.name());
		}
	}
	private <R extends AbstractReadHandle, W extends AbstractWriteHandle> Object execMethod(MethodType method, RequestParameters params, W[] input, R output) {
		HandleImplementation outputBase = HandleAccessor.checkHandle(output, "read");

		Object[] value            = null;
		String[] inputMimetype    = null;
		boolean  hasStreamingPart = false;
		if (input != null) {
			int inputSize = input.length;
			value         = new Object[inputSize];
			inputMimetype = new String[inputSize];
			for (int i=0; i < inputSize; i++) {
				AbstractWriteHandle handle = input[i];
				HandleImplementation handleBase = HandleAccessor.checkHandle(handle, "write");
				value[i]         = handleBase.sendContent();
				inputMimetype[i] = handleBase.getMimetype();
				if (!hasStreamingPart)
					hasStreamingPart = !handleBase.isResendable();
			}
		}

		String outputMimetype = outputBase.getMimetype();
		Class  as             = outputBase.receiveAs();

		switch (method) {
		case PUT:
			return services.putResource(requestLogger, getResourcePath(), params,
					inputMimetype, value, outputMimetype, hasStreamingPart, as);
		case POST:
			return services.postResource(requestLogger, getResourcePath(), params,
					inputMimetype, value, outputMimetype, hasStreamingPart, as);
		default:
			throw new IllegalArgumentException("unknown method type: "+method.name());
		}
	}
	private ServiceResultIterator execMethod(MethodType method, RequestParameters params, AbstractWriteHandle input, String... outputMimetypes) {
		HandleImplementation inputBase = HandleAccessor.checkHandle(input,  "write");

		Object  value         = inputBase.sendContent();
		String  inputMimetype = inputBase.getMimetype();
		boolean isStreaming   = !inputBase.isResendable();

		switch (method) {
		case POST:
			return services.postResource(requestLogger, getResourcePath(), params,
					inputMimetype, value, outputMimetypes, isStreaming);
		default:
			throw new IllegalArgumentException("unknown method type: "+method.name());
		}
	}
	private <W extends AbstractWriteHandle> ServiceResultIterator execMethod(MethodType method, RequestParameters params, W[] input, String... outputMimetypes) {
		Object[] value            = null;
		String[] inputMimetype    = null;
		boolean  hasStreamingPart = false;
		if (input != null) {
			int inputSize = input.length;
			value         = new Object[inputSize];
			inputMimetype = new String[inputSize];
			for (int i=0; i < inputSize; i++) {
				AbstractWriteHandle handle = input[i];
				HandleImplementation handleBase = HandleAccessor.checkHandle(handle, "write");
				value[i]         = handleBase.sendContent();
				inputMimetype[i] = handleBase.getMimetype();
				if (!hasStreamingPart)
					hasStreamingPart = !handleBase.isResendable();
			}
		}

		switch (method) {
		case POST:
			return services.postResource(requestLogger, getResourcePath(), params,
					inputMimetype, value, outputMimetypes, hasStreamingPart);
		default:
			throw new IllegalArgumentException("unknown method type: "+method.name());
		}
	}

	private <R extends AbstractReadHandle> R makeResult(Object response, R output) {
		if (response != null) {
			HandleAccessor.as(output).receiveContent(response);
		}

		return output;
	}
}
