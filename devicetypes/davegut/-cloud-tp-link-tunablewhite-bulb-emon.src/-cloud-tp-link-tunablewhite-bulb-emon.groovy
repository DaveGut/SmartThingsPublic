/*	TP Link Bulbs Device Handler, 2018 Version 3
	Copyright 2018 Dave Gutheinz and Anthony Ramirez

Licensed under the Apache License, Version 2.0(the "License");
you may not use this  file except in compliance with the
License. You may obtain a copy of the License at:

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, 
software distributed under the License is distributed on an 
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
either express or implied. See the License for the specific 
language governing permissions and limitations under the 
License.

Discalimer:  This Service Manager and the associated Device 
Handlers are in no way sanctioned or supported by TP-Link.  
All  development is based upon open-source data on the 
TP-Link devices; primarily various users on GitHub.com.

	===== History ============================================
2018-10-14	Update to Version 3.  Initial compatibility with
			the Classic and new SmartThings Mobile App.  No
            update to Service Manager.  Service Manager must
            be installed via the SmartThings Classic App.
            Thanks to Anthony Ramirez for providing the
            technical information for this update.

	===== Bulb Identifier.  DO NOT EDIT ====================*/
//	def deviceType = "SoftWhite Bulb"	//	Soft White
	def deviceType = "TunableWhite Bulb"	//	ColorTemp
//	def deviceType = "Color Bulb"			//	Color
//	===== Hub or Cloud Installation ==========================
	def installType = "Cloud"
//	def installType = "Hub"
//	==========================================================

