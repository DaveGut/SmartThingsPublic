/*
TP-Link SmartThings Manager, 2018 Version 3

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
primarily various users on GitHub.com.
*/

definition(
	name: "${appLabel()}",
	namespace: "${appNamespace()}",
	author: "${appAuthor()}",
	description: "${textDesc()}",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	singleInstance: true
)

def appVersion() { return "3.9.0" }
def appVerDate() { return "10-17-2018" }
def currentDriverVersion() { return "3.2.0" }

preferences {
	page(name: "welcomePage")
	page(name: "userSelectionAuthenticationPage")
	page(name: "userAuthenticationPreferencesPage")
	page(name: "computerSelectionAuthenticationPage")
	page(name: "userSelectionPage")
	page(name: "computerSelectionPage")
	page(name: "addDevicesPage")
	page(name: "removeDevicesPage")
	page(name: "userApplicationPreferencesPage")
	page(name: "userDevicePreferencesPage")
	page(name: "userSelectionTokenPage")
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
	settingRemove("userSelectedDevicesRemove")
	settingRemove("userSelectedDevicesAdd")
	settingRemove("userSelectedOptionThree")
	if ("${userName}" =~ null || "${userPassword}" =~ null) {
		settingRemove("userName")
		settingRemove("userPassword")
	}
	if (userSelectedOptionTwo =~ "Delete Account") {
		settingRemove("userSelectedOptionTwo")
	}
	if (userSelectedOptionThree =~ "Delete Token") {
		settingRemove("userSelectedOptionThree")
	}
	if (!userSelectedDeveloper) {
		if ("${userName}" =~ null || "${userPassword}" =~ null) {
			state.TpLinkToken = null
			state.currentError = null
			state.errorCount = 0
		} else {
		}
		settingUpdate("userSelectedReload", "false", "bool")
	}
}

//	Remove userSelectedAssistant.  Rationale:  Confusing to user.

//	----- WELCOME PAGE -----
//	Limit available options on device error to Initial Login and Update Token.
//	Limit available options on no token to INitial Login.
//	Provide info in instructions on checking Kasa app for remote control and
//	ability to control devices.
//	Provide link to web server documentation folder in Instructions section.
def welcomePage() {
	setInitialStates()
	def driverVersionText = "Minimum Device Driver Version is ${currentDriverVersion()}."
    def noteText = "You can expand or collaspe the Sections of pages using the + and - symbols."
	def instructionsText1 = "Verify that you have installed this service manager onto your location " +
    						"in the tab My SmartApps. Also Verify that you have installed the " +
                            "required device handlers in the tab My Devices.  Handler information " +
                            "for devices are in the instructions."
    def instructionsText2 = "Brings up page with the available Kasa Cloud installation related options: " +
                            "Initial Installation, Install Devices, Remove Devices, Set Device " +
                            "Preferences, UpdateToken."
    def instructionsText3 = "For advanced users, brings up options for this application."
    def instructionsText4 = "CAUTION!!! Uninstalls all Devices and the Application."
	return dynamicPage (name: "welcomePage", 
    	title: "Welcome Page", 
        install: false, 
        uninstall: false) {
		section() {
			paragraph appInfoDesc(), image: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png"
   			if (state.TpLinkToken == null) {
				paragraph tokenInfoOffline(), image: "https://s3.amazonaws.com/smartapp-icons/Solution/dangers-active@2x.png"
            } else if (state.currentError != null) {
				paragraph kasaErrorMessage(), image: "https://s3.amazonaws.com/smartapp-icons/Solution/dangers-active@2x.png"
 			} else {
				paragraph tokenInfoOnline(), image: "https://s3.amazonaws.com/smartapp-icons/Connections/Cat-Connections@2x.png"
			}
		}
//	Remove Information and Diagnostics page.  Info added above to permanently view.        

		section("Instructions", hideable: true, hidden: true) {
        	paragraph noteText
        	paragraph title: "Verify Files Installed", instructionsText1
        	paragraph title: "Kasa Cloud Actions", instructionsText2
        	paragraph title: "Application Settings", instructionsText3
        	paragraph title: "Uninstall Application and All Devices", instructionsText4
        }
//	Remove userSelectedLauncher as a option.  Rationale:
		section("Kasa Cloud Actions", hideable: true, hidden: false) {
			if (state.currentError != null) {
				paragraph kasaErrorMessage(), image: "https://s3.amazonaws.com/smartapp-icons/Solution/dangers-active@2x.png"
			} else {
				href "userSelectionPage", 
                	title: "Kasa Options", 
                    description: "Tap Here to Kasa Cloud Action", 
            		image: "https://s3.amazonaws.com/smartapp-icons/Internal/network-scanner.png"
			}
		}
//	Remove userSelectedQuickControl
		section("Application Settings", hideable: true, hidden: true) {
			href "userApplicationPreferencesPage", 
            	title: "Go to Application Settings", 
            	description: "Tap to set Application preferences",
                image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
		}
		section("Uninstall Application and All Devices", hideable: true, hidden: true) {
			href "uninstallPage",
            	title: "Go to Uninstall Page", 
                description: "Tap to go to Unstall Page", 
                image: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-NotifyWhenNotHere@2x.png"
		}
		if (userSelectedDeveloper) {
			section("Developer") {
				href "developerPage", 
                	title: "Developer Page", 
                    description: "Tap to view", 
                    image: getAppImg("developerpage.png")
			}
		}
		section("Help and Feedback") {
			href url: getWikiPageUrl(), 
            	style: "embedded", 
                title: "View the Projects Wiki", 
                description: "Tap to open in browser", 
                state: "complete", 
                image: getAppImg("help.png")
			href url: getIssuePageUrl(), 
            	style: "embedded", 
                title: "Report | View Issues", 
                description: "Tap to open in browser", 
                state: "complete", 
                image: getAppImg("issue.png")
		}
		section("About and Changelog") {
			href "aboutPage", title: "About Page", description: "Tap to view", image: getAppImg("aboutpage.png")
			href "changeLogPage", title: "Changelog Page", description: "Tap to view", image: getAppImg("changelogpage.png")
		}
		section("${textCopyright()}")
	}
}

