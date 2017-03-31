import { app, Panel, PanelType } from "Panel";

export class Menu {

    public constructor() {
        document.addEventListener("contextmenu", Menu.showContextMenu, false);
        document.addEventListener("click", Menu.hideAllMenus, false);
        let menuItems = document.querySelectorAll('#bodyMenu li');
        for (let i = 1; i < menuItems.length; i++)
            menuItems.item(i).addEventListener('click', Menu.itemClick, false);
    }

    private static showContextMenu(ev: MouseEvent): void {
        ev.preventDefault();
        let menu = document.getElementById('bodyMenu');
        let element = <HTMLElement>document.getElementById('menuCnnct');
        element.innerHTML = app.getPanel(PanelType[PanelType.Connect]).getHeading();
        Menu.showMenu(ev, 'bodyMenu');
    }

    private static showMenu(ev: MouseEvent, menuName: string): void {
        Menu.hideAllMenus();
        let menu = document.getElementById(menuName);
        menu.style.top = (ev.clientY - 15) + 'px';
        menu.style.left = ev.clientX + 'px';
        menu.style.display = 'block';
        menu.style.zIndex = app.newZindex();
    }

    public static hideAllMenus(): void {
        let menus = document.querySelectorAll('.menu');
        for (let i = 0; i < menus.length; i++)
            (<HTMLElement>menus.item(i)).style.display = 'none';
    }

    private static itemClick(ev: MouseEvent): void {
        ev.stopPropagation();
        Menu.hideAllMenus();
        let target = <HTMLUListElement>ev.target;
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
                let liAttrs = (<Element>ev.target).attributes;
                let dataAction: Attr = liAttrs.getNamedItem('data-action');
                app.showPanel(dataAction.value);
        }
    }

    private static showPanelListMenu(ev: MouseEvent, panelType: PanelType,
        menuHeading: string) {
        let ul: HTMLUListElement = <HTMLUListElement>document.getElementById('panelListMenu');
        while (ul.firstChild)
            ul.removeChild(ul.firstChild);
        let li: HTMLLIElement = document.createElement('li');
        li.classList.add('menuHeading');
        li.innerHTML = menuHeading;
        ul.appendChild(li);
        let panels: Panel[] = app.getPanels();
        panels.forEach((panel: Panel, index: number, array: Panel[]) => {
            if (panel.getType() === panelType) {
                li = document.createElement('li');
                li.innerHTML = panel.getHeading();
                let dataAction: Attr = document.createAttribute('data-action');
                dataAction.value = panel.getId();
                li.attributes.setNamedItem(dataAction);
                li.addEventListener('click', Menu.itemClick, false);
                ul.appendChild(li);
            }
        });
        if (ul.childElementCount > 1)
            Menu.showMenu(ev, 'panelListMenu');
    }
}
