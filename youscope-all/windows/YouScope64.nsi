;NSIS Installer for YouScope
;Written by Moritz Lang

;--------------------------------
;Set to moder design/UI
!include "MUI2.nsh"

;--------------------------------
;General
;Name and file
Name "YouScope"
OutFile "YouScopeSetupWin64.exe"

;Default installation folder
InstallDir "$PROGRAMFILES32\YouScope"
  
;Get installation folder from registry if available
InstallDirRegKey HKCU "Software\YouScope" ""

;Request application privileges for Windows Vista/7
RequestExecutionLevel user
; admin

;--------------------------------
;Variables
Var StartMenuFolder

;--------------------------------
;Interface Settings
!define MUI_ABORTWARNING
!define MUI_ICON "YouScope.ico"
!define MUI_UNICON "YouScope.ico"

;--------------------------------
;Pages
!insertmacro MUI_PAGE_WELCOME

!define MUI_LICENSEPAGE_RADIOBUTTONS
!insertmacro MUI_PAGE_LICENSE "COPYING.txt"

!insertmacro MUI_PAGE_COMPONENTS

!insertmacro MUI_PAGE_DIRECTORY
  
;Start Menu Folder Page Configuration
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU" 
!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\YouScope" 
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "StartMenuFolder"
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuFolder
  
!insertmacro MUI_PAGE_INSTFILES

!define MUI_FINISHPAGE_NOREBOOTSUPPORT
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

;--------------------------------
;Languages
!insertmacro MUI_LANGUAGE "English"


;--------------------------------
; Install types
InstType "Typical"
InstType "Full"
InstType "Minimal"
InstType "Server"
InstType "Client"


;--------------------------------
;Main Program
Section "-Main Program" SecMain
	SectionIn 1 2 3 4 5
  
	SetOutPath "$INSTDIR"
	FILE COPYING.txt
	FILE YouScope.exe
;	FILE YouScope.bat
	FILE YouScope.ico
	FILE youscope-starter.jar

	SetOutPath "$INSTDIR\shared"
	FILE "shared\xpp3_min-1.1.4c.jar"
	FILE "shared\xstream-1.4.2.jar"
	FILE "shared\youscope-shared.jar"
  
	;Store installation folder
	WriteRegStr HKCU "Software\YouScope" "" $INSTDIR
  	 
	; Add start menu entry
	!insertmacro MUI_STARTMENU_WRITE_BEGIN Application
		;Create shortcuts
		SetShellVarContext all
		CreateDirectory "$SMPROGRAMS\$StartMenuFolder"
		CreateShortCut "$SMPROGRAMS\$StartMenuFolder\YouScope.lnk" "$INSTDIR\YouScope.exe"
	!insertmacro MUI_STARTMENU_WRITE_END

SectionEnd

;--------------------------------
;Client
Section "!Client" SecClient
	SetOutPath "$INSTDIR\client"
	SectionIn 1 2 3 5
  
	;Files:
	FILE "client\fugue-icons-2.6.4.jar"
	FILE "client\youscope-client.jar"
	FILE "client\youscope-client-addon.jar"
	FILE "client\youscope-client-uielements.jar"
SectionEnd

;--------------------------------
; Server
Section "!Server" SecServer
	SectionIn 1 2 3 4
  
	;Files:
	SetOutPath "$INSTDIR\server"
	FILE "server\youscope-server.jar"
	File "server\youscope-server-addon.jar"
	SetOutPath "$INSTDIR\plugins"
	File "plugins\youscope-microscopeaccess14.jar"
SectionEnd

;--------------------------------
;Client
Section "!Device Drivers" SecDeviceDrivers
	SetOutPath "$INSTDIR\drivers"
	SectionIn 1 2 4
  
	;Files:
	FILE "drivers\*"
SectionEnd

;--------------------------------
;Microplate Types
SectionGroup "Microplate Types" SecMicroplateTypes
	Section "ANSI/SBS microplates" SecAnsiSbsMicroplates
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 5
  
  		;Files:
  		FILE "plugins\youscope-ansi-sbs-microplates.jar"  
	SectionEnd
	
	Section "Custom microplates" SecCustomMicroplates
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 5
  
  		;Files:
  		FILE "plugins\youscope-custom-microplates.jar"  
	SectionEnd
	
	Section "Multiwell TC microplates" SecMultiwellTC
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 5
  
  		;Files:
  		FILE "plugins\youscope-bd-bioscience-multiwell-tc-microplates.jar"  
	SectionEnd
SectionGroupEnd

