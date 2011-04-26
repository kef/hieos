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
package com.vangent.hieos.authutil.framework;

import com.vangent.hieos.authutil.model.AuthenticationContext;
import com.vangent.hieos.authutil.model.Credentials;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Anand Sastry
 */
public class AuthenticationService {

    private XConfigObject config;

    /**
     *
     * @param config
     */
    public AuthenticationService(XConfigObject config) {
        this.config = config;
    }

    /**
     *
     * @param creds
     * @param config
     * @return
     */
    public AuthenticationContext authenticate(Credentials creds) {
        try {
            return getHandler(config).authenticate(creds);
        } catch (AuthUtilException ex) {
            Logger.getLogger(AuthenticationService.class.getName()).log(Level.SEVERE, null, ex);
        }
        // If we get here, return Failed Authentication Context
        return getFailureAuthenticationContext();
    }

    /**
     *
     * @return
     */
    public static AuthenticationContext getFailureAuthenticationContext() {
        return new AuthenticationContext();
    }

    /**
     *
     * @param config
     * @return
     * @throws AuthUtilException
     */
    private AuthenticationHandler getHandler(XConfigObject config) throws AuthUtilException {
        try {
            return AuthenticationHandlerFactory.getHandler(config);
        } catch (Exception e) {
            throw new AuthUtilException("Configure handler via config file or system property", e);
        }
    }
}
