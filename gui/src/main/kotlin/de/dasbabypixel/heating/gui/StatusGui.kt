package de.dasbabypixel.heating.gui

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed

@RolesAllowed("USER")
@PageTitle("Heizung | Status")
@Route("status", layout = NavbarGui::class)
class StatusGui : Div() {
    init {
        add("Status...")
    }
}