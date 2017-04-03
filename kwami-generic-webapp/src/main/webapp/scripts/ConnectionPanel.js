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
        define(["require", "exports", "Utils", "Panel", "SchemaPanel"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Utils_1 = require("Utils");
    var Panel_1 = require("Panel");
    var SchemaPanel_1 = require("SchemaPanel");
    var ConnectionPanel = (function (_super) {
        __extends(ConnectionPanel, _super);
        function ConnectionPanel(id, heading, debug) {
            if (debug === void 0) { debug = false; }
            var _this = this;
            Utils_1.Utils.debug = debug;
            _this = _super.call(this, Panel_1.PanelType.Connect, id, heading, true) || this;
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
                ConnectionPanel.ajaxGet('tables', ConnectionPanel.setResponse, status);
            };
            _this.show();
            return _this;
        }
        ConnectionPanel.prototype.setUrl = function () {
            var input = document.querySelector("#connectInputs #host");
            var host = input.value;
            input = document.querySelector("#connectInputs #port");
            var port = input.value;
            input = document.querySelector("#connectInputs #context");
            var context = input.value;
            input = document.querySelector("#connectInputs #schema");
            var schema = input.value;
            ConnectionPanel.url = Utils_1.Utils.interpolate("http://{}:{}/{}/{}/", host, port, context, schema);
        };
        ConnectionPanel.prototype.setSqlTemplate = function (value) {
            Utils_1.Utils.sqlTemplate = value;
        };
        ConnectionPanel.ajaxGet = function (url, acb, obj) {
            ConnectionPanel.ajaxJson("GET", url, acb, obj);
        };
        ConnectionPanel.ajaxPost = function (url, acb, data, obj) {
            ConnectionPanel.ajaxJson("POST", url, acb, obj, data);
        };
        ConnectionPanel.ajaxJson = function (method, url, acb, obj, data) {
            console.log("method:" + method + ", url:" + url + ", data:" + data);
            ConnectionPanel.xmlhttp = new XMLHttpRequest();
            ConnectionPanel.respFn = acb;
            ConnectionPanel.xmlhttp.onreadystatechange = function (ev) {
                ev.stopPropagation();
                if (ConnectionPanel.xmlhttp.readyState == 4 && ConnectionPanel.xmlhttp.status == 200) {
                    var overlay_1 = document.querySelector('#ajaxOverlay');
                    overlay_1.classList.remove('displayOn');
                    overlay_1.classList.add('displayOff');
                    var jsonResponse = JSON.parse(ConnectionPanel.xmlhttp.responseText);
                    if (Utils_1.Utils.debug) {
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
            Panel_1.Panel.getPanel("Connect").setUrl();
            var URL = ConnectionPanel.url + url;
            console.log("URL=" + URL);
            ConnectionPanel.xmlhttp.open(method, URL, true);
            ConnectionPanel.xmlhttp.setRequestHeader("Accept", "application/json");
            var overlay = document.querySelector('#ajaxOverlay');
            overlay.classList.remove('displayOff');
            overlay.classList.add('displayOn');
            if (method == "GET")
                ConnectionPanel.xmlhttp.send();
            else if (method == "POST") {
                ConnectionPanel.xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
                ConnectionPanel.xmlhttp.send(data);
            }
        };
        ConnectionPanel.setResponse = function (response, obj) {
            Panel_1.Panel.removePanel(Panel_1.PanelType[Panel_1.PanelType.Schema], true);
            var result = response.results[0];
            var status = obj;
            status.style.fontWeight = 'bold';
            var connException = document.getElementById("connException");
            connException.innerHTML = '';
            if (result.resultType == 'RESULTSET') {
                ConnectionPanel.tables = response;
                Panel_1.Panel.getPanel(Panel_1.PanelType[Panel_1.PanelType.Connect]).hide();
                SchemaPanel_1.SchemaPanel.getInstance().show();
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
        ConnectionPanel.getInstance = function () {
            var x = Panel_1.Panel.getPanel(Panel_1.PanelType[Panel_1.PanelType.Connect]);
            if (x == null) {
                Panel_1.Panel.nextPanelNumber();
                x = new ConnectionPanel(Panel_1.PanelType[Panel_1.PanelType.Connect], "Connection Panel");
                Panel_1.Panel.savePanel(x);
            }
            return x;
        };
        return ConnectionPanel;
    }(Panel_1.Panel));
    ConnectionPanel.xmlhttp = new XMLHttpRequest();
    ConnectionPanel.respFn = null;
    exports.ConnectionPanel = ConnectionPanel;
});