//	----- USER SELECTION PAGES -----
def userSelectionPage() {
	def instructionsText1 = "Select the action that you need to accomplish."
    def instructionsText2 = "Link to your Kasa Cloud account by logging in. You will " +
                            "be able to enter a new Username and Password."
    def instructionsText3 = "Install Kasa devices on your network."
    def instructionsText4 = "Remove individual Kasa devices from SmartThings."
    def instructionsText5 = "Set preferences for individual Kasa Devices. Options are: "+
                            "\nLight Transition Time (bulbs only)" +
                            "\nRefresh Rate"
	def instructionsText6 = "Updates the Token using your current Username and Password"

	return dynamicPage (name: "userSelectionPage", title: "Launcher Page", nextPage: "computerSelectionPage", install: false, uninstall: false) {
		section() {
			paragraph appInfoDesc(), image: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png"
   			if (state.TpLinkToken == null) {
				paragraph tokenInfoOffline(), image: "https://s3.amazonaws.com/smartapp-icons/Solution/dangers-active@2x.png"
            } else if (state.currentError != null) {
				paragraph kasaErrorMessage(), image: "https://s3.amazonaws.com/smartapp-icons/Solution/dangers-active@2x.png"
 			} else {
				paragraph tokenInfoOnline(), image: "https://s3.amazonaws.com/smartapp-icons/Connections/Cat-Connections@2x.png"
			}
		}
        
//	Removed Information and Diagnostics (redundant data)

//	Update and move to Welcome.
		section("Instructions", hideable: true, hidden: true) {
        	paragraph title: "Verify Files Installed", instructionsText1
        	paragraph title: "Initial Installation", instructionsText2
        	paragraph title: "Install Devices", instructionsText3
        	paragraph title: "Remove Devices", instructionsText4
        	paragraph title: "Set Device Preferences", instructionsText5
        	paragraph title: "Update Token", instructionsText6
        }

//	Move to Welcome
		section() {
			input ("userSelectedOptionOne", "enum", title: "Select Kasa Cloud Action", required: true, multiple: false, 
                   submitOnChange: true, metadata: [values:["Initial Installation", "Install Devices", "Remove Devices", 
//                   "Set Device Peferences", "Update Token"]], image: getAppImg("userinput.png"))
                   "Set Device Peferences", "Update Token"]])
		}

//	Move to Welcome
		section() {
			paragraph pageSelectorNullText()
//	Added options and updated to a switch for ease of code reading.
			switch (userSelectedOptionOne) {
				case "Initial Installation":
					href "userSelectionAuthenticationPage", 
                         title: "Login Page",
                         description: "Tap to continue",
//                         image: getAppImg("userselectionauthenticationpage.png")
						 image: "https://s3.amazonaws.com/smartapp-icons/Meta/text@2x.png"
						break
				case "Install Devices":
					href "addDevicesPage",
                         title: "Device Installer Page", 
                         description: "Tap to continue", 
                         image: "https://s3.amazonaws.com/smartapp-icons/Convenience/smartlights@2x.png"
					break
				case "Remove Devices":
					href "removeDevicesPage", 
                    	 title: "Device Uninstaller Page",
                         description: "Tap to continue", 
                         image: getAppImg("removedevicespage.png")
					break
                case "Set Device Preferences":
					href "userDevicePreferencesPage", 
                    	 title: "Set Device Preferences",
                         description: "Tap to set device preferences", 
                         image: getAppImg("userdevicepreferencespage.png")
                    break
                case "Update Token":
					href "getToken", 
                    	 title: "Get a new Kasa Cloud Token",
                         description: "Tap to obtain new Token"
                    break
				default:
                    break
		    }
        }
        
		section("${textCopyright()}")
	}
}

def computerSelectionPage() {
//	Updated switch to match new options.
    switch (userSelectedOptionOne) {
		case "Initial Installation":
			return userSelectionAuthenticationPage()
			break
		case "Install Devices":
			return addDevicesPage()
			break
		case "Remove Devices":
			return removeDevicesPage()
			break
        case "Set Device Preferences":
           	return userDevicePreferencesPage()
            break
        case "Update Token":
         	return getToken()
            break
		default:
			return welcomePage()
            break
    }
}

