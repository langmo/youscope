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
			assetURL.innerHTML = "Windows XP, Vista, 7, 8, 10 (64bit)";			
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
			assetURL.innerHTML = "Windows XP, Vista, 7, 8, 10 (32bit)";			
			assetElem.appendChild(assetURL);
			break;
		}
	}
	return assetElem;
}

function createMostRecent(releases, elementID)
{
	var releasesElement = document.getElementById(elementID);
	while(releasesElement.firstChild) 
	{
		releasesElement.removeChild(releasesElement.firstChild);
	}
	var latestStable = -1;
	var header;
	for(var i=0; i<releases.length; i++)
	{
		if(!releases[i].prerelease)
		{
			latestStable = i;
			header = document.createElement("p");
			var headerText = document.createElement("span");
			if(i==0)
				headerText.appendChild(document.createTextNode("Current Release: "));
			else
				headerText.appendChild(document.createTextNode("Latest Stable Release: "));
			headerText.style.fontWeight="bold";
			header.appendChild(headerText);
			header.appendChild(document.createElement("br"));
			header.appendChild(document.createTextNode(releases[i].name+" ("+releases[i].tag_name+")"));
			
			releasesElement.appendChild(header);
			releasesElement.appendChild(displayReleaseLinks(releases[i]));		
			releasesElement.appendChild(document.createElement("hr"));
			break;
		}
	}
	if(latestStable != 0)
	{
		header = document.createElement("p");
		var headerText = document.createElement("span");
		headerText.appendChild(document.createTextNode("Latest Pre-Release: "));
		headerText.style.fontWeight="bold";
		header.appendChild(headerText);
		header.appendChild(document.createElement("br"));
		header.appendChild(document.createTextNode(releases[0].name+" ("+releases[0].tag_name+")"));
		releasesElement.appendChild(header);
		releasesElement.appendChild(displayReleaseLinks(releases[0]));		
		releasesElement.appendChild(document.createElement("hr"));
	}
}
window.onload = function()
{
	queryReleases(function callback(releases){createMostRecent(releases, "mostRecentReleases");});
};