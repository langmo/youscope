;NSIS Installer for YouScope
;Written by Moritz Lang
;---------------------
;--- Configuration ---
;---------------------

; Comment out to not export win64
!define WIN64
; Comment out to not expoirt win32
!define WIN32






;--------------------------------
;Set to moder design/UI
!include "MUI2.nsh"

;--------------------------------
;General
;Name and file
Name "YouScope"
!ifndef WIN64
	OutFile "YouScope_32bit_Installer.exe"
!else ifndef WIN32
	OutFile "YouScope_64bit_Installer.exe"
!else
	OutFile "YouScope_3264bit_Installer.exe"
!endif

;Default installation folder
!ifndef WIN64
	InstallDir "$PROGRAMFILES32\YouScope2"
!else
	InstallDir "$PROGRAMFILES64\YouScope2"
!endif
  
;Get installation folder from registry if available
!ifndef WIN64
	InstallDirRegKey HKCU "Software\YouScope32" ""
!else
	InstallDirRegKey HKCU "Software\YouScope64" ""
!endif

;Request application privileges for Windows Vista/7
RequestExecutionLevel admin

;--------------------------------
;Variables
Var StartMenuFolder

;--------
; Config
BrandingText "YouScope - The Microscope Control Software" 


;--------------------------------
;Interface Settings
!define MUI_ABORTWARNING
!define MUI_ICON "YouScope.ico"
!define MUI_UNICON "YouScope.ico"

;--------------------------------
;Pages
!insertmacro MUI_PAGE_WELCOME

!define MUI_LICENSEPAGE_RADIOBUTTONS
!insertmacro MUI_PAGE_LICENSE "LICENSE"

!insertmacro MUI_PAGE_COMPONENTS

!insertmacro MUI_PAGE_DIRECTORY
  
;Start Menu Folder Page Configuration
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU" 
!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\YouScope2" 
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
InstType "Full"
InstType "Minimal"
InstType "Server"
InstType "Client"

; Splash screen
Function .onInit
  SetOutPath $TEMP
  File /oname=spltmp.bmp "splash.bmp"

  advsplash::show 1000 600 400 -1 $TEMP\spltmp

  Pop $0 ; $0 has '1' if the user closed the splash screen early,
         ; '0' if everything closed normally, and '-1' if some error occurred.

  Delete $TEMP\spltmp.bmp
FunctionEnd


;--------------------------------
;Main Program
Section "-Main Program" SecMain
	SectionIn 1 2 3 4
  
	SetOutPath "$INSTDIR"
	FILE LICENSE
	FILE README.md
	!ifdef WIN64
		FILE YouScope64.exe
	!endif
	!ifdef WIN32
		FILE YouScope32.exe
	!endif
	FILE YouScope.ico
	FILE youscope-starter.jar

	AccessControl::GrantOnFile "$INSTDIR" "(BU)" "FullAccess" 
	;"GenericRead + GenericWrite"
	Pop $0

	SetOutPath "$INSTDIR\common"
	FILE "common\youscope-common.jar"
	FILE "common\youscope-addon.jar"
  
	SetOutPath "$INSTDIR\lib"
	FILE "lib\*"

	;Store installation folder
	!ifndef WIN64
		WriteRegStr HKCU "Software\YouScope32" "" $INSTDIR
	!else
		WriteRegStr HKCU "Software\YouScope64" "" $INSTDIR
	!endif
  	 
	; Add start menu entry
	!insertmacro MUI_STARTMENU_WRITE_BEGIN Application
		;Create shortcuts
		SetShellVarContext all
		CreateDirectory "$SMPROGRAMS\$StartMenuFolder"
		!ifdef WIN64
			CreateShortCut "$SMPROGRAMS\$StartMenuFolder\YouScope64.lnk" "$INSTDIR\YouScope64.exe"
		!endif
		!ifdef WIN32
			CreateShortCut "$SMPROGRAMS\$StartMenuFolder\YouScope32.lnk" "$INSTDIR\YouScope32.exe"
		!endif
	!insertmacro MUI_STARTMENU_WRITE_END

SectionEnd

;--------------------------------
;Client
Section "!Client" SecClient
	SetOutPath "$INSTDIR\client"
	SectionIn 1 2 4
  
	;Files:
	FILE "client\youscope-client.jar"
SectionEnd

;--------------------------------
; Server
Section "!Server" SecServer
	SectionIn 1 2 3
  
	;Files:
	SetOutPath "$INSTDIR"
	FILE "YSConfig_demo.cfg"
	SetOutPath "$INSTDIR\server"
	FILE "server\youscope-server.jar"
	SetOutPath "$INSTDIR\plugins"
	File "plugins\youscope-microscopeaccess.jar"
