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

import java.util.Set;

import javax.net.ssl.SSLContext;

import com.marklogic.client.DatabaseClientFactory.Authentication;
import com.marklogic.client.DatabaseClientFactory.SSLHostnameVerifier;
import com.marklogic.client.FailedRequestException;
import com.marklogic.client.ForbiddenUserException;
import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.client.ResourceNotResendableException;
import com.marklogic.client.document.DocumentDescriptor;
import com.marklogic.client.document.DocumentManager.Metadata;
import com.marklogic.client.extensions.ResourceServices.ServiceResultIterator;
import com.marklogic.client.io.marker.AbstractReadHandle;
import com.marklogic.client.io.marker.AbstractWriteHandle;
import com.marklogic.client.io.marker.DocumentMetadataReadHandle;
import com.marklogic.client.io.marker.DocumentMetadataWriteHandle;
import com.marklogic.client.query.DeleteQueryDefinition;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.QueryManager.QueryView;
import com.marklogic.client.query.ValuesDefinition;
import com.marklogic.client.query.ValuesListDefinition;
import com.marklogic.client.util.RequestLogger;
import com.marklogic.client.util.RequestParameters;
import com.sun.jersey.api.client.ClientResponse;

public interface RESTServices {
	public void connect(String host, int port, String user, String password, Authentication type,
			SSLContext context, SSLHostnameVerifier verifier);
	public void release();

	public void deleteDocument(RequestLogger logger, DocumentDescriptor desc, String transactionId,
			Set<Metadata> categories)
		throws ResourceNotFoundException, ForbiddenUserException, FailedRequestException;

	public boolean getDocument(RequestLogger logger, DocumentDescriptor desc, String transactionId,
			Set<Metadata> categories, RequestParameters extraParams,
			DocumentMetadataReadHandle metadataHandle, AbstractReadHandle contentHandle)
		throws ResourceNotFoundException, ForbiddenUserException,  FailedRequestException;

	public DocumentDescriptor head(RequestLogger logger, String uri, String transactionId)
		throws ForbiddenUserException, FailedRequestException;

	public void putDocument(RequestLogger logger, DocumentDescriptor desc, String transactionId,
			Set<Metadata> categories, RequestParameters extraParams,
			DocumentMetadataWriteHandle metadataHandle, AbstractWriteHandle contentHandle)
		throws ResourceNotFoundException, ResourceNotResendableException,
			ForbiddenUserException, FailedRequestException;

    public <T> T search(RequestLogger logger, Class <T> as, QueryDefinition queryDef, String mimetype, long start,
            long len, QueryView view, String transactionId)
    	throws ForbiddenUserException, FailedRequestException;

    public void deleteSearch(RequestLogger logger, DeleteQueryDefinition queryDef, String transactionId)
            throws ForbiddenUserException, FailedRequestException;

    public String openTransaction(String name, int timeLimit)
		throws ForbiddenUserException, FailedRequestException;
	public void commitTransaction(String transactionId)
		throws ForbiddenUserException, FailedRequestException;
	public void rollbackTransaction(String transactionId)
		throws ForbiddenUserException, FailedRequestException;

    public <T> T values(Class <T> as, ValuesDefinition valdef, String mimetype, String transactionId)
            throws ForbiddenUserException, FailedRequestException;

    public <T> T valuesList(Class <T> as, ValuesListDefinition valdef, String mimetype, String transactionId)
            throws ForbiddenUserException, FailedRequestException;

    public <T> T optionsList(Class <T> as, String mimetype, String transactionId)
            throws ForbiddenUserException, FailedRequestException;

