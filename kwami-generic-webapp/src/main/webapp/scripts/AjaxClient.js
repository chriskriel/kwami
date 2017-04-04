(function (factory) {
    if (typeof module === "object" && typeof module.exports === "object") {
        var v = factory(require, exports);
        if (v !== undefined) module.exports = v;
    }
    else if (typeof define === "function" && define.amd) {
        define(["require", "exports"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AjaxClient = (function () {
        function AjaxClient() {
        }
        AjaxClient.get = function (url, callback, obj) {
            AjaxClient.ajaxJson("GET", url, callback, obj);
        };
        AjaxClient.post = function (url, callback, data, obj) {
            AjaxClient.ajaxJson("POST", url, callback, obj, data);
        };
        AjaxClient.ajaxJson = function (method, url, callback, obj, data) {
            console.log("method:" + method + ", url:" + url + ", data:" + data);
            AjaxClient.xmlhttp = new XMLHttpRequest();
            AjaxClient.respFn = callback;
            AjaxClient.xmlhttp.onreadystatechange = function (ev) {
                ev.stopPropagation();
                if (AjaxClient.xmlhttp.readyState == 4 && AjaxClient.xmlhttp.status == 200) {
                    var overlay_1 = document.querySelector('#ajaxOverlay');
                    overlay_1.classList.remove('displayOn');
                    overlay_1.classList.add('displayOff');
                    var jsonResponse = JSON.parse(AjaxClient.xmlhttp.responseText);
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
    exports.AjaxClient = AjaxClient;
});
