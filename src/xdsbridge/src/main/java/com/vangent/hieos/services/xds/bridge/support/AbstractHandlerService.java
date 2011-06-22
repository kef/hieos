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

package com.vangent.hieos.services.xds.bridge.support;

import com.vangent.hieos.services.xds.bridge.utils.StringUtils;
import com.vangent.hieos.xutil.exception.XdsValidationException;
import com.vangent.hieos.xutil.services.framework.XAbstractService;
import com.vangent.hieos.xutil.xlog.client.XLogMessage;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-09
 * @author         Jim Horner
 */
public abstract class AbstractHandlerService extends XAbstractService {

    /** Field description */
    private final static Logger logger =
        Logger.getLogger(AbstractHandlerService.class);

    /** Field description */
    private final ActorType actorType;

    /** Field description */
    private String handlerOperationName;

    /** Field description */
    private String handlerServiceName;

    /**
     * Constructs ...
     *
     *
     *
     * @param actorType
     */
    public AbstractHandlerService(ActorType actorType) {

        super();
        this.actorType = actorType;
    }

    /**
     * Method description
     *
     *
     *
     * @param serviceName
     * @param request
     * @param actor
     *
     * @return
     *
     * @throws AxisFault
     */
    @Override
    protected OMElement beginTransaction(String serviceName, OMElement request,
            ActorType actor)
            throws AxisFault {

        OMElement result = null;

        try {

            // this flow can throw NPE, let's wrap and make pretty
            result = super.beginTransaction(serviceName, request, actor);

        } catch (AxisFault e) {

            throw e;

        } catch (Exception e) {

            // log exception
            logger.error(e, e);

            String msg =
                "XDSBridge could not initialize to process the request. Check the XML for validity.";

            result = start_up_error(request, null, getActorType(), msg);

            if (result == null) {

                // start_up_error ate an exception
                // Last resort, THROW A FAULT!!!
                throw getAxisFault(new IllegalStateException(msg));
            }
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param service
     */
    private void configureLoggerLevel(AxisService service) {

        Parameter lcparam = service.getParameter("LoggerCategory");
        Parameter llparam = service.getParameter("LoggerLevel");

        if ((lcparam != null) && (llparam != null)) {

            String loggerCategory = (String) lcparam.getValue();
            Level loggerLevel = Level.toLevel((String) llparam.getValue(),
                                              Level.INFO);

            if (StringUtils.isNotBlank(loggerCategory)) {

                Logger baseLogger = Logger.getLogger(loggerCategory);

                baseLogger.setLevel(loggerLevel);

                baseLogger.info(
                    String.format(
                        "Setting logger [%s] level to %s(%d).", loggerCategory,
                        loggerLevel.toString(), loggerLevel.toInt()));
            }
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public ActorType getActorType() {
        return actorType;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getHandlerOperationName() {
        return handlerOperationName;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getHandlerServiceName() {
        return handlerServiceName;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    protected XLogMessage getLogMessage() {
        return this.log_message;
    }

    /**
     * Method description
     *
     *
     *
     * @param request
     * @param handler
     *
     * @return
     *
     * @throws AxisFault
     */
    protected OMElement handleMessage(OMElement request,
                                      IMessageHandler handler)
            throws AxisFault {

        OMElement response = null;
        OMElement startupError = null;

        try {

            String servName = getHandlerServiceName();
            String operName = getHandlerOperationName();

            String beginMsg = String.format("%s::%s", servName, operName);

            startupError = beginTransaction(beginMsg, request, getActorType());

            if (startupError != null) {

                response = startupError;

            } else {

                validate();

                response = handler.run(getMessageContext(), request);
            }

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            throw getAxisFault(e);

        } finally {

            // if startupError is not null then
            // endTransaction has already been called
            if (startupError == null) {

                XLogMessage lm = getLogMessage();

                if (lm != null) {
                    endTransaction(getLogMessage().isPass());
                } else {
                    endTransaction(false);
                }
            }
        }

        return response;
    }

    /**
     * Method description
     *
     *
     * @param serviceContext
     *
     * @throws AxisFault
     */
    @Override
    public void init(ServiceContext serviceContext) throws AxisFault {

        super.init(serviceContext);

        MessageContext msgctx = getMessageContext();
        OperationContext operctx = msgctx.getOperationContext();

        setHandlerServiceName(serviceContext.getName());
        setHandlerOperationName(operctx.getOperationName());
    }

    /**
     * Method description
     *
     *
     * @param handlerOperationName
     */
    public void setHandlerOperationName(String handlerOperationName) {
        this.handlerOperationName = handlerOperationName;
    }

    /**
     * Method description
     *
     *
     * @param handlerServiceName
     */
    public void setHandlerServiceName(String handlerServiceName) {
        this.handlerServiceName = handlerServiceName;
    }

    /**
     * Method description
     *
     *
     * @param configctx
     * @param service
     */
    @Override
    public void shutDown(ConfigurationContext configctx, AxisService service) {

        String serviceName = service.getName();

        super.shutDown(configctx, service);

        configctx.removeProperty(serviceName);

        logger.info(String.format("%s::shutDown()::%s", serviceName,
                                  service.getParameterValue("ActorName")));
    }

    /**
     * Method description
     *
     *
     * @param configctx
     * @param service
     */
    @Override
    public void startUp(ConfigurationContext configctx, AxisService service) {

        super.startUp(configctx, service);

        configureLoggerLevel(service);

        String serviceName = service.getName();

        logger.info(String.format("%s::startUp()::%s", serviceName,
                                  service.getParameterValue("ActorName")));
    }

    /**
     * Method description
     *
     *
     *
     * @throws XdsValidationException
     */
    protected abstract void validate() throws XdsValidationException;
}