SectionEnd

;--------------------------------
;Drivers
Section "!Device Drivers" SecDeviceDrivers
	SectionIn 1 2 3
  
	!ifdef WIN64
		SetOutPath "$INSTDIR\drivers64"
		FILE /r "drivers64\*"
		SetOutPath "$INSTDIR\drivers64\nemesys"
		RegDLL "$INSTDIR\drivers64\nemesys\NemesysDotCom.dll"
	!endif
	!ifdef WIN32
		SetOutPath "$INSTDIR\drivers32"
		FILE /r "drivers32\*"
		!ifndef WIN64
			SetOutPath "$INSTDIR\drivers32\nemesys"
			RegDLL "$INSTDIR\drivers32\nemesys\NemesysDotCom.dll"
		!endif
	!endif
SectionEnd

;--------------------------------
;Microplate Types
SectionGroup "Microplate Types" SecMicroplateTypes
	Section "ANSI/SBS microplates" SecAnsiSbsMicroplates
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-ansi-sbs-microplates.jar"  
	SectionEnd
	
	Section "Custom microplates" SecCustomMicroplates
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-custom-microplates.jar"  
	SectionEnd
	
	Section "Multiwell TC microplates" SecMultiwellTC
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-bd-bioscience-multiwell-tc-microplates.jar"  
	SectionEnd
SectionGroupEnd

;--------------------------------
;Measurement Types
SectionGroup "Measurement Types" SecMeasurementTypes

	Section "Simple Measurement" SecSimpleMeasurement
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-simple-measurement.jar"  
	SectionEnd


	Section "Microplate Measurement" SecMicroplateMeasurement
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-microplate-measurement.jar"  
	SectionEnd

	Section "Continous Measurement" SecContinousMeasurement
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-continous-imaging.jar"  
	SectionEnd

	Section "Composed Imaging Measurement" SecComposedImagingMeasurement
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-composed-imaging.jar"  
	SectionEnd

	Section "Task Measurement" SecTaskMeasurement
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-task-measurement.jar"  
	SectionEnd

	Section "User-Control Measurement" SecUserControlMeasurement
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-user-control-measurement.jar"  
	SectionEnd

SectionGroupEnd

SectionGroup "Measurement Result Consumers" SecResultConsumers
	;Section "OpenBIS Uploader" SecOpenBIS
  	;	SectionIn 1 3 4
  	;
  	;	;Files:
	;	SetOutPath "$INSTDIR\plugins"
  	;	FILE "plugins\youscope-openbis.jar"  
	;	SetOutPath "$INSTDIR\openbis"
	;	FILE "openbis\*"  
	;	SetOutPath "$INSTDIR\openbis\.ssh"
	;	FILE "openbis\.ssh\DROP_HERE_PRIVATE_OPENSSH_KEY"  
	;SectionEnd
	Section "Folder Opener" SecOpenFolder
  		SectionIn 1 2 3 4
  
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
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-imaging-job.jar"  
	SectionEnd

	Section "Out-Of-Focus Job" SecOutOfFocusJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-out-of-focus-job.jar"  
	SectionEnd

	Section "Z-Slides Job" SecZSlidesJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-z-slides.jar"  
	SectionEnd	

	Section "Scripting Job" SecScriptingJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-scripting-job.jar"  
	SectionEnd	
	
	Section "Stage-Position Job" SecStagePositionJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-change-position-job.jar"  
	SectionEnd
	
	Section "Focusing Job" SecFocusingJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-focusing-job.jar"  
	SectionEnd

	Section "Wait Job" SecWaitJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-wait-job.jar"  
	SectionEnd

	Section "Wait-For-Input Job" SecWaitForUser
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-wait-for-user.jar"  
	SectionEnd

	Section "Device-Settings Job" SecDeviceSettingsJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-device-job.jar"  
	SectionEnd	

	Section "Statistics Job" SecStatisticsJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-statistics-job.jar"  
	SectionEnd	
	
	Section "Device-Slides Job" SecDeviceSlidesJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-multi-position.jar"  
	SectionEnd	

	Section "Job Container" SecJobContainerJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-composite-job.jar"  
	SectionEnd	
	
	Section "Oscillating Device Job" SecOscillatingDeviceJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-oscillating-device-job.jar"  
	SectionEnd

	Section "Autofocus Job" SecAutofocusJob
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-autofocus.jar"  
	SectionEnd
	Section "Life cell-detection" SecCellDetection
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-cell-detection.jar"  
	SectionEnd
	/*Section "CellX cell detection" SecCellX
  		SectionIn 1 3 4
  
  		;Files:
		SetOutPath "$INSTDIR\plugins"
  		FILE "plugins\youscope-cellx.jar"  
		SetOutPath "$INSTDIR\cellx"
		FILE /r "cellx\*"  
	SectionEnd*/
	Section "Controller" SecController
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-controller.jar"  
	SectionEnd
	Section "Custom Job" SecCustomJob
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-custom-job.jar"  
	SectionEnd
	Section "Droplet Microfluidics" SecDropletMicrofluidics
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-droplet-microfluidics.jar"  
	SectionEnd
	Section "Fluigent" SecFluigent
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-fluigent.jar"  
	SectionEnd
	Section "Nemesys" SecNemesys
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-nemesys.jar"  
	SectionEnd
	Section "Live Measurement" SecLiveMeasurement
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-live-modifiable-job.jar"  
	SectionEnd
	Section "Repeat Job" SecRepeatJob
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-repeat-job.jar"  
	SectionEnd
	Section "SLIM" SecSLIM
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-slim.jar"  
	SectionEnd
	Section "Share Execution" SecShareExecution
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-share-execution.jar"  
	SectionEnd
	Section "Wait since last action" SecWaitSinceLastAction
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-wait-since-last-action.jar"  
	SectionEnd
