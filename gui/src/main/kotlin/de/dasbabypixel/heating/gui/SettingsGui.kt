package de.dasbabypixel.heating.gui

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.timepicker.TimePicker
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.OptionalParameter
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route

@PageTitle("Heizung | Einstellungen")
@Route("settings", layout = NavbarGui::class)
class SettingsGui : Div(), HasUrlParameter<String> {
    private val tabs = Tabs()

    init {
        add(TextField("FirstInput"))
        add(Button("HelloButton"))
        add(NumberField("num"))
        add(TimePicker())
        add(DatePicker())

        add(tabs)
    }

    override fun setParameter(
        event: BeforeEvent,
        @OptionalParameter parameter: String?
    ) {
        if (parameter == null) {
            event.forwardTo(javaClass, "default")
            return
        }
        println("Param: $parameter")
    }
}