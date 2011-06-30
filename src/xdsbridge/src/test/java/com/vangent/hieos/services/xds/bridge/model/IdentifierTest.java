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

package com.vangent.hieos.services.xds.bridge.model;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-28
 * @author         Jim Horner    
 */
public class IdentifierTest {

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void equalsTest() throws Exception {
    
        Identifier id1 = new Identifier();
        id1.setRoot("555");
        id1.setExtension("555");
        
        assertTrue(id1.equals(id1));
        assertEquals(id1, id1);
        
        Identifier id2 = new Identifier();
        id2.setRoot("555");
        id2.setExtension("555");
        
        assertTrue(id1.equals(id1));
        assertEquals(id1, id2);
    
        id1.setExtension(null);
        id2.setExtension(null);
        
        assertEquals(id1, id2);
        
        id2.setRoot("556");
        assertFalse(id1.equals(id2));
        
        assertFalse(id1.equals(null));
        assertFalse(id1.equals(new Identifier()));
    }
}