//	----- USER SELECTION AUTHENTICATION PAGE -----
def userSelectionAuthenticationPage() {
	def instructionsText1 = "If possible, open the IDE to your location and select LiveLogging.  " +
    						"Then enter your Kasa Username (e-mail) and Password.  When done, " +
                            "select \n         Login to the Kasa Cloud \nAfter the Login, you " +
                            "will automatically be directed to the Device Installer Page."
	return dynamicPage (name: "userSelectionPage", title: "Launcher Page", nextPage: "computerSelectionPage", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png"
   			if (state.TpLinkToken == null) {
 				paragraph tokenInfoOffline(), image: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-NotifyWhenNotHere@2x.png"
            } else if (state.currentError != null) {
				paragraph kasaErrorMessage(), image: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-NotifyWhenNotHere@2x.png"
			} else {
				paragraph tokenInfoOnline()
			}
		}
        
//	Removed Information and Diagnostics (redundant data)
//	Removed options (they are subsumed in the userSelectionPage).
		section("Instructions", hideable: true, hidden: true) {paragraph instructionsText1}
        
		section("Account Configuration:") {
			input ("userName", "email", title: "Kasa Email Address", required: true, image: getAppImg("email.png"))
			input ("userPassword", "password", title: "Kasa Account Password", required: true, image: getAppImg("password.png"))
		}
        
		section() {
			getToken()
			href "addDevicesPage", title: "Login and Install Devices", description: "Login to the Kasa Cloud", image: getAppImg("adddevicespage.png")
		}
        
		section("${textCopyright()}")
	}
}

//	Removed userAuthenticationPreferences

//	----- ADD DEVICES PAGE -----
def addDevicesPage() {
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
	def addDevicesPageText = "Devices that have not been previously installed and are not in 'Local " +
		"WiFi control only' will appear below. Tap below to see the list of " +
		"TP-Link Kasa Devices available select the ones you want to connect to " +
		"SmartThings.\n\r" + "Press Done when you have selected the devices you " +
		"wish to add, then press Save to add the devices to your SmartThings account."
	return dynamicPage (name: "addDevicesPage", title: "Device Installer Page", install: true, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Information and Diagnostics:", hideable: true, hidden: true) {
			if (state.TpLinkToken != null) {
				paragraph tokenInfoOnline(), image: getAppImg("tokenactive.png")
			} else {
				paragraph tokenInfoOffline(), image: getAppImg("error.png")
			}
			paragraph title: "Information:", addDevicesPageText, image: getAppImg("information.png")
			paragraph title: "Device Error:", errorMsgDev, image: getAppImg("error.png")
		}
		section("Device Controller:") {
			input ("userSelectedDevicesAdd", "enum", required: true, multiple: true, submitOnChange: true, title: "Select Devices to Add (${newDevices.size() ?: 0} found)", metadata: [values:newDevices], image: getAppImg("adddevices.png"))
		}
		section("${textCopyright()}")
	}
}

//	----- REMOVE DEVICES PAGE -----
def removeDevicesPage() {
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
	def removeDevicesPageText = "Devices that have been installed " +
		"will appear below. Tap below to see the list of " +
		"TP-Link Kasa Devices available select the ones you want to connect to " +
		"SmartThings.\n\r" + "Press Done when you have selected the devices you " +
		"wish to remove, then Press Save to remove the devices to your SmartThings account."
	return dynamicPage (name: "removeDevicesPage", title: "Device Uninstaller Page", install: true, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Information and Diagnostics:", hideable: true, hidden: true) {
			if (state.TpLinkToken != null) {
				paragraph tokenInfoOnline(), image: getAppImg("tokenactive.png")
			} else {
				paragraph tokenInfoOffline(), image: getAppImg("error.png")
			}
			paragraph title: "Information:", removeDevicesPageText, image: getAppImg("information.png")
			paragraph title: "Device Error:", errorMsgDev, image: getAppImg("error.png")
		}
		section("Device Controller:") {
			input ("userSelectedDevicesRemove", "enum", required: true, multiple: true, submitOnChange: true, title: "Select Devices to Remove (${oldDevices.size() ?: 0} found)", metadata: [values:oldDevices], image: getAppImg("removedevices.png"))
		}
		section("${textCopyright()}")
	}
}

//	----- USER DEVICE PREFERENCES PAGE -----
def userDevicePreferencesPage() {
	getDevices()
	def devices = state.devices
	def oldDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceMac)
		if (isChild) {
			oldDevices["${it.value.deviceMac}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	def userDevicePreferencesPageText = "Welcome to the Device Preferences page. \n\r" +
		"Enter a value for Transition Time and Refresh Rate then select the devices that you want to update. \n\r" +
		"After that you may procide to save by clicking the save button."
	return dynamicPage (name: "userDevicePreferencesPage", title: "Device Preferences Page", install: true, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Information and Diagnostics:", hideable: true, hidden: true) {
			if (state.TpLinkToken != null) {
				paragraph tokenInfoOnline()
			} else {
				paragraph tokenInfoOffline(), image: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-NotifyWhenNotHere@2x.png"
			}
			paragraph title: "Information:", userDevicePreferencesPageText, image: getAppImg("information.png")
		}
		section("Device Configuration:") {
			input ("userSelectedDevicesUpdate", "enum", required: true, multiple: true, submitOnChange: true, title: "Select Devices to Update (${oldDevices.size() ?: 0} found)", metadata: [values: oldDevices], image: getAppImg("devices.png"))
			input ("userLightTransTime", "enum", required: true, multiple: false, submitOnChange: true, title: "Lighting Transition Time", metadata: [values:["500": "1/2 second", "1000": "1 second", "2000": "2 seconds", "5000": "5 seconds", "10000": "10 seconds"]], image: getAppImg("transition.png"))
			input ("userRefreshRate", "enum", required: true, multiple: false, submitOnChange: true, title: "Device Refresh Rate", metadata: [values:["1" : "Refresh every minute (Not Recommended)", "5" : "Refresh every 5 minutes", "10" : "Refresh every 10 minutes", "15" : "Refresh every 15 minutes", "30" : "Refresh every 30 minutes (Recommended)"]], image: getAppImg("refresh.png"))
		}
		section("${textCopyright()}")
	}
}

