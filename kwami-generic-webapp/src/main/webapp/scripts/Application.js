(function (factory) {
    if (typeof module === "object" && typeof module.exports === "object") {
        var v = factory(require, exports);
        if (v !== undefined) module.exports = v;
    }
    else if (typeof define === "function" && define.amd) {
        define(["require", "exports", "Panel", "RestConnector", "RowPanel", "SchemaPanel", "SqlPanel", "ResultPanel"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Panel_1 = require("Panel");
    var RestConnector_1 = require("RestConnector");
    var RowPanel_1 = require("RowPanel");
    var SchemaPanel_1 = require("SchemaPanel");
    var SqlPanel_1 = require("SqlPanel");
    var ResultPanel_1 = require("ResultPanel");
    var Application = (function () {
        function Application() {
            this.zIndex = 0;
            this.pnlNumber = 0;
            this.panels = [];
            this.newPanel(Panel_1.PanelType.Connect).show();
        }
        Application.prototype.newPanel = function (type, heading) {
            this.pnlNumber++;
            var id = Panel_1.PanelType[type] + String(this.pnlNumber);
            var headTxt;
            var x;
            switch (type) {
                case Panel_1.PanelType.Connect:
                    headTxt = heading == null ? "Connection" : heading;
                    x = new RestConnector_1.RestConnector(Panel_1.PanelType[type], headTxt);
                    break;
                case Panel_1.PanelType.Row:
                    headTxt = heading == null ? "Row Panel " + this.pnlNumber : heading;
                    x = new RowPanel_1.RowPanel(id, headTxt);
                    break;
                case Panel_1.PanelType.Schema:
                    x = Panel_1.Panel.getPanel(Panel_1.PanelType[type]);
                    if (x != null)
                        return x;
                    headTxt = heading == null ? "Schema Panel" : heading;
                    x = new SchemaPanel_1.SchemaPanel(Panel_1.PanelType[type], headTxt);
                    break;
                case Panel_1.PanelType.Sql:
                    headTxt = heading == null ? "SQL Panel " + this.pnlNumber : heading;
                    x = new SqlPanel_1.SqlPanel(id, headTxt);
                    break;
                case Panel_1.PanelType.Result:
                    headTxt = heading == null ? "Result Panel " + this.pnlNumber : heading;
                    x = new ResultPanel_1.ResultPanel(id, headTxt);
                    break;
            }
            this.panels.push(x);
            return x;
        };
        return Application;
    }());
    exports.Application = Application;
});
