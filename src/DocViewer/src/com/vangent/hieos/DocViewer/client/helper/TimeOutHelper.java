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
package com.vangent.hieos.DocViewer.client.helper;

import com.google.gwt.user.client.Timer;
import com.smartgwt.client.util.SC;

/**
 * 
 * @author Bernie Thuman
 * 
 */
public class TimeOutHelper {
	static final int TIMEOUT_INTERVAL_MSECS = 15000; // 15 sec
	static final String PROMPT = "<img src=\"images/loadingSmall.gif\"\"/><span style=\"padding-left:10px;font-size:10pt;\">Searching ...</span>";

	private Timer timer = null;
	private int timeOutInterval = TIMEOUT_INTERVAL_MSECS;
	private boolean abortFlag = false;

	/**
	 * 
	 */
	public TimeOutHelper() {
		// Do nothing.
	}
	
	/**
	 * 
	 * @param timeOutInterval
	 */
	public void setTimeOutInterval(int timeOutInterval)
	{
		this.timeOutInterval = timeOutInterval;
	}

	/**
	 * 
	 * @return
	 */
	public boolean getAbortFlag() {
		return abortFlag;
	}

	/**
	 * 
	 */
	public void startTimer() {
		SC.showPrompt(PROMPT);
		if (timer != null) {
			// Should never happen??
			SC.warn("Command is already running!");
			return;
		}
		timer = new Timer() {
			public void run() {
				abortFlag = true; // TIMEOUT.
				SC.clearPrompt();
				SC.warn("Search request timed out!");
				cancel();  // Cancel the timer.
			}
		};
		abortFlag = false;
		// Start up timer.
		timer.schedule(timeOutInterval);
	}

	/**
	 * 
	 */
	public void cancelTimer() {
		SC.clearPrompt();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
}
