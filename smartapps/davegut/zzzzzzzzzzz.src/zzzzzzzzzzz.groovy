/*
UPDATE VERSION
*/

definition(
//	name: "TP-Link Cloud Connect",
	name: "zzzzzzzzzzz",
	namespace: "davegut",
	author: "Dave Gutheinz, Anthony Rameriz",
	description: "A Service Manager for Kasa/TP-Link devices connecting through the Kasa Cloud",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	singleInstance: true
)

preferences {
	page(name: "startPage")
	page(name: "cloudLogin")
	page(name: "mainPage")
	page(name: "installDevices")
    page(name: "removeDevices")
    page(name: "setDefaultPreferences")
    page(name: "setDevicePreferences")
    page(name: "goToSelection")
}

def setInitialStates() {
log.error "at setInitialStates"
	if (!state.TpLinkToken) {state.TpLinkToken = null}
	if (!state.devices) {state.devices = [:]}
	if (!state.currentError) {state.currentError = null}
	if (!state.errorCount) {state.errorCount = 0}
}

def startPage() {
log.error "at startPage"
	setInitialStates()
	if ("${userName}" =~ null || "${userPassword}" =~ null){
		return cloudLogin()
	} else {
		return mainPage()
	}
}

//	===== Kasa Cloud Login =====
def cloudLogin() {
log.error "at cloudLogin"
	setInitialStates()
	def welcome = "Welcome to the Kasa Device SmartThings Application."
    def whatWillHappen = "WHAT WILL HAPPEN.  On this page, you will log into your Kasa Account which will " +
    					 "automatically get the token necessary to control the device.  If successful, " +
                         "the application will install into SmartThings and exit.  THEN, when you activate " +
                         "application again, it will go to the option selection where you can then select "+
                         "install, remove, or configure devices."
	def errorMsgCom = "None"
	if (state.currentError != null){
		errorMsgCom = "Error communicating with cloud:\n\r" + "${state.currentError}" +
			"\n\rPlease resolve the error and try again."
	}
	return dynamicPage(
		name: "cloudLogin", 
		title: "Kasa Device Service Manager", 
		nextPage: "getToken",
        install: true,
		uninstall: true) {
		section() {
			input( 
				"userName", "string", 
				title:"Enter your Kasa Account E-Mail", 
				required:true, 
				displayDuringSetup: true
			)
			input(
				"userPassword", "password", 
				title:"Enter your Kasa Account Password",
                requred: true,
				displayDuringSetup: true
			)
            paragraph "Then press SAVE in upper right to continue!  Use the < in upper left to return or cancel."
 		}
        section("Login Instructions", hideable: true, hidden: true) {
        	paragraph welcome
        	paragraph whatWillHappen
        }
        section("Current Error Message", hideable: true, hidden: true) {
        	paragraph errorMsgCom
        }
        section("Kasa Device SmartThings Integration.  Copyright 2018, Dave Gutheinz and Anthony Ramirez.")
        section("CAUTION: Below removes both the app and Devices")
	}
    return startPage()
}

//	===== Option Selection =====
def mainPage() {
log.error "at mainPage"
	def installInstructions = 
    	"Select from the available actions: " +
		"1. [Add Devices: Install Kasa Devices] " +
		"2. [Remove Devices: Uninstall Kasa Devices] " +
		"3. [Set Device Preferences: Set selected device preferences] " +
		"4. [Kasa Account Login: Go to Login page to get new token] " +
        "PRESS NEXT AFTER MAKING SELECTION TO CONTINUE"
	def errorMsgCom = "None"
	if (state.currentError != null){
		errorMsgCom = "Error communicating with cloud:\n\r" + "${state.currentError}" +
			"\n\rPlease resolve the error and try again."
	}
	return dynamicPage(
		name: "mainPage",
		title: "Kasa Device Installation Options",
		nextPage: "goToSelection", 
		install: false,
		uninstall: true) {
        section() {
			input(
				"installationOption", "enum",
				title: "Press below to select option.",
				required: true,
				multiple: false,
				submitOnChange: true,
				metadata: [values:["Install Devices", "Remove Devices", "Set Device Preferences", "Kasa Account Login"]],
			)
            paragraph "Then press NEXT in upper right to continue!  Use the < in upper left to return or cancel."
		}
        section("Installation Instructions", hideable: true, hidden: true) {
        	paragraph installInstructions
        }
        section("Current Error Message", hideable: true, hidden: true) {
        	paragraph errorMsgCom
        }
        section("Kasa Device SmartThings Integration.  Copyright 2018, Dave Gutheinz and Anthony Ramirez.")
//        section("About the Kasa Integration", hideable: true, hidden: false) {
//        	paragraph "Kasa Device SmartThings Integration.  Copyright 2018, Dave Gutheinz and Anthony Ramirez."
//        }
        section("CAUTION: Below removes both the app and Devices")
	}
}

