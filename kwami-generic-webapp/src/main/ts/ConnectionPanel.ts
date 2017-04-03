import { Utils } from "Utils";
import { Panel, PanelType } from "Panel";
import { SchemaPanel } from "SchemaPanel";

export interface Row {
    values: string[];
}

export interface ColumnDefinition {
    length: number;
    name: string;
    sqlType: string;
}

export interface Result {
    resultType: string;
    updateCount: number;
    toString: string;
    columnDefinitions: ColumnDefinition[];
    rows: Row[];
}

export interface JsonResponse {
    results: Result[];
}

export class ConnectionPanel extends Panel {
    public static tables: JsonResponse;
    public static url: string;
    private static xmlhttp = new XMLHttpRequest();
    private static respFn: (response: JsonResponse, obj?: Object) => void = null;
    private div2: HTMLDivElement;

    private constructor(id: string, heading: string, debug: boolean = false) {
        Utils.debug = debug;
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
        this.setUrl();
        let bttn = <HTMLElement>document.querySelector("#Connect #connectBtn");
        bttn.onclick = (ev: MouseEvent) => {
            ev.stopImmediatePropagation();
            let status = <HTMLInputElement>document.querySelector('#connectInputs #status');
            status.value = 'Connecting...';
            status.style.color = 'orange';
            status.style.fontWeight = 'bold';
            ConnectionPanel.ajaxGet('tables', ConnectionPanel.setResponse, status);
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
        ConnectionPanel.url = Utils.interpolate("http://{}:{}/{}/{}/", host, port, context, schema);
    }

    public setSqlTemplate(value: string): void {
        Utils.sqlTemplate = value;
    }

    public static ajaxGet(url: string, acb: (json: JsonResponse, obj?: Object) => void,
        obj?: Object): void {
        ConnectionPanel.ajaxJson("GET", url, acb, obj);
    }

    public static ajaxPost(url: string, acb: (json: JsonResponse, obj?: Object) => void,
        data: string, obj?: Object): void {
        ConnectionPanel.ajaxJson("POST", url, acb, obj, data);
    }

    private static ajaxJson(method: string, url: string,
        acb: (json: JsonResponse, obj?: Object) => void,
        obj?: Object, data?: string): void {
        console.log("method:" + method + ", url:" + url + ", data:" + data);
        ConnectionPanel.xmlhttp = new XMLHttpRequest();
        ConnectionPanel.respFn = acb;
        ConnectionPanel.xmlhttp.onreadystatechange = (ev: ProgressEvent): void => {
            ev.stopPropagation();
            if (ConnectionPanel.xmlhttp.readyState == 4 && ConnectionPanel.xmlhttp.status == 200) {
                let overlay = <HTMLElement>document.querySelector('#ajaxOverlay');
                overlay.classList.remove('displayOn');
                overlay.classList.add('displayOff');
                let jsonResponse: JsonResponse = JSON.parse(ConnectionPanel.xmlhttp.responseText);
                if (Utils.debug) {
                    if (jsonResponse != null && jsonResponse.results[0] != null) {
                        if (jsonResponse.results[0].updateCount != null)
                            console.log("JSON update count: " + jsonResponse.results[0].updateCount);
                        if (jsonResponse.results[0].columnDefinitions != null)
                            console.log("JSON contained " + jsonResponse.results[0].columnDefinitions.length + " column definitions");
                        if (jsonResponse.results[0].rows != null)
                            console.log("JSON contained " + jsonResponse.results[0].rows.length + " rows");
                    }
                }
                ConnectionPanel.respFn(jsonResponse, obj);
            }
        };
        (<ConnectionPanel>Panel.getPanel("Connect")).setUrl();
        let URL: string = ConnectionPanel.url + url;
        console.log("URL=" + URL);
        ConnectionPanel.xmlhttp.open(method, URL, true);
        ConnectionPanel.xmlhttp.setRequestHeader("Accept", "application/json");
        let overlay = <HTMLElement>document.querySelector('#ajaxOverlay');
        overlay.classList.remove('displayOff');
        overlay.classList.add('displayOn');
        if (method == "GET")
            ConnectionPanel.xmlhttp.send();
        else if (method == "POST") {
            ConnectionPanel.xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            ConnectionPanel.xmlhttp.send(data);
        }
    }

    public static setResponse(response: JsonResponse, obj?: Object): void {
        Panel.removePanel(PanelType[PanelType.Schema], true);
        let result: Result = response.results[0];
        let status = <HTMLInputElement>obj;
        status.style.fontWeight = 'bold';
        let connException = <HTMLParagraphElement>document.getElementById("connException");
        connException.innerHTML = '';
        if (result.resultType == 'RESULTSET') {
            ConnectionPanel.tables = response;
            Panel.getPanel(PanelType[PanelType.Connect]).hide();
            SchemaPanel.getInstance().show();
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
