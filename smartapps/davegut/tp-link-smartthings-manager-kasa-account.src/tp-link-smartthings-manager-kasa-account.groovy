/*
TP-Link SmartThings Manager and TP-Link Cloud Connect, 2018 Version 4
	Copyright 2018 Dave Gutheinz, Anthony Ramirez
Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at:
	http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License.
Discalimer: This Service Manager and the associated Device
Handlers are in no way sanctioned or supported by TP-Link. All
development is based upon open-source data on the TP-Link Kasa Devices;
primarily various users on GitHub.com.*/

/*	===== CHANGES =====
	1,  Added Installation Type Section to allow file-create Install type definition.
		Note: Plan is to use same appLabel for either version.  We may change to an
		Internal selector.  However, the user can not jump from one to the other
		without causing confusion
	2.	Added install type to appInfoDesc()
        
    
	===== TO DO LIST FOR ADDING NODE APPLET =====
    2.	Update device handlers to use deviceId = deviceIP and
    	appServerUrl = gatewayIP using setAppServerUrl for gateway
        and setDeviceId for deviceIP.
    3.	Update device handler to not allow user options to change
    	the following if installType = Kasa Application
        a)	installType
        b)	deviceIp
        c)	appServerUrl
    5.	Clean up help messages.
    6.	nodeIP.  Vice login, add Node Hub Address page.  Change logic so that 
    	wherever we check for null username, we also check for null nodeIp variable.
    7.	Update discovery functions to account for hub installation.
   
    
    
    
    
	===== RECOMMENDATIONS FOR CHANGES =====
    A.	Remove the userSelectedLauncher and userSelectedQuickControl
    	logic.  Always use user selected quick control options.
        Rationale:  Easier to document the single interface and the
        user selected quick control display is actually VERY good and
        easy for users to understand (good job).
	B.	Replace Information and Diagnostics page with an Instructions
    	page.  Move Diagnostics to Indroduction page so user always
        sees the current error state.
	C.	Use the same error logic for the Node Applet as Kasa Application.
    D.	Get rid of kasaUserSelectionPage and kasaComputerSelectionPage
    E.	Clean up method names to remove kasa and hub except where now needed.
        
*/
//	===== Developer Namespace =====
	def appNamespace()	{ return "davegut" }
//	def appNamespace()	{ return "ramiran2" }
//	===== Installation Type =====
	def installType() { return "Kasa Account" }
//    def installType() { return "Node Applet" }
//	====== Application Information =====
	def appLabel()	{ return "TP-Link SmartThings Manager" }
	def appVersion()	{ return "4.2.0" }
	def appVerDate()	{ return "10-25-2018" }
	def appAuthor()	{ return "Dave Gutheinz, Anthony Ramirez" }
//	==========================================================================================

definition (name: "${appLabel()} - ${installType()}", 
			namespace: "${appNamespace()}", 
            author: "${appAuthor()}", 
            description: "${textDesc()}", 
            category: "Convenience", 
            iconUrl: "${getAppImg("logo.png", on)}", 
            iconX2Url: "${getAppImg("logo.png", on)}", 
            iconX3Url: "${getAppImg("logo.png", on)}", 
            singleInstance: true)

preferences {
	page(name: "startPage")
	page(name: "kasaWelcomePage")
//	page(name: "hubWelcomePage")
	page(name: "kasaUserSelectionAuthenticationPage")
	page(name: "kasaUserAuthenticationPreferencesPage")
	page(name: "kasaComputerSelectionAuthenticationPage")
	page(name: "kasaUserSelectionPage")
	page(name: "kasaComputerSelectionPage")
	page(name: "kasaAddDevicesPage")
	page(name: "hubAddDevicesPage")
	page(name: "kasaRemoveDevicesPage")
	page(name: "hubRemoveDevicesPage")
	page(name: "kasaUserApplicationPreferencesPage")
	page(name: "hubUserApplicationPreferencesPage")
	page(name: "kasaUserDevicePreferencesPage")
	page(name: "hubUserDevicePreferencesPage")
	page(name: "kasaUserSelectionTokenPage")
	page(name: "developerPage")
	page(name: "developerTestingPage")
	page(name: "hiddenPage")
	page(name: "aboutPage")
	page(name: "changeLogPage")
	page(name: "uninstallPage")
}

def setInitialStates() {
	if (!state.TpLinkToken) {state.TpLinkToken = null}
	if (!state.devices) {state.devices = [:]}
	if (!state.currentError) {state.currentError = null}
	if (!state.errorCount) {state.errorCount = 0}
    if (!state.bridgeIp) { state.bridgeIp = null }
	settingUpdate("userSelectedReload", "false", "bool")
	settingRemove("userSelectedDevicesRemoveKasa")
	settingRemove("userSelectedDevicesAddKasa")
	settingRemove("userSelectedDevicesToUpdateKasa")
	settingRemove("userSelectedOptionThree")
    settingRemove("userSelectedOptionTwo")
    settingRemove("userSelectedOptionOne")
	if ("${userName}" =~ null || "${userPassword}" =~ null) {
		settingRemove("userName")
		settingRemove("userPassword")
	}
	if (!userSelectedDeveloper) {
		if ("${userName}" =~ null || "${userPassword}" =~ null) {
			state.TpLinkToken = null
			state.currentError = null
			state.errorCount = 0
//			settingUpdate("userSelectedLauncher", "false", "bool")
//			settingUpdate("userSelectedQuickControl", "false", "bool")
		} else {
//			settingUpdate("userSelectedLauncher", "true", "bool")
//			settingUpdate("userSelectedQuickControl", "true", "bool")
		}
//		settingUpdate("userSelectedDriverNamespace", "false", "bool")	//	If true the DaveGut is set as default
	}
}

def cleanStorage() {
	atomicState?.devManVer = null
	atomicState?.devTWBVer = null
	atomicState?.devSWBVer = null
	atomicState?.devCBVer = null
	atomicState?.devPGVer = null
	atomicState?.devEMPGVer = null
	atomicState?.devSHVer = null
	atomicState?.devDSHVer = null
	atomicState?.devVerLnk = null
}

def startPage() {
	setInitialStates()
	if (installType() == "Kasa Account" && userName == null && password == null) {
		return kasaUserSelectionAuthenticationPage()       	
	} else if (installType() == "NodeApplet" && bridgeIp == null) {
		   return enterHubBridgeIpPage()
	} else {
		kasaWelcomePage()
    }
}

