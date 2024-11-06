package de.dasbabypixel.heating.gui

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.dom.Style.AlignItems
import com.vaadin.flow.dom.Style.AlignSelf.CENTER
import com.vaadin.flow.dom.Style.Display.FLEX
import com.vaadin.flow.dom.Style.JustifyContent
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.ErrorParameter
import com.vaadin.flow.router.HasErrorParameter
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.router.ParentLayout
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.server.auth.AnonymousAllowed
import de.dasbabypixel.heating.gui.CustomNotFound.NotFoundLayout
import jakarta.servlet.http.HttpServletResponse

@AnonymousAllowed
@ParentLayout(NotFoundLayout::class)
@JsModule("./src/auto_theme.ts")
class CustomNotFound : Div(),
    HasErrorParameter<NotFoundException> {
    override fun setErrorParameter(
        event: BeforeEnterEvent,
        parameter: ErrorParameter<NotFoundException>
    ): Int {
        add(Div("404 not found").apply {
            style.setFontSize("max(min(10vw, 40vh), 1em)")
        })
        style.setDisplay(FLEX)
        style.setHeight("100%")
        style.setJustifyContent(JustifyContent.CENTER)
        style.setAlignItems(AlignItems.CENTER)
        return HttpServletResponse.SC_NOT_FOUND
    }

    @AnonymousAllowed
    class NotFoundLayout : AppLayout(),
        RouterLayout {
        init {
            val div = Div()
            div.add(Button("Home").apply {
                this.addClickListener {
                    this.ui.ifPresent {
                        it.navigate(OverviewGui::class.java)
                    }
                }
                style.setAlignSelf(CENTER)
                style.setDisplay(FLEX)
            })
            addToNavbar(true, div.apply {
                style.setWidth("100%")
            })
        }
    }
}