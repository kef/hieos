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

package com.vangent.hieos.xwebtools.servlets.framework;

import java.io.PrintWriter;

/**
 *
 * @author NIST (adapted)
 */
public class HttpUtils {
	PrintWriter writer;
	StringBuffer buf;

        /**
         *
         * @param writer
         */
        public HttpUtils(PrintWriter writer) {
		this.writer = writer;
		buf = new StringBuffer();
	} 
	
        /**
         *
         */
        public void close() {
		writer.print(buf.toString());
		writer.close();
		writer = null;
		buf = null;
	}
	
        /**
         *
         */
        public void flush() {
		writer.print(buf.toString());
		buf = new StringBuffer();
	}
	
        /**
         *
         * @param alert
         */
        public void alert(String alert) {
		writer.print("Alert: " + alert + "<br />");
	}
	
        /**
         *
         * @param o
         */
        public void o(Object o)  {
		buf.append(o);
		buf.append("\n");
	}
	
        /**
         *
         * @param s
         */
        public void open(String s) {
		o("<" + s + ">");
	}

        /**
         *
         * @param s
         * @param attname
         * @param attvalue
         */
        public void open_w_att(String s, String attname, String attvalue) {
		o("<" + s + " " + attname + "=\"" + attvalue +  "\">");
	}

        /**
         *
         * @param s
         */
        public void clos(String s) {
		o("</" + s + ">");
	}
	
        /**
         *
         * @param s
         */
        public void tag(String s) {
		o("<" + s + "/>");
	}
	
        /**
         *
         * @param s
         */
        public void bold(String s) {
		open("b");
		o(s);
		clos("b");
	}
	
        /**
         *
         * @param title
         */
        public void head(String title)   {
		o("<html><head><title>" + title + "</title>");
		open("style");
		o(".indent0 { text-indent: 0em; font-size: 100%}");
		o(".indent1 { text-indent: 1em; font-size: 100% }");
		o(".indent2 { text-indent: 2em; font-size: 100% }");
		clos("style");
		o("</head><body>");
		flush();
	}
	
        /**
         *
         * @param content
         */
        public void indent0(String content) {
		div("indent0", content);
	}
	
        /**
         *
         * @param content
         */
        public void indent1(String content) {
		div("indent1", content);
	}
	
        /**
         *
         * @param content
         */
        public void indent2(String content) {
		div("indent2", content);
	}
	
        /**
         *
         * @param classx
         * @param content
         */
        public void div(String classx, String content) {
		open_w_att("div", "class", classx);
		o(content);
		clos("div");
	}

        /**
         *
         */
        public void tail()   {
		o("</body></html>");	
	}

        /**
         *
         * @param type
         * @param name
         * @param value
         * @param text
         * @param other
         */
        public void input(String type, String name, String value, String text, String other)   {
		if (text == null || text.equals("")) {
			o("<input type=\"" + type + "\" name = \"" + name + "\" value=\"" + value + "\" " + other + "/>");
		} else {
			o("<input type=\"" + type + "\" name = \"" + name + "\" value=\"" + value + "\" " + other + ">" + text + "</input>");			
		}
	}

        /**
         *
         * @param value
         */
        public void label(String value)   {
		o("<label>" + value + "</label>");
	}

        /**
         *
         */
        public void br()  { o("<br />"); }

        /**
         *
         * @param action
         */
        public void get_form(String action)   {
		o("<form action=\"" + action + "\" " + "method=\"get\">");
	}

        /**
         *
         * @param action
         * @param enc_type
         */
        public void post_form(String action, String enc_type)   {
		o("<form action=\"" + action + "\" " + "method=\"post\" " +
				((enc_type != null) ? "enctype=\"" + enc_type + "\"" : "") +
				">");
	}

        /**
         *
         */
        public void end_form()  { o("</form>"); }

        /**
         *
         */
        public void hr()  { o("<hr />"); }

        /**
         *
         * @param value
         */
        public void h1(String value)  { o("<h1>" + value + "</h1>"); }

        /**
         *
         * @param value
         */
        public void h2(String value)   { o("<h2>" + value + "</h2>"); }
	
        /**
         *
         * @param display
         * @param target
         * @return
         */
        public String link(String display, String target) { return("<a href=\"" + target + "\">" + display + "</a>"); }

}
