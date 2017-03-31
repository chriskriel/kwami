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
var ResultPanel = (function (_super) {
    __extends(ResultPanel, _super);
    function ResultPanel(id, heading) {
        var _this = _super.call(this, PanelType.Result, id, heading) || this;
        _this.resultsDisplay = new ResultsDisplay(_this);
        return _this;
    }
    ResultPanel.prototype.setStatement = function (stmnt) {
        this.resultsDisplay.setStatement(stmnt);
    };
    ResultPanel.prototype.addResults = function (resp, filter) {
        if (resp === void 0) { resp = null; }
        this.resultsDisplay.addResults(resp, filter);
    };
    return ResultPanel;
}(Panel));