metadata {
	definition (name: "(${installType}) TP-Link ${deviceType} Emon",
				namespace: "davegut",
				author: "Dave Gutheinz",
				deviceType: "${deviceType}",
				energyMonitor: "EnergyMonitor",
				ocfDeviceType: "oic.d.light",
				mnmn: "SmartThings",
				vid: "generic-rgbw-color-bulb",
				installType: "${installType}") {
		capability "Switch"
		capability "Switch Level"
		capability "refresh"
//		capability "polling"			//	Depreciated.
		capability "Health Check"
		if (deviceType != "SoftWhite Bulb") {
			capability "Color Temperature"
			command "setModeNormal"
			command "setModeCircadian"
			attribute "circadianMode", "string"
		}
		if (deviceType == "Color Bulb") {
			capability "Color Control"
			capability "Color Mode"
		}
		capability "Power Meter"
		command "getPower"
		capability "Energy Meter"
		command "getEnergyStats"
		attribute "monthTotalE", "string"
		attribute "monthAvgE", "string"
		attribute "weekTotalE", "string"
		attribute "weekAvgE", "string"
	}
	tiles(scale:2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light13", backgroundColor:"#00a0dc",
				nextState:"waiting"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light13", backgroundColor:"#ffffff",
				nextState:"waiting"
				attributeState "waiting", label:'${name}', action:"switch.on", icon:"st.Lighting.light13", backgroundColor:"#15EE10",
				nextState:"waiting"
				attributeState "commsError", label: 'Comms Error', action:"switch.on", icon:"st.Lighting.light13", backgroundColor:"#e86d13",
				nextState:"waiting"
			}
			tileAttribute ("deviceError", key: "SECONDARY_CONTROL") {
				attributeState "deviceError", label: '${currentValue}'
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", label: "Brightness: ${currentValue}", action:"switch level.setLevel"
			}
			if (deviceType == "Color Bulb") {
				tileAttribute ("device.color", key: "COLOR_CONTROL") {
					attributeState "color", action:"setColor"
				}
			}
		}
		
		standardTile("refresh", "capability.refresh", width: 2, height: 1, decoration: "flat") {
			state "default", label:"Refresh", action:"refresh.refresh"
		}
		
		if (deviceType == "TunableWhite Bulb") {
			controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 2, height: 1, inactiveLabel: false,
			range:"(2700..6500)") {
				state "colorTemperature", action:"color temperature.setColorTemperature"
			}
		} else if (deviceType == "Color Bulb") {
			controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 2, height: 1, inactiveLabel: false,
			range:"(2500..9000)") {
				state "colorTemperature", action:"color temperature.setColorTemperature"
			}
		}
		
		valueTile("colorTemp", "default", inactiveLabel: false, decoration: "flat", height: 1, width: 2) {
			state "default", label: 'Color\n\rTemperature'
		}
		
		standardTile("bulbMode", "bulbMode", width: 2, height: 1, decoration: "flat") {
			state "normal", label:'Circadian\n\rOFF', action:"setModeCircadian", nextState: "circadian"
			state "circadian", label:'Circadian\n\rOn', action:"setModeNormal", nextState: "normal"
		}

		valueTile("currentPower", "device.power", decoration: "flat", height: 1, width: 2) {
			state "power", label: 'Current Power \n\r ${currentValue} W'
		}

		valueTile("energyToday", "device.energy", decoration: "flat", height: 1, width: 2) {
			state "energy", label: 'Usage Today\n\r${currentValue} WattHr'
		}

		valueTile("monthTotal", "device.monthTotalE", decoration: "flat", height: 1, width: 2) {
			state "monthTotalE", label: '30 Day Total\n\r ${currentValue} KWH'
		}

		valueTile("monthAverage", "device.monthAvgE", decoration: "flat", height: 1, width: 2) {
			state "monthAvgE", label: '30 Day Avg\n\r ${currentValue} KWH'
		}
 
		valueTile("weekTotal", "device.weekTotalE", decoration: "flat", height: 1, width: 2) {
			state "weekTotalE", label: '7 Day Total\n\r ${currentValue} KWH'
		}

		valueTile("weekAverage", "device.weekAvgE", decoration: "flat", height: 1, width: 2) {
			state "weekAvgE", label: '7 Day Avg\n\r ${currentValue} KWH'
		}

		valueTile("2x1Blank", "default", decoration: "flat", height: 1, width: 2) {
			state "default", label: ''
		}
		
		valueTile("2x4Blank", "default", decoration: "flat", height: 2, width: 4) {
			state "default", label: ''
		}
		
		valueTile("4x1Blank", "default", decoration: "flat", height: 1, width: 4) {
			state "default", label: ''
		}

		main("switch")
		if (deviceType == "SoftWhite Bulb") {
			details("switch", "refresh", "4x1Blank", 
					"currentPower", "weekTotal", "monthTotal", 
					"energyToday", "weekAverage", "monthAverage")
		} else {
			details("switch", "colorTemp", "bulbMode", "refresh", 
					"colorTempSliderControl", "4x1Blank",
					"currentPower", "weekTotal", "monthTotal",
					"energyToday", "weekAverage", "monthAverage")
		}
	}

	def rates = [:]
	rates << ["1" : "Refresh every minutes (Not Recommended)"]
	rates << ["5" : "Refresh every 5 minutes"]
	rates << ["10" : "Refresh every 10 minutes"]
	rates << ["15" : "Refresh every 15 minutes"]

	preferences {
		if (installType == "Hub") {
			input("deviceIP", "text", title: "Device IP", required: true, displayDuringSetup: true)
			input("gatewayIP", "text", title: "Gateway IP", required: true, displayDuringSetup: true)
		}
        input name: "transitionTime", type: "enum", description: "", title: "Transition time", options: [[500:"500ms"],[1000:"1s"],[1500:"1.5s"],[2000:"2s"],[5000:"5s"]], defaultValue: 1000
		input name: "refreshRate", type: "enum", title: "Refresh Rate", options: rates, description: "Select Refresh Rate", required: false
	}
}

//	===== Update when installed or setting changed =
def initialize() {
	log.trace "Initialized..."
	sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson(["protocol":"cloud", "scheme":"untracked"]), displayed: false)
}

def ping() {
	refresh()
}

def installed() {
	updated()
}

def updated() {
	unschedule()
	state.deviceType = metadata.definition.deviceType
	state.installType = metadata.definition.installType
	state.emon = metadata.definition.energyMonitor
	state.emeterText = "smartlife.iot.common.emeter"
	state.getTimeText = "smartlife.iot.common.timesetting"
    //	Update Refresh Rate Preference
    if (refreshRate) {
    	setRefreshRate(refreshRate)
    } else {
    	setRefreshRate(30)
    }
    //	Update Light Transition Time Preference
	if (lightTransTime >= 0 && lightTransTime <= 60) {
    	def adjustedTime = lightTransTime*1000
		setLightTransTime(adjustedTime)
	} else {
		setLightTransTime(5000)
	}
	schedule("0 05 0 * * ?", setCurrentDate)
	schedule("0 10 0 * * ?", getEnergyStats)
	setCurrentDate()
	runIn(2, refresh)
	runIn(7, getEnergyStats)
}

void uninstalled() {
	if (state.installType == "Cloud") {
		def alias = device.label
		log.debug "Removing device ${alias} with DNI = ${device.deviceNetworkId}"
		parent.removeChildDevice(alias, device.deviceNetworkId)
	}
}