;--------------------------------
;Measurement Types
SectionGroup "Measurement Types" SecMeasurementTypes
	Section "Microplate Measurement" SecMicroplateMeasurement
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4 5
  
  		;Files:
  		FILE "plugins\youscope-microplate-measurement.jar"  
	SectionEnd

	Section "Continous Measurement" SecContinousMeasurement
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4 5
  
  		;Files:
  		FILE "plugins\youscope-continous-imaging.jar"  
	SectionEnd

	Section "Composed Imaging Measurement" SecComposedImagingMeasurement
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 4 5
  
  		;Files:
  		FILE "plugins\youscope-composed-imaging.jar"  
	SectionEnd

	Section "Task Measurement" SecTaskMeasurement
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4 5
  
  		;Files:
  		FILE "plugins\youscope-configurable-measurement.jar"  
	SectionEnd
SectionGroupEnd

SectionGroup "Measurement Result Consumers" SecResultConsumers
	Section "OpenBIS Uploader" SecOpenBIS
  		SectionIn 2
  
  		;Files:
		SetOutPath "$INSTDIR\plugins"
  		FILE "plugins\youscope-openbis.jar"  
		SetOutPath "$INSTDIR\openbis"
		FILE "openbis\*"  
		SetOutPath "$INSTDIR\openbis\.ssh"
		FILE "openbis\.ssh\DROP_HERE_PRIVATE_OPENSSH_KEY"
	SectionEnd
	Section "Folder Opener" SecOpenFolder
  		SectionIn 1 2 5
  
  		;Files:
		SetOutPath "$INSTDIR\plugins"
  		FILE "plugins\youscope-open-measurement-folder.jar"  
	SectionEnd
SectionGroupEnd

;--------------------------------
;Job Types
SectionGroup "Job Types" SecJobTypes
	Section "Imaging Job" SecImagingJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4 5
  
  		;Files:
  		FILE "plugins\youscope-imaging-job.jar"  
	SectionEnd

	Section "Out-Of-Focus Job" SecOutOfFocusJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4 5
  
  		;Files:
  		FILE "plugins\youscope-out-of-focus-job.jar"  
	SectionEnd

	Section "Z-Slides Job" SecZSlidesJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4 5
  
  		;Files:
  		FILE "plugins\youscope-z-slides.jar"  
	SectionEnd	

	Section "Scripting Job" SecScriptingJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 4 5
  
  		;Files:
  		FILE "plugins\youscope-scripting-job.jar"  
	SectionEnd	
	
	Section "Stage-Position Job" SecStagePositionJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 4 5
  
  		;Files:
  		FILE "plugins\youscope-change-position-job.jar"  
	SectionEnd

	Section "Device-Settings Job" SecDeviceSettingsJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4 5
  
  		;Files:
  		FILE "plugins\youscope-device-job.jar"  
	SectionEnd	
	
	Section "Device-Slides Job" SecDeviceSlidesJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 4 5
  
  		;Files:
  		FILE "plugins\youscope-multi-position.jar"  
	SectionEnd	

	Section "Job Container" SecJobContainerJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 4 5
  
  		;Files:
  		FILE "plugins\youscope-composite-job.jar"  
	SectionEnd	
	
	Section "Oscillating Device Job" SecOscillatingDeviceJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 2 4 5
  
  		;Files:
  		FILE "plugins\youscope-oscillating-device-job.jar"  
	SectionEnd
SectionGroupEnd

;--------------------------------
; Tools
SectionGroup "Tools" SecTools
	Section "YouScope LiveStream" SecLiveStream
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 5
  
  		;Files:
  		FILE "plugins\youscope-livestream.jar"  
	SectionEnd

	Section "YouScope Multi-Color Stream" SecMultiStream
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 5
  
  		;Files:
  		FILE "plugins\youscope-multi-color-stream.jar"  
	SectionEnd

	Section "Stage and Focus Position" SecPositionControl
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 5
  
  		;Files:
  		FILE "plugins\youscope-position-control.jar"  
	SectionEnd

	Section "YouScope Scripting" SecScripting
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 5
  
  		;Files:
  		FILE "plugins\youscope-scripting.jar"  
	SectionEnd

	Section "YouPong" SecYouPong
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 2
  
  		;Files:
  		FILE "plugins\youscope-youpong.jar"  
	SectionEnd
SectionGroupEnd

;----------------------------
; Optional Functionality
SectionGroup "Optional Functionality" SecOptional
	;--------------------------------
	;Installer Matlab
	Section "Matlab" SecMatlab
	  	SetOutPath "$INSTDIR\plugins"
	  	SectionIn 1 2 4 5

	  	;Files:
  		FILE "plugins\matlab-scripting.jar"
	SectionEnd

	;--------------------------------
	;Installer Image formats
	Section "Image Formats" SecImageFormats
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4 5
  
  		;Files:
  		FILE "plugins\jai_imageio.jar"  
	SectionEnd
