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
var RowPanel = (function (_super) {
    __extends(RowPanel, _super);
    function RowPanel(id, heading) {
        var _this = _super.call(this, PanelType.Row, id, heading) || this;
        _this.grid = document.createElement('div');
        _super.prototype.appendChild.call(_this, _this.grid);
        _this.grid.classList.add('scrollable');
        return _this;
    }
    RowPanel.prototype.addResults = function (columnDefinitions, row) {
        while (this.grid.firstChild)
            this.grid.removeChild(this.grid.firstChild);
        var table = document.createElement('table');
        var tr = document.createElement('tr');
        var th = document.createElement('th');
        var td;
        this.grid.appendChild(table);
        table.appendChild(tr);
        tr.appendChild(th);
        th.innerText = 'Column';
        th = document.createElement('th');
        tr.appendChild(th);
        th.innerText = 'Value';
        row.values.forEach(function (value, i, values) {
            tr = document.createElement('tr');
            table.appendChild(tr);
            td = document.createElement('td');
            tr.appendChild(td);
            td.innerText = columnDefinitions[i].name;
            td = document.createElement('td');
            tr.appendChild(td);
            td.innerText = value;
        });
    };
    return RowPanel;
}(Panel));