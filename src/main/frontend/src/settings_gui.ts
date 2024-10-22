import {html, LitElement} from 'lit';
import {customElement} from "lit/decorators.js"
import '@vaadin/button';
import '@vaadin/text-field';
import '@axa-ch/input-text';

@customElement("heating-settings")
class SettingsGui extends LitElement {
    render() {
        return html`
            <div>
                <vaadin-text-field id="firstInput"></vaadin-text-field>
                <axa-input-text id="secondInput"></axa-input-text>
                <vaadin-button id="helloButton">Click me!</vaadin-button>
            </div>
        `;
    }
}
