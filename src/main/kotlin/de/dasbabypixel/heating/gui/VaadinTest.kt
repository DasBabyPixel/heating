package de.dasbabypixel.heating.gui

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.tabs.Tabs.Orientation.VERTICAL
import com.vaadin.flow.router.Route

@Route("gui")
class VaadinTest : VerticalLayout() {
    init {
        add(Button("Click me") {
            Notification.show("Hello vaadin user")
        })

        // A layout with a menu and a content area
        val mainLayout = HorizontalLayout()

        // Create a vertical menu
        val tabs = Tabs()
        tabs.orientation = VERTICAL

        // Add the menu items (simplified)
        tabs.add(
            Tab("Hello World"), Tab("Card List"), Tab("About")
        )

        // The selection will be displayed here
        val content = Div()
        content.add("Content will be here")

        mainLayout.add(tabs, content)
        add(mainLayout)
    }
}