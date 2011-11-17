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
package com.vangent.hieos.DocViewer.client.view.patient;

import com.smartgwt.client.widgets.grid.HoverCustomizer;
import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 *
 * @author Vangent
 */
public class PatientListHoverCustomizer implements HoverCustomizer {

    /** Field description */
    private final String tooltip;

    /**
     * Constructs ...
     *
     *
     * @param str
     */
    public PatientListHoverCustomizer(String str) {

        super();
        this.tooltip = str;
    }

    /**
     * Method description
     *
     *
     * @param value
     * @param record
     * @param rowNum
     * @param colNum
     *
     * @return
     */
    @Override
    public String hoverHTML(Object value, ListGridRecord record, int rowNum,
                            int colNum) {
        return this.tooltip;
    }
}