def updatePreferences() {
	userSelectedDevicesUpdate.each {
		def child = getChildDevice(it)
		child.setLightTransTime(userLightTransTime)
		child.setRefreshRate(userRefreshRate)
		log.info "Kasa device ${child} preferences updated"
		if (userSelectedNotification) {
			sendPush("Successfully updated TP-Link $deviceModel with alias ${device.value.alias}")
		}
	}
}

//	----- USER APPLICATION PREFERENCES PAGE -----
def userApplicationPreferencesPage() {
	def hiddenRecordInput = 0
	def hiddenDeveloperInput = 0
	if (userSelectedDeveloper) {
		hiddenDeveloperInput = 0
	} else {
		hiddenDeveloperInput = 1
	}
	if ("${restrictedRecordPasswordPrompt}" =~ null) {
		hiddenRecordInput = 0
	} else {
		hiddenRecordInput = 1
	}
	def userApplicationPreferencesPageText = "Welcome to the application settings page. \n\r" +
		"Recommended options: Will allow your device to pick a option for you that you are likely to pick."
	return dynamicPage (name: "userApplicationPreferencesPage", title: "Application Settings Page", install: true, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Information and Diagnostics:", hideable: true, hidden: true) {
			if (state.TpLinkToken != null) {
				paragraph tokenInfoOnline(), image: getAppImg("tokenactive.png")
			} else {
				paragraph tokenInfoOffline(), image: getAppImg("error.png")
			}
			paragraph title: "Information:", userApplicationPreferencesPageText, image: getAppImg("information.png")
		}
		section("Application Configuration:") {
			input ("userSelectedNotification", "bool", title: "Do you want to enable notification?", submitOnChange: true, image: getAppImg("notification.png"))
			input ("userSelectedAppIcons", "bool", title: "Do you want to disable application icons?", submitOnChange: true, image: getAppImg("noicon.png"))
//			input ("userSelectedAssistant", "bool", title: "Do you want to enable recommended options?", submitOnChange: true, image: getAppImg("ease.png"))
			input ("userSelectedReload", "bool", title: "Do you want to refresh your current state?", submitOnChange: true, image: getAppImg("sync.png"))
//			if (userSelectedAppIcons && userSelectedAssistant && userSelectedReload || hiddenDeveloperInput == 1) {
			if (userSelectedAppIcons && userSelectedReload || hiddenDeveloperInput == 1) {
				hiddenDeveloperInput = 1
				input ("userSelectedDeveloper", "bool", title: "Do you want to enable developer mode?", submitOnChange: true, image: getAppImg("developer.png"))
			}
			if (userSelectedDeveloper) {
//				input ("userSelectedLauncher", "bool", title: "Do you want to enable the launcher page?", submitOnChange: true, image: getAppImg("launcher.png"))
//				input ("userSelectedQuickControl", "bool", title: "Do you want to enable post install features?", submitOnChange: true, image: getAppImg("quickcontrol.png"))
				input ("devTestingLoaded", "bool", title: "Do you want to enable developer testing mode?", submitOnChange: true, image: getAppImg("developertesting.png"))
			}
			if (devTestingLoaded && userSelectedReload || hiddenRecordInput == 1) {
				hiddenRecordInput = 1
				input ("restrictedRecordPasswordPrompt", type: "password", title: "This is a restricted record, Please input your password", description: "Hint: xKillerMaverick", required: false, submitOnChange: true, image: getAppImg("passwordverification.png"))
			}
			if (userSelectedReload && state.TpLinkToken != null) {
				checkError()
				setInitialStates()
			} else {
				settingUpdate("userSelectedReload", "false", "bool")
			}
//			if (userSelectedAssistant && state.TpLinkToken != null) {
//				setRecommendedOptions()
//			} else {
//				settingUpdate("userSelectedAssistant", "false", "bool")
//			}
		}
		section("${textCopyright()}")
	}
}

//	Remove Token Manager Page.  Token can be removed by uninstalling Service Manager.
//	Token can be updated by Update Token command.
//	Username and Password can be uninstalling Service Manager and reinstalling.
//	The token is automatically checked on any device command to cloud (and the state updated).
//	Error message is provided in user interface.