//	----- WELCOME PAGE -----
def kasaWelcomePage() {
	def kasaWelcomePageText = "Welcome to the new SmartThings application for TP-Link Kasa Devices. If you want to check for updates you can now do that in the changelog page."
	def driverVersionText = "Current Driver Version: ${textDriverVersion()}"
	return dynamicPage (name: "kasaWelcomePage", title: "Introduction Page", install: false, uninstall: false) {
		section("") {
			paragraph "${appLabel()} - ${installType()}", image: getAppImg("kasa.png")
			if (state.currentError != null) {
				paragraph title:"Error Status:", "${state.currentError}! Correct before continuing.", image: getAppImg("error.png")
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Information: ", hideable: true, hidden: true) {
        	paragraph title: "Program Information: ", appInfoDesc(), image: getAppImg("kasa.png")
			paragraph title: "Help: ", kasaWelcomePageText, image: getAppImg("information.png")
		}
        
		section("Device Manager: ") {
			href "kasaAddDevicesPage", title: "Device Installer Page", description: "Tap to view", image: getAppImg("adddevicespage.png")
			href "kasaRemoveDevicesPage", title: "Device Uninstaller Page", description: "Tap to view", image: getAppImg("removedevicespage.png")
		}
        
		section("Settings: ") {
			href "kasaUserDevicePreferencesPage", title: "Device Preferences Page", description: "Tap to view", image: getAppImg("userdevicepreferencespage.png")
			href "kasaUserAuthenticationPreferencesPage", title: "Login Settings Page", description: "Tap to view", image: getAppImg("userauthenticationpreferencespage.png")
		}

		section("Uninstall: ") {
			href "uninstallPage", title: "Uninstall Page", description: "Tap to view", image: getAppImg("uninstallpage.png")
		}
        
		if (userSelectedDeveloper) {
			section("Developer: ") {
				href "developerPage", title: "Developer Page", description: "Tap to view", image: getAppImg("developerpage.png")
			}
		}
        
		section("Help and Feedback: ") {
			href url: getWikiPageUrl(), style: "${strBrowserMode()}", title: "View the Projects Wiki", description: "Tap to open in browser", state: "complete", image: getAppImg("help.png")
			href url: getIssuePageUrl(), style: "${strBrowserMode()}", title: "Report | View Issues", description: "Tap to open in browser", state: "complete", image: getAppImg("issue.png")
		}
        
		section("Changelog and About: ") {
			href "changeLogPage", title: "Changelog Page", description: "Tap to view", image: getAppImg("changelogpage.png")
			href "aboutPage", title: "About Page", description: "Tap to view", image: getAppImg("aboutpage.png")
		}
        
		section("${textCopyright()}")
	}
}

def kasaUserSelectionAuthenticationPage() {
	def kasaUserSelectionAuthenticationPageText = "If possible, open the IDE and select Live Logging. Then, " +
		"enter your Username and Password for TP-Link (same as Kasa app) and the "+
		"action you want to complete. " + "\nAvailable actions: \n" +
		"Activate Account: You will be required to login into TP-Link Kasa Account and you will be required to adds devices to SmartThings Account. \n" +
		"Update Account: You will be required to update your credentials to login into your TP-Link Kasa Account. \n" +
		"Delete Account: Deletes your credentials to login into your TP-Link Kasa Account."
	return dynamicPage (name: "kasaUserSelectionAuthenticationPage", title: "Login Page", nextPage: "kasaComputerSelectionAuthenticationPage", install: false, uninstall: false) {
		section("") {
			paragraph "${appLabel()} - ${installType()}", image: getAppImg("kasa.png")
			if (state.currentError != null) {
				paragraph title:"Error Status:", "${state.currentError}! Correct before continuing.", image: getAppImg("error.png")
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Information and Help: ", hideable: true, hidden: true) {
        	paragraph title: "Program Information: ", appInfoDesc(), image: getAppImg("kasa.png")
			paragraph title: "Help: ", kasaUserSelectionAuthenticationPageText, image: getAppImg("information.png")
		}
        
		section("Account Configuration: ") {
			input ("userName", "email", title: "TP-Link Kasa Email Address", required: true, submitOnChange: false, image: getAppImg("email.png"))
			input ("userPassword", "password", title: "TP-Link Kasa Account Password", required: true, submitOnChange: false, image: getAppImg("password.png"))
		}
        
		section("User Configuration: ") {
			input ("userSelectedOptionTwo", "enum", title: "What do you want to do?", required: true, multiple: false, submitOnChange: true, metadata: [values:["Update Account", "Activate Account", "Delete Account"]], image: getAppImg("userinput.png"))
		}
        
		section("Page Selector: ") {
			if (userSelectedOptionTwo =~ "Activate Account") {
				paragraph "You must Install a device to continue", image: getAppImg("pickapage.png")
				getToken()
				href "kasaAddDevicesPage", title: "Device Installer Page", description: "Tap to continue", image: getAppImg("adddevicespage.png")
			}
            
			if (userSelectedOptionTwo =~ "Update Account") {
				paragraph "You must select below to continue.", image: getAppImg("pickapage.png")
				href "kasaUserSelectionTokenPage", title: "Token Manager Page", description: "Tap to continue", image: getAppImg("userselectiontokenpage.png")
			}
            
			if (userSelectedOptionTwo =~ "Delete Account") {
				paragraph "Select below to inactivate token and delete username and password!  Use the < at top left to back out.", image: getAppImg("pickapage.png")
				settingRemove("userName")
				settingRemove("userPassword")
				state.TpLinkToken = null
				href "kasaWelcomePage", title: "Introduction Page", description: "Tap to view", image: getAppImg("welcomepage.png")
			}
		}
        
		section("${textCopyright()}")
	}
}

//	----- USER AUTHENTICATION PREFERENCES PAGE -----
def kasaUserAuthenticationPreferencesPage() {
	def kasaUserAuthenticationPreferencesPageText = "If possible, open the IDE and select Live Logging. Then, " +
		"enter your Username and Password for TP-Link (same as Kasa app) and the "+
		"action you want to complete."
	return dynamicPage (name: "kasaUserSelectionAuthenticationPage", title: "Login Settings Page", install: false, uninstall: false) {
		section("") {
			paragraph "${appLabel()} - ${installType()}", image: getAppImg("kasa.png")
			if (state.currentError != null) {
				paragraph title:"Error Status:", "${state.currentError}! Correct before continuing.", image: getAppImg("error.png")
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Information and Help: ", hideable: true, hidden: true) {
        	paragraph title: "Program Information: ", appInfoDesc(), image: getAppImg("kasa.png")
			paragraph title: "Help: ", kasaUserAuthenticationPreferencesPageText, image: getAppImg("information.png")
		}
        
		section("Account Configuration: ") {
			input ("userName", "email", title: "TP-Link Kasa Email Address", required: true, submitOnChange: false, image: getAppImg("email.png"))
			input ("userPassword", "password", title: "TP-Link Kasa Account Password", required: true, submitOnChange: false, image: getAppImg("password.png"))
		}
        
		section("Page Selector: ") {
			paragraph "You must select below to continue", image: getAppImg("pickapage.png")
			href "kasaUserSelectionTokenPage", title: "Token Manager Page", description: "Tap to continue", image: getAppImg("userselectiontokenpage.png")
        }
        
		section("${textCopyright()}")
	}
}

def enterHubBridgeIpPage() {
//	To be created.  Single entry and set of state bridgeIp
}

//	----- ADD DEVICES PAGE -----
def kasaAddDevicesPage() {
	getDevices()
	def devices = state.devices
	def errorMsgDev = "None"
	def newDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceMac)
		if (!isChild) {
			newDevices["${it.value.deviceMac}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	if (devices == [:]) {
		errorMsgDev = "We were unable to find any TP-Link Kasa devices on your account. This usually means "+
		"that all devices are in 'Local Control Only'. Correct them then " + "rerun the application."
	}
	if (newDevices == [:]) {
		errorMsgDev = "No new devices to add. Are you sure they are in Remote " + "Control Mode?"
	}
	def kasaAddDevicesPageText = "Devices that have not been previously installed and are not in 'Local " +
		"WiFi control only' will appear below. Tap below to see the list of " +
		"TP-Link Kasa Devices available select the ones you want to connect to " +
		"SmartThings.\n" + "Press Done when you have selected the devices you " +
		"wish to add, then press Save to add the devices to your SmartThings account."
	return dynamicPage (name: "kasaAddDevicesPage", title: "Device Installer Page", install: true, uninstall: false) {
		section("") {
			paragraph "${appLabel()} - ${installType()}", image: getAppImg("kasa.png")
			if (state.currentError != null) {
				paragraph title:"Error Status:", "${state.currentError}! Correct before continuing.", image: getAppImg("error.png")
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Information and Help: ", hideable: true, hidden: true) {
        	paragraph title: "Program Information: ", appInfoDesc(), image: getAppImg("kasa.png")
			paragraph title: "Help: ", kasaAddDevicesPageText, image: getAppImg("information.png")
		}
        
 		section("Device Controller: ") {
			input ("userSelectedDevicesAddKasa", "enum", required: true, multiple: true, submitOnChange: false, title: "Select Devices to Add (${newDevices.size() ?: 0} found)", metadata: [values:newDevices], image: getAppImg("adddevices.png"))
		}
        
		section("${textCopyright()}")
	}
}

//	----- REMOVE DEVICES PAGE -----
def kasaRemoveDevicesPage() {
	getDevices()
	def devices = state.devices
	def errorMsgDev = "None"
	def oldDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceMac)
		if (isChild) {
			oldDevices["${it.value.deviceMac}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	if (devices == [:]) {
		errorMsgDev = "We were unable to find any TP-Link Kasa devices on your account. This usually means "+
		"that all devices are in 'Local Control Only'. Correct them then " + "rerun the application."
	}
	if (oldDevices == [:]) {
		errorMsgDev = "There are no devices to remove from the SmartThings app at this time."
	}
	def kasaRemoveDevicesPageText = "Devices that have been installed " +
		"will appear below. Tap below to see the list of " +
		"TP-Link Kasa Devices available select the ones you want to connect to " +
		"SmartThings.\n" + "Press Done when you have selected the devices you " +
		"wish to remove, then Press Save to remove the devices to your SmartThings account."
	return dynamicPage (name: "kasaRemoveDevicesPage", title: "Device Uninstaller Page", install: true, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Information and Diagnostics: ", hideable: true, hidden: true) {
			if (state.TpLinkToken != null) {
				paragraph tokenInfoOnline(), image: getAppImg("tokenactive.png")
			} else {
				paragraph tokenInfoOffline(), image: getAppImg("error.png")
			}
			paragraph title: "Information: ", kasaRemoveDevicesPageText, image: getAppImg("information.png")
			paragraph title: "Device Error: ", errorMsgDev, image: getAppImg("error.png")
		}
		section("Device Controller: ") {
			input ("userSelectedDevicesRemoveKasa", "enum", required: true, multiple: true, submitOnChange: false, title: "Select Devices to Remove (${oldDevices.size() ?: 0} found)", metadata: [values:oldDevices], image: getAppImg("removedevices.png"))
		}
		section("${textCopyright()}")
	}
}

//	----- USER APPLICATION PREFERENCES PAGE -----
def kasaUserApplicationPreferencesPage() {
	def hiddenRecordInput = 0
	def hiddenDeveloperInput = 0
	if (userSelectedDeveloper) {
		hiddenDeveloperInput = 1
	} else {
		hiddenDeveloperInput = 0
	}
	if ("${restrictedRecordPasswordPrompt}" =~ null) {
		hiddenRecordInput = 0
	} else {
		hiddenRecordInput = 1
	}
	def kasaUserApplicationPreferencesPageText = "Welcome to the application settings page. \n" +
		"Recommended options: Will allow your device to pick a option for you that you are likely to pick."
	return dynamicPage (name: "kasaUserApplicationPreferencesPage", title: "Application Settings Page", install: true, uninstall: false) {
		section("") {
			paragraph "${appLabel()} - ${installType()}", image: getAppImg("kasa.png")
			if (state.currentError != null) {
				paragraph title:"Error Status:", "${state.currentError}! Correct before continuing.", image: getAppImg("error.png")
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Information and Help: ", hideable: true, hidden: true) {
        	paragraph title: "Program Information: ", appInfoDesc(), image: getAppImg("kasa.png")
			paragraph title: "Help: ", kasaUserApplicationPreferencesPageText, image: getAppImg("information.png")
		}
        
		section("Application Configuration: ") {
			input ("userSelectedNotification", "bool", title: "Do you want to enable notification?", submitOnChange: false, image: getAppImg("notification.png"))
			input ("userSelectedAppIcons", "bool", title: "Do you want to disable application icons?", submitOnChange: false, image: getAppImg("noicon.png"))
			input ("userSelectedManagerMode", "bool", title: "Do you want to switch to hub controller mode?", submitOnChange: false, image: getAppImg("samsunghub.png"))
			input ("userSelectedBrowserMode", "bool", title: "Do you want to open all external links within the SmartThings app?", submitOnChange: false, image: getAppImg("browsermode.png"))
			input ("userSelectedReload", "bool", title: "Do you want to refresh your current state?", submitOnChange: true, image: getAppImg("sync.png"))
			if (userSelectedAppIcons && userSelectedReload || hiddenDeveloperInput == 1) {
				hiddenDeveloperInput = 1
				input ("userSelectedDeveloper", "bool", title: "Do you want to enable developer mode?", submitOnChange: true, image: getAppImg("developer.png"))
			}
			if (userSelectedDeveloper) {
				input ("userSelectedTestingPage", "bool", title: "Do you want to enable developer testing mode?", submitOnChange: true, image: getAppImg("developertesting.png"))
			}
			if (userSelectedTestingPage && userSelectedReload || hiddenRecordInput == 1) {
				hiddenRecordInput = 1
				input ("restrictedRecordPasswordPrompt", type: "password", title: "This is a restricted record, Please input your password", description: "Hint: xKillerMaverick", required: false, submitOnChange: false, image: getAppImg("passwordverification.png"))
			}
			if (userSelectedReload) {
				checkError()
				setInitialStates()
			} else {
				settingUpdate("userSelectedReload", "false", "bool")
			}
		}
        
		section("${textCopyright()}")
	}
}

//	----- USER DEVICE PREFERENCES PAGE -----
def kasaUserDevicePreferencesPage() {
	getDevices()
	def devices = state.devices
	def oldDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceMac)
		if (isChild) {
			oldDevices["${it.value.deviceMac}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}

	def kasaUserDevicePreferencesPageText = "Welcome to the Device Preferences page. \n" +
		"Enter a value for Transition Time and Refresh Rate then select the devices that you want to update. \n" +
		"After that you may procide to save by clicking the save button."
	return dynamicPage (name: "kasaUserDevicePreferencesPage", title: "Device Preferences Page", install: true, uninstall: false) {
		section("") {
			paragraph "${appLabel()} - ${installType()}", image: getAppImg("kasa.png")
			if (state.currentError != null) {
				paragraph title:"Error Status:", "${state.currentError}! Correct before continuing.", image: getAppImg("error.png")
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Information and Help: ", hideable: true, hidden: true) {
        	paragraph title: "Program Information: ", appInfoDesc(), image: getAppImg("kasa.png")
			paragraph title: "Help: ", kasaUserDevicePreferencesPage, image: getAppImg("information.png")
		}
        
		section("Device Configuration: ") {
			input ("userSelectedDevicesToUpdateKasa", "enum", required: true, multiple: true, submitOnChange: false, title: "Select Devices to Update (${oldDevices.size() ?: 0} found)", metadata: [values: oldDevices], image: getAppImg("devices.png"))
			input ("userLightTransTime", "enum", required: true, multiple: false, submitOnChange: false, title: "Lighting Transition Time", metadata: [values:["500" : "0.5 second", "1000" : "1 second", "1500" : "1.5 second", "2000" : "2 seconds", "2500" : "2.5 seconds", "5000" : "5 seconds", "10000" : "10 seconds", "20000" : "20 seconds", "40000" : "40 seconds", "60000" : "60 seconds"]], image: getAppImg("transition.png"))
			input ("userRefreshRate", "enum", required: true, multiple: false, submitOnChange: false, title: "Device Refresh Rate", metadata: [values:["1" : "Refresh every minute", "5" : "Refresh every 5 minutes", "10" : "Refresh every 10 minutes", "15" : "Refresh every 15 minutes", "30" : "Refresh every 30 minutes"]], image: getAppImg("refresh.png"))
		}

		section("${textCopyright()}")
	}
}

//	----- MISC LEVEL 2 PAGES -----
def kasaUserSelectionTokenPage() {
	def kasaUserSelectionTokenPageText = "Your current token: ${state.TpLinkToken}" +
		"\nAvailable actions:\n" +
		"Update Token: Updates the token on your SmartThings Account from your TP-Link Kasa Account. \n" +
		"Remove Token: Removes the token on your SmartThings Account from your TP-Link Kasa Account. \n" +
		"Recheck Token: This will attempt to check if the token is valid as well as check for errors."
		def errorMsgTok = "None"
		if (state.TpLinkToken == null) {
			errorMsgTok = "You will be unable to control your devices until you get a new token."
		}
		if (state.currentError != null) {
			errorMsgTok = "You may not be able to control your devices until you update your credentials."
		}
	dynamicPage (name: "kasaUserSelectionTokenPage", title: "Token Manager Page", install: false, uninstall: false) {
		section("") {
			paragraph "${appLabel()} - ${installType()}", image: getAppImg("kasa.png")
			if (state.currentError != null) {
				paragraph title:"Error Status:", "${state.currentError}! Correct before continuing.", image: getAppImg("error.png")
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Information and Help: ", hideable: true, hidden: true) {
        	paragraph title: "Program Information: ", appInfoDesc(), image: getAppImg("kasa.png")
			paragraph title: "Help: ", kasaUserSelectionTokenPageText, image: getAppImg("information.png")
		}
        
		section("Account Status: ") {
			if (state.TpLinkToken != null) {
				paragraph tokenInfoOnline(), image: getAppImg("tokenactive.png")
			} else {
				paragraph tokenInfoOffline(), image: getAppImg("error.png")
			}
		}
        
		section("User Configuration: ") {
			input ("userSelectedOptionThree", "enum", title: "What do you want to do?", required: true, multiple: false, submitOnChange: true, metadata: [values:["Update Token", "Recheck Token", "Delete Token"]], image: getAppImg("token.png"))
		}
        
		section("Command Status: ") {
			if (userSelectedOptionThree != null) {
				if (state.currentError != null) {
					paragraph pageSelectorErrorText(), image: getAppImg("error.png")
				} else {
					paragraph sendingCommandSuccess(), image: getAppImg("sent.png")
				}
			} else {
				paragraph sendingCommandFailed(), image: getAppImg("issue.png")
			}
            
			if (userSelectedOptionThree =~ "Update Token") {
				getToken()
			}
            
			if (userSelectedOptionThree =~ "Delete Token") {
				state.TpLinkToken = null
			}
            
			if (userSelectedOptionThree =~ "Recheck Token") {
				checkError()
			}
		}
        
		section("${textCopyright()}")
	}
}

def aboutPage() {
	dynamicPage (name: "aboutPage", title: "About Page", install: false, uninstall: false) {
		section("") {
			paragraph "${appLabel()} - ${installType()}", image: getAppImg("kasa.png")
			if (state.currentError != null) {
				paragraph title:"Error Status:", "${state.currentError}! Correct before continuing.", image: getAppImg("error.png")
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Information: ", hideable: true, hidden: true) {
        	paragraph title: "Program Information: ", appInfoDesc(), image: getAppImg("kasa.png")
		}
        
		section("Donations: ") {
			paragraph title: "Donations (@DaveGut)", "Donate to a charity", state: "complete", image: getAppImg("heart.png")
			href url: textDonateLinkAntR(), style: "${strBrowserMode()}", required: false, title: "Donations (@ramiran2)", description: "Tap to open in browser", state: "complete", image: getAppImg("paypal.png")
		}
        
		section("Credits: ") {
			paragraph title: "Creator: ", "Dave G. (@DaveGut)", state: "complete", image: getAppImg("dave.png")
			paragraph title: "Co-Author: ", "Anthony R. (@ramiran2)", state: "complete", image: getAppImg("bigmac.png")
			if ("${restrictedRecordPasswordPrompt}" =~ "Mac5089") {
			paragraph title: "Unknown: ", "Lindsey M. (@Unknown)", state: "complete", image: getAppImg("unknown.png")
			}
			paragraph title: "Collaborator: ", "Anthony S. (@tonesto7)", state: "complete", image: getAppImg("tonesto7.png")
		}
        
		section("Application Changes Details: ") {
			href "changeLogPage", title: "View App Revision History", description: "Tap to view", image: getAppImg("changelogpage.png")
		}
        
		section("GitHub: ") {
			href url: linkGitHubDavG(), style: "${strBrowserMode()}", required: false, title: "Dave G. (@DaveGut)", description: "Tap to open in browser", state: "complete", image: getAppImg("github.png")
			href url: linkGitHubAntR(), style: "${strBrowserMode()}", required: false, title: "Anthony R. (@ramiran2)", description: "Tap to open in browser", state: "complete", image: getAppImg("github.png")
			href url: linkGitHubAntS(), style: "${strBrowserMode()}", required: false, title: "Anthony S. (@tonesto7)", description: "Tap to open in browser", state: "complete", image: getAppImg("github.png")
		}
        
		section("Licensing Information: ") {
			paragraph "${textLicense()}"
		}
        
		section("${textCopyright()}")
	}
}

def changeLogPage() {
	cleanStorage()
	checkForUpdates()
	def intUpdateCheckOne = 0
	def intUpdateCheckTwo = 0
	def childDevices = app.getChildDevices(true)
	def strLatestSmartAppVersion = textSmartAppVersion()
	def updateNeeded = "Both the Smart Application and Device Handlers need to be updated"
	def upToDate = "Both the Smart Application and Device Handlers are up to date"
	def driverUpdateNeeded = "Your Device Handlers need to be updated"
	def smartAppUpdateNeeded = "Your Smart Application needs to be updated"
	def updateFailed = "We are unable to check for updates"
	def updateNeedsDevices = "We are unable to check for updates, please check if you have any devices installed"
	dynamicPage (name: "changeLogPage", title: "Changelog Page", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
        
		section("Check for Updates: ") {
			if (childDevices) {
				if ("${strLatestSmartAppVersion}" =~ "${appVersion()}" && "${atomicState?.devManVer}" =~ "${atomicState?.devVerLnk}") {
					paragraph upToDate, image: getAppImg("success.png")
				} else {
					if ("${strLatestSmartAppVersion}" =~ "${appVersion()}" && "${atomicState?.devManVer}" =~ "${atomicState?.devVerLnk}") {
						if ("${strLatestSmartAppVersion}" != "${appVersion()}") {
							paragraph smartAppUpdateNeeded, image: getAppImg("issue.png")
						} else {
							intUpdateCheckOne = 1
						}
						if ("${atomicState?.devManVer}" != "${atomicState?.devVerLnk}") {
							paragraph driverUpdateNeeded, image: getAppImg("issue.png")
						} else {
							intUpdateCheckTwo = 1
						}
						if (intUpdateCheckOne == 1 && intUpdateCheckTwo == 1) {
							paragraph updateFailed, image: getAppImg("error.png")
						}
					} else {
						paragraph updateNeeded, image: getAppImg("error.png")
					}
				}
			} else {
				if ("${strLatestSmartAppVersion}" =~ "${appVersion()}") {
					paragraph upToDate, image: getAppImg("success.png")
				} else {
					paragraph updateNeedsDevices, image: getAppImg("issue.png")
				}
			}
		}
        
		section("Changelog: ") {
			paragraph title: "What's New in this Release...", "", state: "complete", image: getAppImg("new.png")
			paragraph appVerInfo()
		}
        
		section("${textCopyright()}")
	}
}

def uninstallPage() {
	def uninstallPageText = "This will uninstall the All Child Devices including this Application with all it's user data. \nPlease make sure that any devices created by this app are removed from any routines/rules/smartapps before tapping Remove."
	dynamicPage (name: "uninstallPage", title: "Uninstall Page", install: false, uninstall: true) {
		section("") {
			paragraph "${appLabel()} - ${installType()}", image: getAppImg("kasa.png")
			if (state.currentError != null) {
				paragraph title:"Error Status:", "${state.currentError}! Correct before continuing.", image: getAppImg("error.png")
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Information and Help: ", hideable: true, hidden: true) {
        	paragraph title: "Program Information: ", appInfoDesc(), image: getAppImg("kasa.png")
			paragraph title: "Help: ", uninstallPageText, image: getAppImg("information.png")
		}
        
		section("${textCopyright()}")
        
		remove("Uninstall this application", "Warning!!!", "Last Chance to Stop! \nThis action is not reversible \n\nThis will remove All Devices including this Application with all it's user data")
	}
}

def checkForUpdates() {
	def strLatestSmartAppVersion = textSmartAppVersion()
	def strLatestDriverVersion = textDriverVersion()
	def intMessage = 0
	def strDevVersion = atomicState?.devManVer ?: [:]
	strDevVersion["devVer"] = strLatestDriverVersion ?: ""
	atomicState?.devManVer = strDevVersion
	def childDevices = app.getChildDevices(true)
	childDevices?.each {
		def strTypRawData = it?.currentState("devTyp")?.value?.toString()
		def strDeviceType = atomicState?.devTyp ?: [:]
		strDeviceType["devTyp"] = strTypRawData ?: ""
		atomicState?.devTyp = strDeviceType
		if (atomicState?.devTyp =~ "Tunable White Bulb") {
			def strTWBRawData = it?.currentState("devVer")?.value?.toString()
			def strTWB = atomicState?.devTWBVer ?: [:]
			strTWB["devVer"] = strTWBRawData ?: ""
			atomicState?.devTWBVer = strTWB
		}
		if (atomicState?.devTyp =~ "Soft White Bulb") {
			def strSWBRawData = it?.currentState("devVer")?.value?.toString()
			def strSWB = atomicState?.devSWBVer ?: [:]
			strSWB["devVer"] = strSWBRawData ?: ""
			atomicState?.devSWBVer = strSWB
		}
		if (atomicState?.devTyp =~ "Color Bulb") {
			def strCBRawData = it?.currentState("devVer")?.value?.toString()
			def strCB = atomicState?.devCBVer ?: [:]
			strCB["devVer"] = strCBRawData ?: ""
			atomicState?.devCBVer = strCB
		}
		if (atomicState?.devTyp =~ "Plug") {
			def strPGRawData = it?.currentState("devVer")?.value?.toString()
			def strPG = atomicState?.devPGVer ?: [:]
			strPG["devVer"] = strPGRawData ?: ""
			atomicState?.devPGVer = strPG
		}
		if (atomicState?.devTyp =~ "Energy Monitor Plug") {
			def strEMPGRawData = it?.currentState("devVer")?.value?.toString()
			def strEMPG = atomicState?.devEMPGVer ?: [:]
			strEMPG["devVer"] = strEMPGRawData ?: ""
			atomicState?.devEMPGVer = strEMPG
		}
		if (atomicState?.devTyp =~ "Switch") {
			def strSHRawData = it?.currentState("devVer")?.value?.toString()
			def strSH = atomicState?.devSHVer ?: [:]
			strSH["devVer"] = strSHRawData ?: ""
			atomicState?.devSHVer = strSH
		}
		if (atomicState?.devTyp =~ "Dimming Switch") {
			def strDSHRawData = it?.currentState("devVer")?.value?.toString()
			def strDSH = atomicState?.devDSHVer ?: [:]
			strDSH["devVer"] = strDSHRawData ?: ""
			atomicState?.devDSHVer = strDSH
		}
	}
	if (atomicState?.devTWBVer =~ null) {
		atomicState?.devTWBVer = strDevVersion
	} else {
		atomicState?.devVerLnk = atomicState?.devTWBVer
	}
	if (atomicState?.devSWBVer =~ null) {
		atomicState?.devSWBVer = strDevVersion
	} else {
		atomicState?.devVerLnk = atomicState?.devSWBVer
	}
	if (atomicState?.devCBVer =~ null) {
		atomicState?.devCBVer = strDevVersion
	} else {
		atomicState?.devVerLnk = atomicState?.devCBVer
	}
	if (atomicState?.devPGVer =~ null) {
		atomicState?.devPGVer = strDevVersion
	} else {
		atomicState?.devVerLnk = atomicState?.devPGVer
	}
	if (atomicState?.devEMPGVer =~ null) {
		atomicState?.devEMPGVer = strDevVersion
	} else {
		atomicState?.devVerLnk = atomicState?.devEMPGVer
	}
	if (atomicState?.devSHVer =~ null) {
		atomicState?.devSHVer = strDevVersion
	} else {
		atomicState?.devVerLnk = atomicState?.devSHVer
	}
	if (atomicState?.devDSHVer =~ null) {
		atomicState?.devDSHVer = strDevVersion
	} else {
		atomicState?.devVerLnk = atomicState?.devDSHVer
	}
	if ("${atomicState?.devManVer}" =~ "${atomicState?.devVerLnk}") {
		intMessage = 3
	} else {
		if (userSelectedNotification) {
			sendPush("${appLabel()} Device Handlers need to be updated")
		}
	}
	if ("${strLatestSmartAppVersion}" =~ "${appVersion()}" ) {
		if (intMessage == 3) {
			intMessage = 2
		} else {
			intMessage = 1
		}
	} else {
		if (userSelectedNotification) {
			sendPush("${appLabel()} needs to be updated")
		}
	}
}

void settingUpdate(name, value, type=null) {
	log.trace "settingUpdate($name, $value, $type)..."
	if(name) {
		if(value == "" || value == null || value == []) {
			settingRemove(name)
			return
		}
	}
	if(name && type) {
		app?.updateSetting("$name", [type: "$type", value: value])
	}
	else if (name && type == null) { app?.updateSetting(name.toString(), value) }
}

void settingRemove(name) {
	log.trace "settingRemove($name)..."
	if(name) { app?.deleteSetting("$name") }
}

//	----- GET DEVICE DATA INTO THE SYSTEM -----
def getDevices() {
	def currentDevices
	if (installType() == "Kasa Account") {
		currentDevices = getDeviceData()
    } else {
    	currentDevices = getHubDeviceData()
    }
	state.devices = [:]
	def devices = state.devices
	currentDevices.each {
		def device = [:]
		device["deviceMac"] = it.deviceMac
		device["alias"] = it.alias
		device["deviceModel"] = it.deviceModel
		device["deviceId"] = it.deviceId
		device["appServerUrl"] = it.appServerUrl
		devices << ["${it.deviceMac}" : device]
		def isChild = getChildDevice(it.deviceMac)
		if (isChild) {
			isChild.setAppServerUrl(it.appServerUrl)
            isChild.setDeviceId(it.deviceId)
		}
		log.info "Device ${it.alias} added to devices array"
	}
}

def getDeviceData() {
	def currentDevices = ""
	def cmdBody = [method: "getDeviceList"]
	def getDevicesParams = [
		uri: "https://wap.tplinkcloud.com?token=${state.TpLinkToken}",
		requestContentType: 'application/json',
		contentType: 'application/json',
		headers: ['Accept':'application/json; version=1, */*; q=0.01'],
		body : new groovy.json.JsonBuilder(cmdBody).toString()
	]
	httpPostJson(getDevicesParams) {resp ->
		if (resp.status == 200 && resp.data.error_code == 0) {
			currentDevices = resp.data.result.deviceList
			if (state.currentError != null) {
				state.currentError = null
			}
			return currentDevices
		} else if (resp.status != 200) {
			state.currentError = resp.statusLine
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in getDeviceData: ${state.currentError}"
		} else if (resp.data.error_code != 0) {
			state.currentError = resp.data
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in getDeviceData: ${state.currentError}"
		}
	}
}

def getHubDeviceData() {
//	log.debug "sendCmd:  ${command}"
	def bridgeIP = state.bridgeIP
	def currentDevices = ""
	def headers = [:]
	headers.put("HOST", "$bridgeIP:8082")
	headers.put("command", "pollForDevices")
	def cmdStr = new physicalgraph.device.HubAction([
		[headers: headers],
        null
//		, [callback: parseHubDevices]
	])
	sendHubCommand(cmdStr) {resp ->
		if (resp.status == 200 && resp.data.error_code == 0) {
			currentDevices =  parseJson(response.headers["cmd-response"])
			state.currentError = null
			return currentDevices
		} else if (resp.status != 200) {
			state.currentError = resp.statusLine
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in getDeviceData: ${state.currentError}"
		}
    }
}

//	----- ACTION PAGES. Add, Delete, Update Devices.  Delete Device -----
def addDevices() {
	def tpLinkModel = [:]
	//	Plug-Switch Devices (no energy monitor capability)
	tpLinkModel << ["HS100" : "TP-Link Smart Plug"]						//	HS100
	tpLinkModel << ["HS103" : "TP-Link Smart Plug"]						//	HS103
	tpLinkModel << ["HS105" : "TP-Link Smart Plug"]						//	HS105
	tpLinkModel << ["HS200" : "TP-Link Smart Switch"]					//	HS200
	tpLinkModel << ["HS210" : "TP-Link Smart Switch"]					//	HS210
	tpLinkModel << ["KP100" : "TP-Link Smart Plug"]						//	KP100
	//	Dimming Switch Devices
	tpLinkModel << ["HS220" : "TP-Link Smart Dimming Switch"]			//	HS220
	//	Energy Monitor Plugs
	tpLinkModel << ["HS110" : "TP-Link Smart Energy Monitor Plug"]		//	HS110
	tpLinkModel << ["HS115" : "TP-Link Smart Energy Monitor Plug"]		//	HS110
	//	Soft White Bulbs
	tpLinkModel << ["KB100" : "TP-Link Smart Soft White Bulb"]			//	KB100
	tpLinkModel << ["LB100" : "TP-Link Smart Soft White Bulb"]			//	LB100
	tpLinkModel << ["LB110" : "TP-Link Smart Soft White Bulb"]			//	LB110
	tpLinkModel << ["KL110" : "TP-Link Smart Soft White Bulb"]			//	KL110
	tpLinkModel << ["LB200" : "TP-Link Smart Soft White Bulb"]			//	LB200
	//	Tunable White Bulbs
	tpLinkModel << ["LB120" : "TP-Link Smart Tunable White Bulb"]		//	LB120
	tpLinkModel << ["KL120" : "TP-Link Smart Tunable White Bulb"]		//	KL120
	//	Color Bulbs
	tpLinkModel << ["KB130" : "TP-Link Smart Color Bulb"]				//	KB130
	tpLinkModel << ["LB130" : "TP-Link Smart Color Bulb"]				//	LB130
	tpLinkModel << ["KL130" : "TP-Link Smart Color Bulb"]				//	KL130
	tpLinkModel << ["LB230" : "TP-Link Smart Color Bulb"]				//	LB230

	def hub = location.hubs[0]
	def hubId = hub.id
	userSelectedDevicesAddKasa.each { dni ->
		try {
			def isChild = getChildDevice(dni)
			if (!isChild) {
				def device = state.devices.find { it.value.deviceMac == dni }
				def deviceModel = device.value.deviceModel.substring(0,5)
				addChildDevice(
                	"${appNamespace()}", 
                	tpLinkModel["${deviceModel}"],
                    device.value.deviceMac,hubId, [
                    	"label" : device.value.alias,
                    	"name" : device.value.deviceModel,
                    	"data" : [
                        	"deviceId" : device.value.deviceId, 
                            "appServerUrl" : device.value.appServerUrl,
                            "installType" : installType()
                        ]
                    ]
                )
				log.info "Installed TP-Link $deviceModel with alias ${device.value.alias}"
				if (userSelectedNotification) {
					sendPush("Successfully installed TP-Link $deviceModel with alias ${device.value.alias}")
				}
			}
		} catch (e) {
			log.debug "Error Adding ${deviceModel}: ${e}"
		}
	}
}

def removeDevices() {
	userSelectedDevicesRemoveKasa.each { dni ->
		try{
			def isChild = getChildDevice(dni)
			if (isChild) {
				def delete = isChild
				delete.each { deleteChildDevice(it.deviceNetworkId, true) }
			}
			if (userSelectedNotification) {
				sendPush("Successfully uninstalled TP-Link $deviceModel with alias ${device.value.alias}")
			}
		} catch (e) {
			log.debug "Error deleting ${it.deviceNetworkId}: ${e}"
		}
	}
}

def updatePreferences() {
	userSelectedDevicesToUpdateKasa.each {
		def child = getChildDevice(it)
		child.setLightTransTime(userLightTransTime)
		child.setRefreshRate(userRefreshRate)
		log.info "Kasa device ${child} preferences updated"
		if (userSelectedNotification) {
			sendPush("Successfully updated TP-Link $deviceModel with alias ${device.value.alias}")
		}
	}
}

def uninstManagerApp() {
	try {
		//Revokes TP-Link Auth Token
		state.TpLinkToken = null
		state.currentError = null
		state.errorCount = null
		settingRemove("userName")
		settingRemove("userPassword")
		settingRemove("restrictedRecordPasswordPrompt")
		if ("${userName}" =~ null || "${userPassword}" =~ null) {
			if (userSelectedNotification) {
				sendPush("${appLabel()} is uninstalled")
			}
		}
	} catch (ex) {
		log.error "uninstManagerApp Exception: ", ex
	}
}

//	----- GET A NEW TOKEN FROM CLOUD -----
def getToken() {
	def hub = location.hubs[0]
	def cmdBody = [
		method: "login",
		params: [
			appType: "Kasa_Android",
			cloudUserName: "${userName}",
			cloudPassword: "${userPassword}",
			terminalUUID: "${hub.id}"
		]
	]
	def getTokenParams = [
		uri: "https://wap.tplinkcloud.com",
		requestContentType: 'application/json',
		contentType: 'application/json',
		headers: ['Accept':'application/json; version=1, */*; q=0.01'],
		body : new groovy.json.JsonBuilder(cmdBody).toString()
	]
	httpPostJson(getTokenParams) {resp ->
		if (resp.status == 200 && resp.data.error_code == 0) {
			state.TpLinkToken = resp.data.result.token
			log.info "TpLinkToken updated to ${state.TpLinkToken}"
			sendEvent(name: "TokenUpdate", value: "tokenUpdate Successful.")
			if (state.currentError != null) {
				state.currentError = null
			}
		} else if (resp.status != 200) {
			state.currentError = resp.statusLine
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in getToken: ${state.currentError}"
			sendEvent(name: "TokenUpdate", value: state.currentError)
		} else if (resp.data.error_code != 0) {
			state.currentError = resp.data
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in getToken: ${state.currentError}"
			sendEvent(name: "TokenUpdate", value: state.currentError)
		}
	}
}

//	----- SEND DEVICE COMMAND TO CLOUD FOR DH -----
def sendDeviceCmd(appServerUrl, deviceId, command) {
	def cmdResponse = ""
	def cmdBody = [
		method: "passthrough",
		params: [
			deviceId: deviceId,
			requestData: "${command}"
		]
	]
	def sendCmdParams = [
		uri: "${appServerUrl}/?token=${state.TpLinkToken}",
		requestContentType: 'application/json',
		contentType: 'application/json',
		headers: ['Accept':'application/json; version=1, */*; q=0.01'],
		body : new groovy.json.JsonBuilder(cmdBody).toString()
	]
	httpPostJson(sendCmdParams) {resp ->
		if (resp.status == 200 && resp.data.error_code == 0) {
			def jsonSlurper = new groovy.json.JsonSlurper()
			cmdResponse = jsonSlurper.parseText(resp.data.result.responseData)
			if (state.errorCount != 0) {
				state.errorCount = 0
			}
			if (state.currentError != null) {
				state.currentError = null
				sendEvent(name: "currentError", value: null)
				log.debug "state.errorCount = ${state.errorCount}	//	state.currentError = ${state.currentError}"
			}
		//log.debug "state.errorCount = ${state.errorCount}		//	state.currentError = ${state.currentError}"
		} else if (resp.status != 200) {
			state.currentError = resp.statusLine
			cmdResponse = "ERROR: ${resp.statusLine}"
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in sendDeviceCmd: ${state.currentError}"
		} else if (resp.data.error_code != 0) {
			state.currentError = resp.data
			cmdResponse = "ERROR: ${resp.data.msg}"
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in sendDeviceCmd: ${state.currentError}"
		}
	}
	return cmdResponse
}

//	----- INSTALL, UPDATE, INITIALIZE, UNINSTALLED -----
def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	unsubscribe()
	unschedule()
	runEvery5Minutes(checkError)
	runEvery3Hours(cleanStorage)
	runEvery3Hours(checkForUpdates)
	schedule("0 30 2 ? * WED", getToken)
	if (userSelectedDevicesAddKasa) {
		addDevices()
	}
	if (userSelectedDevicesRemoveKasa) {
		removeDevices()
	}
	if (userSelectedDevicesToUpdateKasa) {
		updatePreferences()
	}
}

def uninstalled() {
	uninstManagerApp()
}

//	----- PERIODIC CLOUD MX TASKS -----
def checkError() {
	if (state.currentError == null || state.currentError == "none") {
		log.info "${appLabel()} did not find any errors."
		if (state.currentError == "none") {
			state.currentError = null
		}
		return
	}
	def errMsg = state.currentError.msg
	log.info "Attempting to solve error: ${errMsg}"
	state.errorCount = state.errorCount +1
	if (errMsg == "Token expired" && state.errorCount < 6) {
		sendEvent (name: "ErrHandling", value: "Handle comms error attempt ${state.errorCount}")
		getDevices()
		if (state.currentError == null) {
			log.info "getDevices successful. apiServerUrl updated and token is good."
			return
		}
		log.error "${errMsg} error while attempting getDevices. Will attempt getToken"
		getToken()
		if (state.currentError == null) {
			log.info "getToken successful. Token has been updated."
			getDevices()
			return
		}
	} else {
		log.error "checkError: No auto-correctable errors or exceeded Token request count."
	}
	log.error "checkError residual: ${state.currentError}"
}

//	----- CHILD CALLED TASKS -----
def removeChildDevice(alias, deviceNetworkId) {
//	Need to update to update any static installedDevices database!
	try {
		deleteChildDevice(it.deviceNetworkId)
		sendEvent(name: "DeviceDelete", value: "${alias} deleted")
	} catch (Exception e) {
		sendEvent(name: "DeviceDelete", value: "Failed to delete ${alias}")
	}
}

//	----- DEVELOPER PAGES -----
def developerPage() {
	getDevices()
	cleanStorage()
	checkForUpdates()
	def devices = state.devices
	def newDevices = [:]
	def oldDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceMac)
		if (isChild) {
			oldDevices["${it.value.deviceMac}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
		if (!isChild) {
			newDevices["${it.value.deviceMac}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	def hub = location.hubs[0]
	def hubId = hub.id
	def strLatestSmartAppVersion = textSmartAppVersion()
	def strLatestDriverVersion = textDriverVersion()
	def strLoadedDriverVersion = "Tunable White Bulb: ${atomicState?.devTWBVer}, Soft White Bulb: ${atomicState?.devSWBVer}, Color Bulb: ${atomicState?.devCBVer}, Plug: ${atomicState?.devPGVer}, Energy Monitor Plug: ${atomicState?.devEMPGVer}, Switch: ${atomicState?.devSHVer}, Dimming Switch: ${atomicState?.devDSHVer}"
	return dynamicPage (name: "developerPage", title: "Developer Page", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Application Information: ", hideable: true, hidden: true) {
			paragraph title: "TP-Link Token: ", "${state.TpLinkToken}", image: getAppImg("token.png")
			paragraph title: "Hub: ", "${hub}", image: getAppImg("samsunghub.png")
			paragraph title: "Hub ID: ", "${hubId}", image: getAppImg("samsunghub.png")
			paragraph title: "Latest Smart Application Version: ", "${strLatestSmartAppVersion}", image: getAppImg("kasa.png")
			paragraph title: "Latest Device Handlers Version: ", "${strLatestDriverVersion}", image: getAppImg("devices.png")
			paragraph title: "Current Smart Application Version: ", "${appVersion()}", image: getAppImg("kasa.png")
			paragraph title: "Current Device Handlers Version: ", "${strLoadedDriverVersion}", image: getAppImg("devices.png")
			paragraph title: "Beta Build: ", "${betaMarker()}", image: getAppImg("beta.png")
			paragraph title: "GitHub Namespace: ", "${appNamespace()}", image: getAppImg("github.png")
			paragraph title: "Device Handlers Namespace: ", "${driverNamespace()}", image: getAppImg("devices.png")
			paragraph title: "Username: ", "${userName}", image: getAppImg("email.png")
			paragraph title: "Password: ", "${userPassword}", image: getAppImg("password.png")
			paragraph title: "Managed Devices: ", "${oldDevices}", image: getAppImg("devices.png")
			paragraph title: "New Devices: ", "${newDevices}", image: getAppImg("devices.png")
		}
		section("Page Selector: ") {
			if (userSelectedTestingPage) {
				href "startPage", title: "Initialization Page", description: "This page is not viewable", image: getAppImg("computerpages.png")
			}
			href "kasaWelcomePage", title: "Cloud Controller Introduction Page", description: "Tap to view", image: getAppImg("welcomepage.png")
			href "hubWelcomePage", title: "Hub Controller Introduction Page", description: "Tap to view", image: getAppImg("welcomepage.png")
			href "kasaUserSelectionAuthenticationPage", title: "Login Page", description: "Tap to view", image: getAppImg("userselectionauthenticationpage.png")
			href "hubUserAuthenticationPreferencesPage", title: "Login Settings Page", description: "Tap to view", image: getAppImg("userauthenticationpreferencespage.png")
			if (userSelectedTestingPage) {
				href "kasaComputerSelectionAuthenticationPage", title: "Computer Login Page", description: "This page is not viewable", image: getAppImg("computerpages.png")
			}
			href "kasaUserSelectionPage", title: "Launcher Page", description: "Tap to view", image: getAppImg("userselectionpage.png")
			if (userSelectedTestingPage) {
				href "kasaComputerSelectionPage", title: "Computer Launcher Page", description: "This page is not viewable", image: getAppImg("computerpages.png")
			}
			href "kasaAddDevicesPage", title: "Cloud Device Installer Page", description: "Tap to view", image: getAppImg("adddevicespage.png")
			href "hubAddDevicesPage", title: "Hub Device Installer Page", description: "Tap to view", image: getAppImg("adddevicespage.png")
			href "kasaRemoveDevicesPage", title: "Cloud Device Uninstaller Page", description: "Tap to view", image: getAppImg("removedevicespage.png")
			href "hubRemoveDevicesPage", title: "Hub Device Uninstaller Page", description: "Tap to view", image: getAppImg("removedevicespage.png")
			href "kasaUserApplicationPreferencesPage", title: "Cloud Application Settings Page", description: "Tap to view", image: getAppImg("userapplicationpreferencespage.png")
			href "hubUserApplicationPreferencesPage", title: "Hub Application Settings Page", description: "Tap to view", image: getAppImg("userapplicationpreferencespage.png")
			href "kasaUserDevicePreferencesPage", title: "Cloud Device Preferences Page", description: "Tap to view", image: getAppImg("userdevicepreferencespage.png")
			href "hubUserDevicePreferencesPage", title: "Hub Device Preferences Page", description: "Tap to view", image: getAppImg("userdevicepreferencespage.png")
			href "kasaUserSelectionTokenPage", title: "Token Manager Page", description: "Tap to view", image: getAppImg("userselectiontokenpage.png")
			if (userSelectedTestingPage) {
				href "developerPage", title: "Developer Page", description: "You are currently on this page", image: getAppImg("developerpage.png")
				href "developerTestingPage", title: "Developer Testing Page", description: "Tap to view", image: getAppImg("testingpage.png")
			}
			if ("${restrictedRecordPasswordPrompt}" =~ "Mac5089") {
				href "hiddenPage", title: "xKiller Clan Page", description: "Tap to view", image: getAppImg("xkillerclanpage.png")
			}
			href "aboutPage", title: "About Page", description: "Tap to view", image: getAppImg("aboutpage.png")
			href "changeLogPage", title: "Changelog Page", description: "Tap to view", image: getAppImg("changelogpage.png")
			href "uninstallPage", title: "Uninstall Page", description: "Tap to view", image: getAppImg("uninstallpage.png")
		}
		section("${textCopyright()}")
	}
}

def developerTestingPage() {
	getDevices()
	def devices = state.devices
	def newDevices = [:]
	def oldDevices = [:]
	def errorMsgCom = "None"
	def errorMsgDev = "None"
	def errorMsgNew = "None"
	def errorMsgOld = "None"
	def errorMsgTok = "None"
		if (state.TpLinkToken == null) {
			errorMsgTok = "You will be unable to control your devices until you get a new token."
		}
	devices.each {
		def isChild = getChildDevice(it.value.deviceMac)
		if (isChild) {
			oldDevices["${it.value.deviceMac}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
		if (!isChild) {
			newDevices["${it.value.deviceMac}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	if (state.currentError != null) {
		errorMsgCom = "Error communicating with cloud:\n" + "${state.currentError}" +
			"\nPlease resolve the error and try again."
	}
	if (devices == [:]) {
		errorMsgDev = "We were unable to find any TP-Link Kasa devices on your account. This usually means "+
		"that all devices are in 'Local Control Only'. Correct them then " +
		"rerun the application."
	}
	if (newDevices == [:]) {
		errorMsgNew = "No new devices to add. Are you sure they are in Remote " +
		"Control Mode?"
	}
	if (oldDevices == [:]) {
		errorMsgOld = "No current devices to remove from SmartThings."
	}
	return dynamicPage (name: "developerTestingPage", title: "Developer Testing Page", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Application Information: ", hideable: true, hidden: true) {
			paragraph title: "Communication Error: ", errorMsgCom, image: getAppImg("error.png")
			paragraph title: "Finding Devices Error: ", errorMsgDev, image: getAppImg("error.png")
			paragraph title: "New Devices Error: ", errorMsgNew, image: getAppImg("error.png")
			paragraph title: "Current Devices Error: ", errorMsgOld, image: getAppImg("error.png")
			paragraph title: "Account Error: ", errorMsgTok, image: getAppImg("error.png")
			paragraph title: "Error Count: ", "${state.errorCount}", image: getAppImg("error.png")
			paragraph title: "Current Error: ", "${state.currentError}", image: getAppImg("error.png")
			paragraph title: "Error Messages: ", "${errMsg}", image: getAppImg("error.png")
		}
		section("Information and Diagnostics: ") {
			paragraph tokenInfoOnline(), image: getAppImg("tokenactive.png")
			paragraph tokenInfoOffline(), image: getAppImg("error.png")
		}
		section("Page Selector: ") {
			paragraph pageSelectorErrorText(), image: getAppImg("error.png")
			paragraph sendingCommandSuccess(), image: getAppImg("sent.png")
			paragraph sendingCommandFailed(), image: getAppImg("issue.png")
			paragraph pageSelectorText(), image: getAppImg("pageselected.png")
			paragraph pageSelectorNullText(), image: getAppImg("pickapage.png")
		}
		section("Account Configuration: ") {
			input ("userName", "email", title: "TP-Link Kasa Email Address", required: true, submitOnChange: false,image: getAppImg("email.png"))
			input ("userPassword", "password", title: "TP-Link Kasa Account Password", required: true, submitOnChange: false, image: getAppImg("password.png"))
		}
		section("User Configuration: ") {
			input ("userSelectedOptionTwo", "enum", title: "What do you want to do?", required: true, multiple: false, submitOnChange: true, metadata: [values:["Update Account", "Activate Account", "Delete Account"]], image: getAppImg("userinput.png"))
			input ("userSelectedOptionThree", "enum", title: "What do you want to do?", required: true, multiple: false, submitOnChange: true, metadata: [values:["Update Token", "Recheck Token", "Delete Token"]], image: getAppImg("token.png"))
		}
		section("Device Controller: ") {
			input ("userSelectedDevicesAddKasa", "enum", required: true, multiple: true, submitOnChange: false, title: "Select Devices (${newDevices.size() ?: 0} found)", metadata: [values:newDevices], image: getAppImg("adddevices.png"))
			input ("userSelectedDevicesRemoveKasa", "enum", required: true, multiple: true, submitOnChange: false, title: "Select Devices (${oldDevices.size() ?: 0} found)", metadata: [values:oldDevices], image: getAppImg("removedevices.png"))
		}
		section("Application Configuration: ") {
			input ("userSelectedNotification", "bool", title: "Do you want to enable notification?", submitOnChange: false, image: getAppImg("notification.png"))
			input ("userSelectedAppIcons", "bool", title: "Do you want to disable application icons?", submitOnChange: false, image: getAppImg("noicon.png"))
			input ("userSelectedManagerMode", "bool", title: "Do you want to switch to hub controller mode?", submitOnChange: false, image: getAppImg("samsunghub.png"))
			input ("userSelectedBrowserMode", "bool", title: "Do you want to open all external links within the SmartThings app?", submitOnChange: false, image: getAppImg("browsermode.png"))
			input ("userSelectedReload", "bool", title: "Do you want to refresh your current state?", submitOnChange: true, image: getAppImg("sync.png"))
			input ("userSelectedDeveloper", "bool", title: "Do you want to enable developer mode?", submitOnChange: true, image: getAppImg("developer.png"))
			input ("userSelectedTestingPage", "bool", title: "Do you want to enable developer testing mode?", submitOnChange: true, image: getAppImg("developertesting.png"))
		}
		section("Device Configuration: ") {
			input ("userSelectedDevicesToUpdateKasa", "enum", required: true, multiple: true, submitOnChange: false, title: "Select Devices to Update (${oldDevices.size() ?: 0} found)", metadata: [values: oldDevices], image: getAppImg("devices.png"))
			input ("userLightTransTime", "enum", required: true, multiple: false, submitOnChange: false, title: "Lighting Transition Time", metadata: [values:["500" : "0.5 second", "1000" : "1 second", "1500" : "1.5 second", "2000" : "2 seconds", "2500" : "2.5 seconds", "5000" : "5 seconds", "10000" : "10 seconds", "20000" : "20 seconds", "40000" : "40 seconds", "60000" : "60 seconds"]], image: getAppImg("transition.png"))
			input ("userRefreshRate", "enum", required: true, multiple: false, submitOnChange: false, title: "Device Refresh Rate", metadata: [values:["1" : "Refresh every minute", "5" : "Refresh every 5 minutes", "10" : "Refresh every 10 minutes", "15" : "Refresh every 15 minutes", "30" : "Refresh every 30 minutes"]], image: getAppImg("refresh.png"))
		}
		section("${textCopyright()}")
	}
}

def hiddenPage() {
	def xkMembersInfo = "Although most of these members have left here is a complete list of all the members we had"
	def xkMembers = "xKllerBOSSXXX, xKillerDDigital, xKillerIntense, xKillerMaverick, xKillerKittyKat, xKillerPP, xKillerBrute, xKillerBSOD, xKillerFoxy, xKillerTricky, xKillerReaper, xKillerPain, xKillerRobot, xKillerSasha, XKillerAwesomer, xKillerSonic, xKillerChakra, xKillerDoobage, xKillerSeki, xKillerEvo, xKillerSubXero, xKillerCali, xKillerAsh, xKillerTruKillah,xKillerSierra, Weirdowack"
	def xkGameInfo = "Although we may not play most of these games anymore but as a bunch of friends and some family had fun along the way but i guess some things just don't last"
	dynamicPage (name: "hiddenPage", title: "xKiller Clan Page", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Members: ") {
			paragraph xkMembersInfo, image: getAppImg("xkillerclanv2.png")
			paragraph xkMembers, image: getAppImg("family.png")
		}
		section("Games: ") {
			paragraph xkGameInfo, image: getAppImg("xkillerclanv1.png")
			paragraph "Halo 2 For Windows Vista - RIP late 2015", image: getAppImg("halo2.png")
			paragraph "Battlefield 3", image: getAppImg("battlefield3.png")
			paragraph "Garrys Mod", image: getAppImg("garrysmod.png")
			paragraph "Portal 2", image: getAppImg("portal2.png")
			paragraph "Dead Speace 3", image: getAppImg("deadspace3.png")
			paragraph "Clash of Clans - Clan Tag: #YYCLJ2YR", image: getAppImg("clashofclans.png")
			paragraph "Halo: The Master Chief Collection", image: getAppImg("halomcc.png")
			paragraph "Clash Royale - Clan Tag: #209G8L9", image: getAppImg("clashroyale.png")
			paragraph "Saints Row 3", image: getAppImg("saintsrow3.png")
			paragraph "Boom Beach - Clan Tag: #92V92QCC", image: getAppImg("boombeach.png")
			paragraph "Call of Duty Black Ops 2", image: getAppImg("callofdutyblackops2.png")
			paragraph "Halo 5 Guardians", image: getAppImg("halo5.png")
			paragraph "Vainglory - Guild: XKILLER, Team: xKiller Clan", image: getAppImg("vainglory.png")
			paragraph "Minecraft Bedrock Edition - Realm: 0EOy4uYzhxQ", image: getAppImg("minecraft.png")
		}
		section("Easter Eggs: ") {
			href url: linkYoutubeEE1(), style: "${strBrowserMode()}", required: false, title: "Youtube Link #1", description: "Tap to open in browser", state: "complete", image: getAppImg("youtube.png")
			href url: linkYoutubeEE2(), style: "${strBrowserMode()}", required: false, title: "Youtube Link #2", description: "Tap to open in browser", state: "complete", image: getAppImg("youtube.png")
			href url: linkYoutubeEE3(), style: "${strBrowserMode()}", required: false, title: "Youtube Link #3", description: "Tap to open in browser", state: "complete", image: getAppImg("youtube.png")
		}
		section("Contact: ") {
			href url: linkDiscord(), style: "${strBrowserMode()}", required: false, title: "Discord", description: "Tap to open in browser", state: "complete", image: getAppImg("discord.png")
			href url: linkWaypoint(), style: "${strBrowserMode()}", required: false, title: "Halo Waypoint", description: "Tap to open in browser", state: "complete", image: getAppImg("waypoint.png")
			href url: linkXbox(), style: "${strBrowserMode()}", required: false, title: "Xbox", description: "Tap to open in browser", state: "complete", image: getAppImg("xbox.png")
			href url: linkSteam(), style: "${strBrowserMode()}", required: false, title: "Steam", description: "Tap to open in browser", state: "complete", image: getAppImg("steam.png")
			href url: linkFacebook(), style: "${strBrowserMode()}", required: false, title: "Facebook", description: "Tap to open in browser", state: "complete", image: getAppImg("facebook.png")
		}
		section("${textCopyright()}")
	}
}

//	======== Other Application Values ==========================================================================================================================================================================
def getWebData(params, desc, text=true) {
	try {
		log.info "getWebData: ${desc} data"
		httpGet(params) { resp ->
			if(resp.data) {
				if(text) {
					return resp?.data?.text.toString()
				} else { return resp?.data }
			}
		}
	}
	catch (ex) {
		if(ex instanceof groovyx.net.http.HttpResponseException) {
			log.warn "${desc} file not found"
		} else {
			log.error "getWebData(params: $params, desc: $desc, text: $text) Exception: ", ex
		}
		return "${label} info not found"
	}
}

def appInfoDesc()	{
	def str = ""
	str += "\n" + "• ${textVersion()}"
	str += "\n" + "• ${textModified()}"
	str += "\n" + "• Driver Version: ${textDriverVersion()}"
	return str
}

	def gitBranch()	{ return betaMarker() ? "beta" : "master" }
	def getAppImg(imgName, on = null)	{ return (!userSelectedAppIcons || on) ? "https://raw.githubusercontent.com/${gitPath()}/images/$imgName" : "" }
	def getWikiPageUrl()	{ return "https://github.com/${gitRepo()}/wiki" }
	def getIssuePageUrl()	{ return "https://github.com/${gitRepo()}/issues" }
	def strBrowserMode()	{ return (userSelectedBrowserMode) ? "embedded" : "external" }
	def driverNamespace()	{ return (userSelectedDriverNamespace) ? "DaveGut" : "ramiran2" }
	def gitRepo()		{ return "${appNamespace()}/TP-Link-SmartThings" }
	def gitPath()		{ return "${gitRepo()}/${gitBranch()}"}
	def betaMarker()	{ return false }
	def sendingCommandSuccess()	{ return "Command Sent to SmartThings Application" }
	def sendingCommandFailed()	{ return "Ready to Send Command to SmartThings Application" }
	def tokenInfoOnline()	{ return "Online and Ready to Control Devices" }
	def tokenInfoOffline()	{ return "Offline, Please Fix to Restore Control on Devices" }
	def pageSelectorText()	{ return "Please tap below to continue" }
	def pageSelectorNullText()	{ return "Please select a option to continue" }
	def pageSelectorErrorText()	{ return "Please continue with caution, we have detected a error" }
	def textVersion()	{ return "Version: ${appVersion()}" }
	def textModified()	{ return "Updated: ${appVerDate()}" }
	def appVerInfo()	{ return getWebData([uri: "https://raw.githubusercontent.com/${gitPath()}/data/changelog.txt", contentType: "text/plain; charset=UTF-8"], "changelog") }
	def textLicense()	{ return getWebData([uri: "https://raw.githubusercontent.com/${gitPath()}/data/license.txt", contentType: "text/plain; charset=UTF-8"], "license") }
	def textSmartAppVersion()	{ return getWebData([uri: "https://raw.githubusercontent.com/${gitPath()}/data/appversion.txt", contentType: "text/plain; charset=UTF-8"], "appversion") }
	def textDriverVersion()	{ return getWebData([uri: "https://raw.githubusercontent.com/${gitPath()}/data/driverversion.txt", contentType: "text/plain; charset=UTF-8"], "driverversion") }
	def textDonateLinkAntR()	{ return "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=S2CJBWCJEGVJA" }
	def linkGitHubDavG()	{ return "https://github.com/DaveGut/SmartThings_Cloud-Based_TP-Link-Plugs-Switches-Bulbs" }
	def linkGitHubAntR()	{ return "https://github.com/ramiran2/TP-Link-SmartThings" }
	def linkGitHubAntS()	{ return "https://github.com/tonesto7/nest-manager" }
	def linkYoutubeEE1()	{ return "https://www.youtube.com/watch?v=87JPlNk5ves&list=PL0S-Da7zGmE9PRn_YIitvUZEHYQglJw" }
	def linkYoutubeEE2()	{ return "https://www.youtube.com/watch?v=0eYTZrucx_o" }
	def linkYoutubeEE3()	{ return "https://www.youtube.com/watch?v=4_5kpOeiZyg&index=3&list=PL0S-Da7zGmE-i5MQdHORm6a" }
	def linkDiscord()	{ return "https://discord.gg/JDXeV23" }
	def linkXbox()	{ return "https://account.xbox.com/en-us/clubs/profile?clubid=3379843591790358" }
	def linkWaypoint()	{ return "https://www.halowaypoint.com/en-us/spartan-companies/xkiller%20clan" }
	def linkSteam()	{ return "https://steamcommunity.com/groups/xKillerClan" }
	def linkFacebook()	{ return "https://www.facebook.com/groups/xKillerClan/" }
	def textCopyright()	{ return "Copyright© 2018 - Dave Gutheinz, Anthony Ramirez" }
	def textDesc()	{ return "A Service Manager for the TP-Link Kasa Devices connecting through the TP-Link Servers to SmartThings." }
//end-of-file