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
package org.n52.oss.sir.harvest;

import static org.junit.Assert.assertFalse;

import java.io.File;

import org.n52.sir.harvest.exec.IJSExecute;
import org.n52.sir.harvest.exec.impl.RhinoJSExecute;

public class OWSHarvest {

//	@Test
	public void harvestOWS(){
		File harvestScript = new File(ClassLoader.getSystemResource(
				"Requests/harvestOWS.js").getFile());
		IJSExecute execEngine = new RhinoJSExecute();
		String sensors = execEngine.execute(harvestScript);
		assertFalse(sensors.equals("-1"));
		
	}
}
