function applyOSAutoTheme() {
    const theme: string = window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
    let t = currentTheme()
    if (t == null) {
        setTheme(theme, false)
    } else {
        setTheme(t, false)
    }
}

function heating_toggleTheme() {
    const theme = currentTheme()
    if (theme == null) {
        const theme: string = window.matchMedia("(prefers-color-scheme: dark)").matches ? "light" : "dark";
        setTheme(theme)
    } else if (theme == "light") {
        setTheme("dark")
    } else if (theme == "dark") {
        setTheme("light")
    }
}

function setTheme(theme: string, saveCookie: boolean = true) {
    if (saveCookie) {
        const d: Date = new Date()
        d.setDate(d.getDate() + 5 * 365 * 24 * 60 * 60 * 1000)
        let expires: string = "expires=" + d.toUTCString()
        document.cookie = "theme=" + theme + ";" + expires + ";path=/;samesite=strict"
    }
    document.documentElement.setAttribute("theme", theme)
}

function currentTheme(): string | null {
    let ca: string[] = document.cookie.split(";")
    for (let i: number = 0; i < ca.length; i++) {
        let c: string = ca[i]
        while (c.charAt(0) == ' ') c = c.substring(1, c.length)
        if (c.indexOf("theme") == 0) {
            return c.substring("theme=".length, c.length)
        }
    }
    return null
}

window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", () => applyOSAutoTheme());
(window as any).heating_toggleTheme = heating_toggleTheme;
applyOSAutoTheme()
