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
        define(["require", "exports", "Utils", "Panel", "SchemaPanel", "AjaxClient"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Utils_1 = require("Utils");
    var Panel_1 = require("Panel");
    var SchemaPanel_1 = require("SchemaPanel");
    var AjaxClient_1 = require("AjaxClient");
    var ConnectionPanel = (function (_super) {
        __extends(ConnectionPanel, _super);
        function ConnectionPanel(id, heading, debug) {
            if (debug === void 0) { debug = false; }
            var _this = this;
            Utils_1.Utils.debug = AjaxClient_1.AjaxClient.debug = debug;
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
            var bttn = document.querySelector("#Connect #connectBtn");
            bttn.onclick = function (ev) {
                ev.stopImmediatePropagation();
                _this.setUrl();
                var status = document.querySelector('#connectInputs #status');
                status.value = 'Connecting...';
                status.style.color = 'orange';
                status.style.fontWeight = 'bold';
                AjaxClient_1.AjaxClient.get('tables', ConnectionPanel.setResponse, status);
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
            AjaxClient_1.AjaxClient.url = Utils_1.Utils.interpolate("http://{}:{}/{}/{}/", host, port, context, schema);
        };
        ConnectionPanel.prototype.setSqlTemplate = function (value) {
            Utils_1.Utils.sqlTemplate = value;
        };
        ConnectionPanel.setResponse = function (response, obj) {
            Panel_1.Panel.removePanel(Panel_1.PanelType[Panel_1.PanelType.Schema], true);
            var result = response.results[0];
            var status = obj;
            status.style.fontWeight = 'bold';
            var connException = document.getElementById("connException");
            connException.innerHTML = '';
            if (result.resultType == 'RESULTSET') {
                Panel_1.Panel.getPanel(Panel_1.PanelType[Panel_1.PanelType.Connect]).hide();
                SchemaPanel_1.SchemaPanel.getInstance(response).show();
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
    exports.ConnectionPanel = ConnectionPanel;
});
