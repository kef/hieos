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

package com.vangent.hieos.services.xds.bridge.utils;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-23
 * @author         Jim Horner
 */
public class UUIDUtilsTest {

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void isUUIDTest() throws Exception {
    
        String teststr = "2.25.3.5.6.777789897";
        
        assertFalse(UUIDUtils.isUUID(teststr));
        
        teststr = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6";
        assertTrue(UUIDUtils.isUUID(teststr));
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void toOIDTest() throws Exception {

        UUID uuid = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");

        assertEquals("2.25.329800735698586629295641978511506172918",
                     UUIDUtils.toOID(uuid));
    }
}
