var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
(function (factory) {
    if (typeof module === "object" && typeof module.exports === "object") {
        var v = factory(require, exports);
        if (v !== undefined) module.exports = v;
    }
    else if (typeof define === "function" && define.amd) {
        define(["require", "exports", "Panel"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Panel_1 = require("Panel");
    var RestConnector = (function (_super) {
        __extends(RestConnector, _super);
        function RestConnector(id, heading) {
            var _this = _super.call(this, Panel_1.PanelType.Connect, id, heading, true) || this;
            var x = document.getElementById("connectInputs");
            _this.div2 = x.cloneNode(true);
            x.remove();
            _super.prototype.appendChild.call(_this, _this.div2);
            _this.div2.onmousedown = function (ev) {
                _this.div2.parentElement.setAttribute("draggable", "false");
            };
            _this.div2.onmouseup = function (ev) {
                _this.div2.parentElement.setAttribute("draggable", "true");
            };
            _this.setUrl();
            var bttn = document.querySelector("#Connect #connectBtn");
            bttn.onclick = function (ev) {
                ev.stopImmediatePropagation();
                var status = document.querySelector('#connectInputs #status');
                status.value = 'Connecting...';
                status.style.color = 'orange';
                status.style.fontWeight = 'bold';
                RestConnector.ajaxGet('tables', RestConnector.setResponse, status);
            };
            return _this;
        }
        RestConnector.prototype.setUrl = function () {
            var input = document.querySelector("#connectInputs #host");
            var host = input.value;
            input = document.querySelector("#connectInputs #port");
            var port = input.value;
            input = document.querySelector("#connectInputs #context");
            var context = input.value;
            input = document.querySelector("#connectInputs #schema");
            var schema = input.value;
            RestConnector.url = Panel_1.app.interpolate("http://{}:{}/{}/{}/", host, port, context, schema);
        };
        RestConnector.ajaxGet = function (url, acb, obj) {
            RestConnector.ajaxJson("GET", url, acb, obj);
        };
        RestConnector.ajaxPost = function (url, acb, data, obj) {
            RestConnector.ajaxJson("POST", url, acb, obj, data);
        };
        RestConnector.ajaxJson = function (method, url, acb, obj, data) {
            console.log("method:" + method + ", url:" + url + ", data:" + data);
            RestConnector.xmlhttp = new XMLHttpRequest();
            RestConnector.respFn = acb;
            RestConnector.xmlhttp.onreadystatechange = function (ev) {
                ev.stopPropagation();
                if (RestConnector.xmlhttp.readyState == 4 && RestConnector.xmlhttp.status == 200) {
                    var overlay_1 = document.querySelector('#ajaxOverlay');
                    overlay_1.classList.remove('displayOn');
                    overlay_1.classList.add('displayOff');
                    var jsonResponse = JSON.parse(RestConnector.xmlhttp.responseText);
                    if (Panel_1.app.getDebug) {
                        if (jsonResponse != null && jsonResponse.results[0] != null) {
                            if (jsonResponse.results[0].updateCount != null)
                                console.log("JSON update count: " + jsonResponse.results[0].updateCount);
                            if (jsonResponse.results[0].columnDefinitions != null)
                                console.log("JSON contained " + jsonResponse.results[0].columnDefinitions.length + " column definitions");
                            if (jsonResponse.results[0].rows != null)
                                console.log("JSON contained " + jsonResponse.results[0].rows.length + " rows");
                        }
                    }
                    RestConnector.respFn(jsonResponse, obj);
                }
            };
            Panel_1.app.getPanel("Connect").setUrl();
            var URL = RestConnector.url + url;
            console.log("URL=" + URL);
            RestConnector.xmlhttp.open(method, URL, true);
            RestConnector.xmlhttp.setRequestHeader("Accept", "application/json");
            var overlay = document.querySelector('#ajaxOverlay');
            overlay.classList.remove('displayOff');
            overlay.classList.add('displayOn');
            if (method == "GET")
                RestConnector.xmlhttp.send();
            else if (method == "POST") {
                RestConnector.xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
                RestConnector.xmlhttp.send(data);
            }
        };
        RestConnector.setResponse = function (response, obj) {
            Panel_1.app.removePanel(Panel_1.PanelType[Panel_1.PanelType.Schema], true);
            var result = response.results[0];
            var status = obj;
            status.style.fontWeight = 'bold';
            var connException = document.getElementById("connException");
            connException.innerHTML = '';
            if (result.resultType == 'RESULTSET') {
                RestConnector.tables = response;
                Panel_1.app.getPanel(Panel_1.PanelType[Panel_1.PanelType.Connect]).hide();
                Panel_1.app.newPanel(Panel_1.PanelType.Schema).show();
                status.value = 'OK';
                status.style.color = 'green';
            }
            else {
                status.value = 'FAILED';
                status.style.color = 'red';
                if (result.resultType == 'EXCEPTION') {
                    connException.innerHTML = "Exception: " + result.toString;
                }
            }
        };
        return RestConnector;
    }(Panel_1.Panel));
    RestConnector.xmlhttp = new XMLHttpRequest();
    RestConnector.respFn = null;
    exports.RestConnector = RestConnector;
});
