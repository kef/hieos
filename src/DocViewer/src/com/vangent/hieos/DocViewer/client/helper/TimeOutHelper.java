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
	static final int TIMEOUT_INTERVAL_MSECS = 300000; // 15 sec- changed from 15 sec to 300 sec to be compatible with 
	static final String DEFAULT_PROMPT = "Searching ....";
	private Timer timer = null;
	private int timeOutInterval = TIMEOUT_INTERVAL_MSECS;
	private boolean abortFlag = false;
	private String prompt = DEFAULT_PROMPT;

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
		SC.showPrompt(this.getFormattedPrompt());
		if (timer != null) {
			// Should never happen??
			SC.warn("Command is already running!");
			return;
		}
		timer = new Timer() {
			public void run() {
				abortFlag = true; // TIMEOUT.
				SC.clearPrompt();
				SC.warn("Request timed out!");
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

	/**
	 * 
	 * @param prompt
	 */
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	/**
	 * 
	 * @return
	 */
	public String getPrompt() {
		return prompt;
	}
	
	/**
	 * 
	 * @return
	 */
	private String getFormattedPrompt()
	{
		//"<img src=\"images/loadingSmall.gif\"\"/><span style=\"padding-left:10px;font-size:10pt;\">Searching ...</span>";
		return "<img src=\"images/loadingSmall.gif\"\"/><span style=\"padding-left:10px;font-size:10pt;\">" + this.getPrompt() + "</span>";
	}
}