//	----- DEVELOPER PAGE -----
def developerPage() {
	getDevices()
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
	return dynamicPage (name: "developerPage", title: "Developer Page", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Application Information:", hideable: true, hidden: true) {
			paragraph title: "TP-Link Token:", "${state.TpLinkToken}", image: getAppImg("token.png")
			paragraph title: "Hub:", "${hub}", image: getAppImg("samsunghub.png")
			paragraph title: "Hub ID:", "${hubId}", image: getAppImg("samsunghub.png")
			paragraph title: "GitHub Namespace:", "${appNamespace()}", image: getAppImg("github.png")
			paragraph title: "Username:", "${userName}", image: getAppImg("email.png")
			paragraph title: "Password:", "${userPassword}", image: getAppImg("password.png")
			paragraph title: "Managed Devices:", "${oldDevices}", image: getAppImg("devices.png")
			paragraph title: "New Devices:", "${newDevices}", image: getAppImg("devices.png")
		}
		section("Page Selector:") {
			href "welcomePage", title: "Welcome Page", description: "Tap to view", image: getAppImg("welcomepage.png")
			href "userSelectionAuthenticationPage", title: "Login Page", description: "Tap to view", image: getAppImg("userselectionauthenticationpage.png")
			href "userAuthenticationPreferencesPage", title: "Login Settings Page", description: "Tap to view", image: getAppImg("userauthenticationpreferencespage.png")
			if (devTestingLoaded) {
				href "computerSelectionAuthenticationPage", title: "Computer Login Page", description: "This page is not viewable", image: getAppImg("computerpages.png")
			}
			href "userSelectionPage", title: "Launcher Page", description: "Tap to view", image: getAppImg("userselectionpage.png")
			if (devTestingLoaded) {
				href "computerSelectionPage", title: "Computer Launcher Page", description: "This page is not viewable", image: getAppImg("computerpages.png")
			}
			href "addDevicesPage", title: "Device Installer Page", description: "Tap to view", image: getAppImg("adddevicespage.png")
			href "removeDevicesPage", title: "Device Uninstaller Page", description: "Tap to view", image: getAppImg("removedevicespage.png")
			href "userApplicationPreferencesPage", title: "Application Settings Page", description: "Tap to view", image: getAppImg("userapplicationpreferencespage.png")
			href "userDevicePreferencesPage", title: "Device Preferences Page", description: "Tap to view", image: getAppImg("userdevicepreferencespage.png")
			href "userSelectionTokenPage", title: "Token Manager Page", description: "Tap to view", image: getAppImg("userselectiontokenpage.png")
			if (devTestingLoaded) {
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

//	----- DEVELOPER TESTING PAGE -----
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
		errorMsgCom = "Error communicating with cloud:\n\r" + "${state.currentError}" +
			"\n\rPlease resolve the error and try again."
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
		section("Application Information:", hideable: true, hidden: true) {
			paragraph title: "Communication Error:", errorMsgCom, image: getAppImg("error.png")
			paragraph title: "Finding Devices Error:", errorMsgDev, image: getAppImg("error.png")
			paragraph title: "New Devices Error:", errorMsgNew, image: getAppImg("error.png")
			paragraph title: "Current Devices Error:", errorMsgOld, image: getAppImg("error.png")
			paragraph title: "Account Error:", errorMsgTok, image: getAppImg("error.png")
			paragraph title: "Error Count:", "${state.errorCount}", image: getAppImg("error.png")
			paragraph title: "Current Error:", "${state.currentError}", image: getAppImg("error.png")
			paragraph title: "Error Messages:", "${errMsg}", image: getAppImg("error.png")
		}
		section("Information and Diagnostics:") {
			paragraph tokenInfoOnline(), image: getAppImg("tokenactive.png")
			paragraph tokenInfoOffline(), image: getAppImg("error.png")
		}
		section("Page Selector:") {
			paragraph pageSelectorErrorText(), image: getAppImg("error.png")
			paragraph sendingCommandSuccess(), image: getAppImg("sent.png")
			paragraph sendingCommandFailed(), image: getAppImg("issue.png")
			paragraph pageSelectorText(), image: getAppImg("pageselected.png")
			paragraph pageSelectorNullText(), image: getAppImg("pickapage.png")
		}
		section("Account Configuration:") {
			input ("userName", "email", title: "TP-Link Kasa Email Address", required: true, submitOnChange: true,image: getAppImg("email.png"))
			input ("userPassword", "password", title: "TP-Link Kasa Account Password", required: true, submitOnChange: true, image: getAppImg("password.png"))
		}
		section("User Configuration:") {
			input ("userSelectedOptionTwo", "enum", title: "What do you want to do?", required: true, multiple: false, submitOnChange: true, metadata: [values:["Update Account", "Activate Account", "Delete Account"]], image: getAppImg("userinput.png"))
			input ("userSelectedOptionOne", "enum", title: "What do you want to do?", required: true, multiple: false, submitOnChange: true, metadata: [values:["Add Devices", "Remove Devices", "Update Token", "Initial Installation"]], image: getAppImg("userinput.png"))
			input ("userSelectedOptionThree", "enum", title: "What do you want to do?", required: true, multiple: false, submitOnChange: true, metadata: [values:["Update Token", "Recheck Token", "Delete Token"]], image: getAppImg("token.png"))
		}
		section("Device Controller:") {
			input ("userSelectedDevicesAdd", "enum", required: true, multiple: true, submitOnChange: true, title: "Select Devices (${newDevices.size() ?: 0} found)", metadata: [values:newDevices], image: getAppImg("adddevices.png"))
			input ("userSelectedDevicesRemove", "enum", required: true, multiple: true, submitOnChange: true, title: "Select Devices (${oldDevices.size() ?: 0} found)", metadata: [values:oldDevices], image: getAppImg("removedevices.png"))
		}
		section("Application Configuration:") {
			input ("userSelectedNotification", "bool", title: "Do you want to enable notification?", submitOnChange: true, image: getAppImg("notification.png"))
			input ("userSelectedAppIcons", "bool", title: "Do you want to disable application icons?", submitOnChange: true, image: getAppImg("noicon.png"))
//			input ("userSelectedAssistant", "bool", title: "Do you want to enable recommended options?", submitOnChange: true, image: getAppImg("ease.png"))
			input ("userSelectedReload", "bool", title: "Do you want to refresh your current state?", submitOnChange: true, image: getAppImg("sync.png"))
			input ("userSelectedDeveloper", "bool", title: "Do you want to enable developer mode?", submitOnChange: true, image: getAppImg("developer.png"))
//			input ("userSelectedLauncher", "bool", title: "Do you want to enable the launcher page?", submitOnChange: true, image: getAppImg("launcher.png"))
//			input ("userSelectedQuickControl", "bool", title: "Do you want to enable post install features?", submitOnChange: true, image: getAppImg("quickcontrol.png"))
			input ("devTestingLoaded", "bool", title: "Do you want to enable developer testing mode?", submitOnChange: true, image: getAppImg("developertesting.png"))
		}
		section("Device Configuration:") {
			input ("userSelectedDevicesUpdate", "enum", required: true, multiple: true, submitOnChange: true, title: "Select Devices to Update (${oldDevices.size() ?: 0} found)", metadata: [values: oldDevices], image: getAppImg("devices.png"))
			input ("userLightTransTime", "enum", required: true, multiple: false, submitOnChange: true, title: "Lighting Transition Time", metadata: [values:["500": "1/2 second", "1000": "1 second", "2000": "2 seconds", "5000": "5 seconds", "10000": "10 seconds"]], image: getAppImg("transition.png"))
			input ("userRefreshRate", "enum", required: true, multiple: false, submitOnChange: true, title: "Device Refresh Rate", metadata: [values:["1" : "Refresh every minute (Not Recommended)", "5" : "Refresh every 5 minutes", "10" : "Refresh every 10 minutes", "15" : "Refresh every 15 minutes", "30" : "Refresh every 30 minutes (Recommended)"]], image: getAppImg("refresh.png"))
		}
		section("${textCopyright()}")
	}
}

//	----- HIDDEN PAGE -----
def hiddenPage() {
	def xkMembersInfo = "Although most of these members have left here is a complete list of all the members we had" 
	def xkMembers = "xKllerBOSSXXX, xKillerDDigital, xKillerIntense, xKillerMaverick, xKillerKittyKat, xKillerPP, xKillerBrute, xKillerBSOD, xKillerFoxy, xKillerTricky, xKillerReaper, xKillerPain, xKillerRobot, xKillerSasha, XKillerAwesomer, xKillerSonic, xKillerChakra, xKillerDoobage, xKillerSeki, xKillerEvo, xKillerSubXero, xKillerCali, xKillerAsh, xKillerTruKillah,xKillerSierra, Weirdowack"
	def xkGameInfo = "Although we may not play most of these games anymore but as a bunch of friends and some family had fun along the way but i guess some things just don't last"
	dynamicPage (name: "hiddenPage", title: "xKiller Clan Page", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Members:") {
			paragraph xkMembersInfo, image: getAppImg("xkillerclanv2.png")
			paragraph xkMembers, image: getAppImg("family.png")
		}
		section("Games:") {
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
		section("Easter Eggs:") {
			href url: linkYoutubeEE1(), style: "external", required: false, title: "Youtube Link #1", description: "Tap to open in browser", state: "complete", image: getAppImg("youtube.png")
			href url: linkYoutubeEE2(), style: "external", required: false, title: "Youtube Link #2", description: "Tap to open in browser", state: "complete", image: getAppImg("youtube.png")
			href url: linkYoutubeEE3(), style: "external", required: false, title: "Youtube Link #3", description: "Tap to open in browser", state: "complete", image: getAppImg("youtube.png")
		}
		section("Contact:") {
			href url: linkDiscord(), style: "external", required: false, title: "Discord", description: "Tap to open in browser", state: "complete", image: getAppImg("discord.png")
			href url: linkWaypoint(), style: "external", required: false, title: "Halo Waypoint", description: "Tap to open in browser", state: "complete", image: getAppImg("waypoint.png")
			href url: linkXbox(), style: "external", required: false, title: "Xbox", description: "Tap to open in browser", state: "complete", image: getAppImg("xbox.png")
			href url: linkSteam(), style: "external", required: false, title: "Steam", description: "Tap to open in browser", state: "complete", image: getAppImg("steam.png")
			href url: linkFacebook(), style: "external", required: false, title: "Facebook", description: "Tap to open in browser", state: "complete", image: getAppImg("facebook.png")
		}
		section("${textCopyright()}")
	}
}

//	----- ABOUT PAGE -----
def aboutPage() {
	dynamicPage (name: "aboutPage", title: "About Page", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png", true)
		}
		section("Donations:") {
			paragraph title: "Donations (@DaveGut)", "Donate to a charity", state: "complete", image: getAppImg("heart.png")
			href url: textDonateLinkAntR(), style: "external", required: false, title: "Donations (@ramiran2)", description: "Tap to open in browser", state: "complete", image: getAppImg("paypal.png")
		}
		section("Credits:") {
			paragraph title: "Creator:", "Dave G. (@DaveGut)", state: "complete", image: getAppImg("dave.png")
			paragraph title: "Co-Author:", "Anthony R. (@ramiran2)", state: "complete", image: getAppImg("bigmac.png")
			if ("${restrictedRecordPasswordPrompt}" =~ "Mac5089") {
			paragraph title: "Unknown:", "Lindsey M. (@Unknown)", state: "complete", image: getAppImg("unknown.png")
			}
			paragraph title: "Collaborator:", "Anthony S. (@tonesto7)", state: "complete", image: getAppImg("tonesto7.png")
		}
		section("Application Changes Details:") {
			href "changeLogPage", title: "View App Revision History", description: "Tap to view", image: getAppImg("changelogpage.png")
		}
		section("GitHub:") {
			href url: linkGitHubDavG(), style: "external", required: false, title: "Dave G. (@DaveGut)", description: "Tap to open in browser", state: "complete", image: getAppImg("github.png")
			href url: linkGitHubAntR(), style: "external", required: false, title: "Anthony R. (@ramiran2)", description: "Tap to open in browser", state: "complete", image: getAppImg("github.png")
			href url: linkGitHubAntS(), style: "external", required: false, title: "Anthony S. (@tonesto7)", description: "Tap to open in browser", state: "complete", image: getAppImg("github.png")
		}
		section("Licensing Information:") {
			paragraph "${textCopyright()}\n${textLicense()}"
		}
		section("${textCopyright()}")
	}
}

