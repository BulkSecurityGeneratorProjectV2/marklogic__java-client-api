/*
 * Copyright 2020 MarkLogic Corporation
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

package com.marklogic.client.test.datamovement;

import com.marklogic.client.datamovement.Splitter;
import com.marklogic.client.datamovement.UnarySplitter;
import com.marklogic.client.document.DocumentWriteOperation;
import com.marklogic.client.io.InputStreamHandle;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class UnarySplitterTest {
    static final private String xmlFile = "src/test/resources/data" + File.separator + "/pathSplitter/people.xml";
    static final private String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!--\n" +
            " Copyright 2020 MarkLogic Corporation\n" +
            "\n" +
            " Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
            " you may not use this file except in compliance with the License.\n" +
            " You may obtain a copy of the License at\n" +
            "\n" +
            "    http://www.apache.org/licenses/LICENSE-2.0\n" +
            "\n" +
            " Unless required by applicable law or agreed to in writing, software\n" +
            " distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
            " WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
            " See the License for the specific language governing permissions and\n" +
            " limitations under the License.\n" +
            "-->\n" +
            "<people xmlns=\"http://www.marklogic.com/people/\">\n" +
            "<person president=\"yes\"><first>George</first><last>Washington</last></person>\n" +
            "<person president=\"no\"><first>Betsy</first><last>Ross</last></person>\n" +
            "<skip>\n" +
            "  <person president=\"yes\"><first>John</first><last>Kennedy</last></person>\n" +
            "</skip>\n" +
            "</people>";
    static final private String jsonObjectFile = "src/test/resources/data" + File.separator + "/pathSplitter/JsonSplitterObject.json";
    static final private String jsonContent = "{\n" +
            "  \"context\": [\n" +
            "    {\"record\": \"first record\"},\n" +
            "    {\"record\": \"second record\"}\n" +
            "  ],\n" +
            "  \"context1\": {\"record\": \"ninth record\"},\n" +
            "  \"context2\": [\n" +
            "    {\"record\": \"third record\"},\n" +
            "    [\n" +
            "      {\"record\": \"forth record\"},\n" +
            "      {\"record\": \"fifth record\"}\n" +
            "    ],\n" +
            "    {\"context\": [\n" +
            "      {\"record\": \"sixth record\"},\n" +
            "      {\"record\": \"seventh record\"}\n" +
            "    ]}\n" +
            "  ]\n" +
            "}";

//    @Test
//    public void testUnarySplitterDocWrite() throws Exception {
//expect exception
//        UnarySplitter splitter = new UnarySplitter();
//        FileInputStream fileInputStream = new FileInputStream(new File(xmlFile));
//
//        Stream<DocumentWriteOperation> contentStream = splitter.splitWriteOperations(fileInputStream);
//        assertNotNull(contentStream);
//
//        Iterator<DocumentWriteOperation> itr = contentStream.iterator();
//        while (itr.hasNext()) {
//            DocumentWriteOperation docOp = itr.next();
//            String uri = docOp.getUri();
//            assertNotNull(docOp.getUri());
//
//            assertNotNull(docOp.getContent());
//            String docOpContent = docOp.getContent().toString();
//            assertEquals(docOpContent, xmlContent);
//        }
//        assertTrue(splitter.getCount() <= 1);
//    }

    @Test
    public void testUnarySplitterDocWriteWithName() throws Exception {

        UnarySplitter splitter = new UnarySplitter();
        FileInputStream fileInputStream = new FileInputStream(new File(jsonObjectFile));
        Splitter.UriMaker uriMaker1 = splitter.getUriMaker();
        Stream<DocumentWriteOperation> contentStream = splitter.splitWriteOperations(fileInputStream, "TestJson.json");
        Splitter.UriMaker uriMaker = splitter.getUriMaker();
        uriMaker.setInputAfter("/FilePath/");
        uriMaker.setInputName("NewTestJson.json");
        assertNotNull(contentStream);


        Iterator<DocumentWriteOperation> itr = contentStream.iterator();
        while (itr.hasNext()) {
            DocumentWriteOperation docOp = itr.next();
            String uri = docOp.getUri();
            assertNotNull(docOp.getUri());

            assertNotNull(docOp.getContent());
            String docOpContent = docOp.getContent().toString();
            assertEquals(docOpContent, jsonContent);
        }
        assertTrue(splitter.getCount() <= 1);
    }

    @Test
    public void testUnarySplitter() throws Exception {

        UnarySplitter splitter = new UnarySplitter();
        FileInputStream fileInputStream = new FileInputStream(new File(jsonObjectFile));
        Stream<InputStreamHandle> contentStream = splitter.split(fileInputStream);
        assertNotNull(contentStream);

        Iterator<InputStreamHandle> itr = contentStream.iterator();
        while (itr.hasNext()) {
            InputStreamHandle docOp = itr.next();

            assertNotNull(docOp.toString());
            String content = docOp.toString();
            assertEquals(content, jsonContent);
        }
        assertTrue(splitter.getCount() <= 1);
    }
}
