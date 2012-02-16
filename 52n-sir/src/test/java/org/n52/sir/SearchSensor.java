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

import de.uniMuenster.swsl.sir.SearchSensorRequestDocument;
import de.uniMuenster.swsl.sir.SearchSensorResponseDocument;

/**
 * 
 * @author Daniel Nüst
 * 
 */
public class SearchSensor extends SirTestCase {

    @Test
    public void testPostExampleByID() throws Exception {
        File f = getPostExampleFile("SearchSensor_bySensorIDInSIR.xml");
        SearchSensorRequestDocument req = SearchSensorRequestDocument.Factory.parse(f);

        XmlObject response = Client.xSendPostRequest(req);

        // parse and validate response
        SearchSensorResponseDocument responseDoc = SearchSensorResponseDocument.Factory.parse(response.getDomNode());

        int send = req.getSearchSensorRequest().getSensorIdentificationArray().length;
        int inserted = responseDoc.getSearchSensorResponse().sizeOfSearchResultElementArray();

        assertEquals(send, inserted);
        assertTrue(responseDoc.validate());
    }

    @Test
    public void testPostExampleByDescr() throws Exception {
        File f = getPostExampleFile("SearchSensor_byServiceDescription.xml");
        SearchSensorRequestDocument req = SearchSensorRequestDocument.Factory.parse(f);

        XmlObject response = Client.xSendPostRequest(req);

        // parse and validate response
        SearchSensorResponseDocument responseDoc = SearchSensorResponseDocument.Factory.parse(response.getDomNode());
        assertTrue(responseDoc.validate());
    }
}