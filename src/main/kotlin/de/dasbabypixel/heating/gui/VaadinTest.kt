package de.dasbabypixel.heating.gui

import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.littemplate.LitTemplate
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.dom.Style.AlignItems.CENTER
import com.vaadin.flow.dom.Style.AlignSelf.STRETCH
import com.vaadin.flow.dom.Style.Display.FLEX
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.VaadinServiceInitListener
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.LumoIcon
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Theme("heating-theme")
class HeatingConfig : AppShellConfigurator

@Component
class ServiceListener : VaadinServiceInitListener {
    override fun serviceInit(event: ServiceInitEvent) {
        event.source.addUIInitListener { _ ->
            LoggerFactory.getLogger(javaClass).info("A new UI has been initialized!")
        }
    }
}

@Route("")
class VaadinTest : VerticalLayout() {
    init {
        add(NavbarGui().apply { style.setWidth("100%") })
    }
}

@Route("settings")
class SettingsUI : VerticalLayout() {
    init {
        add(NavbarGui().apply { style.setWidth("100%") })
        add(SettingsGui())
    }
}

class NavbarGui : Div() {
    private val theme: Button = Button()
    private val status: Tab = Tab()
    private val sensors: Tab = Tab()
    private val settings: Tab = Tab()
    private val tabs: Tabs = Tabs()

    init {
        theme.element.setAttribute("onClick", "heating_toggleTheme()")
        theme.icon = LumoIcon.COG.create()

        status.add("Status")
        sensors.add("Sensors")
        settings.add("Settings")
        settings.addClassName("heating_settings")
        tabs.add(status)
        tabs.add(sensors)
        tabs.add(settings)
        add(Div().apply {
            style.setFlexGrow("1")
            style.setFlexShrink("1")
            style.setFlexBasis("1")
            style.setAlignSelf(STRETCH)
            add(tabs)
        })
        add(theme)
        style.setDisplay(FLEX)
        style.setAlignItems(CENTER)
        style.setFlexGrow("1")
    }
}

@Route("test")
class AppLayoutBottomNavbar : AppLayout() {
    init {
        addToNavbar(true, NavbarGui().apply { style.setWidth("100%") })

        setContent(SettingsGui())
    }
}

@Tag("heating-settings")
@NpmPackage(value = "@axa-ch/input-text", version = "4.3.11")
@JsModule("./src/settings_gui.ts")
@JsModule("./src/auto_theme.ts")
class SettingsGui : LitTemplate()