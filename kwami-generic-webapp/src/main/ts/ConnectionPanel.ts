import { Utils } from "Utils";
import { Panel, PanelType } from "Panel";
import { SchemaPanel } from "SchemaPanel";
import { AjaxClient, JsonResponse, Result } from "AjaxClient";

export class ConnectionPanel extends Panel {
    private ajaxClient: AjaxClient;
    private static url: string;
    private div2: HTMLDivElement;

    private constructor(id: string, heading: string, debug: boolean = false) {
        Utils.debug = AjaxClient.debug = debug;
        super(PanelType.Connect, id, heading, true);
        let x = <HTMLDivElement>document.getElementById("connectInputs");
        this.div2 = <HTMLDivElement>x.cloneNode(true);
        x.remove();
        super.appendChild(this.div2);
        this.div2.onmousedown = (ev: MouseEvent): any => {
            this.div2.parentElement.setAttribute("draggable", "false");
        }
        this.div2.onmouseup = (ev: MouseEvent): any => {
            this.div2.parentElement.setAttribute("draggable", "true");
        }
        let bttn = <HTMLElement>document.querySelector("#Connect #connectBtn");
        bttn.onclick = (ev: MouseEvent) => {
            ev.stopImmediatePropagation();
            this.setUrl();
            let status = <HTMLInputElement>document.querySelector('#connectInputs #status');
            status.value = 'Connecting...';
            status.style.color = 'orange';
            status.style.fontWeight = 'bold';
            AjaxClient.get('tables', ConnectionPanel.setResponse, status);
        }
        this.show();
    }

    public setUrl(): void {
        let input = <HTMLInputElement>document.querySelector("#connectInputs #host");
        let host: string = input.value;
        input = <HTMLInputElement>document.querySelector("#connectInputs #port");
        let port: string = input.value;
        input = <HTMLInputElement>document.querySelector("#connectInputs #context");
        let context: string = input.value;
        input = <HTMLInputElement>document.querySelector("#connectInputs #schema");
        let schema: string = input.value;
        AjaxClient.url = Utils.interpolate("http://{}:{}/{}/{}/", host, port, context, schema);
    }

    public setSqlTemplate(value: string): void {
        Utils.sqlTemplate = value;
    }

    public static setResponse(response: JsonResponse, obj?: Object): void {
        Panel.removePanel(PanelType[PanelType.Schema], true);
        let result: Result = response.results[0];
        let status = <HTMLInputElement>obj;
        status.style.fontWeight = 'bold';
        let connException = <HTMLParagraphElement>document.getElementById("connException");
        connException.innerHTML = '';
        if (result.resultType == 'RESULTSET') {
            Panel.getPanel(PanelType[PanelType.Connect]).hide();
            SchemaPanel.getInstance(response).show();
            status.value = 'OK';
            status.style.color = 'green';
        } else {
            status.value = 'FAILED';
            status.style.color = 'red';
            if (result.resultType == 'EXCEPTION') {
                connException.innerHTML = "Exception: " + result.toString;
            }
        }
    }

    public static getInstance(): ConnectionPanel {
        let x: ConnectionPanel = <ConnectionPanel>Panel.getPanel(PanelType[PanelType.Connect]);
        if (x == null) {
            Panel.nextPanelNumber();
            x = new ConnectionPanel(PanelType[PanelType.Connect], "Connection Panel");
            Panel.savePanel(x);
        }
        return x;
    }
}
