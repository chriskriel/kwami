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
        define(["require", "exports", "Panel", "ConnectionPanel", "SqlPanel", "Menu", "Utils"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Panel_1 = require("Panel");
    var ConnectionPanel_1 = require("ConnectionPanel");
    var SqlPanel_1 = require("SqlPanel");
    var Menu_1 = require("Menu");
    var Utils_1 = require("Utils");
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
            var _this = _super.call(this, Panel_1.PanelType.Schema, id, ConnectionPanel_1.ConnectionPanel.url, true) || this;
            _this.prepareTreeTemplate();
            _this.div2 = SchemaPanel.treeTemplate.cloneNode(true);
            _this.div2.style.display = 'block';
            _super.prototype.appendChild.call(_this, _this.div2);
            if (ConnectionPanel_1.ConnectionPanel.tables != null) {
                var result = ConnectionPanel_1.ConnectionPanel.tables.results[0];
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
                        ConnectionPanel_1.ConnectionPanel.ajaxGet("tables/" + value.values[2] + "/metaData", SchemaPanel.processTableMetaData, new TableClickContext(id, li));
                    };
                    ul_1.appendChild(li);
                });
            }
            return _this;
        }
        SchemaPanel.processTableMetaData = function (metaData, ctx) {
            if (Utils_1.Utils.debug)
                console.log("clicked: " + ctx.li.getAttribute('id'));
            var result = metaData.results[0];
            var panel = SqlPanel_1.SqlPanel.getInstance();
            panel.setSql(Utils_1.Utils.getFirstSql(ctx.li.getAttribute('id')));
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
        SchemaPanel.getInstance = function () {
            var x = Panel_1.Panel.getPanel(Panel_1.PanelType[Panel_1.PanelType.Schema]);
            if (x == null) {
                Panel_1.Panel.nextPanelNumber();
                x = new SchemaPanel(Panel_1.PanelType[Panel_1.PanelType.Schema], "Schema Panel");
                Panel_1.Panel.savePanel(x);
            }
            return x;
        };
        return SchemaPanel;
    }(Panel_1.Panel));
    SchemaPanel.treeTemplate = null;
    exports.SchemaPanel = SchemaPanel;
});
