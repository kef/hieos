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

import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRegistryClient;
import com.vangent.hieos.services.xds.bridge.client.XDSDocumentRepositoryClient;
import com.vangent.hieos.services.xds.bridge.mapper.ContentParser;
import com.vangent.hieos.services.xds.bridge.mapper.MapperFactory;
import com.vangent.hieos.services.xds.bridge.message
    .SubmitDocumentRequestBuilder;
import com.vangent.hieos.services.xds.bridge.message
    .SubmitDocumentResponseBuilder;
import com.vangent.hieos.xutil.xconfig.XConfigActor;

/**
 *
 * @author hornja
 */
public class XDSBridgeServiceContext {

    /** Field description */
    private final MapperFactory mapperFactory;

    /** Field description */
    private final XDSDocumentRegistryClient registryClient;

    /** Field description */
    private final XDSDocumentRepositoryClient repositoryClient;

    /** Field description */
    private final SubmitDocumentRequestBuilder submitDocumentRequestBuilder;

    /** Field description */
    private final SubmitDocumentResponseBuilder submitDocumentResponseBuilder;

    /** Field description */
    private final XDSBridgeConfig xdsBridgeConfig;

    /**
     * Constructs ...
     *
     *
     *
     *
     * @param registryActor
     * @param repositoryActor
     * @param bridgeConfig
     */
    public XDSBridgeServiceContext(XConfigActor registryActor,
                                   XConfigActor repositoryActor,
                                   XDSBridgeConfig bridgeConfig) {

        super();
        this.xdsBridgeConfig = bridgeConfig;

        this.submitDocumentRequestBuilder =
            new SubmitDocumentRequestBuilder(bridgeConfig);

        this.submitDocumentResponseBuilder =
            new SubmitDocumentResponseBuilder(bridgeConfig);

        ContentParser conParser = new ContentParser();

        this.mapperFactory = new MapperFactory(bridgeConfig, conParser);

        this.repositoryClient =
            new XDSDocumentRepositoryClient(this.xdsBridgeConfig,
                repositoryActor);

        this.registryClient =
            new XDSDocumentRegistryClient(this.xdsBridgeConfig, registryActor);
    }

    /**
     * Constructs ...
     *
     *
     *
     * @param bridgeConfig
     * @param regClient
     * @param repoClient
     */
    public XDSBridgeServiceContext(XDSBridgeConfig bridgeConfig,
                                   XDSDocumentRegistryClient regClient,
                                   XDSDocumentRepositoryClient repoClient) {

        super();

        this.xdsBridgeConfig = bridgeConfig;

        this.submitDocumentRequestBuilder =
            new SubmitDocumentRequestBuilder(bridgeConfig);

        this.submitDocumentResponseBuilder =
            new SubmitDocumentResponseBuilder(bridgeConfig);

        ContentParser conParser = new ContentParser();

        this.mapperFactory = new MapperFactory(bridgeConfig, conParser);

        this.repositoryClient = repoClient;

        this.registryClient = regClient;
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

    /**
     * Method description
     *
     *
     * @return
     */
    public SubmitDocumentResponseBuilder getSubmitDocumentResponseBuilder() {
        return submitDocumentResponseBuilder;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public XDSBridgeConfig getXdsBridgeConfig() {
        return this.xdsBridgeConfig;
    }
}
