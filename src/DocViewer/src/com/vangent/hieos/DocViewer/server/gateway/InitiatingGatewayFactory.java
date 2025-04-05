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
package com.vangent.hieos.DocViewer.server.gateway;

import com.vangent.hieos.DocViewer.client.model.config.ConfigDTO;
import com.vangent.hieos.DocViewer.server.framework.ServletUtilMixin;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class InitiatingGatewayFactory {
	public enum InitiatingGatewayType {
		NHIN_EXCHANGE, IHE,
	};
	
	/**
	 * 
	 */
	private InitiatingGatewayFactory()
	{
		// Do not allow instantiation.
	}
	
	/**
	 * 
	 * @param searchMode
	 * @return
	 */
	public static InitiatingGateway getInitiatingGateway(String searchMode, ServletUtilMixin servletUtil) {
		InitiatingGatewayFactory.InitiatingGatewayType igType;
		if (searchMode.equals(ConfigDTO.VAL_SEARCH_MODE_HIE)) {
			igType = InitiatingGatewayFactory.InitiatingGatewayType.IHE;

		} else {
			igType = InitiatingGatewayFactory.InitiatingGatewayType.NHIN_EXCHANGE;
		}
		return InitiatingGatewayFactory.getInitiatingGateway(igType,
				servletUtil);
	}

	/**
	 * 
	 * @param type
	 * @param servletUtil
	 * @return
	 */
	public static InitiatingGateway getInitiatingGateway(InitiatingGatewayType type,
			ServletUtilMixin servletUtil) {
		InitiatingGateway ig;
		if (type == InitiatingGatewayType.IHE) {
			ig = new IHEInitiatingGateway(servletUtil);
		} else {
			// Only other option ...
			ig = new NHINExchangeInitiatingGateway(servletUtil);
		}
		return ig;
	}
}