//	===== Basic Bulb Control/Status =====
def on() {
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off":1,"transition_period":${state.transTime}}}}""", "deviceCommand", "commandResponse")
}

def off() {
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off":0,"transition_period":${state.transTime}}}}""", "deviceCommand", "commandResponse")
}

def setLevel(percentage) {
    setLevel(percentage, state.transTime)
}

def setLevel(percentage, rate) {
    if (percentage < 0 || percentage > 100) {
        log.error "$device.name $device.label: Entered brightness is not from 0...100"
        percentage = 50
    }
	percentage = percentage as int
    rate = rate.toInteger()
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"ignore_default":1,"on_off":1,"brightness":${percentage},"transition_period":${rate}}}}""", "deviceCommand", "commandResponse")
}

def setColorTemperature(kelvin) {
    if (kelvin == null) kelvin = state.lastColorTemp
	switch(state.deviceType) {
    	case "TuneableWhite Bulb":
		    if (kelvin < 2700) kelvin = 2700
		    if (kelvin > 6500) kelvin = 6500
 	       break
        
        defalut:
		    if (kelvin < 2500) kelvin = 2500
		    if (kelvin > 9000) kelvin = 9000
    }
	kelvin = kelvin as int
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"ignore_default":1,"on_off":1,"color_temp": ${kelvin},"hue":0,"saturation":0}}}""", "deviceCommand", "commandResponse")
}

def setModeNormal() {
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"mode":"normal"}}}""", "deviceCommand", "commandResponse")
}

def setModeCircadian() {
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"mode":"circadian"}}}""", "deviceCommand", "commandResponse")
	runIn(2, getPower)
}

def setHue(hue) {
    if (hue == null) hue = state.lastHue
    saturation = state.lastSaturation
    setColor([hue: hue, saturation: saturation])
}

def setSaturation(saturation) {
    if (saturation == null) saturation = state.lastSaturation
    hue = state.lastHue
    setColor([hue: hue, saturation: saturation])
    
}

