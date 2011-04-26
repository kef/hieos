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

import com.vangent.hieos.xutil.xconfig.XConfigObject;

/**
 *
 * @author Anand Sastry
 */
public class AuthenticationHandlerFactory {

    private static final String AUTH_HANDLER_CLASS_IMPL = "AuthHandlerClassImpl";

    /**
     *
     * @param config
     * @return
     * @throws AuthUtilException
     */
    public static synchronized AuthenticationHandler getHandler(XConfigObject config) throws AuthUtilException {
        String authHandlerClassImpl = config.getProperty(AUTH_HANDLER_CLASS_IMPL);
        Class authHandlerClass;
        try {
            authHandlerClass = Class.forName(authHandlerClassImpl);
        } catch (ClassNotFoundException ex) {
            throw new AuthUtilException("Unable to load AuthenticationHandler class: " + ex.getMessage());
        }

        AuthenticationHandler authenticationHandler;
        try {
            authenticationHandler = (AuthenticationHandler) authHandlerClass.newInstance();
            authenticationHandler.setConfig(config);
        } catch (InstantiationException ex) {
            throw new AuthUtilException("Unable to instantiate AuthenticationHandler: " + ex.getMessage());
        } catch (IllegalAccessException ex) {
            throw new AuthUtilException("Unable to instantiate AuthenticationHandler: " + ex.getMessage());
        }
        return authenticationHandler;
    }
}
