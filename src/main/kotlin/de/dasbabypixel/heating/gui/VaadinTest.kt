package de.dasbabypixel.heating.gui

import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.littemplate.LitTemplate
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.SessionInitEvent
import com.vaadin.flow.server.UIInitEvent
import com.vaadin.flow.server.VaadinRequest
import com.vaadin.flow.server.VaadinServiceInitListener
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import com.vaadin.hilla.BrowserCallable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Theme(variant = Lumo.DARK)
class HeatingConfig : AppShellConfigurator

@Component
class ServiceListener : VaadinServiceInitListener {
    override fun serviceInit(event: ServiceInitEvent) {
        event.source.addUIInitListener { initEvent: UIInitEvent? ->
            LoggerFactory.getLogger(javaClass).info("A new UI has been initialized!")

            val themeCookie = ThemeUtil.getThemeFromRequest()
            println("Theme: $themeCookie")
            if (themeCookie == null) {
                ThemeUtil.setDark()
            } else if(themeCookie == Theme.LIGHT) {

            }
        }
    }
}

@Route("gui")
class VaadinTest : VerticalLayout() {
    init {

        // add(Button("Click me") {
        //     Notification.show("Hello vaadin user")
        // })
        //
        // // A layout with a menu and a content area
        // val mainLayout = HorizontalLayout()
        //
        // // Create a vertical menu
        // val tabs = Tabs()
        // tabs.orientation = VERTICAL
        //
        // // Add the menu items (simplified)
        // tabs.add(
        //     Tab("Hello World"), Tab("Card List"), Tab("About")
        // )
        //
        // // The selection will be displayed here
        // val content = Div()
        // content.add("Content will be here")
        //
        // mainLayout.add(tabs, content)
        // add(mainLayout)
        //
        // add(SettingsGui())

        add(NavbarGui().apply { style.setWidth("100%") })

        // add(Tabs().apply {
        //     setSizeFull()
        //     add(Tab().apply {
        //         add(Text("Test"))
        //     })
        //     add(Tab().apply {
        //         add(Text("Test"))
        //     })
        //
        //     add(Tab().apply {
        //         add(Text("Test"))
        //     })
        //
        // })
    }
}

@Tag("heating-navbar")
@JsModule("./src/navbar_gui.ts")
class NavbarGui : LitTemplate() {
    init {
        println(ThemeUtil.getThemeFromRequest())
        ThemeUtil.detectTheme { println(it) }
    }
}

@Tag("heating-settings")
@NpmPackage(value = "@axa-ch/input-text", version = "4.3.11")
@JsModule("./src/settings_gui.ts")
class SettingsGui : LitTemplate()