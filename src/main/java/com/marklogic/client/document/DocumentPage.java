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

import com.marklogic.client.Page;
import com.marklogic.client.io.marker.AbstractReadHandle;

import java.io.Closeable;

/** Allows iteration over documents in the page.  When you finish with this instance 
 * you must call close() to free the underlying resources. */
public interface DocumentPage extends Page<DocumentRecord>, Closeable {
    /** Convenience method combines the functionality of Page.next() and DocumentRecord.getContent(). */
    public <T extends AbstractReadHandle> T nextContent(T contentHandle);
    /** Frees the underlying resources, including the http connection. */
    public void close();
}
