function showHide(elementID)
{
	var element = document.getElementById(elementID);
	var placeholder = document.getElementById(elementID+".placeholder");
	if(element.style.display != "block")
	{
		element.style.display = "block";
		placeholder.style.display = "none";
	}
	else
	{
		element.style.display = "none";
		placeholder.style.display = "block";
	}
}
function createReleaseElements(releases, elementID)
{
	var releasesElement = document.getElementById(elementID);
	while(releasesElement.firstChild) 
	{
		releasesElement.removeChild(releasesElement.firstChild);
	}
	
	// Find out important releases
	var latestStable = -1;
	var latestNightly = -1;
	var latestPre = -1;
	for(var i=0; i<releases.length; i++)
	{
		if(releases[i].tag_name == "nightly")
		{
			if(latestNightly < 0)
				latestNightly = i;
		}
		else if(!releases[i].prerelease)
		{
			if(latestStable < 0)
				latestStable = i;
			break;
		}
		else
		{
			if(latestPre < 0)
				latestPre = i;
			break;
		}
		if(latestNightly>=0 && latestStable>= 0 && latestPre >= 0)
			break;
	}
	
	// Latest stable release
	var header = document.createElement("h2");
	if(latestPre>latestStable)
		header.innerHTML = "Current Release";
	else
		header.innerHTML = "Latest Stable Release";
	releasesElement.appendChild(header);
	releasesElement.appendChild(createReleaseElement(releases[latestStable], false));	

	// Latest prerelease
	if(latestPre<latestStable)
	{
		header = document.createElement("h2");
		header.innerHTML = "Latest Pre-Release";
		releasesElement.appendChild(header);
		releasesElement.appendChild(createReleaseElement(releases[latestPre], false));	
	}
	
	if(latestNightly >= 0)
	{
		header = document.createElement("h2");
		header.innerHTML = "Nightly Builds";
		releasesElement.appendChild(header);
		releasesElement.appendChild(createNightlyElement(releases[latestPre], false));	
	}
	
	header = document.createElement("h2");
	header.innerHTML = "Old Releases";
	releasesElement.appendChild(header);
	for(var i=1; i<releases.length; i++)
	{
		if(i==latestStable || (i == latestPre && latestPre < latestStable) || i==latestNightly)
			continue;
		releasesElement.appendChild(createReleaseElement(releases[i], false));		
	}		
}
function createReleaseElement(release, show)
{
	var id = release.tag_name;
	var lines =release.body.split("\n");
	var releaseDiv = document.createElement("div");
	var nameElem = document.createElement("h3");
	nameElem.innerHTML = release.name+" ("+id+", "+release.published_at.substring(0, 10)+")";
	releaseDiv.appendChild(nameElem);
	
	var placeholderElem = document.createElement("div");
	placeholderElem.id = id+".placeholder";
	if(!show)
		placeholderElem.style.display = "block";
	else
		placeholderElem.style.display = "none";
	var textPreviewElem = document.createElement("p");
	var textPreviewElemText = document.createElement("span");
	textPreviewElemText.appendChild(document.createTextNode("Description: "));
	textPreviewElemText.style.fontWeight="bold";
	textPreviewElem.appendChild(textPreviewElemText);
	for(var lID=0; lID<lines.length; lID++)
	{
		if(lines[lID].substring(0,1) != '#' && lines[lID].trim().length > 0)
		{
			if(lines[lID].length <=49)
				textPreviewElem.appendChild(document.createTextNode(lines[lID].trim()+".."));
			else
				textPreviewElem.appendChild(document.createTextNode(lines[lID].substring(0, 49).trim()+".."));
			break;
		}
	}
	var showElem = document.createElement("a");
	showElem.href="javascript:showHide(\""+id+"\")";
	showElem.innerHTML = "show";
	textPreviewElem.appendChild(document.createTextNode("("));
	textPreviewElem.appendChild(showElem);
	textPreviewElem.appendChild(document.createTextNode(")"));
	placeholderElem.appendChild(textPreviewElem);
	releaseDiv.appendChild(placeholderElem);
	
	var optionalElem = document.createElement("div");
	optionalElem.id = id;
	if(show)
		optionalElem.style.display = "block";
	else
		optionalElem.style.display = "none";
	releaseDiv.appendChild(optionalElem);
	var listElem = null;
	for(var lID =0; lID < lines.length; lID++)
	{
		if(lines[lID].substring(0,1) == '-')
		{
			if(listElem == null)
			{
				listElem = document.createElement("ul");
				optionalElem.appendChild(listElem);
			}
			var lineElem = document.createElement("li");
			lineElem.innerHTML = lines[lID].substring(1);
			listElem.appendChild(lineElem);
		}
		else
		{
			listElem = null;
			var lineElem = document.createElement("p");
			if(lines[lID].substring(0,1) == '#')
			{
				lineElem.innerHTML = lines[lID].substring(1);
				lineElem.style.fontWeight="bold";
			}
			else
			{
				lineElem.innerHTML = lines[lID];
			}
			optionalElem.appendChild(lineElem);
		}
	}
	var hideElem = document.createElement("a");
	hideElem.href="javascript:showHide(\""+id+"\")";
	hideElem.innerHTML = "hide";
	var hideAllElem = document.createElement("p");
	hideAllElem.appendChild(document.createTextNode("("));
	hideAllElem.appendChild(hideElem);
	hideAllElem.appendChild(document.createTextNode(")"));
	optionalElem.appendChild(hideAllElem);
	
	var assets = release.assets;
	if(assets.length > 0)
	{
		var addAsset = function(asset)
		{
			var assetURL = document.createElement("a");
			assetURL.href = asset.browser_download_url;
			assetURL.target = "_blank";
			assetURL.innerHTML = asset.name;			
			return assetURL;
		};
		var delIdx = {};
		for(var lID =0; lID < assets.length; lID++)
		{
			var name = assets[lID].name.toLowerCase();
			if(name.indexOf("_32bit_installer.exe")>=0)
			{
				var assetElem = document.createElement("p");
				var assetElemText = document.createElement("span");
				assetElemText.appendChild(document.createTextNode("Windows (32bit): "));
				assetElemText.style.fontWeight="bold";
				assetElem.appendChild(assetElemText);
				assetElem.appendChild(addAsset(assets[lID]));
				releaseDiv.appendChild(assetElem);
				delIdx[lID] = null;
				break;
			}
		}
		for(var lID =0; lID < assets.length; lID++)
		{
			var name = assets[lID].name.toLowerCase();
			if(name.indexOf("_64bit_installer.exe")>=0)
			{
				var assetElem = document.createElement("p");
				var assetElemText = document.createElement("span");
				assetElemText.appendChild(document.createTextNode("Windows (64bit): "));
				assetElemText.style.fontWeight="bold";
				assetElem.appendChild(assetElemText);
				assetElem.appendChild(addAsset(assets[lID]));
				releaseDiv.appendChild(assetElem);
				delIdx[lID] = null;
				break;
			}
		}
		for(var lID =0; lID < assets.length; lID++)
		{
			var name = assets[lID].name.toLowerCase();
			if(name.indexOf("_3264bit_installer.exe")>=0)
			{
				var assetElem = document.createElement("p");
				var assetElemText = document.createElement("span");
				assetElemText.appendChild(document.createTextNode("Windows (combined 32 and 64bit): "));
				assetElemText.style.fontWeight="bold";
				assetElem.appendChild(assetElemText);
				assetElem.appendChild(addAsset(assets[lID]));
				releaseDiv.appendChild(assetElem);
				delIdx[lID] = null;
				break;
			}
		}
		
		var sourceElem = document.createElement("p");
		var sourceElemText = document.createElement("span");
		sourceElemText.appendChild(document.createTextNode("Source Code Version: "));
		sourceElemText.style.fontWeight="bold";
		sourceElem.appendChild(sourceElemText);
		var sourceURL = document.createElement("a");
		sourceURL.href = "https://github.com/langmo/youscope/tree/"+id;
		sourceURL.target = "_blank";
		sourceURL.innerHTML = "Tag "+id;			
		sourceElem.appendChild(sourceURL);
		releaseDiv.appendChild(sourceElem);
		
		
		if(assets.length > Object.keys(delIdx).length)
		{
			var additionalElem = document.createElement("p");
			additionalElem.appendChild(document.createTextNode("Additional files: "));
			additionalElem.style.fontWeight="bold";
			additionalElem.style.marginBottom = "0cm";
			releaseDiv.appendChild(additionalElem);
			var listElem = document.createElement("ul");
			listElem.style.marginLeft = "0cm";
			listElem.style.marginTop = "0cm";
			for(var lID =0; lID < assets.length; lID++)
			{
				if(lID in delIdx)
					continue;
				var assetElem = document.createElement("li");
				assetElem.appendChild(addAsset(assets[lID]));
				listElem.appendChild(assetElem);
			}
			releaseDiv.appendChild(listElem);
		}
		releaseDiv.appendChild(document.createElement("hr"));
	}
	
	return releaseDiv;
}

