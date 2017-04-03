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
        define(["require", "exports", "Panel", "ResultsDisplay"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Panel_1 = require("Panel");
    var ResultsDisplay_1 = require("ResultsDisplay");
    var ResultPanel = (function (_super) {
        __extends(ResultPanel, _super);
        function ResultPanel(id, heading) {
            var _this = _super.call(this, Panel_1.PanelType.Result, id, heading) || this;
            _this.resultsDisplay = new ResultsDisplay_1.ResultsDisplay(_this);
            return _this;
        }
        ResultPanel.prototype.setStatement = function (stmnt) {
            this.resultsDisplay.setStatement(stmnt);
        };
        ResultPanel.prototype.addResults = function (resp, filter) {
            if (resp === void 0) { resp = null; }
            this.resultsDisplay.addResults(resp, filter);
        };
        ResultPanel.getInstance = function () {
            var headTxt = "Result Panel " + Panel_1.Panel.nextPanelNumber();
            var x = new ResultPanel(Panel_1.PanelType[Panel_1.PanelType.Result], headTxt);
            Panel_1.Panel.savePanel(x);
            return x;
        };
        return ResultPanel;
    }(Panel_1.Panel));
    exports.ResultPanel = ResultPanel;
});
