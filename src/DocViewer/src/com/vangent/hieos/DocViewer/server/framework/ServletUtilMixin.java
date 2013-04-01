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
package com.vangent.hieos.DocViewer.server.framework;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;

import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class ServletUtilMixin {

	private static final String XCONFIG_FILE = "/resources/xconfig.xml";
	public static final String SESSION_PROPERTY_AUTH_STATUS = "auth_status";
	public static final String SESSION_PROPERTY_AUTH_CREDS = "auth_creds";
	public static final String SESSION_PROPERTY_AUTH_CONTEXT = "auth_context";
	private ServletContext servletContext;

	/**
	 * 
	 * @param servletContext
	 */
	public void init(ServletContext servletContext) {
		this.servletContext = servletContext;
		// FIXME: ? This may happen more than once, but likely OK since
		// all servlets share the same "xconfig.xml".
		String xConfigRealPath = servletContext
				.getRealPath(ServletUtilMixin.XCONFIG_FILE);
		System.out.println("Real Path: " + xConfigRealPath);
		XConfig.setConfigLocation(xConfigRealPath);
	}

	/**
	 * 
	 * @return
	 */
	public ServletContext getServletContext() {
		return this.servletContext;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	static public boolean isValidSession(HttpServletRequest request) {
		boolean validSession = false;

		// Get session.
		HttpSession session = request.getSession(false);
		if (session != null) {

			// See if we have a valid session.
			String loginSuccess = (String) session
					.getAttribute(SESSION_PROPERTY_AUTH_STATUS);
			if (loginSuccess == null || !loginSuccess.equals("true")) {
				// Do not continue.
				validSession = false;
			} else {
				// Continue.
				validSession = true;
			}
		}
		return validSession;
	}

	/**
	 * 
	 * @param request
	 */
	static public void invalidateSession(HttpServletRequest request) {
		// Get session and invalidate.
		HttpSession session = request.getSession();
		session.invalidate();
	}

	/**
	 * 
	 * @param servletContext
	 * @param key
	 * @return
	 */
	public String getProperty(String key) {
		String value = null;
		XConfigObject configObject = this.getConfig();
		if (configObject != null) {
			value = configObject.getProperty(key);
		}
		return value;
	}

	public String getProperty(String key, String defaultString) {
		String value = null;
		XConfigObject configObject = this.getConfig();
		if (configObject != null) {
			value = configObject.getProperty(key);
			if (StringUtils.isBlank(value)) {
				value = defaultString;
			}
		}
		return value;
	}

	/**
	 * 
	 * @param templateFilename
	 * @return
	 */
	public String getTemplateString(String templateFilename) {
		InputStream is = servletContext.getResourceAsStream("/resources/"
				+ templateFilename);
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(is, writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writer.toString();
	}

	/**
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	public XConfigActor getActorConfig(String name, String type) {
		XConfigActor actorConfig = null;
		XConfigObject configObject = this.getConfig();
		actorConfig = (XConfigActor) configObject.getXConfigObjectWithName(
				name, type);
		return actorConfig;
	}

	/**
	 * 
	 * @return
	 */
	public XConfigObject getConfig() {
		XConfigObject config = null;
		try {
			XConfig xconf = XConfig.getInstance();
			config = xconf.getXConfigObjectByName("DocViewerProperties",
					"DocViewerPropertiesType");

		} catch (XConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return config;
	}
}
