package de.dasbabypixel.heating.gui

import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed
import com.vaadin.flow.dom.Style.AlignItems.CENTER as ACENTER
import com.vaadin.flow.dom.Style.JustifyContent.CENTER as JCENTER

@Route("login")
@PageTitle("Heizung | Login")
@AnonymousAllowed
class LoginGui : VerticalLayout(),
    BeforeEnterObserver {
    val login = LoginForm()

    init {
        addClassName("login-view")
        setSizeFull()
        // style.setFlexGrow("1")
        style.setAlignItems(ACENTER)
        style.setJustifyContent(JCENTER)
        login.action = "login"

        add(H1("Heizung"), login)
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        if (event.location.queryParameters.parameters.containsKey("error")) {
            login.isError = true
        }
    }
}