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

package com.vangent.hieos.services.xds.bridge.transactions.activity;

import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.services.xds.bridge.mapper.IXDSMapper;
import com.vangent.hieos.services.xds.bridge.mapper.MapperFactory;
import com.vangent.hieos.services.xds.bridge.model.Document;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentResponse;
import com.vangent.hieos.services.xds.bridge.model.XDSPnR;
import com.vangent.hieos.services.xds.bridge.utils.DebugUtils;
import org.apache.log4j.Logger;

/**
 * Class description
 *
 *
 * @version        v1.0, 2011-06-22
 * @author         Jim Horner
 */
public class CDAToXDSMapperActivity implements ISubmitDocumentRequestActivity {

    /** Field description */
    private final static Logger logger =
        Logger.getLogger(CDAToXDSMapperActivity.class);

    /** Field description */
    private final MapperFactory mapperFactory;

    /**
     * Constructs ...
     *
     *
     * @param mapperFactory
     */
    public CDAToXDSMapperActivity(MapperFactory mapperFactory) {

        super();
        this.mapperFactory = mapperFactory;
    }

    /**
     * Method description
     *
     *
     * @param context
     *
     * @return
     */
    @Override
    public boolean execute(SDRActivityContext context) {

        boolean result = false;

        Document document = context.getDocument();
        CodedValue type = document.getType();
        IXDSMapper mapper = this.mapperFactory.getMapper(type);

        try {

            XDSPnR pnr = mapper.map(context.getPatientId(), document);

            logger.debug(DebugUtils.toPrettyString(pnr.getNode()));

            context.setXdspnr(pnr);

            result = true;

        } catch (Exception e) {

            SubmitDocumentResponse resp = context.getSubmitDocumentResponse();

            resp.addError("E001", e.getMessage());
        }

        return result;
    }
}