SectionGroupEnd

;--------------------------------
; Tools
SectionGroup "Tools" SecTools
	Section "YouScope LiveStream" SecLiveStream
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 4
  
  		;Files:
  		FILE "plugins\youscope-livestream.jar"  
	SectionEnd

	Section "YouScope Multi-Color Stream" SecMultiStream
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 4
  
  		;Files:
  		FILE "plugins\youscope-multi-color-stream.jar"  
	SectionEnd

	Section "Device Setting Manager" SecDeviceSettingManager
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 4
  
  		;Files:
  		FILE "plugins\youscope-device-setting-manager.jar"  
	SectionEnd


	Section "Stage and Focus Position" SecPositionControl
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 4
  
  		;Files:
  		FILE "plugins\youscope-position-control.jar"  
	SectionEnd

	Section "Measurement Viewer" SecMeasurementViewer
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 4
  
  		;Files:
  		FILE "plugins\youscope-measurement-viewer.jar"  
	SectionEnd

	Section "YouScope Scripting" SecScripting
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-scripting.jar"  
	SectionEnd

	Section "YouPong" SecYouPong
  		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 4
  
  		;Files:
  		FILE "plugins\youscope-youpong.jar"  
	SectionEnd
SectionGroupEnd

;----------------------------
; Misc Functionality
SectionGroup "Misc" SecOptional
	;--------------------------------
	;Installer Matlab
	Section "Matlab" SecMatlab
	  	SetOutPath "$INSTDIR\plugins"
	  	SectionIn 1 3 4

	  	;Files:
  		FILE "plugins\matlab-scripting.jar"
	SectionEnd

	;--------------------------------
	;Installer Image formats
	Section "Image Formats" SecImageFormats
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\jai_imageio.jar"  
	SectionEnd

	Section "Standard Save Settings" SecStandardSaveSettings
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-standard-save-settings.jar"  
	SectionEnd

	Section "Custom Save Settings" SecCustomSaveSettings
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-custom-save-settings.jar"  
	SectionEnd

	Section "Custom Metadata" SecCustomMetadata
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-custom-metadata.jar"  
	SectionEnd

	Section "Standard Paths" SecStandardPaths
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 3 4
  
  		;Files:
  		FILE "plugins\youscope-standard-paths.jar"  
	SectionEnd

	Section "Traveling Salesman" SecTravelingSalesman
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 3 4
  
  		;Files:
  		FILE "plugins\youscope-traveling-salesman.jar"  
	SectionEnd

	Section "Default Skin (System)" SecSystemSkin
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 2 4
  
  		;Files:
  		FILE "plugins\youscope-system-skin.jar"  
	SectionEnd
	Section "Dark Skin" SecDarkSkin
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 4
  
  		;Files:
  		FILE "plugins\youscope-dark-skin.jar"  
	SectionEnd
	Section "Red Skin" SecRedSkin
		SetOutPath "$INSTDIR\plugins"
  		SectionIn 1 4
  
  		;Files:
  		FILE "plugins\youscope-red-skin.jar"  
	SectionEnd