def setColor(Map color) {
	if (color == null) {
    	setColor([hue: state.lastHue, saturation: state.lastSaturation])
        return
    }
	def hue = color.hue * 3.6 as int
	def saturation = color.saturation as int
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"ignore_default":1,"on_off":1,"color_temp":0,"hue":${hue},"saturation":${saturation}}}}""", "deviceCommand", "commandResponse")
}

//def poll() {		//	Depreciated.
//	refresh()
//}

def refresh(){
	sendCmdtoServer('{"system":{"get_sysinfo":{}}}', "deviceCommand", "refreshResponse")
	runIn(2, getPower)
	runIn(7, getConsumption)
}

def refreshResponse(cmdResponse){
	def status = cmdResponse.system.get_sysinfo.light_state
	def onOff = status.on_off
	if (onOff == 1) {
		onOff = "on"
	} else {
		onOff = "off"
		status = status.dft_on_state
	}
	def level = status.brightness
	sendEvent(name: "switch", value: onOff)
 	sendEvent(name: "level", value: level)

    switch(state.deviceType) {
    	case "SoftWhite Bulb":
	        log.info "$device.name $device.label: Power: ${onOff} / Brightness: ${level}%"
			break
            
        case "TuneableWhite Bulb":
			def circadianMode = status.mode
			def color_temp = status.color_temp
			sendEvent(name: "circadianMode", value: circadianMode)
			sendEvent(name: "colorTemperature", value: color_temp)
	        state.lastColorTemp = color_temp
			log.info "$device.name $device.label: Power: ${onOff} / Brightness: ${level}% / Circadian Mode: ${circadianMode} / Color Temp: ${color_temp}K"
			break

		default:	//	Color Bulb
			def circadianMode = status.mode
			def color_temp = status.color_temp
			def hue = status.hue
			def saturation = status.saturation
		    def color = [:]
            def scaledHue = status.hue
            if (scaledHue > 0) scaledHue = status.hue / 3.6	// 0...100 scale
		    color << ["hue" : scaledHue]
		    color << ["saturation" : status.saturation]
			sendEvent(name: "circadianMode", value: circadianMode)
			sendEvent(name: "colorTemperature", value: color_temp)
			sendEvent(name: "hue", value: hue)
			sendEvent(name: "saturation", value: saturation)
			sendEvent(name: "color", value: color)
		    if (color_temp.toInteger() == 0) {
		        state.lastHue = scaledHue
				state.lastSaturation = saturation
                sendEvent(name: "colorMode", value: "color" ,descriptionText: descriptionText)
            } else {
		        state.lastColorTemp = color_temp
                sendEvent(name: "colorMode", value: "colorTemperature" ,descriptionText: descriptionText)
		    }
			log.info "$device.name $device.label: Power: ${onOff} / Brightness: ${level}% / Circadian Mode: ${circadianMode} / Color Temp: ${color_temp}K / Color: ${color}"
    }
}

//	===== Get Current Energy Data =====
def getPower(){
	if (state.emon == "EnergyMonitor") {
		sendCmdtoServer("""{"${state.emeterText}":{"get_realtime":{}}}""", "deviceCommand", "energyMeterResponse")
	}
}

def energyMeterResponse(cmdResponse) {
	if (state.emon == "Standard") {
		return
	} else if (cmdResponse[state.emeterText].err_code == -1) {
		log.error "${device.name} ${device.label}: does not support Energy Monitor.  Energy Monitor disabled."
		state.emon == "Standard"
		unschedule(getEnergyStats)
	} else {
		def realtime = cmdResponse[state.emeterText]["get_realtime"]
		if (realtime.power == null) {
			state.powerScale = "power_mw"
			state.energyScale = "energy_wh"
		} else {
			state.powerScale = "power"
			state.energyScale = "energy"
		}
		def powerConsumption = realtime."${state.powerScale}"
		if (state.powerScale == "power_mw") {
			powerConsumption = Math.round(powerConsumption/10) / 100
		} else {
			powerConsumption = Math.round(100*powerConsumption) / 100
		}
		sendEvent(name: "power", value: powerConsumption)
		log.info "$device.name $device.label: Updated CurrentPower to $powerConsumption"
	}
}

//	===== Get Today's Consumption =====
def getConsumption(){
	sendCmdtoServer("""{"${state.emeterText}":{"get_daystat":{"month": ${state.monthToday}, "year": ${state.yearToday}}}}""", "emeterCmd", "useTodayResponse")
}

def useTodayResponse(cmdResponse) {
	def wattHrToday
	def wattHrData
	def dayList = cmdResponse[state.emeterText]["get_daystat"].day_list
	for (int i = 0; i < dayList.size(); i++) {
		wattHrData = dayList[i]
		if(wattHrData.day == state.dayToday) {
			wattHrToday = wattHrData."${state.energyScale}"
 		}
	}
	if (state.powerScale == "power") {
		wattHrToday = Math.round(1000*wattHrToday)
	}
	sendEvent(name: "energy", value: wattHrToday)
	log.info "$device.name $device.label: Updated Usage Today to ${wattHrToday}"
}

//	===== Get Weekly and Monthly Stats =====
def getEnergyStats() {
	if (state.emon != "EnergyMonitor") {
		log.info "${device.name} ${device.label}: does not support Energy Monitor."
		return
	}
	state.monTotEnergy = 0
	state.monTotDays = 0
	state.wkTotEnergy = 0
	state.wkTotDays = 0
	sendCmdtoServer("""{"${state.emeterText}":{"get_daystat":{"month": ${state.monthToday}, "year": ${state.yearToday}}}}""", "emeterCmd", "engrStatsResponse")
	runIn(4, getPrevMonth)
}

def getPrevMonth() {
	def prevMonth = state.monthStart
	if (state.monthToday == state.monthStart) {
		//	If all of the data is in this month, do not request previous month.
		//	This will occur when the current month is 31 days.
		return
	} else if (state.monthToday - 2 == state.monthStart) {
		//	If the start month is 2 less than current, we must handle
		//	the data to get a third month - January.
		state.handleFeb = "yes"
		prevMonth = prevMonth + 1
		runIn(4, getJan)
	}
	sendCmdtoServer("""{"${state.emeterText}":{"get_daystat":{"month": ${prevMonth}, "year": ${state.yearStart}}}}""", "emeterCmd", "engrStatsResponse")
}

def getJan() {
//	Gets January data on March 1 and 2.  Only accessed if current month = 3
//	and start month = 1
	sendCmdtoServer("""{"${state.emeterText}":{"get_daystat":{"month": ${state.monthStart}, "year": ${state.yearStart}}}}""", "emeterCmd", "engrStatsResponse")
}

def engrStatsResponse(cmdResponse) {
/*	
	This method parses up to two energy status messages from the device,
	adding the energy for the previous 30 days and week, ignoring the
	current day.  It then calculates the 30 and 7 day average formatted
	in kiloWattHours to two decimal places.
*/
	def dayList = cmdResponse[state.emeterText]["get_daystat"].day_list
	if (!dayList[0]) {
		log.info "$device.name $device.label: Month has no energy data."
		return
	}
	def monTotEnergy = state.monTotEnergy
	def wkTotEnergy = state.wkTotEnergy
	def monTotDays = state.monTotDays
	def wkTotDays = state.wkTotDays
    def startDay = state.dayStart
	def dataMonth = dayList[0].month
	if (dataMonth == state.monthToday) {
		for (int i = 0; i < dayList.size(); i++) {
			def energyData = dayList[i]
			monTotEnergy += energyData."${state.energyScale}"
			monTotDays += 1
			if (state.dayToday < 8 || energyData.day >= state.weekStart) {
				wkTotEnergy += energyData."${state.energyScale}"
				wkTotDays += 1
			}
			if(energyData.day == state.dayToday) {
				monTotEnergy -= energyData."${state.energyScale}"
				wkTotEnergy -= energyData."${state.energyScale}"
				monTotDays -= 1
				wkTotDays -= 1
			}
		}
	} else if (state.handleFeb == "yes" && dataMonth == 2) {
    	startDay = 1
		for (int i = 0; i < dayList.size(); i++) {
			def energyData = dayList[i]
			if (energyData.day >= startDay) {
				monTotEnergy += energyData."${state.energyScale}"
				monTotDays += 1
			}
			if (energyData.day >= state.weekStart && state.dayToday < 8) {
				wkTotEnergy += energyData."${state.energyScale}"
				wkTotDays += 1
			}
		}
	} else if (state.handleFeb == "yes" && dataMonth == 1) {
		for (int i = 0; i < dayList.size(); i++) {
			def energyData = dayList[i]
			if (energyData.day >= startDay) {
				monTotEnergy += energyData."${state.energyScale}"
				monTotDays += 1
			}
			state.handleFeb = ""
		}
	} else {
		for (int i = 0; i < dayList.size(); i++) {
			def energyData = dayList[i]
			if (energyData.day >= startDay) {
				monTotEnergy += energyData."${state.energyScale}"
				monTotDays += 1
			}
			if (energyData.day >= state.weekStart && state.dayToday < 8) {
				wkTotEnergy += energyData."${state.energyScale}"
				wkTotDays += 1
			}
		}
	}
	state.monTotDays = monTotDays
	state.monTotEnergy = monTotEnergy
	state.wkTotEnergy = wkTotEnergy
	state.wkTotDays = wkTotDays
	log.info "$device.name $device.label: Update 7 and 30 day energy consumption statistics"
    if (monTotDays == 0) {
    	//	Aviod divide by zero on 1st of month
    	monTotDays = 1
        wkTotDays = 1 
	}
	def monAvgEnergy =monTotEnergy/monTotDays
	def wkAvgEnergy = wkTotEnergy/wkTotDays
	if (state.powerScale == "power_mw") {
		monAvgEnergy = Math.round(monAvgEnergy/10)/100
		wkAvgEnergy = Math.round(wkAvgEnergy/10)/100
		monTotEnergy = Math.round(monTotEnergy/10)/100
		wkTotEnergy = Math.round(wkTotEnergy/10)/100
	} else {
		monAvgEnergy = Math.round(100*monAvgEnergy)/100
		wkAvgEnergy = Math.round(100*wkAvgEnergy)/100
		monTotEnergy = Math.round(100*monTotEnergy)/100
		wkTotEnergy = Math.round(100*wkTotEnergy)/100
	}
	sendEvent(name: "monthTotalE", value: monTotEnergy)
	sendEvent(name: "monthAvgE", value: monAvgEnergy)
	sendEvent(name: "weekTotalE", value: wkTotEnergy)
	sendEvent(name: "weekAvgE", value: wkAvgEnergy)
}

def setCurrentDate() {
	sendCmdtoServer('{"smartlife.iot.common.timesetting":{"get_time":null}}', "deviceCommand", "currentDateResponse")
}

def currentDateResponse(cmdResponse) {
	def currDate =  cmdResponse["smartlife.iot.common.timesetting"]["get_time"]
	state.dayToday = currDate.mday.toInteger()
	state.monthToday = currDate.month.toInteger()
	state.yearToday = currDate.year.toInteger()
	def dateToday = Date.parse("yyyy-MM-dd", "${currDate.year}-${currDate.month}-${currDate.mday}")
	def monStartDate = dateToday - 30
	def wkStartDate = dateToday - 7
	state.dayStart = monStartDate[Calendar.DAY_OF_MONTH].toInteger()
	state.monthStart = monStartDate[Calendar.MONTH].toInteger() + 1
	state.yearStart = monStartDate[Calendar.YEAR].toInteger()
	state.weekStart = wkStartDate[Calendar.DAY_OF_MONTH].toInteger()
}

//	===== Send the Command to the Cloud or Bridge =====
private sendCmdtoServer(command, hubCommand, action) {
	try {
		if (state.installType == "Cloud") {
			sendCmdtoCloud(command, hubCommand, action)
		} else {
			sendCmdtoHub(command, hubCommand, action)
		}
	} catch (ex) {
		log.error "Sending Command Exception:", ex
	}
}

private sendCmdtoCloud(command, hubCommand, action){
	def appServerUrl = getDataValue("appServerUrl")
	def deviceId = getDataValue("deviceId")
	def cmdResponse = parent.sendDeviceCmd(appServerUrl, deviceId, command)
	String cmdResp = cmdResponse.toString()
	if (cmdResp.substring(0,5) == "ERROR"){
		def errMsg = cmdResp.substring(7,cmdResp.length())
		log.error "${device.name} ${device.label}: ${errMsg}"
		sendEvent(name: "switch", value: "commsError", descriptionText: errMsg)
		sendEvent(name: "deviceError", value: errMsg)
		sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
		action = ""
	} else {
		sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
		sendEvent(name: "deviceError", value: "OK")
	}
	actionDirector(action, cmdResponse)
}

private sendCmdtoHub(command, hubCommand, action){
	def headers = [:] 
	headers.put("HOST", "$gatewayIP:8082")	//	Same as on Hub.
	headers.put("tplink-iot-ip", deviceIP)
	headers.put("tplink-command", command)
	headers.put("action", action)
	headers.put("command", hubCommand)
	sendHubCommand(new physicalgraph.device.HubAction([
		headers: headers],
		device.deviceNetworkId,
		[callback: hubResponseParse]
	))
}

def hubResponseParse(response) {
	def action = response.headers["action"]
	def cmdResponse = parseJson(response.headers["cmd-response"])
	if (cmdResponse == "TcpTimeout") {
		log.error "$device.name $device.label: Communications Error"
		sendEvent(name: "switch", value: "offline", descriptionText: "ERROR - OffLine in hubResponseParse")
		sendEvent(name: "deviceError", value: "TCP Timeout in Hub")
		sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
	} else {
		sendEvent(name: "deviceError", value: "OK")
		sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
		actionDirector(action, cmdResponse)
	}
}

def actionDirector(action, cmdResponse) {
	switch(action) {
		case "commandResponse":
        	refresh()
			break

		case "refreshResponse":
			refreshResponse(cmdResponse)
			break

		case "energyMeterResponse":
			energyMeterResponse(cmdResponse)
			break
            
		case "useTodayResponse":
			useTodayResponse(cmdResponse)
			break
            
		case "currentDateResponse":
			currentDateResponse(cmdResponse)
			break
            
		case "engrStatsResponse":
			engrStatsResponse(cmdResponse)
			break
            
		default:
			log.info "Interface Error.  See SmartApp and Device error message."
	}
}

//	===== Child / Parent Interchange =====
def syncAppServerUrl(newAppServerUrl) {
	updateDataValue("appServerUrl", newAppServerUrl)
		log.info "Updated appServerUrl for ${device.name} ${device.label}"
}

def setLightTransTime(lightTransTime) {
	state.transTime = lightTransTime
	log.info "Light Transition Time for ${device.name} ${device.label} set to ${state.transTime} miliseconds"
}

def setRefreshRate(refreshRate) {
	switch(refreshRate) {
		case "5":
			runEvery5Minutes(refresh)
			log.info "${device.name} ${device.label} Refresh Scheduled for every 5 minutes"
			break
		case "10":
			runEvery10Minutes(refresh)
			log.info "${device.name} ${device.label} Refresh Scheduled for every 10 minutes"
			break
		case "15":
			runEvery15Minutes(refresh)
			log.info "${device.name} ${device.label} Refresh Scheduled for every 15 minutes"
			break
		default:
			runEvery30Minutes(refresh)
			log.info "${device.name} ${device.label} Refresh Scheduled for every 30 minutes"
	}
}