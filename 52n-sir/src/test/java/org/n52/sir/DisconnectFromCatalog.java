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
package org.n52.sir;

import java.io.File;

import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.n52.sir.client.Client;
import org.n52.sir.client.ConnectToCatalogBean;
import org.n52.sir.client.DisconnectFromCatalogBean;

import de.uniMuenster.swsl.sir.DisconnectFromCatalogRequestDocument;
import de.uniMuenster.swsl.sir.DisconnectFromCatalogResponseDocument;

/**
 * 
 * @author Daniel Nüst
 * 
 */
public class DisconnectFromCatalog extends SirTestCase {

    private String catalogURL = "http://localhost:8080/ergorr/webservice";

    @Test
    public void testPost() throws Exception {
        setUpConnection(this.catalogURL);

        // buildRequest
        DisconnectFromCatalogBean dfcb = new DisconnectFromCatalogBean(this.catalogURL);

        dfcb.buildRequest();

        // send request
        String response = Client.sendPostRequest(dfcb.getRequestString());

        // parse and validate response
        DisconnectFromCatalogResponseDocument responseDoc = DisconnectFromCatalogResponseDocument.Factory.parse(response);
        assertTrue(responseDoc.validate());

        assertEquals(this.catalogURL, responseDoc.getDisconnectFromCatalogResponse().getCatalogURL());
    }

    @Test
    public void testPostExample() throws Exception {
        File f = getPostExampleFile("ConnectToCatalog.xml");
        DisconnectFromCatalogRequestDocument dfcrd = DisconnectFromCatalogRequestDocument.Factory.parse(f);

        String sentUrl = dfcrd.getDisconnectFromCatalogRequest().getCatalogURL();
        setUpConnection(sentUrl);

        XmlObject response = Client.xSendPostRequest(dfcrd);

        // parse and validate response
        DisconnectFromCatalogResponseDocument responseDoc = DisconnectFromCatalogResponseDocument.Factory.parse(response.getDomNode());
        assertTrue(responseDoc.validate());

        assertEquals(sentUrl, responseDoc.getDisconnectFromCatalogResponse().getCatalogURL());
    }

    /**
     * 
     * Junit tag @Before did not work...
     * 
     * @param url
     * @throws Exception
     */
    private void setUpConnection(String url) throws Exception {
        System.out.println("Creating temporary conneciton for immediate deletion: " + url);
        int pushInterval = 3600; // needs to be with repetition, otherwise not saved in database for removal.
        ConnectToCatalogBean ctcb = new ConnectToCatalogBean(url, pushInterval);
        ctcb.buildRequest();
        Client.sendPostRequest(ctcb.getRequestString());
    }

}