SectionGroupEnd

;--------------------------------
;Documentation
Section "Documentation" SecDocumentation
	SectionIn 1 4
  
	;Files:
	SetOutPath "$INSTDIR\documentation"
	FILE "documentation\documentation.pdf"  
SectionEnd

;--------------------------------
;Multi Camera
SectionGroup "Multi-Camera" SecMultiCam
	Section "Multi-Camera Job" SecParallelImagingJob
		SetOutPath "$INSTDIR\plugins"
		SectionIn 1 3 4

	  	;Files:
  		FILE "plugins\youscope-parallel-imaging-job.jar"  
	SectionEnd

	Section "Multi-Camera Stream" SecMultiCameraStream
		SetOutPath "$INSTDIR\plugins"
		SectionIn 1 4

	  	;Files:
  		FILE "plugins\youscope-multi-camera-stream.jar"  
	SectionEnd

	Section "Multi-Camera and Color Stream" SecMultiCameraAndColorStream
		SetOutPath "$INSTDIR\plugins"
		SectionIn 1 4

	  	;Files:
  		FILE "plugins\youscope-multi-camera-and-color-stream.jar"  
	SectionEnd

	Section "Multi-Camera Measurement" SecMultiCameraMeasurement
		SetOutPath "$INSTDIR\plugins"
		SectionIn 1 3 4

	  	;Files:
  		FILE "plugins\youscope-multi-camera-continous-imaging.jar"  
	SectionEnd


SectionGroupEnd

