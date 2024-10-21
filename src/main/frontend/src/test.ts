import { html, LitElement } from 'lit';
import '@vaadin/button';
import '@vaadin/text-field';
import '@axa-ch/input-text';

class HelloWorld extends LitElement {
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

customElements.define('hello-world', HelloWorld);