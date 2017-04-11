class AjaxClient {
    static get(url, Ajaxcallback, objs) {
        AjaxClient.ajaxJson("GET", url, Ajaxcallback, null, objs);
    }
    static post(url, Ajaxcallback, data, objs) {
        AjaxClient.ajaxJson("POST", url, Ajaxcallback, data, objs);
    }
    static ajaxJson(method, url, Ajaxcallback, data, objs) {
        console.log("method:" + method + ", url:" + url + ", data:" + data);
        AjaxClient.xmlhttp = new XMLHttpRequest();
        AjaxClient.respFn = Ajaxcallback;
        AjaxClient.xmlhttp.onreadystatechange = (ev) => {
            ev.stopPropagation();
            if (AjaxClient.xmlhttp.readyState == 4 && AjaxClient.xmlhttp.status == 200) {
                let overlay = document.querySelector('#ajaxOverlay');
                overlay.classList.remove('displayOn');
                overlay.classList.add('displayOff');
                AjaxClient.respFn(AjaxClient.xmlhttp.responseText, objs);
            }
        };
        let URL = AjaxClient.url + url;
        console.log("URL=" + URL);
        AjaxClient.xmlhttp.open(method, URL, true);
        AjaxClient.xmlhttp.setRequestHeader("Accept", "application/json");
        let overlay = document.querySelector('#ajaxOverlay');
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
AjaxClient.debug = false;
AjaxClient.xmlhttp = new XMLHttpRequest();
AjaxClient.respFn = null;
