import {html, LitElement} from "lit";
import {customElement} from "lit/decorators.js";
import '@vaadin/tabsheet';
import '@vaadin/tabs';
import '@vaadin/button'
import '@vaadin/icon'

@customElement("heating-navbar")
class NavbarGui extends LitElement {
    render() {
        return html`
            <vaadin-app-layout style="--vaadin-app-layout-touch-optimized: true">
                
            </vaadin-app-layout>
            <vaadin-tabs id="htabs">
                <vaadin-tab id="status">Status</vaadin-tab>
                <vaadin-tab id="sensors">Sensoren</vaadin-tab>
                <vaadin-tab id="settings" class="heating_settings">Einstellungen</vaadin-tab>
                <vaadin-button id="theme" theme="icon" aria-label="Theme">
                    <vaadin-icon icon="vaadin:cog"></vaadin-icon>
                </vaadin-button>
            </vaadin-tabs>

            <!--            <vaadin-tabsheet>-->
            <!--                <div slot="suffix">-->
            <!--                    <vaadin-button id="theme" theme="icon" aria-label="Theme">-->
            <!--                        <vaadin-icon icon="vaadin:cog"></vaadin-icon>-->
            <!--                    </vaadin-button>-->
            <!--                </div>-->

            <!--                <div tab="status">-->
            <!--                    This is the Payment tab content-->
            <!--                </div>-->
            <!--                <div tab="sensors">-->
            <!--                    <heating-settings></heating-settings>-->
            <!--                </div>-->
            <!--                <div tab="settings">-->
            <!--                    <heating-settings></heating-settings>-->
            <!--                </div>-->
            <!--            </vaadin-tabsheet>-->
            <!--            <vaadin-button>Test</vaadin-button>-->
        `;
    }
}
