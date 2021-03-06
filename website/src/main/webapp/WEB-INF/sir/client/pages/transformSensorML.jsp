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
<!DOCTYPE html>

<html lang="en">
<head>
<title>Transform SensorML Document | Open Sensor Search</title>

<jsp:include page="htmlHead.jsp"></jsp:include>

</head>

<body>

	<div id="content">

		<jsp:include page="header.jsp" />
		<jsp:include page="../menu.jsp" />

		<h2>Transform SensorML Document</h2>

		<p>Insert the SensorML description of a sensor:</p>

		<form id="sml" method="post">
			<p class="textareaBorder">
				<textarea id="input" name="request" class="smallTextarea" rows="10"
					cols="10">
					<!-- enter SensorML here -->
				</textarea>
			</p>
			<p>
				<input type="submit" name="requestTransformation" value="Transform" />
			</p>
		</form>

		<p>Transformed ebRIM representation of sensor description:</p>

		<p class="textareaBorder">
			<textarea id="output" class="largeTextarea" rows="10" cols="10">...</textarea>
		</p>

	</div>

	<script type="text/javascript">
		$("#sml").on(
				"submit",
				function(e) {
					e.preventDefault();
					var url = ossApiEndpoint + "/convert";
					var payload = $("#input").val();
					console.log("Sending request to " + url + " with data: "
							+ payload);
					$.ajax({
						type : "POST",
						cache : false,
						url : url,
						data : payload, //$(this).serialize(),
						dataType : "xml",
						success : function(data) {
							console.log(data);
							var xmlstr = data.xml ? data.xml : (new XMLSerializer()).serializeToString(data);
							$("#output").val(xmlstr);
						}
					}).fail(
							function(data) {
								console.log(data);
								$("#output").val(
										data.status + " " + data.statusText
												+ "\n\n" + data.responseText);
							});

				});
	</script>
</body>
</html>