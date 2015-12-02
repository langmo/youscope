@echo off

REM Enter here the path for the JRE to start YouScope with. 
REM The JRE should be made for the same architecture as the YouScope version you have installed,
REM and at least Java version 1.6 or higher.
REM E.g. C:\Program Files\Java\jdk1.6.0_23\bin\

set JRE_YOUSCOPE_PATH=C:\Program Files (x86)\Java\jdk1.6.0_23\bin

PATH=%CD%\lib;%PATH%
"%JRE_YOUSCOPE_PATH%\java" -jar onix-controller.jar