def goToSelection() {
log.error "at goToSelection"
    switch (installationOption) {
      	case "Install Devices":
          	return installDevices()
            break
        case "Remove Devices":
           	return removeDevices()
            break
        case "Set Device Preferences":
           	return setDefaultPreferences()
            break
        case "Kasa Account Login":
           	return cloudLogin()
            break
        default:
           	return startPage()
    }
}

//	===== Device Installation =====
def installDevices() {
log.error "at installDevices"
	if (state.currentError != null) {
		getToken()
	}
	getDevices()
	def devices = state.devices
	if (state.currentError != null) {
		return cloudLogin()
	}
	def errorMsg = ""
	if (devices == [:]) {
		errorMsg = "There were no devices from Kasa.  This usually means "+
			"that all devices are in 'Local Control Only'.  Correct then " +
			"rerun."
	}
	def newDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceMac)
		if (!isChild) {
			newDevices["${it.value.deviceMac}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	if (newDevices == [:]) {
		errorMsg = "No new devices to add.  Are you sure the devices are in Remote Control Mode?"
		}
    def errorMsgCom = "None"
	if (state.currentError != null){
		errorMsgCom = "Error communicating with cloud:\n\r" + "${state.currentError}.  " +
			"Please resolve the error and try again."
	}
	settings.selectedDevices = null
	return dynamicPage(
		name: "installDevices", 
		title: "Add a Kasa device to SmartThings", 
		install: true,
		uninstall: false) {
        if (errorMsg == "") {
	        section("") {
				input (
	               	"selectedDevices", "enum",
					required:false,
					multiple:true, 
					title: "Tap below to select devices (${newDevices.size() ?: 0} found)",
					metadata: [values: newDevices]
	            )
                paragraph "Then press DONE in upper right to continue!  Use the < in upper left to return to previous page."
			}
	        section("Current Error Message", hideable: true, hidden: true) {
				paragraph errorMsgCom
                paragraph "Use the < in upper left to return to previous page."
	        }
        } else {
	        section("No Devices Found to Install", hideable: false, hidden: false) {
	        	paragraph errorMsg
                paragraph "Use the < in upper left to return to previous page."
			}
        }
        section("Kasa Device SmartThings Integration.  Copyright 2018, Dave Gutheinz and Anthony Ramirez.")
	}
}

