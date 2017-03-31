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
        element.innerHTML = app.getPanel(PanelType[PanelType.Connect]).getHeading();
        Menu.showMenu(ev, 'bodyMenu');
    };
    Menu.showMenu = function (ev, menuName) {
        Menu.hideAllMenus();
        var menu = document.getElementById(menuName);
        menu.style.top = (ev.clientY - 15) + 'px';
        menu.style.left = ev.clientX + 'px';
        menu.style.display = 'block';
        menu.style.zIndex = app.newZindex();
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
            case PanelType[PanelType.Connect]:
                app.showPanel(PanelType[PanelType.Connect]);
                Menu.hideAllMenus();
                break;
            case PanelType[PanelType.Schema]:
                app.newPanel(PanelType.Schema).show();
                Menu.hideAllMenus();
                break;
            case PanelType[PanelType.Sql]:
                app.newPanel(PanelType.Sql).show();
                Menu.hideAllMenus();
                break;
            case "sqls":
                Menu.showPanelListMenu(ev, PanelType.Sql, "SQL Panels");
                break;
            case "rows":
                Menu.showPanelListMenu(ev, PanelType.Row, "Row Panels");
                break;
            case "results":
                Menu.showPanelListMenu(ev, PanelType.Result, "Result Panels");
                break;
            default:
                var liAttrs = ev.target.attributes;
                var dataAction = liAttrs.getNamedItem('data-action');
                app.showPanel(dataAction.value);
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
        var panels = app.getPanels();
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
