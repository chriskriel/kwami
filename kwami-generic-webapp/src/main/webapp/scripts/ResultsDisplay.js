(function (factory) {
    if (typeof module === "object" && typeof module.exports === "object") {
        var v = factory(require, exports);
        if (v !== undefined) module.exports = v;
    }
    else if (typeof define === "function" && define.amd) {
        define(["require", "exports", "Panel", "Menu"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Panel_1 = require("Panel");
    var Menu_1 = require("Menu");
    var ResultsDisplay = (function () {
        function ResultsDisplay(panel, cellTitle) {
            if (cellTitle === void 0) { cellTitle = null; }
            this.panel = panel;
            this.stmnt = document.createElement('p');
            this.stmnt.setAttribute('id', 'stmnt');
            this.panel.appendChild(this.stmnt);
            this.updateCnt = document.createElement('p');
            this.updateCnt.setAttribute('id', 'updateCnt');
            this.updateCnt.innerHTML = '';
            this.panel.appendChild(this.updateCnt);
            this.grid = document.createElement('div');
            this.grid.classList.add('scrollable');
            this.panel.appendChild(this.grid);
            this.cellTitle = cellTitle;
        }
        ResultsDisplay.prototype.setStatement = function (stmnt) {
            this.stmnt.innerHTML = stmnt;
        };
        ResultsDisplay.prototype.addValueCallback = function (valueCallback) {
            this.valueCallback = valueCallback;
        };
        ResultsDisplay.prototype.addResults = function (resp, filter) {
            var _this = this;
            if (resp === void 0) { resp = null; }
            if (filter === void 0) { filter = null; }
            this.updateCnt.innerHTML = '';
            while (this.grid.firstChild)
                this.grid.removeChild(this.grid.firstChild);
            if (resp == null)
                return;
            var result = resp.results[0];
            if (result.updateCount != undefined) {
                this.updateCnt.innerHTML = 'Update Count: ' + String(result.updateCount);
                return;
            }
            var table = this.appendTable();
            var tr = this.appendRow(table);
            var td;
            this.appendHdrCell(tr, "Row");
            result.columnDefinitions.forEach(function (colDef, i, colDefs) {
                if (filter == null || (filter != null && filter.indexOf(i) >= 0))
                    _this.appendHdrCell(tr, colDef.name);
            });
            var rowPanel;
            result.rows.forEach(function (row, index, rows) {
                tr = _this.createRowHtml(table, row, index, rowPanel, result);
                _this.appendCell(tr, String(index));
                row.values.forEach(function (value, i, values) {
                    if (filter == null || (filter != null && filter.indexOf(i) >= 0)) {
                        td = _this.appendCell(tr);
                        _this.prepareDataCell(td, value);
                    }
                });
            });
        };
        ResultsDisplay.prototype.createRowHtml = function (table, row, index, panel, result) {
            var _this = this;
            var tr = this.appendRow(table);
            tr.onclick = function (ev) {
                ev.stopPropagation();
                var head = _this.panel.getHeading() + ' Row ' + index;
                panel = Panel_1.app.newPanel(Panel_1.PanelType.Row, head);
                panel.addResults(result.columnDefinitions, row);
                panel.show();
                Menu_1.Menu.hideAllMenus();
            };
            return tr;
        };
        ResultsDisplay.prototype.prepareDataCell = function (td, value) {
            td.classList.add('nsItem');
            if (value.length > 32)
                td.innerHTML = value.substr(0, 32) + " ... ";
            else
                td.innerHTML = value;
        };
        ResultsDisplay.prototype.appendTable = function () {
            var _this = this;
            var table = document.createElement('table');
            this.grid.appendChild(table);
            if (this.valueCallback != null) {
                table.oncontextmenu = function (ev) {
                    ev.preventDefault();
                    ev.stopImmediatePropagation();
                    var tdTgt = ev.target;
                    _this.valueCallback(tdTgt.innerHTML);
                };
            }
            return table;
        };
        ResultsDisplay.prototype.appendRow = function (table) {
            var tr = document.createElement('tr');
            table.appendChild(tr);
            return tr;
        };
        ResultsDisplay.prototype.appendCell = function (tr, value) {
            if (value === void 0) { value = null; }
            var td;
            td = document.createElement('td');
            tr.appendChild(td);
            if (value != null)
                td.innerHTML = value;
            if (this.cellTitle != null)
                td.setAttribute('title', this.cellTitle);
            return td;
        };
        ResultsDisplay.prototype.appendHdrCell = function (tr, value) {
            if (value === void 0) { value = null; }
            var th;
            th = document.createElement('th');
            tr.appendChild(th);
            if (value != null)
                th.innerHTML = value;
            return th;
        };
        return ResultsDisplay;
    }());
    exports.ResultsDisplay = ResultsDisplay;
});
