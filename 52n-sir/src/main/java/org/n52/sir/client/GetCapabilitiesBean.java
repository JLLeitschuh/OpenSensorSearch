/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.sir.client;

import net.opengis.ows.x11.AcceptVersionsType;
import net.opengis.ows.x11.SectionsType;

import org.n52.sir.SirConfigurator;
import org.n52.sir.SirConfigurator.Section;
import org.n52.sir.util.XmlTools;

import de.uniMuenster.swsl.sir.GetCapabilitiesDocument;
import de.uniMuenster.swsl.sir.GetCapabilitiesDocument.GetCapabilities;

/**
 * @author Jan Schulte
 * 
 */
public class GetCapabilitiesBean extends AbstractBean {

    private String service;

    private String updateSequence = "";

    private String acceptVersions;

    private boolean serviceIdentification;

    private boolean serviceProvider;

    private boolean operationsMetadata;

    private boolean contents;

    private boolean all;

    /**
     * 
     */
    public GetCapabilitiesBean() {
        super();
        
        StringBuilder sb = new StringBuilder();
        String[] acceptedServiceVersions = SirConfigurator.getInstance().getAcceptedServiceVersions();
        for (String s : acceptedServiceVersions) {
            sb.append(s);
            sb.append(",");
        }
        sb.replace(sb.length()-1, sb.length(), "");
        this.acceptVersions = sb.toString();
    }

    /**
     * @param service
     * @param updateSequence
     * @param acceptVersions
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     * @param contents
     * @param all
     */
    public GetCapabilitiesBean(String service,
                               String updateSequence,
                               String acceptVersions,
                               boolean serviceIdentification,
                               boolean serviceProvider,
                               boolean operationsMetadata,
                               boolean contents,
                               boolean all) {
        super();

        this.service = service;
        this.updateSequence = updateSequence;
        this.acceptVersions = acceptVersions;
        this.serviceIdentification = serviceIdentification;
        this.serviceProvider = serviceProvider;
        this.operationsMetadata = operationsMetadata;
        this.contents = contents;
        this.all = all;
    }

    /**
     * @return the service
     */
    public String getService() {
        return this.service;
    }

    /**
     * @param service
     *        the service to set
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * @return the updateSequence
     */
    public String getUpdateSequence() {
        return this.updateSequence;
    }

    /**
     * @param updateSequence
     *        the updateSequence to set
     */
    public void setUpdateSequence(String updateSequence) {
        this.updateSequence = updateSequence;
    }

    /**
     * @return the acceptVersions
     */
    public String getAcceptVersions() {
        return this.acceptVersions;
    }

    /**
     * @param acceptVersions
     *        the acceptVersions to set
     */
    public void setAcceptVersions(String acceptVersions) {
        this.acceptVersions = acceptVersions;
    }

    /**
     * @return the serviceIdentification
     */
    public boolean isServiceIdentification() {
        return this.serviceIdentification;
    }

    /**
     * @param serviceIdentification
     *        the serviceIdentification to set
     */
    public void setServiceIdentification(boolean serviceIdentification) {
        this.serviceIdentification = serviceIdentification;
    }

    /**
     * @return the serviceProvider
     */
    public boolean isServiceProvider() {
        return this.serviceProvider;
    }

    /**
     * @param serviceProvider
     *        the serviceProvider to set
     */
    public void setServiceProvider(boolean serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    /**
     * @return the operationsMetadata
     */
    public boolean isOperationsMetadata() {
        return this.operationsMetadata;
    }

    /**
     * @param operationsMetadata
     *        the operationsMetadata to set
     */
    public void setOperationsMetadata(boolean operationsMetadata) {
        this.operationsMetadata = operationsMetadata;
    }

    /**
     * @return the contents
     */
    public boolean isContents() {
        return this.contents;
    }

    /**
     * @param contents
     *        the contents to set
     */
    public void setContents(boolean contents) {
        this.contents = contents;
    }

    /**
     * @return the all
     */
    public boolean isAll() {
        return this.all;
    }

    /**
     * @param all
     *        the all to set
     */
    public void setAll(boolean all) {
        this.all = all;
    }

    @Override
    public void buildRequest() {
        this.responseString = "";

        GetCapabilitiesDocument requestDoc = GetCapabilitiesDocument.Factory.newInstance();
        GetCapabilities request = requestDoc.addNewGetCapabilities();

        // service
        request.setService(this.service);

        // update sequence
        if ( !this.updateSequence.isEmpty()) {
            request.setUpdateSequence(this.updateSequence);
        }

        // accept versions
        if ( !this.acceptVersions.isEmpty()) {
            AcceptVersionsType acceptVersion = request.addNewAcceptVersions();
            String[] versions = this.acceptVersions.split(",");
            for (String s : versions) {
                acceptVersion.addVersion(s);
            }
        }

        // Sections
        if (this.all || this.contents || this.operationsMetadata || this.serviceIdentification || this.serviceProvider) {
            SectionsType sections = request.addNewSections();

            // all
            if (this.all) {
                sections.addSection(Section.ServiceIdentification.name());
                sections.addSection(Section.ServiceProvider.name());
                sections.addSection(Section.OperationsMetadata.name());
                sections.addSection(Section.Contents.name());
            }

            else {
                // service Identification
                if (this.serviceIdentification) {
                    sections.addSection(Section.ServiceIdentification.name());
                }
                // service provider
                if (this.serviceProvider) {
                    sections.addSection(Section.ServiceProvider.name());
                }
                // operations Metadata
                if (this.operationsMetadata) {
                    sections.addSection(Section.OperationsMetadata.name());
                }
                // Contents
                if (this.contents) {
                    sections.addSection(Section.Contents.name());
                }
            }
        }

        // TODO implement handling of acceptFormats

        XmlTools.addSirAndSensorMLSchemaLocation(request);

        if (requestDoc.validate())
            this.requestString = requestDoc.xmlText(XmlTools.xmlOptionsForNamespaces());
        else
            this.requestString = XmlTools.validateAndIterateErrors(requestDoc);
    }

}