import {html, LitElement} from "lit";
import {customElement} from "lit/decorators.js";
import '@vaadin/tabsheet';
import '@vaadin/tabs';
import '@vaadin/icon'

@customElement("heating-navbar")
class NavbarGui extends LitElement {
    render() {
        return html`
            <vaadin-tabsheet orientation="horizontal" theme="bordered">
                <vaadin-button id="settings" slot="suffix" theme="icon" aria-label="Einstellungen">
                    <vaadin-icon icon="vaadin:cog"></vaadin-icon>
                </vaadin-button>
                <vaadin-tabs slot="tabs">
                    <vaadin-tab id="states">Status</vaadin-tab>
                    <vaadin-tab id="sensors">SensorenSSS</vaadin-tab>
                </vaadin-tabs>

                <div tab="states">This is the Dashboard tab content</div>
                <div tab="sensors">This is the Payment tab content</div>
            </vaadin-tabsheet>
        `;
    }
}
