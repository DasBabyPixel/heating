package de.dasbabypixel.heating.gui

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.server.AppShellSettings
import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.VaadinServiceInitListener
import com.vaadin.flow.theme.Theme
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Theme("heating-theme")
class HeatingConfig : AppShellConfigurator {
    override fun configurePage(settings: AppShellSettings) {
        settings.addFavIcon("icon", "icons/favicon.ico", "16x16");
    }
}

@Component
class ServiceListener : VaadinServiceInitListener {
    override fun serviceInit(event: ServiceInitEvent) {
        event.source.addUIInitListener { _ ->
            LoggerFactory.getLogger(javaClass).info("A new UI has been initialized!")
        }
    }
}
