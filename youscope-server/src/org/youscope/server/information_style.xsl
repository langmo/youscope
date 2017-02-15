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
		}
		.leftrightbutton
		{
			display:inline-block;
			text-align:center;
			color:#ffffff;
			background-color:#000000;
			overflow:hidden;
			z-index:1;
			padding:0;
			cursor:pointer;
			font-size:24px;
			width:40px;
			height:300px;
			line-height:300px
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
		function readImageTable(file)
		{
			// IE doesn't like XMLHttpRequest on localhost
			var rawFile = null;
			try
			{
				//IE
				rawFile = new ActiveXObject("MSXML2.XMLHTTP");
			}
			catch(e)
			{
				// other browsers
				rawFile = new XMLHttpRequest();
				rawFile.overrideMimeType("text/plain");
			}
			rawFile.open("GET", file, true);
			rawFile.onreadystatechange = function ()
			{
				if(rawFile.readyState === 4)
				{
					if(rawFile.status === 200 || rawFile.status == 0)
					{
						var allText = rawFile.responseText;
						processImageTable(allText);
					}
				}
			}
			rawFile.send(null);
		}
		var images = {};
		function processImageTable(table)
		{
			var lines = table.split("\n");
			var i, len;
			for (i = 1, len = lines.length; i &lt; len; ++i) 
			{
				var entries = lines[i].split(";");
				if(entries.length &lt; 12)
					continue;
				var posString = entries[5].replace(/["]/g, '').trim();
				var wellString = entries[4].replace(/["]/g, '').trim();
				var position;
				if(!posString &amp;&amp; !wellString)
					position ="base";
				else if(!posString)
					position = wellString;
				else if(!wellString)
					position = posString;
				else
					position = wellString+"."+posString;
				if(!(position in images))
					images[position] = {};
				var channel = entries[10].replace(/["]/g, '').trim();
				if(!(channel in images[position]))
					images[position][channel] = {};
				var imageNumber = entries[0].replace(/["]/g, '').trim();
				images[position][channel][imageNumber] = entries[6].replace(/["]/g, '');
			}
			list = document.getElementById("imagePositionList");
			for(var position in images)
			{
				var opt = document.createElement("option");
				opt.text = position;
				opt.value = position;
				list.add(opt, null);
		    }
			loadChannels();
		}
		function loadChannels()
		{
			positionList = document.getElementById("imagePositionList");
			channelList = document.getElementById("imageChannelList");
			while (channelList.options.length > 0) 
			{
				channelList.remove(channelList.options.length - 1);
			}
			if(!positionList.options[positionList.selectedIndex])
				return;
			position = positionList.options[positionList.selectedIndex].value;
			for(var channel in images[position])
			{
				var opt = document.createElement("option");
				opt.text = channel;
				opt.value = channel;
				channelList.add(opt, null);
			}
			loadImages();
		}
		var currentImageIndex = 0;
		var currentPosition = 0;
		var currentChannel = 0;
		var currentIndices = [];
		function loadImages()
		{
			var positionList = document.getElementById("imagePositionList");
			var channelList = document.getElementById("imageChannelList");
			
			currentImageIndex = 0;
			if(!positionList.options[positionList.selectedIndex])
				return;
			currentPosition = positionList.options[positionList.selectedIndex].value;
			currentChannel = channelList.options[channelList.selectedIndex].value;
			currentIndices = [];
			for(var index in images[currentPosition][currentChannel])
			{
				currentIndices.push(index);
		    }
			currentIndices.sort();
			
			loadImage(currentImageIndex);
		}
		function slideImage(n) 
		{
			loadImage(currentImageIndex += n);
		}

		function loadImage(n) 
		{
			var imageDiv = document.getElementById("imageDiv");
			var image = document.getElementById("image");
			var imageLink = document.getElementById("imageLink");
			var currentImage = document.getElementById("currentImage");
			if(currentIndices.length &lt;= 0)
			{
				// remove previous image
				while(imageDiv.firstChild) 
				{
					imageDiv.removeChild(imageDiv.firstChild);
				}
				currentImage.innerHTML="No images available.";
			}
			else
			{
				if(n &gt;= currentIndices.length)
					currentImageIndex = 0;
				else if(n &lt; 0) 
					currentImageIndex = currentIndices.length-1;
				var imageFile = images[currentPosition][currentChannel][currentIndices[currentImageIndex]];
				var imageFileType = imageFile.substring(imageFile.lastIndexOf(".")+1);
				if(!image || !imageLink)
				{
					while(imageDiv.firstChild) 
					{
						imageDiv.removeChild(imageDiv.firstChild);
					}
					
					imageLink = document.createElement("a");
					imageLink.href = imageFile;
					imageLink.target = "_blank";
				
					image = document.createElement("img");
					image.src = imageFile;
					image.id = "image";
					image.height = 300;
					image.style ="margin:0px;padding:0px;border:0;";
					imageLink.appendChild(image);
					imageDiv.appendChild(imageLink);
				}
				else
				{
					image.src = imageFile;
					imageLink.href = imageFile;
				}
				image.alt ="Either the image file could not be found, or your browser does not support the file ending "+imageFileType+".";
					
				currentImage.innerHTML="Image "+(currentImageIndex+1)+" of "+ currentIndices.length+" ("+imageFile+")";
			}
		}
		window.onload = function() 
		{
	  		readImageTable("<xsl:value-of select="/measurement-information/files/file[@name = 'image-table']/@path"/>");
		};
		
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
  <h2>Images (<span class="showHideButton" onclick="minimizeMaximize('images')" id="images.button">hide</span>)</h2>
  <div id="images" class="showHide" style="display:block">
  	 <p>
		 Position: <select id="imagePositionList" onchange="loadChannels()">
		 </select> 
		 Channel: <select id="imageChannelList" onchange="loadImages()">
		 </select>
	 </p>
	 <div style="overflow:hidden;">
		 <span style="float:left;width:40px;height:300px;margin:0px;padding:0px;">
			<a class="leftrightbutton" onclick="slideImage(-1)">&#10094;</a>
		</span>
		<span id="imageDiv" style="float:left;height:300px;margin:0px;padding:0px">
		</span>
		<span style="float:left;width:40px;height:300px;margin:0px;padding:0px;">
			<a class="leftrightbutton" onclick="slideImage(+1)">&#10095;</a>
		</span>
	 </div>
	 <p id="currentImage">No images available.</p>
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