    // namespaces, etc.
	public <T> T getValue(RequestLogger logger, String type, String key,
			boolean isNullable, String mimetype, Class<T> as)
		throws ResourceNotFoundException, ForbiddenUserException, FailedRequestException;
	public <T> T getValues(RequestLogger logger, String type, String mimetype, Class<T> as)
		throws ForbiddenUserException, FailedRequestException;
	public void postValue(RequestLogger logger, String type, String key, String mimetype, Object value)
		throws ResourceNotResendableException, ForbiddenUserException, FailedRequestException;
	public void putValue(RequestLogger logger, String type, String key,
			String mimetype, Object value)
		throws ResourceNotFoundException, ResourceNotResendableException, ForbiddenUserException,
			FailedRequestException;
	public void putValue(RequestLogger logger, String type, String key, RequestParameters extraParams,
			String mimetype, Object value)
		throws ResourceNotFoundException, ResourceNotResendableException, ForbiddenUserException,
			FailedRequestException;
	public void deleteValue(RequestLogger logger, String type, String key)
		throws ResourceNotFoundException, ForbiddenUserException, FailedRequestException;
	public void deleteValues(RequestLogger logger, String type)
		throws ForbiddenUserException, FailedRequestException;

	public <R extends AbstractReadHandle> R getResource(RequestLogger reqlog, String path, RequestParameters params, R output)
		throws ResourceNotFoundException, ForbiddenUserException, FailedRequestException;
	public ServiceResultIterator getIteratedResource(
			RequestLogger reqlog, String path, RequestParameters params, String... mimetypes)
		throws ResourceNotFoundException, ForbiddenUserException, FailedRequestException;

	public <R extends AbstractReadHandle> R putResource(
			RequestLogger reqlog, String path, RequestParameters params,
			AbstractWriteHandle input, R output)
		throws ResourceNotFoundException, ResourceNotResendableException, ForbiddenUserException,
			FailedRequestException;
	public <R extends AbstractReadHandle, W extends AbstractWriteHandle> R putResource(
			RequestLogger reqlog, String path, RequestParameters params,
			W[] input, R output)
		throws ResourceNotFoundException, ResourceNotResendableException, ForbiddenUserException,
			FailedRequestException;

	public <R extends AbstractReadHandle> R postResource(
			RequestLogger reqlog, String path, RequestParameters params,
			AbstractWriteHandle input, R output)
		throws ResourceNotFoundException, ResourceNotResendableException, ForbiddenUserException,
			FailedRequestException;
	public <R extends AbstractReadHandle, W extends AbstractWriteHandle> R postResource(
			RequestLogger reqlog, String path, RequestParameters params,
			W[] input, R output)
		throws ResourceNotFoundException, ResourceNotResendableException, ForbiddenUserException,
			FailedRequestException;
	public ServiceResultIterator postIteratedResource(
			RequestLogger reqlog, String path, RequestParameters params,
			AbstractWriteHandle input, String... outputMimetypes)
		throws ResourceNotFoundException, ResourceNotResendableException, ForbiddenUserException,
			FailedRequestException;
	public <W extends AbstractWriteHandle> ServiceResultIterator postIteratedResource(
			RequestLogger reqlog, String path, RequestParameters params,
			W[] input, String... outputMimetypes)
		throws ResourceNotFoundException, ResourceNotResendableException, ForbiddenUserException,
			FailedRequestException;

	public <R extends AbstractReadHandle> R deleteResource(
			RequestLogger reqlog, String path, RequestParameters params, R output)
		throws  ResourceNotFoundException, ForbiddenUserException, FailedRequestException;

	// backdoor
	public Object getClientImplementation();

	public enum ResponseStatus {
		OK() {
			public boolean isExpected(ClientResponse.Status status) {
				return status == ClientResponse.Status.OK;
			}
		},
		CREATED() {
			public boolean isExpected(ClientResponse.Status status) {
				return status == ClientResponse.Status.CREATED;
			}
		},
		NO_CONTENT() {
			public boolean isExpected(ClientResponse.Status status) {
				return status == ClientResponse.Status.NO_CONTENT;
			}
		},
		CREATED_OR_NO_CONTENT() {
			public boolean isExpected(ClientResponse.Status status) {
				return (status == ClientResponse.Status.CREATED ||
						status == ClientResponse.Status.NO_CONTENT);
			}
		},
		SEE_OTHER() {
			public boolean isExpected(ClientResponse.Status status) {
				return status == ClientResponse.Status.SEE_OTHER;
			}
		};
		public boolean isExpected(ClientResponse.Status status) {
			return false;
		}
	}
}
