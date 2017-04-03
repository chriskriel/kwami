(function (factory) {
    if (typeof module === "object" && typeof module.exports === "object") {
        var v = factory(require, exports);
        if (v !== undefined) module.exports = v;
    }
    else if (typeof define === "function" && define.amd) {
        define(["require", "exports", "Panel", "SchemaPanel", "SqlPanel"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Panel_1 = require("Panel");
    var SchemaPanel_1 = require("SchemaPanel");
    var SqlPanel_1 = require("SqlPanel");
    var Menu = (function () {
        function Menu() {
            document.addEventListener("contextmenu", Menu.showContextMenu, false);
            document.addEventListener("click", Menu.hideAllMenus, false);
            var menuItems = document.querySelectorAll('#bodyMenu li');
            for (var i = 1; i < menuItems.length; i++)
                menuItems.item(i).addEventListener('click', Menu.itemClick, false);
        }
        Menu.showContextMenu = function (ev) {
            ev.preventDefault();
            var menu = document.getElementById('bodyMenu');
            var element = document.getElementById('menuCnnct');
            element.innerHTML = Panel_1.Panel.getPanel(Panel_1.PanelType[Panel_1.PanelType.Connect]).getHeading();
            Menu.showMenu(ev, 'bodyMenu');
        };
        Menu.showMenu = function (ev, menuName) {
            Menu.hideAllMenus();
            var menu = document.getElementById(menuName);
            menu.style.top = (ev.clientY - 15) + 'px';
            menu.style.left = ev.clientX + 'px';
            menu.style.display = 'block';
            menu.style.zIndex = Panel_1.Panel.newZindex();
        };
        Menu.hideAllMenus = function () {
            var menus = document.querySelectorAll('.menu');
            for (var i = 0; i < menus.length; i++)
                menus.item(i).style.display = 'none';
        };
        Menu.itemClick = function (ev) {
            ev.stopPropagation();
            Menu.hideAllMenus();
            var target = ev.target;
            switch (target.getAttribute("data-action")) {
                case Panel_1.PanelType[Panel_1.PanelType.Connect]:
                    Panel_1.Panel.showPanel(Panel_1.PanelType[Panel_1.PanelType.Connect]);
                    Menu.hideAllMenus();
                    break;
                case Panel_1.PanelType[Panel_1.PanelType.Schema]:
                    SchemaPanel_1.SchemaPanel.getInstance().show();
                    Menu.hideAllMenus();
                    break;
                case Panel_1.PanelType[Panel_1.PanelType.Sql]:
                    SqlPanel_1.SqlPanel.getInstance().show();
                    Menu.hideAllMenus();
                    break;
                case "sqls":
                    Menu.showPanelListMenu(ev, Panel_1.PanelType.Sql, "SQL Panels");
                    break;
                case "rows":
                    Menu.showPanelListMenu(ev, Panel_1.PanelType.Row, "Row Panels");
                    break;
                case "results":
                    Menu.showPanelListMenu(ev, Panel_1.PanelType.Result, "Result Panels");
                    break;
                default:
                    var liAttrs = ev.target.attributes;
                    var dataAction = liAttrs.getNamedItem('data-action');
                    Panel_1.Panel.showPanel(dataAction.value);
            }
        };
        Menu.showPanelListMenu = function (ev, panelType, menuHeading) {
            var ul = document.getElementById('panelListMenu');
            while (ul.firstChild)
                ul.removeChild(ul.firstChild);
            var li = document.createElement('li');
            li.classList.add('menuHeading');
            li.innerHTML = menuHeading;
            ul.appendChild(li);
            var panels = Panel_1.Panel.getPanels();
            panels.forEach(function (panel, index, array) {
                if (panel.getType() === panelType) {
                    li = document.createElement('li');
                    li.innerHTML = panel.getHeading();
                    var dataAction = document.createAttribute('data-action');
                    dataAction.value = panel.getId();
                    li.attributes.setNamedItem(dataAction);
                    li.addEventListener('click', Menu.itemClick, false);
                    ul.appendChild(li);
                }
            });
            if (ul.childElementCount > 1)
                Menu.showMenu(ev, 'panelListMenu');
        };
        return Menu;
    }());
    exports.Menu = Menu;
});
