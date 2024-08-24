/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.plugins.output.adapter.mqtt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import io.entgra.device.mgt.plugins.output.adapter.mqtt.MQTTEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import io.entgra.device.mgt.core.identity.jwt.client.extension.service.JWTClientManagerService;
import org.osgi.service.component.annotations.*;

@Component(
        name = "io.entgra.device.mgt.plugins.output.adapter.mqtt.internal.MQTTEventAdapterServiceComponent",
        immediate = true)
public class MQTTEventAdapterServiceComponent {

    private static final Log log = LogFactory.getLog(MQTTEventAdapterServiceComponent.class);

    /**
     * Deployment of the MQTT event adapter service will be done.
     * @param context bundle context where service is registered
     */
    @Activate
    protected void activate(ComponentContext context) {
        try {
            OutputEventAdapterFactory mqttEventAdapterFactory = new MQTTEventAdapterFactory();
            context.getBundleContext().registerService(OutputEventAdapterFactory.class.getName(),
                    mqttEventAdapterFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("The MQTT publisher service has been deployed successfully");
            }
        } catch (RuntimeException e) {
            log.error("Exception occurred when deploying MQTT publisher service", e);
        }
    }

    @Reference(
            name = "jwt.client.manager.service",
            service = io.entgra.device.mgt.core.identity.jwt.client.extension.service.JWTClientManagerService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetJWTClientManagerService")
    protected void setJWTClientManagerService(JWTClientManagerService jwtClientManagerService) {
        OutputAdapterServiceDataHolder.setJwtClientManagerService(jwtClientManagerService);
    }

    protected void unsetJWTClientManagerService(JWTClientManagerService jwtClientManagerService) {
        OutputAdapterServiceDataHolder.setJwtClientManagerService(null);
    }

}
