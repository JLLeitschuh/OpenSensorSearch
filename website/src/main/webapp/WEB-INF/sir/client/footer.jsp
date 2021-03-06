<%--

    ﻿Copyright (C) 2013 52°North Initiative for Geospatial Open Source Software GmbH

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<div id="footer">
	<p class="infotext">
		Open Sensor Search is powered by the 52&deg;North Sensor Instance
		Registry. <a
			href="http://52north.org/communities/sensorweb/incubation/discovery/"
			title="Sensor Discovery by 52N">Find out more</a>.
	</p>
	<p class="infotext">
		<a href="./">Home</a> | <a href="client.jsp">Extended Client</a> | <a
			href="formClient.html">Form Client</a>
	</p>
	<p class="infotext">
		&copy;
		<%=new java.text.SimpleDateFormat("yyyy").format(new java.util.Date())%>
		<a href="http://52north.org">52&deg;North Initiative for
			Geospatial Software GmbH</a>
	</p>
	<p class="infotext">
		For harvesting your sensor <a href="/scriptUpload.jsp">Upload here</a>.
	</p>

	<p class="infotext">${project.build.finalName} ${version}</p>

</div>