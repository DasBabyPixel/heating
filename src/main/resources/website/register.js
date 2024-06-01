let connected = false

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

function showConnectionLost() {
    document.getElementById("connection-lost").style.display = "block";
}
function hideConnectionLost() {
    document.getElementById("connection-lost").style.display = "none";
}

function register() {
    console.log("Connecting...");
    const source = new EventSource("/register");

    // Reconnect if the connection fails
    source.onerror = () => {
        console.log("Disconnected.");
        console.log("State: " + source.readyState);
        connected = false;
        showConnectionLost();
        console.log("Reconnecting...");
        source.close();
        console.trace();
        setTimeout(register, 1);
    }

    source.onopen = () => {
        connected = true
        hideConnectionLost();
    }

    window.onbeforeunload = () => {
        console.log("Closing");
        source.close();
    };

    source.onmessage = (event) => {
        // const state = JSON.parse(event.data);
        const object = JSON.parse(event.data)
        const type = object.type
        if (type == "user_login") {
            const userId = object.userId
            console.log("User Login with ID " + userId)
        } else if (type == "state_value") {
            const stateName = object.stateName
            const value = object.value
            const timestamp = new Date(Date.parse(object.timestamp))
            timestamp.setMilliseconds(500) // To make auto updater more consistent. We don't display millis, so shouldn't make a difference
            const frequency = object.frequency

            const stateValue = new StateValue(value, timestamp)

            updateOrCreate(stateName, stateValue)
            console.log("State Update: " + stateName + " to " + value + " at " + timestamp)
        } else {
            console.log("Unknown Type: " + type)
            console.log("JSON: " + event.data);
        }

        // change web content here
    }
};

function updateAll() {
    stateValues.forEach((stateValue, stateName) => {
        updateOrCreate(stateName, stateValue)
    })
}

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

/**
 * @param {Date} timestamp
 */
function timeToDisplay(timestamp) {
    const totalMillisPassed = Date.now() - timestamp.getTime()
    const totalSecondsPassed = Math.floor(totalMillisPassed / 1000)
    const totalMinutesPassed = Math.floor(totalSecondsPassed / 60)
    const totalHoursPassed = Math.floor(totalMinutesPassed / 60)
    const totalDaysPassed = Math.floor(totalHoursPassed / 24)

    /**
     * @param {String} text
     * @param {String} addition
     */
    const add = function (text, addition) {
        if (text.length != 0) {
            return text + " " + addition
        }
        return addition
    }

    let displayText = ""
    if (totalDaysPassed > 0) {
        displayText = add(displayText, totalDaysPassed + "d")
    }
    if (totalHoursPassed > 0) {
        displayText = add(displayText, totalHoursPassed % 24 + "h")
    }
    if (totalMinutesPassed > 0) {
        displayText = add(displayText, totalMinutesPassed % 60 + "m")
    }
    if (totalSecondsPassed >= 0) {
        displayText = add(displayText, totalSecondsPassed % 60 + "s")
    } else {
        displayText = "In der Zukunft"
    }

    return displayText
}

function startUpdateTimer() {
    const interval = (1000 - new Date().getUTCMilliseconds()) + 5;
    updateAll();
    setTimeout(startUpdateTimer, interval);
}

register();
startUpdateTimer();
