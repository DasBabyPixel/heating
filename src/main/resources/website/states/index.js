import {register, timeToDisplay} from "../register.js";

class StateValue {
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
 * @type {!Map<String, StateValue>}
 */
const stateValues = new Map()

/**
 * @param {String} stateName
 * @param {StateValue} stateValue
 */
function updateOrCreate(stateName, stateValue) {
    stateValues.set(stateName, stateValue)
    const elementId = "state-" + stateName
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
        cellName.textContent = stateName
        const cellValue = document.createElement("td")
        cellValue.id = valueId
        cellValue.textContent = stateValue.value
        const cellTimestamp = document.createElement("td")
        cellTimestamp.id = timestampId
        cellTimestamp.textContent = timeToDisplay(stateValue.timestamp)

        tableRow.appendChild(cellName)
        tableRow.appendChild(cellValue)
        tableRow.appendChild(cellTimestamp)

        const table = document.getElementById("state-container-table")
        table.appendChild(tableRow)
    } else {
        valueElement.textContent = stateValue.value
        const timestampElement = document.getElementById(timestampId)

        // update time
        timestampElement.textContent = timeToDisplay(stateValue.timestamp)
    }
}

function updateAll() {
    stateValues.forEach((stateValue, stateName) => {
        updateOrCreate(stateName, stateValue)
    })
}

/**
 * @param {any} object
 * @return {boolean}
 */
function handleMessage(object) {
    const type = object.type
    if (type === "state_value") {
        const stateName = object.stateName
        const value = object.value
        const timestamp = new Date(Date.parse(object.timestamp))
        timestamp.setMilliseconds(500) // To make auto updater more consistent. We don't display millis, so shouldn't make a difference
        const frequency = object.frequency

        const stateValue = new StateValue(value, timestamp)

        updateOrCreate(stateName, stateValue)
        console.log("State Update: " + stateName + " to " + value + " at " + timestamp)
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
