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
        define(["require", "exports", "Panel", "RestConnector", "Menu"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Panel_1 = require("Panel");
    var RestConnector_1 = require("RestConnector");
    var Menu_1 = require("Menu");
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
            var _this = _super.call(this, Panel_1.PanelType.Schema, id, RestConnector_1.RestConnector.url, true) || this;
            _this.prepareTreeTemplate();
            _this.div2 = SchemaPanel.treeTemplate.cloneNode(true);
            _this.div2.style.display = 'block';
            _super.prototype.appendChild.call(_this, _this.div2);
            if (RestConnector_1.RestConnector.tables != null) {
                var result = RestConnector_1.RestConnector.tables.results[0];
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
                        RestConnector_1.RestConnector.ajaxGet("tables/" + value.values[2] + "/metaData", SchemaPanel.processTableMetaData, new TableClickContext(id, li));
                    };
                    ul_1.appendChild(li);
                });
            }
            return _this;
        }
        SchemaPanel.processTableMetaData = function (metaData, ctx) {
            if (Panel_1.app.getDebug())
                console.log("clicked: " + ctx.li.getAttribute('id'));
            var result = metaData.results[0];
            var panel = Panel_1.app.newPanel(Panel_1.PanelType.Sql);
            panel.setSql(Panel_1.app.getFirstSql(ctx.li.getAttribute('id')));
            if (result.resultType == 'EXCEPTION') {
                panel.setStatement("Exception: " + result.toString);
                panel.addResults();
            }
            else {
                panel.setStatement(ctx.li.getAttribute('id'));
                panel.addResults(metaData, [3, 5, 6, 10]);
            }
            panel.show();
            Menu_1.Menu.hideAllMenus();
        };
        SchemaPanel.prototype.prepareTreeTemplate = function () {
            if (SchemaPanel.treeTemplate == null) {
                SchemaPanel.treeTemplate = document.getElementById("metaTree");
                SchemaPanel.treeTemplate.remove();
            }
        };
        return SchemaPanel;
    }(Panel_1.Panel));
    SchemaPanel.treeTemplate = null;
    exports.SchemaPanel = SchemaPanel;
});
