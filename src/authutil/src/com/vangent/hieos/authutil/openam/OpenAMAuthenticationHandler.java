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

package com.vangent.hieos.authutil.openam;

import com.vangent.hieos.authutil.framework.AuthenticationHandler;
import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.authutil.model.Credentials;
import com.vangent.hieos.xutil.xconfig.XConfigObject;

/**
 *
 * @author sastan
 */
public class OpenAMAuthenticationHandler implements AuthenticationHandler {

    public AuthenticationContext authenticate(Credentials creds) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setConfig(XConfigObject config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