;--------------------------------
;Example Scripts
Section "Example Scripts" SecExampleScripts
	SectionIn 1 4
  
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
;	LangString DESC_SecOpenBIS ${LANG_ENGLISH} "Uploading of measurement images and meta-data to the OpenBIS screening database."
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

	LangString DESC_SecStatisticsJob ${LANG_ENGLISH} "Collects statistics of the run-time of imaging tasks and saves it as CSV."
	LangString DESC_SecMultiCameraMeasurement ${LANG_ENGLISH} "Measurement which continuously takes images with multiple cameras, either in burst mode or with fixed period. Experimental!"
	LangString DESC_SecMultiCameraAndColorStream ${LANG_ENGLISH} "Displays the current images of multiple cameras as an overlay image, in which each camera has a separate color. Experimental!"
	LangString DESC_SecDeviceSettingManager ${LANG_ENGLISH} "Allows to directly set the state of the single devices."
	LangString DESC_SecWaitJob ${LANG_ENGLISH} "Job which pauses execution either for a fixed time period, or executes sub-jobs and guarantees a minimal overall execution time. Experimental!"
	LangString DESC_SecParallelImagingJob ${LANG_ENGLISH} "Job to take images whith multiple cameras in parallel. Experimental!"
	LangString DESC_SecFocusingJob ${LANG_ENGLISH} "Job to change the focus position."
	LangString DESC_SecMultiCameraStream ${LANG_ENGLISH} "Displays the current image of multiple cameras in a grid like fashion. Experimental!"
	LangString DESC_SecSimpleMeasurement ${LANG_ENGLISH} "Measurement to execute a given protocol at a given position. Protocol can be executed more than once, with different period settings."
	LangString DESC_SecWaitForUser ${LANG_ENGLISH} "Job to display a message to the user. The execution of the protocol gets paused until user acknowledges/confirms message."
	LangString DESC_SecUserControlMeasurement ${LANG_ENGLISH} "Measurement which displays the current microscope image similar to the life-stream. When user hits a button, the currently displayed image gets saved."


	LangString DESC_SecAutofocusJob ${LANG_ENGLISH} "Job to find the focal plane by different software based autofocus algorithms." 
	LangString DESC_SecCellDetection ${LANG_ENGLISH} "Job to detect and track cells during a measurement." 
	;LangString DESC_SecCellX ${LANG_ENGLISH} "Advanced cell detection algorithm. Requires cell detection job." 
	LangString DESC_SecController ${LANG_ENGLISH} "Allows to implement feedback control algorithms." 
	LangString DESC_SecCustomJob ${LANG_ENGLISH} "Allows to define custom reusable job types." 
	LangString DESC_SecDropletMicrofluidics ${LANG_ENGLISH} "Droplet based microfluidic control algorithm." 
	LangString DESC_SecFluigent ${LANG_ENGLISH} "User interface and job to control Fluigent microfluidic pumps." 
	LangString DESC_SecNemesys ${LANG_ENGLISH} "User interface and job to control Nemesys syringe systems." 
	LangString DESC_SecLiveMeasurement ${LANG_ENGLISH} "Job allowing to modify its sub-jobs during a measurement." 
	LangString DESC_SecRepeatJob ${LANG_ENGLISH} "Job repeating its child jobs several times."
	LangString DESC_SecSLIM ${LANG_ENGLISH} "Support for SLIM microscopy."


	LangString DESC_SecCustomMetadata ${LANG_ENGLISH} "Possibility to customize which metadata is by default saved for measurements, and which has to be saved." 
	LangString DESC_SecCustomSaveSettings ${LANG_ENGLISH} "Possibility to customize how measurement is saved to disk." 
	LangString DESC_SecStandardSaveSettings ${LANG_ENGLISH} "Standard settings how measurement is saved to disk." 
	LangString DESC_SecSystemSkin ${LANG_ENGLISH} "Default skin, in agreement to operating system look-and-feel." 
	LangString DESC_SecDarkSkin ${LANG_ENGLISH} "A dark skin for dark rooms." 
	LangString DESC_SecRedSkin ${LANG_ENGLISH} "A red skin, good for the eyes in dark rooms." 

	LangString DESC_SecTravelingSalesman ${LANG_ENGLISH} "Traveling salesman optimizers for path through microplate." 
 	LangString DESC_SecStandardPaths ${LANG_ENGLISH} "Simple standard paths through microplate." 

	LangString DESC_SecWaitSinceLastAction ${LANG_ENGLISH} "Job which guarantees that between its last execution and the current execution a certain minimal time has passed." 
	LangString DESC_SecShareExecution ${LANG_ENGLISH} "Staggering between wells and multiple positions." 

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
		;!insertmacro MUI_DESCRIPTION_TEXT ${SecOpenBIS} $(DESC_SecOpenBIS)
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
		!insertmacro MUI_DESCRIPTION_TEXT ${SecStandardPaths} $(DESC_SecStandardPaths)

		!insertmacro MUI_DESCRIPTION_TEXT ${SecStatisticsJob} $(DESC_SecStatisticsJob)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecMultiCameraMeasurement} $(DESC_SecMultiCameraMeasurement)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecMultiCameraAndColorStream} $(DESC_SecMultiCameraAndColorStream)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecDeviceSettingManager} $(DESC_SecDeviceSettingManager)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecWaitJob} $(DESC_SecWaitJob)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecParallelImagingJob} $(DESC_SecParallelImagingJob)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecFocusingJob} $(DESC_SecFocusingJob)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecMultiCameraStream} $(DESC_SecMultiCameraStream)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecSimpleMeasurement} $(DESC_SecSimpleMeasurement)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecCellDetection} $(DESC_SecCellDetection)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecAutofocusJob} $(DESC_SecAutofocusJob)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecUserControlMeasurement} $(DESC_SecUserControlMeasurement)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecWaitForUser} $(DESC_SecWaitForUser)

		;!insertmacro MUI_DESCRIPTION_TEXT ${SecCellX} $(DESC_SecCellX) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecController} $(DESC_SecController) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecCustomJob} $(DESC_SecCustomJob) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecDropletMicrofluidics} $(DESC_SecDropletMicrofluidics) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecFluigent} $(DESC_SecFluigent) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecNemesys} $(DESC_SecNemesys) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecLiveMeasurement} $(DESC_SecLiveMeasurement) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecRepeatJob} $(DESC_SecRepeatJob) 
		!insertmacro MUI_DESCRIPTION_TEXT ${SecSLIM} $(DESC_SecSLIM)

		!insertmacro MUI_DESCRIPTION_TEXT ${SecCustomMetadata} $(DESC_SecCustomMetadata)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecCustomSaveSettings} $(DESC_SecCustomSaveSettings)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecStandardSaveSettings} $(DESC_SecStandardSaveSettings)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecSystemSkin} $(DESC_SecSystemSkin)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecDarkSkin} $(DESC_SecDarkSkin)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecRedSkin} $(DESC_SecRedSkin)

		!insertmacro MUI_DESCRIPTION_TEXT ${SecTravelingSalesman} $(DESC_SecTravelingSalesman)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecWaitSinceLastAction} $(DESC_SecWaitSinceLastAction)
		!insertmacro MUI_DESCRIPTION_TEXT ${SecShareExecution} $(DESC_SecShareExecution)

	!insertmacro MUI_FUNCTION_DESCRIPTION_END