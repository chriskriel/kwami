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
var SqlPanel = (function (_super) {
    __extends(SqlPanel, _super);
    function SqlPanel(id, heading) {
        var _this = _super.call(this, PanelType.Sql, id, heading) || this;
        if (SqlPanel.sqlTemplate == null) {
            SqlPanel.sqlTemplate = document.getElementById("sqlPanel");
            SqlPanel.sqlTemplate.remove();
        }
        _this.div2 = SqlPanel.sqlTemplate.cloneNode(true);
        _this.div2.onmousedown = function (ev) {
            _this.div2.parentElement.setAttribute("draggable", "false");
        };
        _this.div2.onmouseup = function (ev) {
            _this.div2.parentElement.setAttribute("draggable", "true");
        };
        _super.prototype.appendChild.call(_this, _this.div2);
        var selector = app.interpolate('#{} #statement', id);
        _this.sql = document.querySelector(selector);
        selector = app.interpolate('#{} #clear', id);
        var bttn = document.querySelector(selector);
        bttn.onclick = function (ev) {
            ev.stopImmediatePropagation();
            _this.sql.value = '';
        };
        selector = app.interpolate('#{} #exec', id);
        bttn = document.querySelector(selector);
        bttn.onclick = function (ev) {
            ev.stopImmediatePropagation();
            RestConnector.ajaxPost("sql?maxRows=-1", SqlPanel.processResults, "sql=" + _this.sql.value, _this.sql.value);
        };
        _this.resultsDisplay = new ResultsDisplay(_this, "right-click to copy to SQL");
        _this.resultsDisplay.addValueCallback(function (value) {
            var sqlText = _this.sql.value;
            var cursorPos = _this.sql.selectionStart + value.length;
            sqlText = _this.sql.value.substr(0, _this.sql.selectionStart) + value
                + _this.sql.value.substr(_this.sql.selectionEnd);
            _this.sql.focus();
            _this.sql.value = sqlText;
            _this.sql.setSelectionRange(cursorPos, cursorPos);
            console.log("called back with: " + value);
        });
        return _this;
    }
    SqlPanel.prototype.setSql = function (sql) {
        this.sql.value = sql;
    };
    SqlPanel.prototype.setStatement = function (stmnt) {
        this.resultsDisplay.setStatement(stmnt);
    };
    SqlPanel.prototype.addResults = function (resp, filter) {
        if (resp === void 0) { resp = null; }
        this.resultsDisplay.addResults(resp, filter);
    };
    SqlPanel.processResults = function (metaData, sql) {
        console.log("executed: " + sql);
        var panel = app.newPanel(PanelType.Result);
        var result = metaData.results[0];
        if (result.resultType == 'EXCEPTION') {
            panel.setStatement("Exception: " + result.toString);
            panel.addResults();
        }
        else {
            panel.setStatement("Statement: " + sql);
            panel.addResults(metaData);
        }
        panel.show();
        Menu.hideAllMenus();
    };
    return SqlPanel;
}(Panel));
SqlPanel.sqlTemplate = null;
