/**
 * ﻿Copyright (C) 2012 52°North Initiative for Geospatial Open Source Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @author Yakoub
 */

package org.n52.oss.IT.solr;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.Collection;

import net.opengis.sensorML.x101.SensorMLDocument;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.oss.sir.api.SirDetailedSensorDescription;
import org.n52.oss.sir.api.SirSearchResultElement;
import org.n52.oss.sir.api.SirSensor;
import org.n52.oss.sir.ows.OwsExceptionReport;
import org.n52.sir.ds.solr.SOLRInsertSensorInfoDAO;
import org.n52.sir.ds.solr.SOLRSearchSensorDAO;
import org.n52.sir.ds.solr.SolrConnection;
import org.n52.sir.sml.SensorMLDecoder;

public class SearchByValidTimeTest {

    private static SolrConnection connection;

    @BeforeClass
    public static void prepare() {
        connection = new SolrConnection("http://localhost:8983/solr", 2000);
    }

	@Before
	public void insertTestSensor() throws XmlException, IOException,
			OwsExceptionReport {
		String basePath = (this.getClass().getResource("/Requests").getFile());
		File sensor_file = new File(basePath+"/testSensor.xml");
		SensorMLDocument doc = SensorMLDocument.Factory.parse(sensor_file);
        SensorMLDecoder d = new SensorMLDecoder();
        SirSensor sensor = d.decode(doc);
        SOLRInsertSensorInfoDAO dao = new SOLRInsertSensorInfoDAO(connection);
		dao.insertSensor(sensor);
	}

	@Test
	public void searchByValidTime() {
        SOLRSearchSensorDAO dao = new SOLRSearchSensorDAO(connection);
		Date start = new Date(1262296800000l);
		Date end = new Date(1325282400000l);

		Collection<SirSearchResultElement> results = dao
				.searchByValidTimeRange(start, end);
		for (SirSearchResultElement element : results) {
			SirDetailedSensorDescription description = (SirDetailedSensorDescription) element
					.getSensorDescription();
			assertTrue(description.getBegineDate().getTime() >= start.getTime());
			assertTrue(description.getEndDate().getTime() <= end.getTime());
		}

	}

	@After
	public void deleteSensor() throws SolrServerException, IOException {
        connection.deleteSensor(""); // FIXME delete inserted sensor
	}

}
