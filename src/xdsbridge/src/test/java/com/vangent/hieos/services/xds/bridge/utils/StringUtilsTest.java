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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-13
 * @author         Jim Horner
 */
public class StringUtilsTest {

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void substringAfterLastTest() throws Exception {

        String pid = "1.2.36.1.2001.1003.0.8003601234512345";

        String extension = StringUtils.substringAfterLast(pid, ".");

        assertEquals("8003601234512345", extension);

        // let's try some tricky things
        assertEquals(null, StringUtils.substringAfterLast(null, null));
        assertEquals("", StringUtils.substringAfterLast(".", null));
        assertEquals("", StringUtils.substringAfterLast("fred", "."));
        assertEquals("bc", StringUtils.substringAfterLast("abc", "a"));
        assertEquals("", StringUtils.substringAfterLast("abc", "c"));
    }

    /**
     * Method description
     *
     *
     * @throws Exception
     */
    @Test
    public void substringBeforeLastTest() throws Exception {

        String pid = "1.2.36.1.2001.1003.0.8003601234512345";

        String root = StringUtils.substringBeforeLast(pid, ".");

        assertEquals("1.2.36.1.2001.1003.0", root);

        // let's try some tricky things
        assertEquals(null, StringUtils.substringBeforeLast(null, null));
        assertEquals("", StringUtils.substringBeforeLast(".", null));
        assertEquals("", StringUtils.substringBeforeLast("fred", "."));
        assertEquals("", StringUtils.substringBeforeLast("abc", "a"));
        assertEquals("ab", StringUtils.substringBeforeLast("abc", "c"));
    }
}
