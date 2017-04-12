var AjaxClient = (function () {
    function AjaxClient() {
    }
    AjaxClient.get = function (url, Ajaxcallback, objs) {
        AjaxClient.ajaxJson("GET", url, Ajaxcallback, null, objs);
    };
    AjaxClient.post = function (url, Ajaxcallback, data, objs) {
        AjaxClient.ajaxJson("POST", url, Ajaxcallback, data, objs);
    };
    AjaxClient.ajaxJson = function (method, url, Ajaxcallback, data, objs) {
        console.log("method:" + method + ", url:" + url + ", data:" + data);
        AjaxClient.xmlhttp = new XMLHttpRequest();
        AjaxClient.respFn = Ajaxcallback;
        AjaxClient.xmlhttp.onreadystatechange = function (ev) {
            ev.stopPropagation();
            if (AjaxClient.xmlhttp.readyState == 4 && AjaxClient.xmlhttp.status == 200) {
                var overlay_1 = document.querySelector('#ajaxOverlay');
                overlay_1.classList.remove('displayOn');
                overlay_1.classList.add('displayOff');
                AjaxClient.respFn(AjaxClient.xmlhttp.responseText, objs);
            }
        };
        var URL = AjaxClient.url + url;
        console.log("URL=" + URL);
        AjaxClient.xmlhttp.open(method, URL, true);
        AjaxClient.xmlhttp.setRequestHeader("Accept", "application/json");
        var overlay = document.querySelector('#ajaxOverlay');
        overlay.classList.remove('displayOff');
        overlay.classList.add('displayOn');
        if (method == "GET")
            AjaxClient.xmlhttp.send();
        else if (method == "POST") {
            AjaxClient.xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            AjaxClient.xmlhttp.send(data);
        }
    };
    return AjaxClient;
}());
AjaxClient.debug = false;
AjaxClient.xmlhttp = new XMLHttpRequest();
AjaxClient.respFn = null;
