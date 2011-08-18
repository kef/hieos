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
package com.vangent.hieos.policyutil.pdp.impl;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.cond.FunctionBase;
import com.sun.xacml.ctx.Status;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 *
 * @author Bernie Thuman
 */
public class XPathNodeCountFunction extends FunctionBase {

    private static final Logger logger =
            Logger.getLogger(XPathNodeCountFunction.class.getName());
    /**
     * Standard identifier for the xpath-node-count function.
     */
    public static final String XPATH_NODE_COUNT =
            FUNCTION_NS + "xpath-node-count";

    public XPathNodeCountFunction() {
        super(
                XPATH_NODE_COUNT /* functionName */,
                0 /* functionId */,
                StringAttribute.identifier /* paramType */,
                false /* paramIsBag */,
                1 /* numParams */,
                IntegerAttribute.identifier /* returnType */,
                false /* returnsBag */);
    }

    /**
     * Evaluate the function, using the specified parameters.
     *
     * @param inputs a <code>List</code> of <code>Evaluatable</code>
     *               objects representing the arguments passed to the function
     * @param context an <code>EvaluationCtx</code> so that the
     *                <code>Evaluatable</code> objects can be evaluated
     * @return an <code>EvaluationResult</code> representing the
     *         function's result
     */
    public EvaluationResult evaluate(List inputs, EvaluationCtx context) {

        // Evaluate the arguments
        AttributeValue[] argValues = new AttributeValue[inputs.size()];
        EvaluationResult result = evalArgs(inputs, context, argValues);
        if (result != null) {
            return result;
        }

        // Now that we have real values, perform the xpath-node-count operation
        String xpathExpression = ((StringAttribute) argValues[0]).getValue();

        // Get request Root (as starting point).
        Node requestRoot = context.getRequestRoot();
        NodeList matches = null;
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            //Node policyRoot = this.getPolicyRoot(requestRoot);
            //NamespaceContextImpl namespaceContext = new NamespaceContextImpl(policyRoot);
            //xpath.setNamespaceContext(namespaceContext);
            matches = (NodeList) xpath.evaluate(xpathExpression, requestRoot, XPathConstants.NODESET);
        } catch (Exception e) {
            logger.log(Level.ALL, "Error during xpath.evaluate(...)", e);

            // in the case of any exception, we need to return an error
            return createProcessingError("error in XPath: " + e.getMessage());
        }
        int nodeCount = matches.getLength();
        result = new EvaluationResult(new IntegerAttribute(nodeCount));
        return result;
    }

    /**
     * Private helper to create a new processing error status result
     */
    private EvaluationResult createProcessingError(String msg) {
        ArrayList code = new ArrayList();
        code.add(Status.STATUS_PROCESSING_ERROR);
        return new EvaluationResult(new Status(code, msg));
    }
}
