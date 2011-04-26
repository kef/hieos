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

import org.apache.commons.io.IOUtils;

import com.vangent.hieos.xutil.exception.XConfigException;
import com.vangent.hieos.xutil.xconfig.XConfig;
import com.vangent.hieos.xutil.xconfig.XConfigActor;
import com.vangent.hieos.xutil.xconfig.XConfigObject;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class ServletUtilMixin {

	static final String XCONFIG_FILE = "/resources/xconfig.xml";
	private ServletContext servletContext;

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
