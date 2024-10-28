package de.dasbabypixel.heating.gui

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route

@PageTitle("Heizung | Übersicht")
@Route("overview", layout = NavbarGui::class)
class OverviewGui : Div() {
    init {
        add("Overview")
    }
}
