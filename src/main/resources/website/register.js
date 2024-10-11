let connected = false

function showConnectionLost() {
    document.getElementById("connection-lost").style.display = "block";
}

function hideConnectionLost() {
    document.getElementById("connection-lost").style.display = "none";
}

export function register(message) {
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
        setTimeout(() => register(message), 1000);
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
        const object = JSON.parse(event.data)
        if (!message(object)) {
            console.log("Ignored message: " + event.data)
        }
    }
    return source
}

/**
 * @param {Date} timestamp
 */
export function timeToDisplay(timestamp) {
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
        if (text.length !== 0) {
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