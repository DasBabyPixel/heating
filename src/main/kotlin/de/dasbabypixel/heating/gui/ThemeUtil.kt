package de.dasbabypixel.heating.gui

import com.vaadin.flow.theme.lumo.Lumo

enum class Theme(val lumo: String) {
    LIGHT(Lumo.LIGHT),
    DARK(Lumo.DARK);

    companion object {
        val DEFAULT = DARK

        fun find(theme: String?): Theme {
            if (theme == null) return DEFAULT
            return entries.firstOrNull { it.lumo == theme } ?: DEFAULT
        }
    }
}

object ThemeUtil {
    fun setLight() = setTheme(Theme.LIGHT)
    fun setDark() = setTheme(Theme.DARK)

    private fun setTheme(theme: Theme) = CookieUtil.setCookie("theme", theme.lumo)

    fun getThemeFromRequest(): Theme? {
        return Theme.find(CookieUtil.getCookieFromRequest("theme")?.value)
    }

    fun detectTheme(callback: ThemeCallback) {
        CookieUtil.detectCookieValue("theme") {
            val theme = Theme.find(it)
            callback.accept(theme)
        }
    }
}

fun interface ThemeCallback {
    fun accept(theme: Theme)
}
