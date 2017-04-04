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

type ResponseCallback = (response: JsonResponse, obj?: Object) => void;

export abstract class AjaxClient {
    public static debug: boolean = false;
    public static url: string;
    private static xmlhttp = new XMLHttpRequest();
    private static respFn: ResponseCallback = null;

    public static get(url: string, callback: ResponseCallback, obj?: Object): void {
        AjaxClient.ajaxJson("GET", url, callback, obj);
    }

    public static post(url: string, callback: ResponseCallback, data: string, obj?: Object): void {
        AjaxClient.ajaxJson("POST", url, callback, obj, data);
    }

    private static ajaxJson(method: string, url: string, callback: ResponseCallback, obj?: Object, data?: string): void {
        console.log("method:" + method + ", url:" + url + ", data:" + data);
        AjaxClient.xmlhttp = new XMLHttpRequest();
        AjaxClient.respFn = callback;
        AjaxClient.xmlhttp.onreadystatechange = (ev: ProgressEvent): void => {
            ev.stopPropagation();
            if (AjaxClient.xmlhttp.readyState == 4 && AjaxClient.xmlhttp.status == 200) {
                let overlay = <HTMLElement>document.querySelector('#ajaxOverlay');
                overlay.classList.remove('displayOn');
                overlay.classList.add('displayOff');
                let jsonResponse: JsonResponse = JSON.parse(AjaxClient.xmlhttp.responseText);
                if (AjaxClient.debug) {
                    if (jsonResponse != null && jsonResponse.results[0] != null) {
                        if (jsonResponse.results[0].updateCount != null)
                            console.log("JSON update count: " + jsonResponse.results[0].updateCount);
                        if (jsonResponse.results[0].columnDefinitions != null)
                            console.log("JSON contained " + jsonResponse.results[0].columnDefinitions.length + " column definitions");
                        if (jsonResponse.results[0].rows != null)
                            console.log("JSON contained " + jsonResponse.results[0].rows.length + " rows");
                    }
                }
                AjaxClient.respFn(jsonResponse, obj);
            }
        };
        let URL: string = AjaxClient.url + url;
        console.log("URL=" + URL);
        AjaxClient.xmlhttp.open(method, URL, true);
        AjaxClient.xmlhttp.setRequestHeader("Accept", "application/json");
        let overlay = <HTMLElement>document.querySelector('#ajaxOverlay');
        overlay.classList.remove('displayOff');
        overlay.classList.add('displayOn');
        if (method == "GET")
            AjaxClient.xmlhttp.send();
        else if (method == "POST") {
            AjaxClient.xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            AjaxClient.xmlhttp.send(data);
        }
    }
}
