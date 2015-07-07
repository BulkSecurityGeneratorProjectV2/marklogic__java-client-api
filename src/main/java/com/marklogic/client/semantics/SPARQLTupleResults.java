/*
 * Copyright 2012-2015 MarkLogic Corporation
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
package com.marklogic.client.semantics;

import com.marklogic.client.io.marker.SPARQLReadHandle;

/**
 * A tuples result from {@link SPARQLQueryManager#executeSelect}
 * For details about semantics in MarkLogic see
 * {@link https://docs.marklogic.com/guide/semantics Semantics Developer's Guide}
 */
public interface SPARQLTupleResults extends Iterable<SPARQLTuple>, SPARQLReadHandle {
    public String[] getBindingNames();
    public long size();
}
