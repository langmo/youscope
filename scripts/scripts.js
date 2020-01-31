var requestURL = "https://api.github.com/repos/langmo/youscope/releases";
var releases = null;
function queryReleases(callback)
{
	if(releases != null)
	{
		callback(releases);
		return;
	}
	var xhr = new XMLHttpRequest();
	xhr.open('GET', requestURL);
	xhr.onreadystatechange = function ()
	{
		if(xhr.readyState === 4)
		{
			if(xhr.status === 200 || xhr.status == 0)
			{
				releases = JSON.parse(xhr.responseText);
				callback(releases);				
			}
		}
	}
	xhr.send(null);
	return;
}

function minimizeMaximize(elementID)
{
	element = document.getElementById(elementID);
	plusMinus = document.getElementById(elementID + "-plusMinus");
	
	if(element.style.display != "block")
	{
		element.style.display = "block";
		plusMinus.src = "img/minus.gif";
	}
	else
	{
		element.style.display = "none";
		plusMinus.src = "img/plus.gif";
	}
}

function displayReleaseLinks(release)
{
	var assets = release.assets;
	var addAsset = function(asset)
	{
		var assetURL = document.createElement("a");
		assetURL.href = asset.browser_download_url;
		assetURL.target = "_blank";
		assetURL.innerHTML = asset.name;			
		return assetURL;
	};
	var assetElem = document.createElement("p");
	assetElem.style.fontWeight="bold";
	for(var lID =0; lID < assets.length; lID++)
	{
		var name = assets[lID].name.toLowerCase();
		if(name.indexOf("_64bit_installer.exe")>=0)
		{
			var assetURL = document.createElement("a");
			assetURL.href = assets[lID].browser_download_url;
			assetURL.target = "_blank";
			assetURL.innerHTML = "Windows 64bit";			
			assetElem.appendChild(assetURL);
			break;
		}
	}
	assetElem.appendChild(document.createElement("br"));
	for(var lID =0; lID < assets.length; lID++)
	{
		var name = assets[lID].name.toLowerCase();
		if(name.indexOf("_32bit_installer.exe")>=0)
		{
			var assetURL = document.createElement("a");
			assetURL.href = assets[lID].browser_download_url;
			assetURL.target = "_blank";
			assetURL.innerHTML = "Windows 32bit";			
			assetElem.appendChild(assetURL);
			break;
		}
	}
	return assetElem;
}
function displayNightlyLinks(release, releasesElement)
{
	var nightlies = {};
	var assets = release.assets;
	for(let assetID in assets)
	{
		var asset = assets[assetID];
		var created = asset.created_at.substring(0, asset.created_at.indexOf("T"));
		if(!nightlies[created])
		{
			nightlies[created] = {};
		}
		if(asset.content_type == "application/tar")
		{
			nightlies[created].tar = {name: asset.name, url: asset.browser_download_url};
		}
		else if(asset.content_type == "application/zip")
		{
			nightlies[created].zip = {name: asset.name, url: asset.browser_download_url};
		}
	};
	let keys = [];
	for (let key in nightlies) 
	{      
		keys.push(key);
	}
	keys.sort();
	var created = keys[keys.length-1];
	
	var assetElem = document.createElement("p");
	assetElem.style.fontWeight="bold";
	var assetURL = document.createElement("a");
	assetURL.href = nightlies[created].zip.url;
	assetURL.target = "_blank";
	assetURL.innerHTML = "Windows 32/64bit";			
	assetElem.appendChild(assetURL);
	
	var header = document.createElement("p");
	var headerText = document.createElement("span");
	headerText.appendChild(document.createTextNode("Latest Nightly-Build:"));
	headerText.style.fontWeight="bold";
	header.appendChild(headerText);
	header.appendChild(document.createElement("br"));
	header.appendChild(document.createTextNode("Date: "+created));
	releasesElement.appendChild(header);
	releasesElement.appendChild(assetElem);		
	releasesElement.appendChild(document.createElement("hr"));		
}

function createMostRecent(releases, elementID)
{
	var releasesElement = document.getElementById(elementID);
	while(releasesElement.firstChild) 
	{
		releasesElement.removeChild(releasesElement.firstChild);
	}
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
		}
		else
		{
			if(latestPre < 0)
				latestPre = i;
		}
		if(latestNightly>=0 && latestStable>= 0 && latestPre >= 0)
			break;
	}
	// Latest stable release
	if(latestStable >= 0)
	{
		var header = document.createElement("p");
		var headerText = document.createElement("span");
		if(latestPre < 0 || latestPre>latestStable)
			headerText.appendChild(document.createTextNode("Current Release: "));
		else
			headerText.appendChild(document.createTextNode("Latest Stable Release: "));
		headerText.style.fontWeight="bold";
		header.appendChild(headerText);
		header.appendChild(document.createElement("br"));
		header.appendChild(document.createTextNode(releases[latestStable].name+" ("+releases[latestStable].tag_name+")"));
		
		releasesElement.appendChild(header);
		releasesElement.appendChild(displayReleaseLinks(releases[latestStable]));		
		releasesElement.appendChild(document.createElement("hr"));
	}
	// Latest prerelease
	if(latestPre>= 0 && latestPre<latestStable)
	{
		var header = document.createElement("p");
		var headerText = document.createElement("span");
		headerText.appendChild(document.createTextNode("Latest Pre-Release: "));
		headerText.style.fontWeight="bold";
		header.appendChild(headerText);
		header.appendChild(document.createElement("br"));
		header.appendChild(document.createTextNode(releases[latestPre].name+" ("+releases[latestPre].tag_name+")"));
		releasesElement.appendChild(header);
		releasesElement.appendChild(displayReleaseLinks(releases[latestPre]));		
		releasesElement.appendChild(document.createElement("hr"));	
	}
	// Nightly builds
	if(latestNightly >= 0)
	{
		displayNightlyLinks(releases[latestNightly], releasesElement));		
	}
}
window.onload = function()
{
	queryReleases(function callback(releases){createMostRecent(releases, "mostRecentReleases");});
};