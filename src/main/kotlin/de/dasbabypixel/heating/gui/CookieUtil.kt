package de.dasbabypixel.heating.gui

import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.HasValue.ValueChangeEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.page.Page
import com.vaadin.flow.server.VaadinRequest
import jakarta.servlet.http.Cookie
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object CookieUtil {

    fun setCookie(
        key: String?,
        value: String?
    ) {
        setCookie(key, value, "/")
    }

    fun setCookie(
        key: String?,
        value: String?,
        expirationTime: LocalDateTime
    ) {
        val expires = toCookieGMTDate(expirationTime)
        getPage().executeJs(
            String.format("document.cookie = \"%s=%s; expires=%s\";", key, value, expires)
        )
    }

    private fun getPage(): Page {
        return getUI().page
    }

    private fun getUI(): UI {
        return UI.getCurrent()!!
    }

    private fun toCookieGMTDate(expirationTime: LocalDateTime): String {
        val zdt = ZonedDateTime.of(expirationTime, ZoneOffset.UTC)
        val expires = zdt.format(DateTimeFormatter.RFC_1123_DATE_TIME)
        return expires
    }

    fun setCookie(
        key: String?,
        value: String?,
        path: String?,
        expirationTime: LocalDateTime
    ) {
        val expires = toCookieGMTDate(expirationTime)

        getPage().executeJs(
            String.format(
                "document.cookie = \"%s=%s; path=%s\"; Expires=%s\";", key, value, path, expires
            )
        )
    }

    fun setCookie(
        key: String?,
        value: String?,
        path: String?
    ) {
        getPage().executeJs(
            String.format(
                "document.cookie = \"%s=%s; path=%s\";", key, value, path
            )
        )
    }

    fun getCookieFromRequest(name: String): Cookie? {
        VaadinRequest.getCurrent().cookies.forEach {
            println(it.name + "=" + it.value)
            if (it.name == name) {
                return it
            }
        }
        return null
    }

    fun detectCookieValue(
        key: String,
        callback: Callback
    ) {
        UI.getCurrent().element.executeJs(
            """
                var keyEq = "$key=";var ca = document.cookie.split(";");
                for (var i = 0; i < ca.length; i++) {
                    let c = ca[i];
                    while (c.charAt(0) == " ") c = c.substring(1, c.length);
                    if (c.indexOf(keyEq) == 0) {
                        return c.substring(keyEq.length, c.length);
                    }
                }
                return "";
            """.trimIndent()
        ).then(String::class.java) { cookieValue ->
            callback.onValueDetected(cookieValue)
        }
    }

    /**
     *
     * Binds a HasValue&lt;V&gt; to a cookie that lives for a month. The cookies
     * value is updated via a ValueChangeListener.
     *
     * @param <V> The value-type of the HasValue&lt;&gt;
     * @param <E> the type of the value change event fired by field
     * @param field The HasValue&lt;V&gt; that gets bound.
     * @param name The name of the cookie
     * @param cb A BrowserCookie.Callback that gets called with the actual value
     * of the cookie. The value is guaranteed to be not null.
     *
     * @throws IllegalArgumentException if field or name are null or if name is
     * empty.
    </E></V> */
    fun <E : ValueChangeEvent<V>, V> bindValueToCookie(
        field: HasValue<E, V>,
        name: String,
        cb: Callback
    ) {
        require(name.isEmpty()) { "Name must not be empty" }

        detectCookieValue(name) { v ->
            if (v != null) {
                cb.onValueDetected(v)
            }
        }

        field.addValueChangeListener { event: E ->
            setCookie(name, event.value.toString(), LocalDateTime.now().plusMonths(1L))
        }
    }

    /**
     * Binds a HasValue&lt;String&gt; to a cookie that lives for a month. The
     * cookies value is updated via a ValueChangeListener. Its crrent value is
     * copied into the HasValue&lt;String&gt;.
     *
     * @param field The HasValue&lt;String&gt; that gets bound.
     * @param name The name of the cookie
     *
     * @throws IllegalArgumentException if field or name are null or if name is
     * empty.
     */
    fun bindValueToCookie(
        field: HasValue<out ValueChangeEvent<String>, String>,
        name: String
    ) {
        require(name.isEmpty()) { "Name must not be empty" }

        detectCookieValue(name) { v ->
            if (v != null) {
                field.value = v
            }
        }

        field.addValueChangeListener { event: ValueChangeEvent<String> ->
            setCookie(name, event.value, LocalDateTime.now().plusMonths(1L))
        }
    }

    fun interface Callback {
        fun onValueDetected(value: String?)
    }
}
