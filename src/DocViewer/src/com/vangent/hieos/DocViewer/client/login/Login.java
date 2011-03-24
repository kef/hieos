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
package com.vangent.hieos.DocViewer.client.login;

import com.google.gwt.user.client.ui.Composite;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.PasswordItem;
import com.smartgwt.client.widgets.form.fields.SubmitItem;

public class Login extends Composite {

	public Login() {
		
		DynamicForm dynamicForm = new DynamicForm();
		dynamicForm.setFields(new FormItem[] { new TextItem("newTextItem_1", "New TextItem"), new PasswordItem("newPasswordItem_2", "New PasswordItem"), new SubmitItem()});
		initWidget(dynamicForm);
	}
}