function createNightlyElement(release, show)
{
	var id = release.tag_name;
	var lines =release.body.split("\n");
	var releaseDiv = document.createElement("div");	
	var placeholderElem = document.createElement("div");
	placeholderElem.id = id+".placeholder";
	if(!show)
		placeholderElem.style.display = "block";
	else
		placeholderElem.style.display = "none";
	var textPreviewElem = document.createElement("p");
	var textPreviewElemText = document.createElement("span");
	textPreviewElemText.appendChild(document.createTextNode("Description: "));
	textPreviewElemText.style.fontWeight="bold";
	textPreviewElem.appendChild(textPreviewElemText);
	for(var lID=0; lID<lines.length; lID++)
	{
		if(lines[lID].substring(0,1) != '#' && lines[lID].trim().length > 0)
		{
			if(lines[lID].length <=49)
				textPreviewElem.appendChild(document.createTextNode(lines[lID].trim()+".."));
			else
				textPreviewElem.appendChild(document.createTextNode(lines[lID].substring(0, 49).trim()+".."));
			break;
		}
	}
	var showElem = document.createElement("a");
	showElem.href="javascript:showHide(\""+id+"\")";
	showElem.innerHTML = "show";
	textPreviewElem.appendChild(document.createTextNode("("));
	textPreviewElem.appendChild(showElem);
	textPreviewElem.appendChild(document.createTextNode(")"));
	placeholderElem.appendChild(textPreviewElem);
	releaseDiv.appendChild(placeholderElem);
	
	var optionalElem = document.createElement("div");
	optionalElem.id = id;
	if(show)
		optionalElem.style.display = "block";
	else
		optionalElem.style.display = "none";
	releaseDiv.appendChild(optionalElem);
	var listElem = null;
	for(var lID =0; lID < lines.length; lID++)
	{
		if(lines[lID].substring(0,1) == '-')
		{
			if(listElem == null)
			{
				listElem = document.createElement("ul");
				optionalElem.appendChild(listElem);
			}
			var lineElem = document.createElement("li");
			lineElem.innerHTML = lines[lID].substring(1);
			listElem.appendChild(lineElem);
		}
		else
		{
			listElem = null;
			var lineElem = document.createElement("p");
			if(lines[lID].substring(0,1) == '#')
			{
				lineElem.innerHTML = lines[lID].substring(1);
				lineElem.style.fontWeight="bold";
			}
			else
			{
				lineElem.innerHTML = lines[lID];
			}
			optionalElem.appendChild(lineElem);
		}
	}
	var hideElem = document.createElement("a");
	hideElem.href="javascript:showHide(\""+id+"\")";
	hideElem.innerHTML = "hide";
	var hideAllElem = document.createElement("p");
	hideAllElem.appendChild(document.createTextNode("("));
	hideAllElem.appendChild(hideElem);
	hideAllElem.appendChild(document.createTextNode(")"));
	optionalElem.appendChild(hideAllElem);
	
	var assets = release.assets;
	if(assets.length > 0)
	{
		var addAsset = function(asset)
		{
			var assetURL = document.createElement("a");
			assetURL.href = asset.browser_download_url;
			assetURL.target = "_blank";
			assetURL.innerHTML = asset.name;			
			return assetURL;
		};
		
		var listElem = document.createElement("ul");
		listElem.style.marginLeft = "0cm";
		listElem.style.marginTop = "0cm";
		for(var lID =0; lID < assets.length; lID++)
		{
			if(lID in delIdx)
				continue;
			var assetElem = document.createElement("li");
			assetElem.innerHTML = asset.created_at + ":";
			assetElem.appendChild(addAsset(assets[lID]));
			listElem.appendChild(assetElem);
		}
		releaseDiv.appendChild(listElem);
	}
	
	return releaseDiv;
}