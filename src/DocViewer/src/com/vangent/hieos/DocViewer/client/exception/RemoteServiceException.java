/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2013 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.DocViewer.client.exception;

import java.io.Serializable;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class RemoteServiceException extends Exception implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1074715163184551727L;
	private String message;

	/**
	 * 
	 * @param message
	 */
	public RemoteServiceException() {
	}

	/**
	 * 
	 * @param message
	 */
	public RemoteServiceException(String message) {
		this.message = message;
	}

	/**
	 * 
	 */
	public String getMessage() {
		return this.message;
	}

}
