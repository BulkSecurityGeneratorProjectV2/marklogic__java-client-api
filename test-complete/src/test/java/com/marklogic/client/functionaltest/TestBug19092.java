/*
 * Copyright 2014-2015 MarkLogic Corporation
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

package com.marklogic.client.functionaltest;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.Authentication;
import com.marklogic.client.admin.QueryOptionsManager;
import com.marklogic.client.admin.config.QueryOptionsBuilder;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.QueryOptionsHandle;
import com.marklogic.client.io.StringHandle;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.*;
public class TestBug19092 extends BasicJavaClientREST {

	private static String dbName = "Bug19092DB";
	private static String [] fNames = {"Bug19092DB-1"};
	private static String restServerName = "REST-Java-Client-API-Server";
@BeforeClass
	public static void setUp() throws Exception 
	{
	  System.out.println("In setup");
	  setupJavaRESTServer(dbName, fNames[0],restServerName,8011);
	  setupAppServicesConstraint(dbName);
	}

@SuppressWarnings("deprecation")
@Test
	public void testBug19092() throws IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testBug19092");

		DatabaseClient client = DatabaseClientFactory.newClient("localhost", 8011, "rest-admin", "x", Authentication.DIGEST);

		// create query options manager
		QueryOptionsManager optionsMgr = client.newServerConfigManager().newQueryOptionsManager();
		
		// create query options builder
		QueryOptionsBuilder builder = new QueryOptionsBuilder();
		
		// create query options handle
        QueryOptionsHandle handle = new QueryOptionsHandle();
        
        // build query options
        handle.withTerm(builder.term("case-sensitive"));
        
        // write query options
        optionsMgr.writeOptions("DefaultTermOpt", handle);
        
        // read query option
     	StringHandle readHandle = new StringHandle();
     	readHandle.setFormat(Format.XML);
     	optionsMgr.readOptions("DefaultTermOpt", readHandle);
     	String output = readHandle.get();
     	System.out.println(output);
     	
     	assertTrue("Default term is not correct", output.contains("<ns2:term-option>case-sensitive</ns2:term-option>") || output.contains("<search:term-option>case-sensitive</search:term-option>"));
     	assertFalse("Weight element exists", output.contains("<ns2:weight>0.0</ns2:weight>") || output.contains("<search:weight>0.0</search:weight>"));
     	assertFalse("Default element exists", output.contains("<ns2:default/>") || output.contains("<search:default/>"));
     	
		// release client
		client.release();		
	}

@SuppressWarnings("deprecation")
@Test
	public void testBug19092WithJson() throws IOException, ParserConfigurationException, SAXException, XpathException, TransformerException
	{	
		System.out.println("Running testBug19092WithJson");

		DatabaseClient client = DatabaseClientFactory.newClient("localhost", 8011, "rest-admin", "x", Authentication.DIGEST);

		// create query options manager
		QueryOptionsManager optionsMgr = client.newServerConfigManager().newQueryOptionsManager();
		
		// create query options builder
		QueryOptionsBuilder builder = new QueryOptionsBuilder();
		
		// create query options handle
        QueryOptionsHandle handle = new QueryOptionsHandle();
        
        // build query options
        handle.withTerm(builder.term("case-sensitive"));
        
        // write query options
        optionsMgr.writeOptions("DefaultTermOpt", handle);
        
        // read query option
     	StringHandle readHandle = new StringHandle();
     	readHandle.setFormat(Format.JSON);
     	optionsMgr.readOptions("DefaultTermOpt", readHandle);
     	String output = readHandle.get();
     	System.out.println(output);
     	
     	assertTrue("Default term is not correct", output.contains("{\"options\":{\"term\":{\"term-option\":[\"case-sensitive\"]}}}"));
     	
		// release client
		client.release();		
	}
	@AfterClass
	public static void tearDown() throws Exception
	{
		System.out.println("In tear down");
		tearDownJavaRESTServer(dbName, fNames, restServerName);
		
	}
}