def getDevices() {
log.error "at getDevices"
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

def addDevices() {
log.error "at addDevices"
	if (installationOption == "Remove Devices") {
   		return deleteDevices()
    } else if (installationOption == "Set Device Preferences") {
    	return updatePreferences()
    }
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
	selectedDevices.each { dni ->
		def device = state.devices.find { it.value.deviceMac == dni }
		def deviceModel = device.value.deviceModel.substring(0,5)
		addChildDevice(
			"davegut",
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
		log.info "Installed Kasa $deviceModel with alias ${device.value.alias}"
	}
}

//	===== Device Removal =====
def removeDevices() {
log.error "at removeDevices"
	def devices = state.devices
	def errorMsg = ""
	if (devices == [:]) {
		errorMsg = "There were no devices from Kasa to remove.  Correct " +
			"then rerun."
	}
    def installedDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceMac)
		if (isChild) {
			installedDevices["${it.value.deviceMac}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	if (installedDevices == [:]) {
		errorMsg = "No devices are installed."
	}
    def errorMsgCom = "None"
	if (state.currentError != null){
		errorMsgCom = "Error communicating with cloud:\n\r" + "${state.currentError}.  " +
			"Please resolve the error and try again."
	}
	settings.selectedDevices = null
	return dynamicPage(
		name: "removeDevices", 
		title: "Remove a Kasa device from SmartThings", 
		install: true,
		uninstall: false) {
        if (errorMsg == "") {
	        section("") {
				input (
                	"selectedDevices", "enum",
					required:false,
					multiple:true, 
					title: "Tap below to select devices (${installedDevices.size() ?: 0} found)",
					metadata: [values: installedDevices]
                )
                paragraph "Then press DONE in upper right to continue!  Use the < in upper left to return to previous page."
			}
	        section("Current Error Message", hideable: true, hidden: true) {
	        	paragraph errorMsgCom
                paragraph "Use the < in upper left to return to previous page."
	        }
        } else {
	        section("No Devices Found to Remove", hideable: false, hidden: false) {
	        	paragraph errorMsg
                paragraph "Use the < in upper left to return to previous page."
            }
		}
        section("Kasa Device SmartThings Integration.  Copyright 2018, Dave Gutheinz and Anthony Ramirez.")
	}
}

def deleteDevices() {
log.error "at deleteDevices"
	selectedDevices.each { dni ->
		def isChild = getChildDevice(dni)
		if (isChild) {
			def delete = isChild
	        log.info "Removed one Kasa device."
			delete.each { deleteChildDevice(it.deviceNetworkId, true) }
		}
	}
}

//	===== Device Preferences =====
def setDefaultPreferences() {
log.error "at setDefaultPreferences"
	def preferencesText = "Enter a value for Transition Time and Refresh Rate."
	def errorMsg = ""
	return dynamicPage(
		name: "setDefaultPreferences", 
		title: "Set Default Device Preferences", 
		nextPage: "setDevicePreferences",
		uninstall: false) {
		section() {
			input(
				"defaultTransTime", "enum",
				required: true,
				multiple: false,
				submitOnChange: true,
				title: "Lighting Transition Time in Miliseconds",
				metadata: [values:["500": "1/2 second", "1000": "1 second", "2000": "2 seconds", "5000": "5 seconds", "10000": "10 seconds"]],
			)
			input(
				"defaultRefreshRate", "enum",
				required: true,
				multiple: false,
				submitOnChange: true,
				title: "Device Refresh Rate",
				metadata: [values:["5" : "Every 5 minutes", "10" : "Every 10 minutes", "15" : "Every 15 minutes"]],
			)
            paragraph "Then press NEXT in upper right to continue!  Use the < in upper left to return or cancel."
		}
	    section("Set Default Preferences Instructions", hideable: true, hidden: true) {
	        paragraph preferencesText
        }
        section("Kasa Device SmartThings Integration.  Copyright 2018, Dave Gutheinz and Anthony Ramirez.")
	}
}

def setDevicePreferences() {
log.error "at setDevicePreferences"
	def devices = state.devices
	def errorMsg = ""
	if (devices == [:]) {
		errorMsg = "There were no devices from Kasa to remove.  Correct " +
			"then rerun.\n\r\n\r"
	}
	def installedDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceMac)
		if (isChild) {
			installedDevices["${it.value.deviceMac}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	if (installedDevices == [:]) {
		errorMsg = "No devices are installed.\n\r\n\r"
		}
	settings.selectedDevices = null
	def preferencesDevicesMsg = "Devices selected will have their Refresh Rate and " +
		"transition time (bulbs only) set to the new default preferences. the below" +
		"Kasa devices are installed. Select the ones you want to set preferences on.\n\r\n\r" +
		"Press DONE when you have selected the devices you wish to set preferences on" +
		"then press DONE again to update the preferences.  Press	<	" +
		"to return to the previous page."
	return dynamicPage(
		name: "setDevicePreferences", 
		title: "Update Device Preferences", 
		install: true,
		uninstall: false) {
		section(errorMsg)
		section(PreferencesDevicesMsg) {
			input "selectedDevices", "enum",
			required:false, 
			multiple:true, 
			title: "Below, Select the Devices to Update (${installedDevices.size() ?: 0} found)",
			metadata: [values: installedDevices]
            paragraph "Then press DONE in upper right to continue!  Use the < in upper left to return or cancel."
		}
	    section("Set Preferences Instructions", hideable: true, hidden: true) {
	        paragraph preferencesDevicesMsg
        }
        section("Kasa Device SmartThings Integration.  Copyright 2018, Dave Gutheinz and Anthony Ramirez.")
	}
}

def updatePreferences() {
log.error "at updatePreferences"
	selectedDevices.each {
		def child = getChildDevice(it)
		child.setLightTransTime(defaultTransTime)
		child.setRefreshRate(defaultRefreshRate)
		log.info "Kasa device ${child} preferences updated"
	}
}

//	----- GET A NEW TOKEN FROM CLOUD -----
def getToken() {
log.error "at getToken"
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
log.error "at getDevices"
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
				log.debug "state.errorCount = ${state.errorCount} //	state.currentError = ${state.currentError}"
			}
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

//	----- INSTALL, UPDATE, INITIALIZE -----
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
	if (selectedDevices) {
		addDevices()
	}
}

//	----- PERIODIC CLOUD MX TASKS -----
def checkError() {
	if (state.currentError == null || state.currentError == "none") {
		log.info "TP-Link Connect did not have any set errors."
		return
	}
	def errMsg = state.currentError.msg
	log.info "Attempting to solve error: ${errMsg}"
	state.errorCount = state.errorCount +1
	if (errMsg == "Token expired" && state.errorCount < 6) {
		sendEvent (name: "ErrHandling", value: "Handle comms error attempt ${state.errorCount}")
		getDevices()
		if (state.currentError == null) {
			log.info "getDevices successful.  apiServerUrl updated and token is good."
			return
		}
		log.error "${errMsg} error while attempting getDevices.  Will attempt getToken"
		getToken()
		if (state.currentError == null) {
			log.info "getToken successful.  Token has been updated."
			getDevices()
			return
		}
	} else {
		log.error "checkError:  No auto-correctable errors or exceeded Token request count."
	}
	log.error "checkError residual:  ${state.currentError}"
}

//	===== Remove Device from Device Handle =====
def removeChildDevice(alias, deviceNetworkId) {
	try {
		deleteChildDevice(it.deviceNetworkId)
		sendEvent(name: "DeviceDelete", value: "${alias} deleted")
	} catch (Exception e) {
		sendEvent(name: "DeviceDelete", value: "Failed to delete ${alias}")
	}
}