//	----- CHANGELOG PAGE -----
def changeLogPage() {
	dynamicPage (name: "changeLogPage", title: "Changelog Page", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Changelog:") {
			paragraph title: "What's New in this Release...", "", state: "complete", image: getAppImg("new.png")
			paragraph appVerInfo()
		}
		section("${textCopyright()}")
	}
}

//	----- UNINSTALL PAGE -----
def uninstallPage() {
	def uninstallPageText = "This will uninstall the App, All Child Devices. \nPlease make sure that any devices created by this app are removed from any routines/rules/smartapps before tapping Remove."
	dynamicPage (name: "uninstallPage", title: "Uninstall Page", install: false, uninstall: true) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Information:") {
			paragraph title: "", uninstallPageText, image: getAppImg("information.png")
		}
		section("${textCopyright()}")
		remove("Uninstall this application", "WARNING!!!", "Last Chance to Stop! \nThis action is not reversible \n\nThis App, All Devices will be removed")
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

def getDevices() {
	def currentDevices = getDeviceData()
	state.devices = [:]
	def devices = state.devices
	currentDevices.each {
		def device = [:]
		device["deviceMac"] = it.deviceMac
		device["alias"] = it.alias
		device["deviceModel"] = it.deviceModel
		device["deviceId"] = it.deviceId
		device["appServerUrl"] = it.appServerUrl
		devices << ["${it.deviceMac}": device]
		def isChild = getChildDevice(it.deviceMac)
		if (isChild) {
			isChild.syncAppServerUrl(it.appServerUrl)
		}
		log.info "Device ${it.alias} added to devices array"
	}
}

