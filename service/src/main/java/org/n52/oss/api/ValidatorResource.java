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

package org.n52.oss.api;

import java.io.IOException;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.oss.sir.ows.OwsExceptionReport;
import org.n52.sir.SirConfigurator;
import org.n52.sir.xml.IProfileValidator;

import com.google.inject.Inject;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * @author Yakoub
 * 
 */
@Path(ApiPaths.CHECK_PATH)
@Api(value = "/" + ApiPaths.CHECK_PATH, description = "validation of SensorML for future harvesting")
public class ValidatorResource {
    // private IProfileValidator validator;

    private IProfileValidator validator;

    @Inject
    public ValidatorResource(SirConfigurator config) throws OwsExceptionReport {
        this.validator = config.getInstance().getValidatorFactory().getSensorMLProfile4DiscoveryValidator();
    }

    private boolean validateSensorMLDocument(String s) throws XmlException, IOException {
        boolean b = this.validator.validate(XmlObject.Factory.parse(s));
        return b;
    }

    private String returnJSON(String s) {
        try {
            boolean b = validateSensorMLDocument(s);
            if (b)
                return "{\"status\": \"valid\"}";

            String details = this.validator.getValidationFailuresAsString();
            return "{\"status\" : \"invalid \", \"error\" : \" " + details + " \" }";

        }
        catch (XmlException | IOException exception) {
            return "{error:" + exception.getMessage() + "}";
        }
    }

    @GET
    @ApiOperation(value = "index of the available validations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIndex() {
        StringBuilder sb = new StringBuilder();

        sb.append("{ \"validations\" : [");
        sb.append(" { \"text/xml;subtype='sensorML/1.0.0'\" : ");
        sb.append("conformity with discovery profile");
        sb.append(" } ");
        sb.append("] }");

        return Response.ok(sb.toString()).build();
    }

    @POST
    @Path("/sensorML")
    @ApiOperation(value = "Validates a given SensorML Document")
    @Produces(MediaType.APPLICATION_JSON)
    public Response check(@FormParam("sensor")
    String sensor, @FormParam("format")
    String format) {
        if (format.equals("json")) {
            return Response.ok(returnJSON(sensor)).build();
        }
        return Response.ok().build();
    }

}
