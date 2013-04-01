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
	private TimeOutHelper timeOutHelper;

	/**
	 * 
	 * @param observer
	 * @param timeOutHelper
	 */
	public ProxyService(Observer observer, TimeOutHelper timeOutHelper) {
		this.observer = observer;
		this.timeOutHelper = timeOutHelper;
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
	public TimeOutHelper getTimeOutHelper() {
		return timeOutHelper;
	}

	/**
	 * 
	 */
	public void startTimer() {
		timeOutHelper.startTimer();
	}

	/**
	 * 
	 */
	public void cancelTimer() {
		timeOutHelper.cancelTimer();
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
		return timeOutHelper.getAbortFlag();
	}
}
