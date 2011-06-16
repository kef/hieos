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

/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */

package com.vangent.hieos.services.xds.bridge.serviceimpl;

import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRegistryClient;
import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRepositoryClient;
import com.vangent.hieos.services.xds.bridge.mapper.MapperFactory;
import com.vangent.hieos.services.xds.bridge.model.SubmitDocumentRequestBuilder;

/**
 *
 * @author hornja
 */
public class XDSBridgeConfig {

    /** Field description */
    private final MapperFactory mapperFactory;

    /** Field description */
    private final XDSDocumentRegistryClient registryClient;

    /** Field description */
    private final XDSDocumentRepositoryClient repositoryClient;

    /** Field description */
    private final SubmitDocumentRequestBuilder submitDocumentRequestBuilder;

    /**
     * Constructs ...
     *
     *
     * @param mapperFactory
     * @param registryClient
     * @param repositoryClient
     * @param submitDocumentRequestBuilder
     */
    public XDSBridgeConfig(
            MapperFactory mapperFactory,
            SubmitDocumentRequestBuilder submitDocumentRequestBuilder,
            XDSDocumentRegistryClient registryClient,
            XDSDocumentRepositoryClient repositoryClient) {

        super();
        this.mapperFactory = mapperFactory;
        this.registryClient = registryClient;
        this.repositoryClient = repositoryClient;
        this.submitDocumentRequestBuilder = submitDocumentRequestBuilder;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public MapperFactory getMapperFactory() {
        return mapperFactory;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public XDSDocumentRegistryClient getRegistryClient() {
        return registryClient;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public XDSDocumentRepositoryClient getRepositoryClient() {
        return repositoryClient;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public SubmitDocumentRequestBuilder getSubmitDocumentRequestBuilder() {
        return submitDocumentRequestBuilder;
    }
}
