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
package com.vangent.hieos.DocViewer.client.services.proxy;

import com.vangent.hieos.DocViewer.client.helper.Observer;
import com.vangent.hieos.DocViewer.client.helper.TimeOutHelper;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class ProxyService {
	private Observer observer;
	private TimeOutHelper progressHelper;

	/**
	 * 
	 * @param observer
	 * @param progressHelper
	 */
	public ProxyService(Observer observer, TimeOutHelper progressHelper) {
		this.observer = observer;
		this.progressHelper = progressHelper;
	}

	/**
	 * 
	 * @return
	 */
	public Observer getObserver() {
		return observer;
	}

	/**
	 * 
	 * @return
	 */
	public TimeOutHelper getProgressHelper() {
		return progressHelper;
	}

	/**
	 * 
	 */
	public void startTimer() {
		progressHelper.startTimer();
	}

	/**
	 * 
	 */
	public void cancelTimer() {
		progressHelper.cancelTimer();
	}

	/**
	 * 
	 * @param object
	 */
	public void update(Object object) {
		observer.update(object);
	}

	/**
	 * 
	 * @return
	 */
	public boolean getAbortFlag() {
		return progressHelper.getAbortFlag();
	}
}
