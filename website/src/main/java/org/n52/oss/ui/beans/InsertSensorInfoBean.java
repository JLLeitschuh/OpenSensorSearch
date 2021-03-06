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
package org.n52.oss.ui.beans;

import net.opengis.sensorML.x101.AbstractProcessType;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML.Member;
import net.opengis.sensorML.x101.SystemType;

import org.apache.xmlbeans.XmlException;
import org.n52.oss.util.XmlTools;
import org.x52North.sir.x032.InsertSensorInfoRequestDocument;
import org.x52North.sir.x032.InsertSensorInfoRequestDocument.InsertSensorInfoRequest;
import org.x52North.sir.x032.InsertSensorInfoRequestDocument.InsertSensorInfoRequest.InfoToBeInserted;
import org.x52North.sir.x032.ServiceReferenceDocument.ServiceReference;

/**
 * @author Jan Schulte, Daniel Nüst
 * 
 */
public class InsertSensorInfoBean extends TestClientBean {

    private String addRefSensorID = "";

    private String addRefType = "";

    private String addRefURL = "";

    private String sensorDescription = "";

    private String sensorId = "";

    private String serviceInfosServiceSpecificSensorID = "";

    private String serviceInfosServiceType = "";

    private String serviceInfosServiceURL = "";

    @Override
    public void buildRequest() {
        InsertSensorInfoRequestDocument requestDoc = InsertSensorInfoRequestDocument.Factory.newInstance(XmlTools.xmlOptionsForNamespaces());
        InsertSensorInfoRequest request = requestDoc.addNewInsertSensorInfoRequest();
        request.setService(ClientConstants.SERVICE_NAME);
        request.setVersion(ClientConstants.getServiceVersionEnum());

        // InfoToBeInserted
        InfoToBeInserted infoToBeInserted = request.addNewInfoToBeInserted();

        // SensorDescription with optional service reference
        if ( !this.sensorDescription.isEmpty()) {
            this.sensorDescription = this.sensorDescription.trim();

            try {
                AbstractProcessType sensorDescr = infoToBeInserted.addNewSensorDescription();
                SystemType system = (SystemType) sensorDescr.changeType(SystemType.type);
                SensorMLDocument doc = SensorMLDocument.Factory.parse(this.sensorDescription);
                Member member = doc.getSensorML().getMemberArray(0);
                system.set(member.getProcess());
            }
            catch (XmlException e) {
                this.requestString = "Please check the sensor description, it must be a sml:SensorML document.";
                this.requestString += "\n\n" + e.getMessage();
                return;
            }

            // ServiceReference
            if ( !this.serviceInfosServiceType.isEmpty() && !this.serviceInfosServiceType.isEmpty()
                    && !this.serviceInfosServiceSpecificSensorID.isEmpty()) {
                ServiceReference serviceRef = infoToBeInserted.addNewServiceReference();
                // serviceURL
                serviceRef.setServiceURL(this.serviceInfosServiceURL);
                // serviceType
                serviceRef.setServiceType(this.serviceInfosServiceType);
                // serviceSpecificSensorID
                serviceRef.setServiceSpecificSensorID(this.serviceInfosServiceSpecificSensorID);
            }
        }

        // sensor id and service reference
        else if ( !this.sensorId.isEmpty()) {
            infoToBeInserted.setSensorIDInSIR(this.sensorId);

            if ( !this.addRefURL.isEmpty() && !this.addRefType.isEmpty() && !this.addRefSensorID.isEmpty()) {
                ServiceReference serviceRef = infoToBeInserted.addNewServiceReference();
                // serviceURL
                serviceRef.setServiceURL(this.addRefURL);
                // serviceType
                serviceRef.setServiceType(this.addRefType);
                // serviceSpecificSensorID
                serviceRef.setServiceSpecificSensorID(this.addRefSensorID);
            }
        }

        XmlTools.addSirAndSensorMLSchemaLocation(request);

        if ( !requestDoc.validate(XmlTools.xmlOptionsForNamespaces()))
            this.requestString = XmlTools.validateAndIterateErrors(requestDoc);
        else
            this.requestString = requestDoc.xmlText(XmlTools.xmlOptionsForNamespaces());
    }

    public String getAddRefSensorID() {
        return this.addRefSensorID;
    }

    public String getAddRefType() {
        return this.addRefType;
    }

    public String getAddRefURL() {
        return this.addRefURL;
    }

    public String getSensorDescription() {
        return this.sensorDescription;
    }

    public String getSensorId() {
        return this.sensorId;
    }

    public String getServiceInfosServiceSpecificSensorID() {
        return this.serviceInfosServiceSpecificSensorID;
    }

    public String getServiceInfosServiceType() {
        return this.serviceInfosServiceType;
    }

    public String getServiceInfosServiceURL() {
        return this.serviceInfosServiceURL;
    }

    public void setAddRefSensorID(String addRefSensorID) {
        this.addRefSensorID = addRefSensorID;
    }

    public void setAddRefType(String addRefType) {
        this.addRefType = addRefType;
    }

    public void setAddRefURL(String addRefURL) {
        this.addRefURL = addRefURL;
    }

    public void setSensorDescription(String sensorDescription) {
        this.sensorDescription = sensorDescription;
    }

    public void setSensorId(String id) {
        this.sensorId = id;
    }

    public void setServiceInfosServiceSpecificSensorID(String serviceInfosServiceSpecificSensorID) {
        this.serviceInfosServiceSpecificSensorID = serviceInfosServiceSpecificSensorID;
    }

    public void setServiceInfosServiceType(String serviceInfosServiceType) {
        this.serviceInfosServiceType = serviceInfosServiceType;
    }

    public void setServiceInfosServiceURL(String serviceInfosServiceURL) {
        this.serviceInfosServiceURL = serviceInfosServiceURL;
    }

}
