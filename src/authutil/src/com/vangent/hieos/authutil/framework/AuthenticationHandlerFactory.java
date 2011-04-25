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

/**
 *
 * @author Anand Sastry
 */

public class AuthenticationHandlerFactory {

    /**
     * 
     * @return
     * @throws AuthUtilException
     */
    public static synchronized AuthenticationHandler getHandler() throws AuthUtilException {
        String authHandlerName = "";
        // get from config file. If not available, failover to System Property
        if (authHandlerName == null || authHandlerName.trim().length() == 0) {
            authHandlerName = System.getProperty("com.vangent.hieos.authutil.AuthenticationHandler");
            try {
                return (AuthenticationHandler) Class.forName(authHandlerName).newInstance();
            } catch (Exception e) {
                throw new AuthUtilException ("Error instantiating handler, " + authHandlerName + ", ", e);
            }
        }
        return null;
    }


}
