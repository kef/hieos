/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2008-2009 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vangent.hieos.xtest.config;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * XTestConfigRegistry encapsulates a Registry definition.
 *
 * @author Anand Sastry
 */
public class XTestConfigRegistry extends XTestConfigActor {
    /**
     * 
     * @return
     */
    @Override
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("\n Registry:");
        sbuf.append("\n   name: " + this.getName());
        sbuf.append("\n   uniqueId: " + this.getUniqueId());
        ArrayList<XTestConfigTransaction> al = this.getTransactions();
        for (Iterator it = al.iterator(); it.hasNext(); ) {
            XTestConfigTransaction txn = (XTestConfigTransaction) it.next();
            sbuf.append(txn.toString());
        }
        return sbuf.toString();
    }
}
