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
package com.marklogic.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marklogic.client.FailedRequestException;
import com.marklogic.client.ForbiddenUserException;
import com.marklogic.client.MarkLogicInternalException;
import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.client.ResourceNotResendableException;
import com.marklogic.client.DatabaseClientFactory.HandleFactoryRegistry;
import com.marklogic.client.admin.ExtensionLibrariesManager;
import com.marklogic.client.admin.NamespacesManager;
import com.marklogic.client.admin.QueryOptionsManager;
import com.marklogic.client.admin.ResourceExtensionsManager;
import com.marklogic.client.admin.ServerConfigurationManager;
import com.marklogic.client.admin.TransformExtensionsManager;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.OutputStreamHandle;
import com.marklogic.client.io.OutputStreamSender;

class ServerConfigurationManagerImpl
	implements ServerConfigurationManager, OutputStreamSender
{
	static final private Logger logger = LoggerFactory.getLogger(ServerConfigurationManagerImpl.class);

	static final private String REST_API_NS = "http://marklogic.com/rest-api";

	private Boolean validatingQueries;
	private Boolean validatingQueryOptions;
	private String  defaultDocumentReadTransform;
	private Boolean defaultDocumentReadTransformAll;
	private Boolean serverRequestLogging;
	private Policy  contentVersions;
	private Format  errorFormat;

    private RESTServices          services;
	private HandleFactoryRegistry handleRegistry;

	public ServerConfigurationManagerImpl(RESTServices services) {
		super();
		this.services = services;
	}

	HandleFactoryRegistry getHandleRegistry() {
		return handleRegistry;
	}
	void setHandleRegistry(HandleFactoryRegistry handleRegistry) {
		this.handleRegistry = handleRegistry;
	}

	@Override
	public void readConfiguration()
	throws ForbiddenUserException, FailedRequestException {
		try {
			if (logger.isInfoEnabled())
				logger.info("Reading server configuration");

			InputStream stream = services.getValues(null, "config/properties", "application/xml", InputStream.class);
			if (stream == null)
				return;

			XMLInputFactory factory = XMLInputFactory.newFactory();
			factory.setProperty("javax.xml.stream.isNamespaceAware", true);
			factory.setProperty("javax.xml.stream.isValidating",     false);

			XMLStreamReader reader = factory.createXMLStreamReader(stream);

			validatingQueries               = null;
			validatingQueryOptions          = null;
			defaultDocumentReadTransform    = null;
			defaultDocumentReadTransformAll = null;
			serverRequestLogging            = null;
			contentVersions                 = null;

			while (reader.hasNext()) {
				if (reader.next() != XMLStreamReader.START_ELEMENT)
					continue;

				String localName = reader.getLocalName();
				if ("validate-queries".equals(localName)) {
					validatingQueries = Boolean.valueOf(reader.getElementText());
				} else if ("validate-options".equals(localName)) {
					validatingQueryOptions = Boolean.valueOf(reader.getElementText());
				} else if ("document-transform-out".equals(localName)) {
					defaultDocumentReadTransform = reader.getElementText();
				} else if ("document-transform-all".equals(localName)) {
					defaultDocumentReadTransformAll = Boolean.valueOf(reader.getElementText());
				} else if ("debug".equals(localName)) {
					serverRequestLogging = Boolean.valueOf(reader.getElementText());
				} else if ("content-versions".equals(localName)) {
					contentVersions = Enum.valueOf(Policy.class, reader.getElementText().toUpperCase());
				} else if ("error-format".equals(localName)) {
					errorFormat = Format.valueOf(reader.getElementText().toUpperCase());
				}
			}

			reader.close();
		} catch (XMLStreamException e) {
			logger.error("Failed to read server configuration stream",e);
			throw new MarkLogicInternalException(e);
		}
	}

	@Override
	public void writeConfiguration()
	throws ResourceNotFoundException, ResourceNotResendableException, ForbiddenUserException, FailedRequestException {
		if (logger.isInfoEnabled())
			logger.info("Writing server configuration");

		OutputStreamHandle handle = new OutputStreamHandle(this);
		handle.setResendable(true);

		services.putValue(null, "config/properties", null, "application/xml", handle);
	}
	@Override
	public void write(OutputStream out) throws IOException {
		try {
			XMLOutputFactory factory = XMLOutputFactory.newFactory();
			factory.setProperty("javax.xml.stream.isRepairingNamespaces", true);

			XMLStreamWriter serializer = factory.createXMLStreamWriter(out, "utf-8");

			serializer.writeStartElement(REST_API_NS, "properties");

			if (validatingQueries != null) {
				serializer.writeStartElement(REST_API_NS, "validate-queries");
				serializer.writeCharacters(validatingQueries.toString());
				serializer.writeEndElement();
			}
			if (validatingQueryOptions != null) {
				serializer.writeStartElement(REST_API_NS, "validate-options");
				serializer.writeCharacters(validatingQueryOptions.toString());
				serializer.writeEndElement();
			}
			if (defaultDocumentReadTransform != null) {
				serializer.writeStartElement(REST_API_NS, "document-transform-out");
				serializer.writeCharacters(defaultDocumentReadTransform);
				serializer.writeEndElement();
			}
			if (defaultDocumentReadTransformAll != null) {
				serializer.writeStartElement(REST_API_NS, "document-transform-all");
				serializer.writeCharacters(defaultDocumentReadTransformAll.toString());
				serializer.writeEndElement();
			}
			if (serverRequestLogging != null) {
				serializer.writeStartElement(REST_API_NS, "debug");
				serializer.writeCharacters(serverRequestLogging.toString());
				serializer.writeEndElement();
			}
			if (contentVersions != null) {
				serializer.writeStartElement(REST_API_NS, "content-versions");
				serializer.writeCharacters(contentVersions.name().toLowerCase());
				serializer.writeEndElement();
			}
			if (errorFormat != null) {
				serializer.writeStartElement(REST_API_NS, "error-format");
				serializer.writeCharacters(errorFormat.name().toLowerCase());
				serializer.writeEndElement();
			}

//			serializer.writeEndElement();

			serializer.writeEndDocument();
		} catch (XMLStreamException e) {
			logger.error("Failed to write server configuration stream",e);
			throw new MarkLogicInternalException(e);
		}
	}

	@Override
	public Boolean getQueryOptionValidation() {
		return validatingQueryOptions;
	}
	@Override
	public void setQueryOptionValidation(Boolean on) {
		validatingQueryOptions = on;
	}

	@Override
	public Boolean getQueryValidation() {
		return validatingQueries;
	}
	@Override
	public void setQueryValidation(Boolean on) {
		validatingQueries = on;
	}
	
	@Override
	public String getDefaultDocumentReadTransform() {
		return defaultDocumentReadTransform;
	}
	@Override
	public void setDefaultDocumentReadTransform(String name) {
		defaultDocumentReadTransform = name;
	}

	@Override
	public Boolean getDefaultDocumentReadTransformAll() {
		return defaultDocumentReadTransformAll;
	}
	@Override
	public void setDefaultDocumentReadTransformAll(Boolean on) {
		defaultDocumentReadTransformAll = on;
	}

	@Override
	public Boolean getServerRequestLogging() {
		return serverRequestLogging;
	}
	@Override
	public void setServerRequestLogging(Boolean on) {
		serverRequestLogging = on;
	}

	@Override
	public Policy getContentVersionRequests() {
		return contentVersions;
	}
	@Override
	public void setContentVersionRequests(Policy policy) {
		contentVersions = policy;
	}

	@Override
	public ExtensionLibrariesManager newExtensionLibrariesManager() {
		ExtensionLibrariesManagerImpl extensionMgr =
			new ExtensionLibrariesManagerImpl(services);
		extensionMgr.setHandleRegistry(getHandleRegistry());
		return extensionMgr;
	}
	@Override
    public NamespacesManager newNamespacesManager() {
    	return new NamespacesManagerImpl(services);
    }
	@Override
	public QueryOptionsManager newQueryOptionsManager() {
		QueryOptionsManagerImpl optMgr = new QueryOptionsManagerImpl(services);
		optMgr.setHandleRegistry(getHandleRegistry());
		return optMgr;
	}
	@Override
	public ResourceExtensionsManager newResourceExtensionsManager() {
		ResourceExtensionsImpl resourceExtensionMgr = new ResourceExtensionsImpl(services);
		resourceExtensionMgr.setHandleRegistry(getHandleRegistry());
		return resourceExtensionMgr;
	}
	@Override
	public TransformExtensionsManager newTransformExtensionsManager() {
		TransformExtensionsImpl transformExtensionMgr = new TransformExtensionsImpl(services);
		transformExtensionMgr.setHandleRegistry(getHandleRegistry());
		return transformExtensionMgr;
	}
	
	@Override
	public Format getErrorFormat() {
		return errorFormat;
	}
	@Override
	public void setErrorFormat(Format errorFormat) {
		if (errorFormat == Format.JSON || errorFormat == Format.XML) 
			this.errorFormat = errorFormat;
		else
			throw new IllegalArgumentException("The only supported values for error format are JSON and XML.");
	}
}
