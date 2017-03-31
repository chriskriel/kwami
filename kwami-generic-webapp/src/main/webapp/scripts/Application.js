(function (factory) {
    if (typeof module === "object" && typeof module.exports === "object") {
        var v = factory(require, exports);
        if (v !== undefined) module.exports = v;
    }
    else if (typeof define === "function" && define.amd) {
        define(["require", "exports", "Panel", "RestConnector", "RowPanel", "SchemaPanel", "Menu", "SqlPanel", "ResultPanel"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Panel_1 = require("Panel");
    var RestConnector_1 = require("RestConnector");
    var RowPanel_1 = require("RowPanel");
    var SchemaPanel_1 = require("SchemaPanel");
    var Menu_1 = require("Menu");
    var SqlPanel_1 = require("SqlPanel");
    var ResultPanel_1 = require("ResultPanel");
    var Application = (function () {
        function Application() {
            this.debug = false;
            this.sqlTemplate = 'select [first 10] * from {} browse access;';
            this.zIndex = 0;
            this.pnlNumber = 0;
            this.panels = [];
            this.newPanel(Panel_1.PanelType.Connect).show();
            this.menu = new Menu_1.Menu();
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
                    x = this.getPanel(Panel_1.PanelType[type]);
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
        Application.prototype.getFirstSql = function (tableName) {
            return this.interpolate(this.sqlTemplate, tableName);
        };
        Application.prototype.showPanel = function (id) {
            for (var i = 0; i < this.panels.length; i++) {
                var panel = this.panels[i];
                if (panel.getId() === id) {
                    panel.show();
                    return;
                }
            }
        };
        Application.prototype.removePanel = function (id, removeHtml) {
            if (removeHtml === void 0) { removeHtml = false; }
            for (var i = 0; i < this.panels.length; i++) {
                var panel = this.panels[i];
                if (panel.getId() === id) {
                    if (removeHtml && panel.getHtml() != null)
                        panel.getHtml().remove();
                    this.panels.splice(i, 1);
                    return;
                }
            }
        };
        Application.prototype.getPanel = function (id) {
            for (var i = 0; i < this.panels.length; i++) {
                var panel = this.panels[i];
                if (panel.getId() === id) {
                    return panel;
                }
            }
            return null;
        };
        Application.prototype.getPanels = function () {
            return this.panels;
        };
        Application.prototype.interpolate = function (template) {
            var values = [];
            for (var _i = 1; _i < arguments.length; _i++) {
                values[_i - 1] = arguments[_i];
            }
            if (template === undefined || template === null)
                return null;
            var parts = template.split("{}");
            var i = 0;
            var result = parts[0];
            for (var j = 1; j < parts.length; j++)
                result += values[i++] + parts[j];
            return result;
        };
        Application.prototype.newZindex = function () {
            return this.zIndex++ + '';
        };
        Application.prototype.getDebug = function () {
            return this.debug;
        };
        Application.prototype.setDebug = function (debug) {
            this.debug = debug;
        };
        Application.prototype.getSqlTemplate = function () {
            return this.sqlTemplate;
        };
        Application.prototype.setSqlTemplate = function (sql) {
            this.sqlTemplate = sql;
        };
        return Application;
    }());
    exports.Application = Application;
});
