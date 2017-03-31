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
var TableClickContext = (function () {
    function TableClickContext(panelId, li) {
        this.panelId = panelId;
        this.li = li;
    }
    return TableClickContext;
}());
var SchemaPanel = (function (_super) {
    __extends(SchemaPanel, _super);
    function SchemaPanel(id, heading) {
        var _this = _super.call(this, PanelType.Schema, id, RestConnector.url, true) || this;
        _this.prepareTreeTemplate();
        _this.div2 = SchemaPanel.treeTemplate.cloneNode(true);
        _this.div2.style.display = 'block';
        _super.prototype.appendChild.call(_this, _this.div2);
        if (RestConnector.tables != null) {
            var result = RestConnector.tables.results[0];
            var ul_1 = document.querySelector('#' + id + ' #tree');
            while (ul_1.firstChild)
                ul_1.removeChild(ul_1.firstChild);
            result.rows.forEach(function (value, index, array) {
                var li = document.createElement("li");
                li.innerHTML = value.values[3] + '=' + value.values[2];
                var attr = document.createAttribute('id');
                attr.value = value.values[2];
                li.attributes.setNamedItem(attr);
                li.classList.add('nsItem');
                li.onclick = function (ev) {
                    RestConnector.ajaxGet("tables/" + value.values[2] + "/metaData", SchemaPanel.processTableMetaData, new TableClickContext(id, li));
                };
                ul_1.appendChild(li);
            });
        }
        return _this;
    }
    SchemaPanel.processTableMetaData = function (metaData, ctx) {
        if (app.getDebug())
            console.log("clicked: " + ctx.li.getAttribute('id'));
        var result = metaData.results[0];
        var panel = app.newPanel(PanelType.Sql);
        panel.setSql(app.getFirstSql(ctx.li.getAttribute('id')));
        if (result.resultType == 'EXCEPTION') {
            panel.setStatement("Exception: " + result.toString);
            panel.addResults();
        }
        else {
            panel.setStatement(ctx.li.getAttribute('id'));
            panel.addResults(metaData, [3, 5, 6, 10]);
        }
        panel.show();
        Menu.hideAllMenus();
    };
    SchemaPanel.prototype.prepareTreeTemplate = function () {
        if (SchemaPanel.treeTemplate == null) {
            SchemaPanel.treeTemplate = document.getElementById("metaTree");
            SchemaPanel.treeTemplate.remove();
        }
    };
    return SchemaPanel;
}(Panel));
SchemaPanel.treeTemplate = null;
