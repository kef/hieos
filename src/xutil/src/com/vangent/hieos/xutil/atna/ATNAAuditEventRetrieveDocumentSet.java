/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2011 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.xutil.atna;

import java.util.ArrayList;
import java.util.List;

/**
 * Covers ITI-39 and ITI-43.
 *
 * @author Bernie Thuman
 */
public class ATNAAuditEventRetrieveDocumentSet extends ATNAAuditEvent {

    private List<ATNAAuditDocument> documents = new ArrayList<ATNAAuditDocument>();


    /**
     *
     * @return
     */
    public List<ATNAAuditDocument> getDocuments() {
        return documents;
    }

    /**
     * 
     * @param document
     */
    public void addDocument(ATNAAuditDocument document) {
        this.documents.add(document);
    }
}
