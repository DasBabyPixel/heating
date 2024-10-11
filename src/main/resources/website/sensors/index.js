import {register, timeToDisplay} from "../register.js";

class SensorValue {
    /**
     * @param {String} value
     * @param {Date} timestamp
     */
    constructor(value, timestamp) {
        this.value = value
        this.timestamp = timestamp
    }
}

/**
 * @type {!Map<String, SensorValue>}
 */
const sensorValues = new Map()

/**
 * @param {String} sensorName
 * @param {SensorValue} sensorValue
 */
function updateOrCreate(sensorName, sensorValue) {
    sensorValues.set(sensorName, sensorValue)
    const elementId = "sensor-" + sensorName
    const nameId = elementId + "-name"
    const valueId = elementId + "-value"
    const timestampId = elementId + "-timestamp"

    const valueElement = document.getElementById(valueId)

    if (!valueElement) {
        // Create row
        const tableRow = document.createElement("tr")
        tableRow.id = elementId
        const cellName = document.createElement("td")
        cellName.id = nameId
        cellName.textContent = sensorName
        const cellValue = document.createElement("td")
        cellValue.id = valueId
        cellValue.textContent = sensorValue.value
        const cellTimestamp = document.createElement("td")
        cellTimestamp.id = timestampId
        cellTimestamp.textContent = timeToDisplay(sensorValue.timestamp)

        tableRow.appendChild(cellName)
        tableRow.appendChild(cellValue)
        tableRow.appendChild(cellTimestamp)

        const table = document.getElementById("sensor-container-table")
        table.appendChild(tableRow)
    } else {
        valueElement.textContent = sensorValue.value
        const timestampElement = document.getElementById(timestampId)

        // update time
        timestampElement.textContent = timeToDisplay(sensorValue.timestamp)
    }
}

function updateAll() {
    sensorValues.forEach((stateValue, stateName) => {
        updateOrCreate(stateName, stateValue)
    })
}

/**
 * @param {any} object
 * @return {boolean}
 */
function handleMessage(object) {
    const type = object.type
    if (type === "sensor_value") {
        const stateName = object.stateName
        const value = object.value
        const timestamp = new Date(Date.parse(object.timestamp))
        timestamp.setMilliseconds(500) // To make auto updater more consistent. We don't display millis, so shouldn't make a difference
        const frequency = object.frequency

        const stateValue = new SensorValue(value, timestamp)

        updateOrCreate(stateName, stateValue)
        console.log("Sensor Update: " + stateName + " to " + value + " at " + timestamp)
        return true
    }
    return false
}

function startUpdateTimer() {
    const interval = (1000 - new Date().getUTCMilliseconds()) + 5;
    updateAll();
    setTimeout(startUpdateTimer, interval);
}


register(handleMessage)
startUpdateTimer()
