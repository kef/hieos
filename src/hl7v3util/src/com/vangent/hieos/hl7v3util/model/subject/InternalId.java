/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2012 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.hl7v3util.model.subject;

/**
 *
 * @author Bernie Thuman
 */
public class InternalId implements Cloneable {

    private Long id;
    private int seqNo = 0;

    /**
     *
     */
    private InternalId() {
        // Do not allow.
    }

    /**
     *
     * @param id
     */
    public InternalId(Long id) {
        this.id = id;
    }

    /**
     *
     * @param id
     * @param seqNo
     */
    public InternalId(Long id, int seqNo) {
        this.id = id;
        this.seqNo = seqNo;
    }

    /**
     *
     * @return
     */
    public Long getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public int getSeqNo() {
        return seqNo;
    }

    /**
     *
     * @param seqNo
     */
    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    /**
     *
     * @param compareId
     * @return
     */
    public boolean equals(InternalId compareId) {
        return compareId.id.equals(id) && (compareId.seqNo == seqNo);
    }
}
