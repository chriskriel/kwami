type AjaxCallback = (response: string, objs: Object[]) => void;

abstract class AjaxClient {
    public static debug: boolean = false;
    public static url: string;
    private static xmlhttp = new XMLHttpRequest();
    private static respFn: AjaxCallback = null;

    public static get(url: string, Ajaxcallback: AjaxCallback, objs: Object[]): void {
        AjaxClient.ajaxJson("GET", url, Ajaxcallback, null, objs);
    }

    public static post(url: string, Ajaxcallback: AjaxCallback, data: string, objs: Object[]): void {
        AjaxClient.ajaxJson("POST", url, Ajaxcallback, data, objs);
    }

    private static ajaxJson(method: string, url: string, Ajaxcallback: AjaxCallback, data: string, objs: Object[]): void {
        console.log("method:" + method + ", url:" + url + ", data:" + data);
        AjaxClient.xmlhttp = new XMLHttpRequest();
        AjaxClient.respFn = Ajaxcallback;
        AjaxClient.xmlhttp.onreadystatechange = (ev: ProgressEvent): void => {
            ev.stopPropagation();
            if (AjaxClient.xmlhttp.readyState == 4 && AjaxClient.xmlhttp.status == 200) {
                let overlay = <HTMLElement>document.querySelector('#ajaxOverlay');
                overlay.classList.remove('displayOn');
                overlay.classList.add('displayOff');
                AjaxClient.respFn(AjaxClient.xmlhttp.responseText, objs);
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
