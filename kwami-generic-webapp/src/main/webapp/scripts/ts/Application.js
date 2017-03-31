var app;
var Application = (function () {
    function Application() {
        this.debug = false;
        this.sqlTemplate = 'select [first 10] * from {} browse access;';
        this.zIndex = 0;
        this.pnlNumber = 0;
        this.panels = [];
        app = this;
        this.newPanel(PanelType.Connect).show();
        this.menu = new Menu();
    }
    Application.prototype.newPanel = function (type, heading) {
        this.pnlNumber++;
        var id = PanelType[type] + String(this.pnlNumber);
        var headTxt;
        var x;
        switch (type) {
            case PanelType.Connect:
                headTxt = heading == null ? "Connection" : heading;
                x = new RestConnector(PanelType[type], headTxt);
                break;
            case PanelType.Row:
                headTxt = heading == null ? "Row Panel " + this.pnlNumber : heading;
                x = new RowPanel(id, headTxt);
                break;
            case PanelType.Schema:
                x = app.getPanel(PanelType[type]);
                if (x != null)
                    return x;
                headTxt = heading == null ? "Schema Panel" : heading;
                x = new SchemaPanel(PanelType[type], headTxt);
                break;
            case PanelType.Sql:
                headTxt = heading == null ? "SQL Panel " + this.pnlNumber : heading;
                x = new SqlPanel(id, headTxt);
                break;
            case PanelType.Result:
                headTxt = heading == null ? "Result Panel " + this.pnlNumber : heading;
                x = new ResultPanel(id, headTxt);
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
