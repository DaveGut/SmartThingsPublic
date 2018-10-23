/*
SmartThings Manager, 2018 Version 4
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
	iconUrl: "${getAppImg("kasa.png", on)}",
	iconX2Url: "${getAppImg("kasa.png", on)}",
	iconX3Url: "${getAppImg("kasa.png", on)}",
	singleInstance: true
)

def appVersion() { return "4.0.0" }
def appVerDate() { return "10-23-2018" }

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
			settingUpdate("userSelectedLauncher", "false", "bool")
			settingUpdate("userSelectedQuickControl", "false", "bool")
		} else {
			settingUpdate("userSelectedLauncher", "true", "bool")
			settingUpdate("userSelectedQuickControl", "true", "bool")
		}
		settingUpdate("userSelectedReload", "false", "bool")
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
}

def setRecommendedOptions() {
	if (userSelectedAssistant) {
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
		if ("${userName}" =~ null || "${userPassword}" =~ null) {
			settingUpdate("userSelectedOptionTwo", "Activate Account", "enum")
		} else {
			settingUpdate("userSelectedOptionTwo", "Update Account", "enum")
		}
		if (state.TpLinkToken != null) {
			if (newDevices != [:]) {
				settingUpdate("userSelectedOptionOne", "Add Devices", "enum")
			}
			if (oldDevices != [:] && newDevices =~ [:]) {
				settingUpdate("userSelectedOptionOne", "Remove Devices", "enum")
			}
		} else {
			if ("${userName}" =~ null || "${userPassword}" =~ null) {
				settingUpdate("userSelectedOptionOne", "Initial Installation", "enum")
			} else {
				settingUpdate("userSelectedOptionOne", "Update Token", "enum")
			}
		}
		if (state.currentError != null) {
			settingUpdate("userSelectedOptionThree", "Recheck Token", "enum")
		} else {
			if (state.TpLinkToken != null) {
				if (userSelectedOptionTwo =~ "Update Account") {
					settingUpdate("userSelectedOptionThree", "Update Token", "enum")
				}
			} else {
				settingUpdate("userSelectedOptionThree", "Update Token", "enum")
			}
		}
	}
}

//	----- WELCOME PAGE -----
def welcomePage() {
	setInitialStates()
	setRecommendedOptions()
//	def strLatestDriverVersion = textDriverVersion()
	def welcomePageText = "Welcome to the new SmartThings application for TP-Link Kasa Devices."
//	def driverVersionText = "Current Driver Version: ${strLatestDriverVersion}"
	return dynamicPage (name: "welcomePage", title: "Welcome Page", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Information and Diagnostics:", hideable: true, hidden: true) {
			if (state.TpLinkToken != null) {
				paragraph tokenInfoOnline(), image: getAppImg("tokenactive.png")
			} else {
				paragraph tokenInfoOffline(), image: getAppImg("error.png")
			}
			paragraph title: "Information:", welcomePageText, image: getAppImg("information.png")
//			paragraph title: "Driver Version:", driverVersionText, image: getAppImg("devices.png")
		}
		if (!userSelectedLauncher) {
			section("Page Selector:") {
				if (state.currentError != null) {
					paragraph pageSelectorErrorText(), image: getAppImg("error.png")
				} else {
					paragraph pageSelectorText(), image: getAppImg("pageselected.png")
				}
				if ("${userName}" =~ null || "${userPassword}" =~ null) {
					href "userSelectionAuthenticationPage", title: "Login Page", description: "Tap to continue", image: getAppImg("userselectionauthenticationpage.png")
				} else {
					href "userSelectionPage", title: "Launcher Page", description: "Tap to continue", image: getAppImg("userselectionpage.png")
				}
			}
		}
		if (userSelectedQuickControl) {
			section("Device Manager:") {
				href "addDevicesPage", title: "Device Installer Page", description: "Tap to view", image: getAppImg("adddevicespage.png")
				href "removeDevicesPage", title: "Device Uninstaller Page", description: "Tap to view", image: getAppImg("removedevicespage.png")
			}
		}
		section("Settings:") {
			if (userSelectedQuickControl) {
				href "userDevicePreferencesPage", title: "Device Preferences Page", description: "Tap to view", image: getAppImg("userdevicepreferencespage.png")
				href "userAuthenticationPreferencesPage", title: "Login Settings Page", description: "Tap to view", image: getAppImg("userauthenticationpreferencespage.png")
			}
			href "userApplicationPreferencesPage", title: "Application Settings Page", description: "Tap to view", image: getAppImg("userapplicationpreferencespage.png")
		}
		section("Uninstall:") {
			href "uninstallPage", title: "Uninstall Page", description: "Tap to view", image: getAppImg("uninstallpage.png")
		}
		if (userSelectedDeveloper) {
			section("Developer:") {
				href "developerPage", title: "Developer Page", description: "Tap to view", image: getAppImg("developerpage.png")
			}
		}
		section("Help and Feedback:") {
			href url: getWikiPageUrl(), style: "${strBrowserMode()}", title: "View the Projects Wiki", description: "Tap to open in browser", state: "complete", image: getAppImg("help.png")
			href url: getIssuePageUrl(), style: "${strBrowserMode()}", title: "Report | View Issues", description: "Tap to open in browser", state: "complete", image: getAppImg("issue.png")
		}
		section("Changelog and About:") {
			href "changeLogPage", title: "Changelog Page", description: "Tap to view", image: getAppImg("changelogpage.png")
			href "aboutPage", title: "About Page", description: "Tap to view", image: getAppImg("aboutpage.png")
		}
		section("${textCopyright()}")
	}
}

//	----- USER SELECTION AUTHENTICATION PAGE -----
def userSelectionAuthenticationPage() {
	def userSelectionAuthenticationPageText = "If possible, open the IDE and select Live Logging. Then, " +
		"enter your Username and Password for TP-Link (same as Kasa app) and the "+
		"action you want to complete. " + "\n\rAvailable actions: \n\r" +
		"Activate Account: You will be required to login into TP-Link Kasa Account and you will be required to adds devices to SmartThings Account. \n\r" +
		"Update Account: You will be required to update your credentials to login into your TP-Link Kasa Account. \n\r" +
		"Delete Account: Deletes your credentials to login into your TP-Link Kasa Account."
	return dynamicPage (name: "userSelectionAuthenticationPage", title: "Login Page", nextPage: "computerSelectionAuthenticationPage", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Information and Diagnostics:", hideable: true, hidden: true) {
			if (state.TpLinkToken != null) {
				paragraph tokenInfoOnline(), image: getAppImg("tokenactive.png")
			} else {
				paragraph tokenInfoOffline(), image: getAppImg("error.png")
			}
			paragraph title: "Information:", userSelectionAuthenticationPageText, image: getAppImg("information.png")
		}
		section("Account Configuration:") {
			input ("userName", "email", title: "TP-Link Kasa Email Address", required: true, submitOnChange: true,image: getAppImg("email.png"))
			input ("userPassword", "password", title: "TP-Link Kasa Account Password", required: true, submitOnChange: true, image: getAppImg("password.png"))
		}
		section("User Configuration:") {
			input ("userSelectedOptionTwo", "enum", title: "What do you want to do?", required: true, multiple: false, submitOnChange: true, metadata: [values:["Update Account", "Activate Account", "Delete Account"]], image: getAppImg("userinput.png"))
		}
		section("Page Selector:") {
			if (userSelectedOptionTwo != null) {
				if (state.currentError != null) {
					paragraph pageSelectorErrorText(), image: getAppImg("error.png")
				} else {
					paragraph pageSelectorText(), image: getAppImg("pageselected.png")
				}
			} else {
				paragraph pageSelectorNullText(), image: getAppImg("pickapage.png")
			}
			if (userSelectedOptionTwo =~ "Activate Account") {
				getToken()
				href "addDevicesPage", title: "Device Installer Page", description: "Tap to continue", image: getAppImg("adddevicespage.png")
			}
			if (userSelectedOptionTwo =~ "Update Account") {
				href "userSelectionTokenPage", title: "Token Manager Page", description: "Tap to continue", image: getAppImg("userselectiontokenpage.png")
			}
			if (userSelectedOptionTwo =~ "Delete Account") {
				settingRemove("userName")
				settingRemove("userPassword")
				href "welcomePage", title: "Welcome Page", description: "Tap to view", image: getAppImg("welcomepage.png")
			}
		}
		section("${textCopyright()}")
	}
}

//	----- USER AUTHENTICATION PREFERENCES PAGE -----
def userAuthenticationPreferencesPage() {
	def userAuthenticationPreferencesPageText = "If possible, open the IDE and select Live Logging. Then, " +
		"enter your Username and Password for TP-Link (same as Kasa app) and the "+
		"action you want to complete."
	return dynamicPage (name: "userSelectionAuthenticationPage", title: "Login Settings Page", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Information and Diagnostics:", hideable: true, hidden: true) {
			if (state.TpLinkToken != null) {
				paragraph tokenInfoOnline(), image: getAppImg("tokenactive.png")
			} else {
				paragraph tokenInfoOffline(), image: getAppImg("error.png")
			}
			paragraph title: "Information:", userAuthenticationPreferencesPageText, image: getAppImg("information.png")
		}
		section("Account Configuration:") {
			input ("userName", "email", title: "TP-Link Kasa Email Address", required: true, submitOnChange: true, image: getAppImg("email.png"))
			input ("userPassword", "password", title: "TP-Link Kasa Account Password", required: true, submitOnChange: true, image: getAppImg("password.png"))
		}
		section("${textCopyright()}")
	}
}

//	----- COMPUTER SELECTION AUTHENTICATION PAGE -----
def computerSelectionAuthenticationPage() {
	switch (userSelectedOptionTwo) {
		case "Update Account":
			return userSelectionTokenPage()
			break
		case "Activate Account":
			return addDevicesPage()
			break
		case "Delete Account":
			return welcomePage()
			break
		default:
			if ("${userName}" =~ null || "${userPassword}" =~ null) {
				return welcomePage()
			} else {
				return userSelectionPage()
			}
    }
}

//	----- USER SELECTION PAGE -----
def userSelectionPage() {
	def userSelectionPageText = "Available actions: \n\r" +
		"Add Devices: You will be able to add devices to your SmartThings Hub so you can control them from the SmartThings application. \n\r" +
		"Remove Devices: You will be able to remove any device from your SmartThings Hub that is controlled by this application. \n\r" +
		"Update Token: You will be able to request for a new token or delete your current token from the application. \n\r" +
		"Initial Installation: You will be asked to login into TP-Link Account and you may be asked to adds devices if you have not done so already."
	def errorMsgCom = "None"
	if (state.currentError != null) {
		errorMsgCom = "Error communicating with cloud:\n\r" + "${state.currentError}" +
			"\n\rPlease resolve the error and try again."
	}
	return dynamicPage (name: "userSelectionPage", title: "Launcher Page", nextPage: "computerSelectionPage", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Information and Diagnostics:", hideable: true, hidden: true) {
			if (state.TpLinkToken != null) {
				paragraph tokenInfoOnline(), image: getAppImg("tokenactive.png")
			} else {
				paragraph tokenInfoOffline(), image: getAppImg("error.png")
			}
			paragraph title: "Information:", userSelectionPageText, image: getAppImg("information.png")
			paragraph title: "Communication Error:", errorMsgCom, image: getAppImg("error.png")
		}
		section("User Configuration:") {
			input ("userSelectedOptionOne", "enum", title: "What do you want to do?", required: true, multiple: false, submitOnChange: true, metadata: [values:["Add Devices", "Remove Devices", "Update Token", "Initial Installation"]], image: getAppImg("userinput.png"))
		}
		section("Page Selector:") {
			if (userSelectedOptionOne != null) {
				if (state.currentError != null) {
					paragraph pageSelectorErrorText(), image: getAppImg("error.png")
				} else {
					paragraph pageSelectorText(), image: getAppImg("pageselected.png")
				}
			} else {
				paragraph pageSelectorNullText(), image: getAppImg("pickapage.png")
			}
			if (userSelectedOptionOne =~ "Initial Installation") {
				href "userSelectionAuthenticationPage", title: "Login Page", description: "Tap to continue", image: getAppImg("userselectionauthenticationpage.png")
			}
			if (userSelectedOptionOne =~ "Add Devices") {
				href "addDevicesPage", title: "Device Installer Page", description: "Tap to continue", image: getAppImg("adddevicespage.png")
			}
			if (userSelectedOptionOne =~ "Remove Devices") {
				href "removeDevicesPage", title: "Device Uninstaller Page", description: "Tap to continue", image: getAppImg("removedevicespage.png")
			}
			if (userSelectedOptionOne =~ "Update Token") {
				href "userSelectionTokenPage", title: "Token Manager Page", description: "Tap to continue", image: getAppImg("userselectiontokenpage.png")
			}
		}
		section("${textCopyright()}")
	}
}

//	----- COMPUTER SELECTION PAGE -----
def computerSelectionPage() {
    switch (userSelectedOptionOne) {
		case "Initial Installation":
			return userSelectionAuthenticationPage()
			break
		case "Add Devices":
			return addDevicesPage()
			break
		case "Remove Devices":
			return removeDevicesPage()
			break
		case "Update Token":
			return userSelectionTokenPage()
			break
		default:
			return welcomePage()
    }
}

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

//	----- USER APPLICATION PREFERENCES PAGE -----
def userApplicationPreferencesPage() {
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
			input ("userSelectedAssistant", "bool", title: "Do you want to enable recommended options?", submitOnChange: true, image: getAppImg("ease.png"))
			input ("userSelectedBrowserMode", "bool", title: "Do you want to open all external links within the SmartThings app?", submitOnChange: true, image: getAppImg("browsermode.png"))
			input ("userSelectedReload", "bool", title: "Do you want to refresh your current state?", submitOnChange: true, image: getAppImg("sync.png"))
			if (userSelectedAppIcons && userSelectedAssistant && userSelectedReload || hiddenDeveloperInput == 1) {
				hiddenDeveloperInput = 1
				input ("userSelectedDeveloper", "bool", title: "Do you want to enable developer mode?", submitOnChange: true, image: getAppImg("developer.png"))
			}
			if (userSelectedDeveloper) {
				input ("userSelectedLauncher", "bool", title: "Do you want to enable the launcher page?", submitOnChange: true, image: getAppImg("launcher.png"))
				input ("userSelectedQuickControl", "bool", title: "Do you want to enable post install features?", submitOnChange: true, image: getAppImg("quickcontrol.png"))
				input ("userSelectedTestingPage", "bool", title: "Do you want to enable developer testing mode?", submitOnChange: true, image: getAppImg("developertesting.png"))
				input ("userSelectedDriverNamespace", "bool", title: "Do you want to switch the device handlers namespace?", submitOnChange: true, image: getAppImg("drivernamespace.png"))
			}
			if (userSelectedTestingPage && userSelectedReload || hiddenRecordInput == 1) {
				hiddenRecordInput = 1
				input ("restrictedRecordPasswordPrompt", type: "password", title: "This is a restricted record, Please input your password", description: "Hint: xKillerMaverick", required: false, submitOnChange: true, image: getAppImg("passwordverification.png"))
			}
			if (userSelectedReload && state.TpLinkToken != null) {
				checkError()
				setInitialStates()
			} else {
				settingUpdate("userSelectedReload", "false", "bool")
			}
			if (userSelectedAssistant && state.TpLinkToken != null) {
				setRecommendedOptions()
			} else {
				settingUpdate("userSelectedAssistant", "false", "bool")
			}
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
				paragraph tokenInfoOnline(), image: getAppImg("tokenactive.png")
			} else {
				paragraph tokenInfoOffline(), image: getAppImg("error.png")
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

//	----- TOKEN MANAGER PAGE -----
def userSelectionTokenPage() {
	def userSelectionTokenPageText = "Your current token: ${state.TpLinkToken}" + 
		"\n\rAvailable actions:\n\r" +
		"Update Token: Updates the token on your SmartThings Account from your TP-Link Kasa Account. \n\r" +
		"Remove Token: Removes the token on your SmartThings Account from your TP-Link Kasa Account. \n\r" +
		"Recheck Token: This will attempt to check if the token is valid as well as check for errors."
		def errorMsgTok = "None"
		if (state.TpLinkToken == null) {
			errorMsgTok = "You will be unable to control your devices until you get a new token."
		}
		if (state.currentError != null) {
			errorMsgTok = "You may not be able to control your devices until you update your credentials."
		}
	dynamicPage (name: "userSelectionTokenPage", title: "Token Manager Page", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Information and Diagnostics:", hideable: true, hidden: true) {
			paragraph title: "Information:", userSelectionTokenPageText, image: getAppImg("information.png")
			paragraph title: "Account Error:", errorMsgTok, image: getAppImg("error.png")
		}
		section("Account Status:") {
			if (state.TpLinkToken != null) {
				paragraph tokenInfoOnline(), image: getAppImg("tokenactive.png")
			} else {
				paragraph tokenInfoOffline(), image: getAppImg("error.png")
			}
		}
		section("User Configuration:") {
			input ("userSelectedOptionThree", "enum", title: "What do you want to do?", required: true, multiple: false, submitOnChange: true, metadata: [values:["Update Token", "Recheck Token", "Delete Token"]], image: getAppImg("token.png"))
		}
		section("Command Status:") {
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

//	----- DEVELOPER PAGE -----
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
		section("Application Information:", hideable: true, hidden: true) {
			paragraph title: "TP-Link Token:", "${state.TpLinkToken}", image: getAppImg("token.png")
			paragraph title: "Hub:", "${hub}", image: getAppImg("samsunghub.png")
			paragraph title: "Hub ID:", "${hubId}", image: getAppImg("samsunghub.png")
			paragraph title: "Latest Smart Application Version:", "${strLatestSmartAppVersion}", image: getAppImg("kasa.png")
			paragraph title: "Latest Device Handlers Version:", "${strLatestDriverVersion}", image: getAppImg("devices.png")
			paragraph title: "Current Smart Application Version:", "${appVersion()}", image: getAppImg("kasa.png")
			paragraph title: "Current Device Handlers Version:", "${strLoadedDriverVersion}", image: getAppImg("devices.png")
			paragraph title: "Beta Build:", "${betaMarker()}", image: getAppImg("beta.png")
			paragraph title: "GitHub Namespace:", "${appNamespace()}", image: getAppImg("github.png")
			paragraph title: "Device Handlers Namespace:", "${driverNamespace()}", image: getAppImg("devices.png")
			paragraph title: "Username:", "${userName}", image: getAppImg("email.png")
			paragraph title: "Password:", "${userPassword}", image: getAppImg("password.png")
			paragraph title: "Managed Devices:", "${oldDevices}", image: getAppImg("devices.png")
			paragraph title: "New Devices:", "${newDevices}", image: getAppImg("devices.png")
		}
		section("Page Selector:") {
			href "welcomePage", title: "Welcome Page", description: "Tap to view", image: getAppImg("welcomepage.png")
			href "userSelectionAuthenticationPage", title: "Login Page", description: "Tap to view", image: getAppImg("userselectionauthenticationpage.png")
			href "userAuthenticationPreferencesPage", title: "Login Settings Page", description: "Tap to view", image: getAppImg("userauthenticationpreferencespage.png")
			if (userSelectedTestingPage) {
				href "computerSelectionAuthenticationPage", title: "Computer Login Page", description: "This page is not viewable", image: getAppImg("computerpages.png")
			}
			href "userSelectionPage", title: "Launcher Page", description: "Tap to view", image: getAppImg("userselectionpage.png")
			if (userSelectedTestingPage) {
				href "computerSelectionPage", title: "Computer Launcher Page", description: "This page is not viewable", image: getAppImg("computerpages.png")
			}
			href "addDevicesPage", title: "Device Installer Page", description: "Tap to view", image: getAppImg("adddevicespage.png")
			href "removeDevicesPage", title: "Device Uninstaller Page", description: "Tap to view", image: getAppImg("removedevicespage.png")
			href "userApplicationPreferencesPage", title: "Application Settings Page", description: "Tap to view", image: getAppImg("userapplicationpreferencespage.png")
			href "userDevicePreferencesPage", title: "Device Preferences Page", description: "Tap to view", image: getAppImg("userdevicepreferencespage.png")
			href "userSelectionTokenPage", title: "Token Manager Page", description: "Tap to view", image: getAppImg("userselectiontokenpage.png")
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
			input ("userSelectedAssistant", "bool", title: "Do you want to enable recommended options?", submitOnChange: true, image: getAppImg("ease.png"))
			input ("userSelectedBrowserMode", "bool", title: "Do you want to open all external links within the SmartThings app?", submitOnChange: true, image: getAppImg("browsermode.png"))
			input ("userSelectedReload", "bool", title: "Do you want to refresh your current state?", submitOnChange: true, image: getAppImg("sync.png"))
			input ("userSelectedDeveloper", "bool", title: "Do you want to enable developer mode?", submitOnChange: true, image: getAppImg("developer.png"))
			input ("userSelectedLauncher", "bool", title: "Do you want to enable the launcher page?", submitOnChange: true, image: getAppImg("launcher.png"))
			input ("userSelectedQuickControl", "bool", title: "Do you want to enable post install features?", submitOnChange: true, image: getAppImg("quickcontrol.png"))
			input ("userSelectedTestingPage", "bool", title: "Do you want to enable developer testing mode?", submitOnChange: true, image: getAppImg("developertesting.png"))
			input ("userSelectedDriverNamespace", "bool", title: "Do you want to switch the device handlers namespace?", submitOnChange: true, image: getAppImg("drivernamespace.png"))
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
			href url: linkYoutubeEE1(), style: "${strBrowserMode()}", required: false, title: "Youtube Link #1", description: "Tap to open in browser", state: "complete", image: getAppImg("youtube.png")
			href url: linkYoutubeEE2(), style: "${strBrowserMode()}", required: false, title: "Youtube Link #2", description: "Tap to open in browser", state: "complete", image: getAppImg("youtube.png")
			href url: linkYoutubeEE3(), style: "${strBrowserMode()}", required: false, title: "Youtube Link #3", description: "Tap to open in browser", state: "complete", image: getAppImg("youtube.png")
		}
		section("Contact:") {
			href url: linkDiscord(), style: "${strBrowserMode()}", required: false, title: "Discord", description: "Tap to open in browser", state: "complete", image: getAppImg("discord.png")
			href url: linkWaypoint(), style: "${strBrowserMode()}", required: false, title: "Halo Waypoint", description: "Tap to open in browser", state: "complete", image: getAppImg("waypoint.png")
			href url: linkXbox(), style: "${strBrowserMode()}", required: false, title: "Xbox", description: "Tap to open in browser", state: "complete", image: getAppImg("xbox.png")
			href url: linkSteam(), style: "${strBrowserMode()}", required: false, title: "Steam", description: "Tap to open in browser", state: "complete", image: getAppImg("steam.png")
			href url: linkFacebook(), style: "${strBrowserMode()}", required: false, title: "Facebook", description: "Tap to open in browser", state: "complete", image: getAppImg("facebook.png")
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
			href url: textDonateLinkAntR(), style: "${strBrowserMode()}", required: false, title: "Donations (@ramiran2)", description: "Tap to open in browser", state: "complete", image: getAppImg("paypal.png")
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
			href url: linkGitHubDavG(), style: "${strBrowserMode()}", required: false, title: "Dave G. (@DaveGut)", description: "Tap to open in browser", state: "complete", image: getAppImg("github.png")
			href url: linkGitHubAntR(), style: "${strBrowserMode()}", required: false, title: "Anthony R. (@ramiran2)", description: "Tap to open in browser", state: "complete", image: getAppImg("github.png")
			href url: linkGitHubAntS(), style: "${strBrowserMode()}", required: false, title: "Anthony S. (@tonesto7)", description: "Tap to open in browser", state: "complete", image: getAppImg("github.png")
		}
		section("Licensing Information:") {
			paragraph "${textLicense()}"
		}
		section("${textCopyright()}")
	}
}

//	----- CHANGELOG PAGE -----
def changeLogPage() {
	cleanStorage()
	checkForUpdates()
	def strLatestSmartAppVersion = textSmartAppVersion()
	def updateNeeded = "Both the Smart Application and Device Handlers need to be updated"
	def upToDate = "Both the Smart Application and Device Handlers are up to date"
	def driverUpdateNeeded = "Your Device Handlers need to be update"
	def smartAppUpdateNeeded = "Your Smart Application needs to be update"
	dynamicPage (name: "changeLogPage", title: "Changelog Page", install: false, uninstall: false) {
		section("") {
			paragraph appInfoDesc(), image: getAppImg("kasa.png")
		}
		section("Check for Updates:") {
			if ("${strLatestSmartAppVersion}" =~ "${appVersion()}" && "${atomicState?.devManVer}" ==~ "${atomicState?.devTWBVer}" && "${atomicState?.devSWBVer}" && "${atomicState?.devCBVer}" && "${atomicState?.devPGVer}" && "${atomicState?.devEMPGVer}" && "${atomicState?.devSHVer}" && "${atomicState?.devDSHVer}") {
				paragraph upToDate, image: getAppImg("success.png")
			} else {
				if ("${strLatestSmartAppVersion}" != "${appVersion()}" && "${atomicState?.devManVer}" != "${atomicState?.devTWBVer}" && "${atomicState?.devSWBVer}" && "${atomicState?.devCBVer}" && "${atomicState?.devPGVer}" && "${atomicState?.devEMPGVer}" && "${atomicState?.devSHVer}" && "${atomicState?.devDSHVer}") {
					paragraph updateNeeded, image: getAppImg("error.png")
				} else {
					if ("${strLatestSmartAppVersion}" != "${appVersion()}") {
						paragraph smartAppUpdateNeeded, image: getAppImg("issue.png")
					} else {
						paragraph driverUpdateNeeded, image: getAppImg("issue.png")
					}
				}
			}
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

def checkForUpdates() {
	def strLatestSmartAppVersion = textSmartAppVersion()
	def strLatestDriverVersion = textDriverVersion()
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
	}
	if (atomicState?.devSWBVer =~ null) {
		atomicState?.devSWBVer = strDevVersion
	}
	if (atomicState?.devCBVer =~ null) {
		atomicState?.devCBVer = strDevVersion
	}
	if (atomicState?.devPGVer =~ null) {
		atomicState?.devPGVer = strDevVersion
	}
	if (atomicState?.devEMPGVer =~ null) {
		atomicState?.devEMPGVer = strDevVersion
	}
	if (atomicState?.devSHVer =~ null) {
		atomicState?.devSHVer = strDevVersion
	}
	if (atomicState?.devDSHVer =~ null) {
		atomicState?.devDSHVer = strDevVersion
	}
	if ("${atomicState?.devManVer}" != "${atomicState?.devTWBVer}" && "${atomicState?.devSWBVer}" && "${atomicState?.devCBVer}" && "${atomicState?.devPGVer}" && "${atomicState?.devEMPGVer}" && "${atomicState?.devSHVer}" && "${atomicState?.devDSHVer}") {
		if (userSelectedNotification) {
			sendPush("${appLabel()} Device Handlers need to be update")
		}
	}
	if ("${strLatestSmartAppVersion}" != "${appVersion()}" ) {
		if (userSelectedNotification) {
			sendPush("${appLabel()} needs to be update")
		}
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
		tpLinkModel << ["HS100" : "(Cloud) TP-Link Plug"]					//	HS100
		tpLinkModel << ["HS103" : "(Cloud) TP-Link Plug"]					//	HS103
		tpLinkModel << ["HS105" : "(Cloud) TP-Link Plug"]					//	HS105
		tpLinkModel << ["HS200" : "(Cloud) TP-Link Switch"]					//	HS200
		tpLinkModel << ["HS210" : "(Cloud) TP-Link Switch"]					//	HS210
		tpLinkModel << ["KP100" : "(Cloud) TP-Link Plug"]					//	KP100
		//	Dimming Switch Devices
		tpLinkModel << ["HS220" : "(Cloud) TP-Link Dimming Switch"]			//	HS220
		//	Energy Monitor Plugs
		tpLinkModel << ["HS110" : "(Cloud) TP-Link Energy Monitor Plug"]	//	HS110
		tpLinkModel << ["HS115" : "(Cloud) TP-Link Energy Monitor Plug"]	//	HS110
			//	Soft White Bulbs
		tpLinkModel << ["KB100" : "(Cloud) TP-Link Soft White Bulb"]		//	KB100
		tpLinkModel << ["LB100" : "(Cloud) TP-Link Soft White Bulb"]		//	LB100
		tpLinkModel << ["LB110" : "(Cloud) TP-Link Soft White Bulb"]		//	LB110
		tpLinkModel << ["KL110" : "(Cloud) TP-Link Soft White Bulb"]		//	KL110
		tpLinkModel << ["LB200" : "(Cloud) TP-Link Soft White Bulb"]		//	LB200
		//	Tunable White Bulbs
		tpLinkModel << ["LB120" : "(Cloud) TP-Link Tunable White Bulb"]		//	LB120
		tpLinkModel << ["KL120" : "(Cloud) TP-Link Tunable White Bulb"]		//	KL120
		//	Color Bulbs
		tpLinkModel << ["KB130" : "(Cloud) TP-Link Color Bulb"]				//	KB130
		tpLinkModel << ["LB130" : "(Cloud) TP-Link Color Bulb"]				//	LB130
		tpLinkModel << ["KL130" : "(Cloud) TP-Link Color Bulb"]				//	KL130
		tpLinkModel << ["LB230" : "(Cloud) TP-Link Color Bulb"]				//	LB230
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
	runEvery3Hours(cleanStorage)
	runEvery3Hours(checkForUpdates)
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

	def appNamespace()	{ return "davegut" }
//	def appNamespace()	{ return "ramiran2" }
	def gitName()	{ return "SmartThings_Cloud-Based_TP-Link-Plugs-Switches-Bulbs" }
//	def gitName()	{ return "TP-Link-SmartThings" }
	def appLabel()	{ return "TP-Link Cloud Connect" }
//	def appLabel()	{ return "TP-Link SmartThings Manager" }

def driverNamespace() { return userSelectedDriverNamespace ? "DaveGut" : "ramiran2" }

def gitBranch()	{ return betaMarker() ? "beta" : "master" }
def getAppImg(imgName, on = null)	{ return (!userSelectedAppIcons || on) ? "https://raw.githubusercontent.com/${gitPath()}/images/$imgName" : "" }
def getWikiPageUrl()	{ return "https://github.com/${gitRepo()}/wiki" }
def getIssuePageUrl()	{ return "https://github.com/${gitRepo()}/issues" }
def strBrowserMode()	{ return userSelectedBrowserMode ? "embedded" : "external" }
def gitRepo()		{ return "${appNamespace()}/${gitName()}" }
def gitPath()		{ return "${gitRepo()}/${gitBranch()}"}
def betaMarker()	{ return false }
def sendingCommandSuccess()	{ return "Command Sent to SmartThings Application" }
def sendingCommandFailed()	{ return "Ready to Send Command to SmartThings Application" }
def tokenInfoOnline()	{ return "Online and Ready to Control Devices" }
def tokenInfoOffline()	{ return "Offline, Please Fix to Restore Control on Devices" }
def pageSelectorText()	{ return "Please tap below to continue" }
def pageSelectorNullText()	{ return "Please select a option to continue" }
def pageSelectorErrorText()	{ return "Please continue with caution, we have detected a error" }
def appInfoDesc()	{
	def str = ""
	str += "${appLabel()}"
	str += "\n" + "• ${textVersion()}"
	str += "\n" + "• ${textModified()}"
	return str
}
def appAuthor() { return "Dave Gutheinz, Anthony Ramirez" }
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
def textDesc() { return "A Service Manager for the TP-Link Kasa Devices connecting through the TP-Link Servers to SmartThings." }
