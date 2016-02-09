// Parameters for scope movement.
plateX = 10;//680;
plateY = 110;//310;

wellWidth = 23.65;
wellHeight = 8.5;
deltaXVert = -17.75;
nWellX = 8;
nWellY = 3;
nextWell = new Array(0, 0);
wellSpeedX = Math.ceil(wellWidth / 10);
wellSpeedY = Math.ceil(wellHeight / 10);
well0X = 10.5;
well0Y = -112;

scopeX = well0X;
scopeY = well0Y;

deltaBeamX = 36;//30;
deltaBeamY = 44;//45;

var beam = new Array(2);
waitInWell = new Array(10, 0);
waitTimes = -1;
beamNr = 0;

doNotChangeWell = false;

var scope, plate, beam;

wellActivation = new Array(new Array(true, true, true, true, true, true, true, true),
				 new Array(true, true, true, true, true, true, true, true),
				 new Array(true, true, true, true, false, false, false, false));

// Detection of used browser
var DHTML = false, DOM = false, MSIE4 = false, NS4 = false, OP = false;
function detectBrowser()
{
	if (document.getElementById)
	{
		DHTML = true;
		DOM = true;
	}
	else if (document.all)
	{
		DHTML = true;
		MSIE4 = true;
	}
}
function minimizeMaximize(elementID)
{
	if(!DHTML)
		return;
	if(DOM)
	{
		element = document.getElementById(elementID);
		plusMinus = document.getElementById(elementID + "-plusMinus");
	}
	else if(MSIE4)
	{
		element = document.all(elementID);
		plusMinus = document.all(elementID + "-plusMinus");
	}

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

function setExposure(beam, select)
{
	waitInWell[beam] = select.options[select.options.selectedIndex].value / 10;
}

// Main function for scope movement.
function moveScope()
{
	if(waitTimes > 0 && waitTimes == waitInWell[beamNr])
	{
		beam[beamNr].style.visibility = "visible";
	}
	if(waitTimes > 0)
	{
		waitTimes--;
		if(beam[beamNr].filters)
		{
			beam[beamNr].filters[0].phase += 20;
		}
		return;
	}
	else if(waitTimes == 0)
	{
		beam[beamNr].style.visibility = "hidden";
		beamNr++;
		if(beamNr < beam.length)
		{
			waitTimes = waitInWell[beamNr];
		}
		else
		{
			beamNr = 0;
			waitTimes = -1;
		}
		return;
	}

	gotoX = well0X + nextWell[0] * wellWidth + nextWell[1] * deltaXVert ;
	gotoY = well0Y + nextWell[1] * wellHeight;

	// Move scope
	if(Math.abs(scopeX - gotoX) < wellSpeedX)
	{
		scopeX = gotoX;
	}
	else if(scopeX < gotoX)
	{
		scopeX = scopeX + wellSpeedX;
	}
	else if(scopeX > gotoX)
	{
		scopeX = scopeX - wellSpeedX;
	}

	if(Math.abs(scopeY - gotoY) < wellSpeedY)
	{
		scopeY = gotoY;
	}
	else if(scopeY < gotoY)
	{
		scopeY = scopeY + wellSpeedY;
	}
	else if(scopeY > gotoY)
	{
		scopeY = scopeY - wellSpeedY;
	}

	// Detect if we arrived at the goal.
	if(scopeX == gotoX && scopeY == gotoY)
	{
		if(doNotChangeWell)
			waitTimes = 99999;
		else
			waitTimes = waitInWell[0];
		if(!doNotChangeWell)
		{
			gotoNextWell();
		}
	}

	// Set position
	scope.style.left = Math.round(plateX + scopeX) + "px";
	scope.style.top = Math.round(plateY + scopeY) + "px";
	for(i=0; i<beam.length;i++)
	{
		beam[i].style.left = Math.round(plateX + scopeX + deltaBeamX) + "px";
		beam[i].style.top = Math.round(plateY + scopeY + deltaBeamY) + "px";
	}
}

var moveRight = true;
function gotoNextWell()
{
	startX = nextWell[0];
	startY = nextWell[1];
	while(true)
	{
		if(moveRight)
		{
			nextWell[0]++;
		}
		else
		{
			nextWell[0]--;
		}
		if(nextWell[0] >= nWellX)
		{
			if(nextWell[1] == startY)
			{
				moveRight = !moveRight;
				nextWell[0] = nWellX -1;
			}
			else
				nextWell[0] = 0;
			nextWell[1]++;
		}
		else if(nextWell[0] < 0)
		{
			if(nextWell[1] == startY)
			{
				moveRight = !moveRight;
				nextWell[0] = 0;
			}
			else
				nextWell[0] = nWellX -1;
			nextWell[1]++;
		}
		if(nextWell[1] >= nWellY)
		{
			moveRight = true;
			nextWell[0] = 0;
			nextWell[1] = 0;
		}
		if(wellActivation[nextWell[1]][nextWell[0]] || (startX == nextWell[0] && startY == nextWell[1]))
			break;
	}
}

// Goto a predefined well when mouse is over a link.
function activateWell(wellX, wellY)
{
	nextWell[0] = wellX;
	nextWell[1] = wellY;
	doNotChangeWell = true;
	waitTimes = 0;
}

// Continue normal processing when mouse leaves link
function continueMoving()
{
	doNotChangeWell = false;
	if(nextWell[1] % 2 == 0)
	{
		nextWell[0]++;
	}
	else
	{
		nextWell[0]--;
	}
	waitTimes = 0;
}
function switchWellState(wellX, wellY)
{
	if(!DHTML)
		return;
	if(DOM)
	{
		well = document.getElementById("well" + wellX + wellY);
	}
	else if(MSIE4)
	{
		well = document.all("well" + wellX + wellY);
	}
	wellActivation[wellY][wellX] = !wellActivation[wellY][wellX];
	if(wellActivation[wellY][wellX])
		well.style.backgroundColor="#7777FF";
	else
		well.style.backgroundColor="#eeeeee";
}

// Initializes plate measurement
function initialize()
{
	detectBrowser();
	if(!DHTML)
		return;

	// Get scope object
	if(DOM)
	{
		scope = document.getElementById("scope");
		beam[0] = document.getElementById("beam_blue");
		beam[1] = document.getElementById("beam_green");
		plate = document.getElementById("plate");
	}
	else if(MSIE4)
	{
		scope = document.all("scope");
		beam[0] = document.all("beam_blue");
		beam[1] = document.all("beam_green");
		plate = document.all("plate");
	}
	if(!scope || !beam || !plate)
		return;

	scope.style.left = Math.round(plateX + scopeX) + "px";
	scope.style.top = Math.round(plateY + scopeY) + "px";
	for(i=0; i<beam.length; i++)
	{
		beam[i].style.left = Math.round(plateX + scopeX + deltaBeamX) + "px";
		beam[i].style.top = Math.round(plateY + scopeY + deltaBeamY) + "px";
	}
	plate.style.left = Math.round(plateX) + "px";
	plate.style.top = Math.round(plateY) + "px";

	for(wellY = 0; wellY < wellActivation.length; wellY++)
	{
		for(wellX = 0; wellX < wellActivation[wellY].length; wellX++)
		{
			if(DOM)
			{
				well = document.getElementById("well" + wellX + wellY);
			}
			else if(MSIE4)
			{
				well = document.all("well" + wellX + wellY);
			}
			if(wellActivation[wellY][wellX])
				well.style.backgroundColor="#7777FF";
			else
				well.style.backgroundColor="#eeeeee";
		}
	}

	window.setInterval('moveScope()', 100)
}