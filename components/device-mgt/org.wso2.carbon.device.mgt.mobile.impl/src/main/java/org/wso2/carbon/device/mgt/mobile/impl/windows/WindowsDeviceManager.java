/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.mobile.impl.windows;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.device.mgt.extensions.license.mgt.registry.RegistryBasedLicenseManager;
import org.wso2.carbon.device.mgt.mobile.common.MobileDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.mobile.common.MobilePluginConstants;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDevice;
import org.wso2.carbon.device.mgt.mobile.impl.windows.dao.WindowsDAOFactory;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;

public class WindowsDeviceManager implements DeviceManager {

    private MobileDeviceManagementDAOFactory daoFactory;
    private LicenseManager licenseManager;

    private static final Log log = LogFactory.getLog(WindowsDeviceManagementService.class);

    public WindowsDeviceManager() {
        this.daoFactory = new WindowsDAOFactory();
        this.licenseManager = new RegistryBasedLicenseManager();
    }

    @Override
    public FeatureManager getFeatureManager() {
        return null;
    }

    @Override
    public boolean saveConfiguration(TenantConfiguration tenantConfiguration)
            throws DeviceManagementException {
        boolean status = false;
        Resource resource;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Persisting windows configurations in Registry");
            }
            String resourcePath = MobileDeviceManagementUtil.getPlatformConfigPath(
                    DeviceManagementConstants.
                            MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(TenantConfiguration.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(tenantConfiguration, writer);

            resource = MobileDeviceManagementUtil.getRegistry().newResource();
            resource.setContent(writer.toString());
            resource.setMediaType(MobilePluginConstants.MEDIA_TYPE_XML);
            MobileDeviceManagementUtil.putRegistryResource(resourcePath, resource);
            status = true;
        } catch (MobileDeviceMgtPluginException e) {
            throw new DeviceManagementException(
                    "Error occurred while retrieving the Registry instance : " + e.getMessage(), e);
        } catch (RegistryException e) {
            throw new DeviceManagementException(
                    "Error occurred while persisting the Registry resource of Windows configuration : " + e.getMessage(), e);
        } catch (JAXBException e) {
            throw new DeviceManagementException(
                    "Error occurred while parsing the Windows configuration : " + e.getMessage(), e);
        }
        return status;
    }

    @Override
    public TenantConfiguration getConfiguration() throws DeviceManagementException {
        Resource resource;
        try {
            String androidRegPath =
                    MobileDeviceManagementUtil.getPlatformConfigPath(DeviceManagementConstants.
                                                                             MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
            resource = MobileDeviceManagementUtil.getRegistryResource(androidRegPath);
            JAXBContext context = JAXBContext.newInstance(TenantConfiguration.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (TenantConfiguration) unmarshaller.unmarshal(
                    new StringReader(new String((byte[]) resource.getContent(), Charset
                            .forName(MobilePluginConstants.CHARSET_UTF8))));

        } catch (MobileDeviceMgtPluginException e) {
            throw new DeviceManagementException(
                    "Error occurred while retrieving the Registry instance : " + e.getMessage(), e);
        } catch (JAXBException e) {
            throw new DeviceManagementException(
                    "Error occurred while parsing the Windows configuration : " + e.getMessage(), e);
        } catch (RegistryException e) {
            throw new DeviceManagementException(
                    "Error occurred while retrieving the Registry resource of Windows configuration : " + e.getMessage(), e);
        }
    }

    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        boolean status;
        MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Modifying the Windows device enrollment data");
            }
            WindowsDAOFactory.beginTransaction();
            status = daoFactory.getMobileDeviceDAO()
                    .updateMobileDevice(mobileDevice);
            WindowsDAOFactory.commitTransaction();
        } catch (MobileDeviceManagementDAOException e) {
            try {
                WindowsDAOFactory.rollbackTransaction();
            } catch (MobileDeviceManagementDAOException mobileDAOEx) {
                String msg = "Error occurred while roll back the update device transaction :" + device.toString();
                log.warn(msg, mobileDAOEx);
            }
            String msg = "Error while updating the enrollment of the Windows device : " +
                    device.getDeviceIdentifier();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean setActive(DeviceIdentifier deviceId, boolean status)
            throws DeviceManagementException {
        return true;
    }

    public List<Device> getAllDevices() throws DeviceManagementException {
        return null;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType)
            throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean isClaimable(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setStatus(DeviceIdentifier deviceIdentifier, String currentUser,
                             EnrolmentInfo.Status status) throws DeviceManagementException {
        return false;
    }

    @Override
    public License getLicense(String languageCode) throws LicenseManagementException {
        return licenseManager.getLicense(WindowsDeviceManagementService.DEVICE_TYPE_WINDOWS, languageCode);
    }

    @Override
    public void addLicense(License license) throws LicenseManagementException {
        licenseManager.addLicense(WindowsDeviceManagementService.DEVICE_TYPE_WINDOWS, license);
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier,
                                    Device device) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        boolean status;
        MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);
        try {
            status = daoFactory.getMobileDeviceDAO().addMobileDevice(mobileDevice);
        } catch (MobileDeviceManagementDAOException e) {
            throw new DeviceManagementException("Error while enrolling the Windows device '" +
                    device.getDeviceIdentifier() + "'", e);
        }
        return status;
    }

}
