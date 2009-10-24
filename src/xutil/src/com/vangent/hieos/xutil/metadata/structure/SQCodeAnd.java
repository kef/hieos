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
package com.vangent.hieos.xutil.metadata.structure;

import java.util.ArrayList;
import java.util.List;

public class SQCodeAnd extends SQCodedTerm {

	public List<SQCodeOr> codeOrs;
	
	public SQCodeAnd() {
		codeOrs = new ArrayList<SQCodeOr>();
	}
	
	public void add(SQCodeOr or) {
		codeOrs.add(or);
		or.setIndex(codeOrs.size());  // so unique names can be generated
	}
	
	public List<SQCodeOr> getCodeOrs() {
		return codeOrs;
	}
	
	public String toString() {
		return "SQCodeAnd: [\n" +
		codeOrs + 
		"]\n";
	}

	public boolean isEmpty() {
		return codeOrs.size() == 0;
	}
	
	public List<String> getCodeVarNames() {
		List<String> names = new ArrayList<String>();
		
		for (SQCodeOr or : codeOrs) {
			names.add(or.getCodeVarName());
		}
		return names;
	}
	
	public List<String> getSchemeVarNames() {
		List<String> names = new ArrayList<String>();
		
		for (SQCodeOr or : codeOrs) {
			names.add(or.getSchemeVarName());
		}
		return names;
	}


}
