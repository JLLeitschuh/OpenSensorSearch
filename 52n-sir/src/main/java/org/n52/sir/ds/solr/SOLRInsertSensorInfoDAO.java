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
/**
 * @author Yakoub
 */

package org.n52.sir.ds.solr;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.swe.x101.PositionType;
import net.opengis.swe.x101.VectorType;
import net.opengis.swe.x101.VectorType.Coordinate;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.n52.sir.datastructure.SirSensor;
import org.n52.sir.datastructure.SirSensorIdentification;
import org.n52.sir.datastructure.SirServiceReference;
import org.n52.sir.datastructure.SirTimePeriod;
import org.n52.sir.ds.IInsertSensorInfoDAO;
import org.n52.sir.ows.OwsExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOLRInsertSensorInfoDAO implements IInsertSensorInfoDAO {
	private static Logger log = LoggerFactory
			.getLogger(SOLRInsertSensorInfoDAO.class);

	@Override
	public String addNewReference(SirSensorIdentification sensIdent,
			SirServiceReference servRef) throws OwsExceptionReport {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String deleteReference(SirSensorIdentification sensIdent,
			SirServiceReference servRef) throws OwsExceptionReport {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String deleteSensor(SirSensorIdentification sensIdent)
			throws OwsExceptionReport {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String insertSensor(SirSensor sensor) throws OwsExceptionReport {
		// TODO Auto-generated method stub
		// Get the keywords first

		// TODO moh-yakoub: implement a mechanism to get access to the various
		// identifiers
		// Map<String, String> futherIds = sensor.getIdentifiers();

		Collection<String> keywords = sensor.getKeywords();
		// Get the connection of solr Server
		SolrConnection connection = new SolrConnection();
		SolrInputDocument inputDocument = new SolrInputDocument();

		for (String s : keywords) {
			inputDocument.addField(SolrConstants.KEYWORD, s);
		}
		// FIXME use OSS-wide id generator
		String id = UUID.randomUUID().toString();
		inputDocument.addField(SolrConstants.ID, id);
		String longitude = sensor.getLongitude();
		String latitude = sensor.getLatitude();
		if (sensor.getbBox() != null) {
			double[] center = sensor.getbBox().getCenter();
			if (center != null) {
				String center_cords = center[0] + "," + center[1];
				inputDocument.addField(SolrConstants.BBOX_CENTER, center_cords);
			}
		}

		SirTimePeriod timePeriod = sensor.getTimePeriod();
		if (timePeriod != null) {
			Date startDate = timePeriod.getStartTime();
			Date endDate = timePeriod.getEndTime();
			if (startDate != null)
				inputDocument.addField(SolrConstants.START_DATE, startDate.getTime());
			if (endDate != null)
				inputDocument.addField(SolrConstants.END_DATE, endDate.getTime());
		}
		if (longitude.length() > 0 && latitude.length() > 0)
			inputDocument.addField(SolrConstants.LOCATION, latitude + ","
					+ longitude);

		try {
			connection.addInputDocument(inputDocument);
			connection.commitChanges();
		} catch (SolrServerException e) {
			log.error("Could not create connection to Solr.", e);
		} catch (IOException e) {
			log.error("IOException", e);
		}

		// TODO add the database ID
		return null;
	}

	@Override
	public String updateSensor(SirSensorIdentification sensIdent,
			SirSensor sensor) throws OwsExceptionReport {
		// TODO Auto-generated method stub
		return null;
	}

}
