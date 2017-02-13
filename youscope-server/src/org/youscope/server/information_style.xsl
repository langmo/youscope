<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html"/>
<xsl:template match="/">
<html>
<head>
	<style>
		h1
		{
			font-size: 18pt;
			color: black;
			font-weight: bold;
			margin-bottom:10px;
		}
		h2
		{
			font-size: 14pt;
			color: black;
			font-weight: bold;
			margin-bottom:5px;
			margin-top:10px;
		}
		h3
		{
			font-size: 12pt;
			color: black;
			font-weight: bold;
			margin-bottom:5px;
			margin-top:10px;
		}
		p,li,td
		{
			font-size: 10pt;
			color: black;
		}
		p
		{
			margin-top:0;
			margin-bottom:5px;
		}
		li
		{
			margin-top:0;
			margin-bottom:0px;
		}
		td
		{
			vertical-align: text-top;
		}
		.property
		{
			font-weight: bold;
		}
		ul 
		{
			list-style-type: square;
			list-style-position: inside;
			padding: 0;
			margin-top:0;
			margin-bottom:5px;
		}
		ul.silentlist
		{
			list-style-type: none;
		}
		div.showHide
		{
			display:none;
			margin-left:10px;
		}
		.showHideButton
		{
			font-weight: normal;
			color: black;
			text-decoration: underline;
			cursor:pointer;
			cursor:hand;
		}
	</style>
	<title><xsl:value-of select="/measurement-information/name"/></title>
	<script>
		function minimizeMaximize(elementID)
		{
			element = document.getElementById(elementID)
			button = document.getElementById(elementID+".button");
			if(element.style.display != "block")
			{
				element.style.display = "block";
				button.innerHTML = "hide";
			}
			else
			{
				element.style.display = "none";
				button.innerHTML = "show";
			}
		}
	</script>
</head>
<body>
  <h1>Measurement <xsl:value-of select="/measurement-information/name"/></h1>
  <h2>General Information (<span class="showHideButton" onclick="minimizeMaximize('generalInformation')" id="generalInformation.button">hide</span>)</h2>
  <div id="generalInformation" class="showHide" style="display:block">
	  <table border="0">
		<tr>
		  <td class="property">Name:</td>
		  <td><xsl:value-of select="/measurement-information/name"/></td>
		</tr>
		<xsl:for-each select="/measurement-information/metadata-properties/metadata-property">
		<tr>
		  <td class="property"><xsl:value-of select="@name"/>:</td>
		  <td><xsl:value-of select="@value"/></td>
		</tr>
		</xsl:for-each>
		<xsl:for-each select="/measurement-information/description">
		<tr>
		  <td class="property">Description:</td>
		  <td><xsl:apply-templates select="ul|p"/></td>
		</tr>
		</xsl:for-each>
	  </table>
  </div>
  <h2>Channel Configurations (<span class="showHideButton" onclick="minimizeMaximize('channelConfiguration')" id="channelConfiguration.button">show</span>)</h2>
  <div id="channelConfiguration" class="showHide">
	  <xsl:for-each select="/measurement-information/channel-definitions/channel-definition">
		<h3><xsl:value-of select="@group"/>.<xsl:value-of select="@name"/> (<span class="showHideButton" onclick="minimizeMaximize('channel.{@group}.{@name}')" id="channel.{@group}.{@name}.button">show</span>)</h3>
		<div id="channel.{@group}.{@name}" class="showHide">
			<table border="0">
				<tr>
						<td class="property">Shutter:</td>
						<td><xsl:value-of select="@shutter-name"/></td>
				</tr>
				<tr>
						<td class="property">Shutter-Delay:</td>
						<td><xsl:value-of select="@shutter-delay"/> ms</td>
				</tr>
				<tr>
						<td class="property">Settings before imaging:</td>
						<td>
							<ul class="silentlist">
								<xsl:for-each select="device-settings-on/device-setting">
									<li>
										<xsl:value-of select="@device"/>.<xsl:value-of select="@property"/>
										<xsl:choose>
											<xsl:when test="@is-absolute-value='true'">
												=
											</xsl:when>
											<xsl:otherwise>
												+=
											</xsl:otherwise>
										</xsl:choose>
										<xsl:value-of select="@value"/>
									</li>
								</xsl:for-each>
							</ul>
						</td>
				</tr>
				<tr>
						<td class="property">Settings after imaging:</td>
						<td>
							<ul class="silentlist">
								<xsl:for-each select="device-settings-off/device-setting">
									<li>
										<xsl:value-of select="@device"/>.<xsl:value-of select="@property"/>
										<xsl:choose>
											<xsl:when test="@is-absolute-value='true'">
												=
											</xsl:when>
											<xsl:otherwise>
												+=
											</xsl:otherwise>
										</xsl:choose>
										<xsl:value-of select="@value"/>
									</li>
								</xsl:for-each>
							</ul>
						</td>
				</tr>
			</table>
		</div>
	  </xsl:for-each>
  </div>
  <h2>Initial Microscope State (<span class="showHideButton" onclick="minimizeMaximize('initialState')" id="initialState.button">show</span>)</h2>
  <div id="initialState" class="showHide">
	  <xsl:for-each select="/measurement-information/device-settings-at-startup/device">
		<h3><xsl:value-of select="@name"/> (<span class="showHideButton" onclick="minimizeMaximize('device.{@name}')" id="device.{@name}.{button}">show</span>)</h3>
		<div id="device.{@name}" class="showHide">
			<table border="0">
				<xsl:for-each select="property">
				<tr>
						<td class="property"><xsl:value-of select="@name"/>:</td>
						<td>
							<xsl:value-of select="@value"/>
						</td>
				</tr>
				</xsl:for-each>
			</table>
		</div>
	  </xsl:for-each>
  </div>
</body>
</html>
</xsl:template>
<xsl:template match="p">
	<p>
		<xsl:apply-templates/>
	</p>
</xsl:template>
<xsl:template match="ul">
	<ul>
		<xsl:for-each select="li">
			<li>
				<xsl:apply-templates/>
			</li>
		</xsl:for-each>
	</ul>
</xsl:template>
<xsl:template match="text()">
	<xsl:value-of select="normalize-space()"/>
</xsl:template>
</xsl:stylesheet>
