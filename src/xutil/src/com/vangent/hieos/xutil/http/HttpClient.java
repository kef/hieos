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

/*
 * HttpClient.java
 *
 * Created on September 29, 2003, 8:06 AM
 */
package com.vangent.hieos.xutil.http;

import com.vangent.hieos.xutil.exception.HttpCodeException;
import com.vangent.hieos.xutil.exception.XdsInternalException;
import com.vangent.hieos.xutil.iosupport.Io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * HttpClient handles all non-SOAP HTTP based requests.
 */
public class HttpClient implements HostnameVerifier {

    /**
     * Do not allow instantiation outside of this class.
     */
    private HttpClient() {
    }

    /**
     *
     * @param uri
     * @return
     * @throws XdsInternalException
     */
    public static String httpGet(String uri) throws XdsInternalException {
        try {
            URI u_uri = new URI(uri);
            HttpClient hc = new HttpClient();
            return hc.doRawGet(u_uri);
        } catch (URISyntaxException ex) {
            Logger.getLogger(HttpClient.class.getName()).log(Level.SEVERE, null, ex);
            throw HttpClient.getException(ex, uri);
        }
    }

    /**
     *
     * @param hostname
     * @param session
     * @return
     */
    public boolean verify(String hostname,
            SSLSession session) {
        return true;
    }

    /**
     *
     */
    private String doRawGet(URI uri)
            throws XdsInternalException {
        HttpURLConnection conn = null;
        String response = null;
        try {
            // Make a connection.
            URL url;
            try {
                url = uri.toURL();
            } catch (Exception e) {
                throw HttpClient.getException(e, uri.toString());
            }
            HttpsURLConnection.setDefaultHostnameVerifier(this); // call verify() above to validate hostnames
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "text/html, text/xml, text/plain, */*");
            conn.connect();  // Finally.

            // Now get the content.
            response = this.getResponse(conn);
        } catch (IOException e) {
            throw HttpClient.getException(e, uri.toString());
        } finally {
            // Now do any final cleanup.
            if (conn != null) {
                conn.disconnect();
            }
        }
        return response;
    }

    /**
     *
     * @param e
     * @param uri
     * @return
     */
    static private XdsInternalException getException(Exception e, String uri) {
        return new XdsInternalException("Error trying to retrieve " + uri + " : " + e.getMessage());
    }

    /**
     *
     * @param conn
     * @return
     * @throws XdsInternalException
     */
    private String getResponse(HttpURLConnection conn)
            throws XdsInternalException {
        String result = null;
        FilterInputStream in = null;
        String encoding = conn.getContentEncoding();
        try {
            if (encoding == null) {
                in = (FilterInputStream) conn.getInputStream();
            } else {
                Object o = conn.getContent();
                in = (FilterInputStream) o;
            }
            result = Io.getStringFromInputStream(in);
        } catch (IOException e) {
            try {
                int code = conn.getResponseCode();
                //System.out.println("ERROR: code: " + String.valueOf(code) + " message: " + conn.getResponseMessage());
                InputStream is = conn.getErrorStream();
                if (is == null) {
                    String msg = conn.getResponseMessage();
                    URL url = conn.getURL();
                    throw new XdsInternalException("Error retieving content of " + url.toString() + "; response was " + msg);
                } else {
                    StringBuffer b = new StringBuffer();
                    byte[] by = new byte[256];
                    while (is.read(by, 0, 256) > 0) {
                        b.append(new String(by)); // get junk at end, should be sensitive to number of bytes read
                    }
                    throw new XdsInternalException(
                            "ERROR: HttpClient: code: " + String.valueOf(code) +
                            " message: " + conn.getResponseMessage() + "\n" + new String(b) + "\n");
                }
            } catch (IOException ex) {
                URL url = conn.getURL();
                throw new XdsInternalException("Error retieving content of " + url.toString() + "; response was " + ex.getMessage());
            }
        } finally {
            // Now do any final cleanup.
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    // Ignore.
                    Logger.getLogger(HttpClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return result;
        //return conn.getHeaderField("Content-Type");
    }
}