def removeDevices() {
	userSelectedDevicesRemove.each { dni ->
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
			log.error "getWebData(params: $params, desc: $desc, text: $text) Exception:", ex
		}
		return "${label} info not found"
	}
}

def addDevices() {
	def tpLinkModel = [:]
		//	Plug-Switch Devices (no energy monitor capability)
		tpLinkModel << ["HS100" : "TP-Link Smart Plug - Kasa Account"]						//	HS100
		tpLinkModel << ["HS103" : "TP-Link Smart Plug - Kasa Account"]						//	HS103
		tpLinkModel << ["HS105" : "TP-Link Smart Plug - Kasa Account"]						//	HS105
		tpLinkModel << ["HS200" : "TP-Link Smart Switch - Kasa Account"]					//	HS200
		tpLinkModel << ["HS210" : "TP-Link Smart Switch - Kasa Account"]					//	HS210
		tpLinkModel << ["KP100" : "TP-Link Smart Plug - Kasa Account"]						//	KP100
		//	Dimming Switch Devices
		tpLinkModel << ["HS220" : "TP-Link Smart Dimming Switch - Kasa Account"]			//	HS220
		//	Energy Monitor Plugs
		tpLinkModel << ["HS110" : "TP-Link Smart Energy Monitor Plug - Kasa Account"]		//	HS110
		tpLinkModel << ["HS115" : "TP-Link Smart Energy Monitor Plug - Kasa Account"]		//	HS110
			//	Soft White Bulbs
		tpLinkModel << ["KB100" : "TP-Link Smart Soft White Bulb - Kasa Account"]			//	KB100
		tpLinkModel << ["LB100" : "TP-Link Smart Soft White Bulb - Kasa Account"]			//	LB100
		tpLinkModel << ["LB110" : "TP-Link Smart Soft White Bulb - Kasa Account"]			//	LB110
		tpLinkModel << ["KL110" : "TP-Link Smart Soft White Bulb - Kasa Account"]			//	KL110
		tpLinkModel << ["LB200" : "TP-Link Smart Soft White Bulb - Kasa Account"]			//	LB200
		//	Tunable White Bulbs
		tpLinkModel << ["LB120" : "TP-Link Smart Tunable White Bulb - Kasa Account"]		//	LB120
		tpLinkModel << ["KL120" : "TP-Link Smart Tunable White Bulb - Kasa Account"]		//	KL120
		//	Color Bulbs
		tpLinkModel << ["KB130" : "TP-Link Smart Color Bulb - Kasa Account"]				//	KB130
		tpLinkModel << ["LB130" : "TP-Link Smart Color Bulb - Kasa Account"]				//	LB130
		tpLinkModel << ["KL130" : "TP-Link Smart Color Bulb - Kasa Account"]				//	KL130
		tpLinkModel << ["LB230" : "TP-Link Smart Color Bulb - Kasa Account"]				//	LB230
	def hub = location.hubs[0]
	def hubId = hub.id
	userSelectedDevicesAdd.each { dni ->
		try {
			def isChild = getChildDevice(dni)
			if (!isChild) {
				def device = state.devices.find { it.value.deviceMac == dni }
				def deviceModel = device.value.deviceModel.substring(0,5)
				addChildDevice(
					"${appNamespace()}",
					tpLinkModel["${deviceModel}"],
					device.value.deviceMac,
					hubId, [
						"label": device.value.alias,
							"name": device.value.deviceModel,
						"data": [
							"deviceId" : device.value.deviceId,
							"appServerUrl": device.value.appServerUrl,
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

//	----- GET DEVICE DATA FROM THE CLOUD -----
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
		log.error "uninstManagerApp Exception:", ex
	}
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
	schedule("0 30 2 ? * WED", getToken)
	if (userSelectedDevicesAdd) {
		addDevices()
	}
	if (userSelectedDevicesRemove) {
		removeDevices()
	}
	if (userSelectedDevicesUpdate) {
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
	try {
		deleteChildDevice(it.deviceNetworkId)
		sendEvent(name: "DeviceDelete", value: "${alias} deleted")
	} catch (Exception e) {
		sendEvent(name: "DeviceDelete", value: "Failed to delete ${alias}")
	}
}

//	===== Definitions =====
//	----- Version Definitions -----
def appAuthor() { return "Dave Gutheinz, Anthony Ramirez" }
def appLabel()	{ return "TP-Link Kasa Service Manager" }		//	Renamed Application
def appNamespace()	{ return "davegut" }
//def appNamespace()	{ return "ramiran2" }

//	----- GitHub Data -----
def betaMarker()	{ return false }
def gitBranch()	{ return betaMarker() ? "beta" : "master" }
def getIssuePageUrl()	{ return "https://github.com/${gitRepo()}/issues" }
def gitName()		{ return "SmartThings_Cloud-Based_TP-Link-Plugs-Switches-Bulbs"}
def gitPath()		{ return "${gitRepo()}/${gitBranch()}"}
def getWikiPageUrl()	{ return "https://github.com/${gitRepo()}/wiki" }
def gitRepo()		{ return "${appNamespace()}/${gitName()}" }
def linkGitHubDavG()	{ return "https://github.com/DaveGut/SmartThings_Cloud-Based_TP-Link-Plugs-Switches-Bulbs" }
def linkGitHubAntR()	{ return "https://github.com/ramiran2/TP-Link-SmartThings" }
//def gitName()		{ return "TP-Link-SmartThings"}

//	----- Image Data -----
def getAppImg(imgName, on = null)	{ return (!userSelectedAppIcons || on) ? "https://raw.githubusercontent.com/${gitPath()}/images/$imgName" : "" }

//	----- Recurring Text Data -----
def appInfoDesc()	{ return "${appLabel()}\n • ${textVersion()}\n • ${textModified()}\n • ${textDriver()}" }
def appVerInfo()	{ return getWebData([uri: "https://raw.githubusercontent.com/${gitPath()}/data/changelog.txt", contentType: "text/plain; charset=UTF-8"], "changelog") }
def pageSelectorErrorText()	{ return "Kasa Cloud error detected.  See error message near top of page" }
def pageSelectorNullText()	{ return "Please select a option to continue" }
def pageSelectorText()	{ return "Please tap below to continue" }
def kasaErrorMessage() {return "Kasa Cloud Error: ${state.currentError}  \nResolve this error and try again."}
def sendingCommandFailed()	{ return "Ready to Send Command to SmartThings Application" }
def sendingCommandSuccess()	{ return "Command Sent to SmartThings Application" }
def textCopyright()	{ return "Copyright© 2018 - Dave Gutheinz, Anthony Ramirez" }
def textDesc() { return "A Service Manager for the TP-Link Kasa Devices connecting through the TP-Link Servers to SmartThings." }
def textDriver() {return "Minimum Device Driver: ${currentDriverVersion()}" }
def textModified()	{ return "Updated: ${appVerDate()}" }
def textVerInfo()	{ return "${appVerInfo()}" }
def textVersion()	{ return "Version: ${appVersion()}" }
def textLicense()	{ return getWebData([uri: "https://raw.githubusercontent.com/${gitPath()}/data/license.txt", contentType: "text/plain; charset=UTF-8"], "license") }
def tokenInfoOffline()	{ return "You do not have a token.  Either complete the initial Login or Update Token!" }
def tokenInfoOnline()	{ return "No detected errors. You are ready to continue." }


//	----- Misc Data -----
def textDonateLinkAntR()	{ return "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=S2CJBWCJEGVJA" }
def linkGitHubAntS()	{ return "https://github.com/tonesto7/nest-manager" }
def linkYoutubeEE1()	{ return "https://www.youtube.com/watch?v=87JPlNk5ves&list=PL0S-Da7zGmE9PRn_YIitvUZEHYQglJw" }
def linkYoutubeEE2()	{ return "https://www.youtube.com/watch?v=0eYTZrucx_o" }
def linkYoutubeEE3()	{ return "https://www.youtube.com/watch?v=4_5kpOeiZyg&index=3&list=PL0S-Da7zGmE-i5MQdHORm6a" }
def linkDiscord()	{ return "https://discord.gg/JDXeV23" }
def linkXbox()	{ return "https://account.xbox.com/en-us/clubs/profile?clubid=3379843591790358" }
def linkWaypoint()	{ return "https://www.halowaypoint.com/en-us/spartan-companies/xkiller%20clan" }
def linkSteam()	{ return "https://steamcommunity.com/groups/xKillerClan" }
def linkFacebook()	{ return "https://www.facebook.com/groups/xKillerClan/" }
