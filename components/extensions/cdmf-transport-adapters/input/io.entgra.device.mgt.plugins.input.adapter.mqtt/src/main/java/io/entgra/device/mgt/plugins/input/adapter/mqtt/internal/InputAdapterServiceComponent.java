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
package io.entgra.device.mgt.plugins.input.adapter.mqtt.internal;

import io.entgra.device.mgt.core.apimgt.application.extension.APIManagementProviderService;
import io.entgra.device.mgt.plugins.input.adapter.extension.InputAdapterExtensionService;
import io.entgra.device.mgt.plugins.input.adapter.mqtt.MQTTEventAdapterFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
		name = "io.entgra.device.mgt.plugins.input.adapter.mqtt.internal.InputAdapterServiceComponent",
		immediate = true)
public class InputAdapterServiceComponent {

	private static final Log log = LogFactory.getLog(InputAdapterServiceComponent.class);

	@Activate
	protected void activate(ComponentContext context) {
		try {
			InputEventAdapterFactory mqttEventAdapterFactory = new MQTTEventAdapterFactory();
			context.getBundleContext().registerService(InputEventAdapterFactory.class.getName(),
													   mqttEventAdapterFactory, null);
			if (log.isDebugEnabled()) {
				log.debug("Successfully deployed the input adapter service");
			}
		} catch (RuntimeException e) {
			log.error("Can not create the input adapter service ", e);
		}
	}

	@Reference(
			name = "http.service",
			service = org.osgi.service.http.HttpService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.DYNAMIC,
			unbind = "unsetHttpService")
	protected void setHttpService(HttpService httpService) {
		InputAdapterServiceDataHolder.registerHTTPService(httpService);
	}

	protected void unsetHttpService(HttpService httpService) {
		InputAdapterServiceDataHolder.registerHTTPService(null);
	}

	@Reference(
			name = "input.adaptor.extension.service",
			service = io.entgra.device.mgt.plugins.input.adapter.extension.InputAdapterExtensionService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.DYNAMIC,
			unbind = "unsetInputAdapterExtensionService")
    protected void setInputAdapterExtensionService(InputAdapterExtensionService inputAdapterExtensionService) {
        InputAdapterServiceDataHolder.setInputAdapterExtensionService(inputAdapterExtensionService);
    }

    protected void unsetInputAdapterExtensionService(InputAdapterExtensionService inputAdapterExtensionService) {
        InputAdapterServiceDataHolder.setInputAdapterExtensionService(null);
    }

	@Reference(
			name = "input.event.adaptor.service",
			service = org.wso2.carbon.event.input.adapter.core.InputEventAdapterService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.DYNAMIC,
			unbind = "unsetInputEventAdapterService")
	protected void setInputEventAdapterService(InputEventAdapterService inputEventAdapterService) {
		InputAdapterServiceDataHolder.setInputEventAdapterService(inputEventAdapterService);
	}

	protected void unsetInputEventAdapterService(InputEventAdapterService inputEventAdapterService) {
		InputAdapterServiceDataHolder.setInputEventAdapterService(null);
	}

	@Reference(
			name = "configuration.context.service",
			service = org.wso2.carbon.utils.ConfigurationContextService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.DYNAMIC,
			unbind = "unsetConfigurationContextService")
	protected void setConfigurationContextService(ConfigurationContextService contextService) {
		ConfigurationContext serverConfigContext = contextService.getServerConfigContext();
		InputAdapterServiceDataHolder.setMainServerConfigContext(serverConfigContext);
	}

	protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
		InputAdapterServiceDataHolder.setMainServerConfigContext(null);
	}

	@Reference(
			name = "api.mgt.provider.extension",
			service = io.entgra.device.mgt.core.apimgt.application.extension.APIManagementProviderService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.DYNAMIC,
			unbind = "unsetApiManagementProviderService")
	protected void setApiManagementProviderService(APIManagementProviderService apiManagementProviderService) {
		if (log.isDebugEnabled()) {
			log.info("API management provider service is initializing");
		}
		InputAdapterServiceDataHolder.setApiManagementProviderService(apiManagementProviderService);
	}

	protected void unsetApiManagementProviderService(APIManagementProviderService apiManagementProviderService) {
		if (log.isDebugEnabled()) {
			log.info("API management provider service is uninitialized");
		}
		InputAdapterServiceDataHolder.setApiManagementProviderService(null);
	}

}
