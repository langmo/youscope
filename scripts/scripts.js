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
