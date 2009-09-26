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
package com.vangent.hieos.xutil.exception;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Utility class to report exceptions in user readable format.
 */
public class ExceptionUtil {

    /**
     * Prepare an exception string suitable for presentation (no stack trace).
     *
     * @param e The Exception itself.
     * @param message Brief description of the exception.
     * @return A string representation of the exception.
     */
    static public String exception_details(Exception e, String message) {
        if (e == null) {
            return "";
        }
        e.printStackTrace();  // Debug:
        String emessage = e.getMessage();
        if (emessage == null) {
            emessage = "No Message";
        }
        return "Exception: " + e.getClass().getName() + "\n" +
                ((message != null) ? message + "\n" : "") +
                emessage.replaceAll("<", "&lt;");
    }

    /**
     * Prepare an exception string suitable for presentation (includes stack trace).
     *
     * @param e The Exception itself.
     * @param message Brief description of the exception.
     * @return A string representation of the exception.
     */
    static public String exception_long_details(Exception e, String message) {
        if (e == null) {
            return "No stack trace available";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        String emessage = e.getMessage();
        if (emessage == null) {
            emessage = "No Message";
        }
        return "Exception thrown: " + e.getClass().getName() + "\n" +
                ((message != null) ? message + "\n" : "") +
                emessage.replaceAll("<", "&lt;") + "\n" + new String(baos.toByteArray());
    }

    /**
     * Prepare an exception string suitable for presentation (no stack trace).
     * 
     * @param e The Exception itself.
     * @return A string representing the exception.
     */
    static public String exception_details(Exception e) {
        return exception_details(e, null);
    }

    /**
     * Prints out a user readable exception string (the first N lines) - no stack trace.
     *
     * @param e The Exception itself.
     * @param numLines The number of text lines to include.
     * @return A string representatoin of the exception.
     */
    static public String exception_details(Exception e, int numLines) {
        return firstNLines(exception_details(e), numLines);
    }

    /**
     * Returns the complete stack trace for the given Exception.
     *
     * @param e The Exception itself.
     * @return A string representation of the Exception stack trace.
     */
    static public String exception_local_stack(Exception e) {
        StringBuffer buf = new StringBuffer();

        String[] lines = exception_long_details(e, null).split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.indexOf("com.vangent") != -1) {
                buf.append(line).append("\n");
            }
        }
        return buf.toString();
    }

    /**
     * Returns the first 'n' lines of the given string.
     *
     * @param str The string in question.
     * @param n The maximum number of lines to return in the result.
     * @return The resulting string (limited by 'n' lines max).
     */
    static private String firstNLines(String str, int n) {
        int startingAt = 0;
        for (int i = 0; i < n; i++) {
            if (startingAt != -1) {
                startingAt = str.indexOf('\n', startingAt + 1) + 1;
            }
        }
        if (startingAt == -1) {
            return str;
        }
        return str.substring(0, startingAt);
    }
}
