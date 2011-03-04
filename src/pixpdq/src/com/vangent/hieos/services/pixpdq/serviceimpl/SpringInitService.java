/*
 * This code is subject to the HIEOS License, Version 1.0
 *
 * Copyright(c) 2010 Vangent, Inc.  All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vangent.hieos.services.pixpdq.serviceimpl;

import com.vangent.hieos.services.pixpdq.adapter.mpi.EMPIAdapter;
import com.vangent.hieos.services.pixpdq.adapter.mpi.EMPIFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ServiceLifeCycle;

/**
 *
 * @author Bernie Thuman
 */
public class SpringInitService implements ServiceLifeCycle {

    /**
     *
     * @param ignore
     * @param service
     */
    public void startUp(ConfigurationContext context, AxisService service) {

        try {
            /* WORKS ...
            ClassLoader classLoader = service.getClassLoader();
            ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext(new String[]{"applicationContext.xml"}, false);
            appCtx.setClassLoader(classLoader);
            appCtx.refresh();
             */
            // FIXME: This is really to get OpenEMPI up and running and does not belong
            // here (in this package).

            // FIXME: THIS IS NOT THE FIX (initialization works) ... need to get into Context class and
            // set the class loader on the ClassPathXmlApplicationContext class.
            EMPIAdapter adapter = EMPIFactory.getInstance();
            ClassLoader axisClassLoader = service.getClassLoader();
            adapter.startup(axisClassLoader);
            /*
            System.out.println("Starting spring init");
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader axisClassLoader = service.getClassLoader();
            Thread.currentThread().setContextClassLoader(axisClassLoader);
            Context.startup();
            Thread.currentThread().setContextClassLoader(currentClassLoader);  // Restore.
            System.out.println("spring loaded");*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param context
     * @param service
     */
    public void shutDown(ConfigurationContext context, AxisService service) {
        //  Do nothing.
    }
}