SectionGroupEnd

;--------------------------------
;Documentation
Section "Documentation" SecDocumentation
	SectionIn 1 2 5
  
	;Files:
	SetOutPath "$INSTDIR\documentation"
	FILE "documentation\*.html"  
	SetOutPath "$INSTDIR\documentation\images"
	FILE "documentation\images\*.jpg"  
	FILE "documentation\images\*.gif"  
SectionEnd

;--------------------------------
;Example Scripts
Section "Example Scripts" SecExampleScripts
	SectionIn 1 2 5
  
	;Files:
	SetOutPath "$INSTDIR\scripts"
	FILE /r "scripts\"  
SectionEnd

;--------------------------------
;Descriptions
	;Language strings
	LangString DESC_SecMain ${LANG_ENGLISH} "The main program. Must be installed."
	LangString DESC_SecClient ${LANG_ENGLISH} "The graphical user interface (GUI)."
	LangString DESC_SecServer ${LANG_ENGLISH} "The microscope control software."
	LangString DESC_SecDeviceDrivers ${LANG_ENGLISH} "The drivers for the microscope devices. Should always be installed with the server, except if the drivers of an installed MicroManager, version 1.4 or higher, should be used."

	LangString DESC_SecMicroplateTypes ${LANG_ENGLISH} "Types of microplate which can be screened."
	LangString DESC_SecAnsiSbsMicroplates ${LANG_ENGLISH} "96, 384, and 1536 well microplates (ANSI/SBS 1-2004 through ANSI/SBS 4-2004)."
	LangString DESC_SecCustomMicroplates ${LANG_ENGLISH} "Possibility to define own microplate types."
	LangString DESC_SecMultiwellTC ${LANG_ENGLISH} "6, 12, and 24 well microplates (BD Bioscience - Multiwell TC Plate)."

	LangString DESC_SecMeasurementTypes ${LANG_ENGLISH} "Types of measurements which can be performed by YouScope."
	LangString DESC_SecMicroplateMeasurement ${LANG_ENGLISH} "A microplate measurement screens through all wells of a microplate."
	LangString DESC_SecContinousMeasurement ${LANG_ENGLISH} "Continuously imaging one channel at one position. Including burst mode."
	LangString DESC_SecComposedImagingMeasurement ${LANG_ENGLISH} "Imaging of overlapping positions, such that the single images can later on be composed to a large one."
	LangString DESC_SecTaskMeasurement ${LANG_ENGLISH} "Running of one or more imaging protocols in parallel, e.g. to image several channels with the same or different period lengths."

	LangString DESC_SecResultConsumers ${LANG_ENGLISH} "Elements which can process or display the results of a measurement."
	LangString DESC_SecOpenBIS ${LANG_ENGLISH} "Uploading of measurement images and meta-data to the OpenBIS screening database."
	LangString DESC_SecOpenFolder ${LANG_ENGLISH} "Simple tool to open the folder in which the measurement was saved."

	LangString DESC_SecJobTypes ${LANG_ENGLISH} "Jobs are small bricks of actions which compose an imaging protocol, and can be added to several different measurement protocols."
	LangString DESC_SecImagingJob ${LANG_ENGLISH} "Taking an image in a channel."
	LangString DESC_SecOutOfFocusJob ${LANG_ENGLISH} "Taking an out-of-focus image."
	LangString DESC_SecZSlidesJob ${LANG_ENGLISH} "Taking several images at different focus positions."
	LangString DESC_SecScriptingJob ${LANG_ENGLISH} "Possibility to control the microscope using scripts."
	LangString DESC_SecStagePositionJob ${LANG_ENGLISH} "Changing the relative or absolute position of the stage."
	LangString DESC_SecDeviceSettingsJob ${LANG_ENGLISH} "Changing a setting of a device."
	LangString DESC_SecDeviceSlidesJob ${LANG_ENGLISH} "Setting a device property to several values and performing the same jobs (e.g. imaging) for every value. Generalized version of z-slides."
	LangString DESC_SecJobContainerJob ${LANG_ENGLISH} "A job which simply consists of other jobs. Used to arrange jobs similar to the file system."
	LangString DESC_SecOscillatingDeviceJob ${LANG_ENGLISH} "Sending of an oscillatory signal (e.g. light) to the cells."

	LangString DESC_SecTools ${LANG_ENGLISH} "Tools to manipulate the microscope or prepare a measurement."
	LangString DESC_SecLiveStream ${LANG_ENGLISH} "The LiveStream continuously displays the current microscope image in a given channel."
	LangString DESC_SecMultiStream ${LANG_ENGLISH} "Similar to the LiveStream. Overlays the images of several channels (up to 4) in a color image."
	LangString DESC_SecPositionControl ${LANG_ENGLISH} "Possibility to display and change the position of the stage and the focus."
	LangString DESC_SecMeasurementViewer ${LANG_ENGLISH} "Displays the results of a measurement."
	LangString DESC_SecScripting ${LANG_ENGLISH} "Environment to display, manipulate, run and debug scripts in various languages."
	LangString DESC_SecYouPong ${LANG_ENGLISH} "Small game to be played by changing the stage position and focus. Experimental."
	
	LangString DESC_SecOptional ${LANG_ENGLISH} "Optional functionality."
	LangString DESC_SecMatlab ${LANG_ENGLISH} "Enabling Matlab(TM) scripting. An installation of Matlab is needed for this plugin."
	LangString DESC_SecImageFormats ${LANG_ENGLISH} "Possibility to save images in various image formats like tiff."

	LangString DESC_SecDocumentation ${LANG_ENGLISH} "In program documentation of the YouScope tools and measurement types."
	LangString DESC_SecExampleScripts ${LANG_ENGLISH} "Script snipplets which exemplify how jobs can be programmed by using different script engines."

	;Assign language strings to sections
	!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
		!insertmacro MUI_DESCRIPTION_TEXT ${SecMain} $(DESC_SecMain)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecClient} $(DESC_SecClient)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecServer} $(DESC_SecServer)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecDeviceDrivers} $(DESC_SecDeviceDrivers)

		!insertmacro MUI_DESCRIPTION_TEXT ${SecMicroplateTypes} $(DESC_SecMicroplateTypes)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecAnsiSbsMicroplates} $(DESC_SecAnsiSbsMicroplates) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecCustomMicroplates} $(DESC_SecCustomMicroplates) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecMultiwellTC} $(DESC_SecMultiwellTC) 

		!insertmacro MUI_DESCRIPTION_TEXT ${SecMeasurementTypes} $(DESC_SecMeasurementTypes) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecMicroplateMeasurement} $(DESC_SecMicroplateMeasurement) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecContinousMeasurement} $(DESC_SecContinousMeasurement) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecComposedImagingMeasurement} $(DESC_SecComposedImagingMeasurement) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecTaskMeasurement} $(DESC_SecTaskMeasurement) 

		!insertmacro MUI_DESCRIPTION_TEXT ${SecResultConsumers} $(DESC_SecResultConsumers) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecOpenBIS} $(DESC_SecOpenBIS)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecOpenFolder} $(DESC_SecOpenFolder) 

		!insertmacro MUI_DESCRIPTION_TEXT ${SecJobTypes} $(DESC_SecJobTypes) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecImagingJob} $(DESC_SecImagingJob) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecOutOfFocusJob} $(DESC_SecOutOfFocusJob) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecZSlidesJob} $(DESC_SecZSlidesJob) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecScriptingJob} $(DESC_SecScriptingJob) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecStagePositionJob} $(DESC_SecStagePositionJob) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecDeviceSettingsJob} $(DESC_SecDeviceSettingsJob) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecDeviceSlidesJob} $(DESC_SecDeviceSlidesJob) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecJobContainerJob} $(DESC_SecJobContainerJob) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecOscillatingDeviceJob} $(DESC_SecOscillatingDeviceJob) 

		!insertmacro MUI_DESCRIPTION_TEXT ${SecTools} $(DESC_SecTools) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecLiveStream} $(DESC_SecLiveStream) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecMultiStream} $(DESC_SecMultiStream) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecPositionControl} $(DESC_SecPositionControl) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecMeasurementViewer} $(DESC_SecMeasurementViewer) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecScripting} $(DESC_SecScripting) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecYouPong} $(DESC_SecYouPong) 

		!insertmacro MUI_DESCRIPTION_TEXT ${SecOptional} $(DESC_SecOptional) 	
		!insertmacro MUI_DESCRIPTION_TEXT ${SecMatlab} $(DESC_SecMatlab)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecImageFormats} $(DESC_SecImageFormats)

		!insertmacro MUI_DESCRIPTION_TEXT ${SecDocumentation} $(DESC_SecDocumentation) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecExampleScripts} $(DESC_SecExampleScripts)
	!insertmacro MUI_FUNCTION_DESCRIPTION_END
 