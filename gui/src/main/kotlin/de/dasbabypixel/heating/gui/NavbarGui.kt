package de.dasbabypixel.heating.gui

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.dom.Style
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.HighlightAction
import com.vaadin.flow.router.HighlightConditions
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.vaadin.flow.theme.lumo.LumoIcon

@AnonymousAllowed
@PageTitle("Heizung")
@Route("")
@JsModule("./src/auto_theme.ts")
class NavbarGui : AppLayout(),
    RouterLayout,
    BeforeEnterObserver {
    private val theme: Button = Button()

    init {
        val navbarDiv = Div()

        theme.element.setAttribute("onClick", "heating_toggleTheme()")
        theme.icon = LumoIcon.COG.create()
        val tabs = RouteTabs()
        tabs.add(RouterLink("Overview", OverviewGui::class.java))
        tabs.add(RouterLink("Status", StatusGui::class.java))
        tabs.add(RouterLink("Sensors", SensorsGui::class.java))
        tabs.add(RouterLink("Settings", SettingsGui::class.java)).apply { this.addClassName("heating_settings") }
        navbarDiv.add(Div().apply {
            style.setFlexGrow("1")
            style.setFlexShrink("1")
            style.setFlexBasis("1")
            style.setAlignSelf(Style.AlignSelf.STRETCH)
            add(tabs)
        })
        navbarDiv.add(theme)
        navbarDiv.style.setDisplay(Style.Display.FLEX)
        addToNavbar(true, navbarDiv.apply { style.setWidth("100%") })
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        if (event.navigationTarget == NavbarGui::class.java) {
            event.forwardTo(OverviewGui::class.java)
        }
    }

    private class RouteTabs : Tabs(),
        BeforeEnterObserver {
        private val routerLinkTabMap = HashMap<RouterLink, Tab>()

        fun add(routerLink: RouterLink): Tab {
            routerLink.highlightCondition = HighlightConditions.sameLocation()
            routerLink.highlightAction = HighlightAction { _: RouterLink?, shouldHighlight: Boolean ->
                if (shouldHighlight) selectedTab = routerLinkTabMap[routerLink]
            }
            val tab = Tab(routerLink)
            routerLinkTabMap[routerLink] = tab
            add(tab)
            return tab
        }

        override fun beforeEnter(event: BeforeEnterEvent) { // In case no tabs will match
            selectedTab = null
        }
    }
}