/**
 * Copyright 2013 52°North Initiative for Geospatial Open Source Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.oss.sir;

/**
 * @author Jan Schulte
 * 
 */
public class SirConstants {

    public enum CapabilitiesSection {
        All, Contents, OperationsMetadata, ServiceIdentification, ServiceProvider
    }

    /**
     * enum with parameter names for GetCapabilities HTTP GET request
     */
    public enum GetCapGetParams {
        ACCEPTFORMATS, ACCEPTVERSIONS, REQUEST, SECTIONS, SERVICE, UPDATESEQUENCE;
    }

    /**
     * enum with parameters of DescribeSensor HTTP GET request
     */
    public enum GetDescSensorParams {
        REQUEST, SENSORIDINSIR;
    }

    /**
     * enum for all supported request operations by the SIR
     */
    public enum Operations {
        CancelSensorStatusSubscription, ConnectToCatalog, DeleteSensorInfo, DescribeSensor, DisconnectFromCatalog, GetCapabilities, GetSensorStatus, HarvestService, InsertSensorInfo, InsertSensorStatus, RenewSensorStatusSubscription, SearchSensor, SubscribeSensorStatus, UpdateSensorDescription
    }

    public static final String CHARSET_NAME = "UTF-8";

    public static final String CONTENT_TYPE_XML = "text/xml";

    /**
     * Name of the DescribeSensor operation in a capabitilities document
     */
    public static final String DESCRIBE_SENSOR_OPERATION_NAME = "DescribeSensor";

    public static final String GETREQUESTPARAM = "REQUEST";

    public static final String GETVERSIONPARAM = "version";

    public static final String IOOSCATAL0G_SERVICE_TYPE = "IOOSCATALOG";

    public static final String REQUEST_CONTENT_CHARSET = CHARSET_NAME;

    public static final String REQUEST_CONTENT_TYPE = CONTENT_TYPE_XML;

    public static final String RESPONSE_CONTENT_CHARSET = CHARSET_NAME;

    public static final String SERVICE_NAME = "SIR";

    public static final String SERVICE_VERSION_0_3_0 = "0.3.0";

    public static final String SERVICE_VERSION_0_3_1 = "0.3.1";

    public static final String SERVICE_VERSION_0_3_2 = "0.3.2";

    public static final Object SERVICEPARAM = "service";

    public static final String SOS_SERVICE_TYPE = "SOS";

    public static final String SOS_VERSION = "1.0.0";

    public static final String SPS_SERVICE_TYPE = "SPS";

}