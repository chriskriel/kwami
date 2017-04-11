var PanelType;
(function (PanelType) {
    PanelType[PanelType["Schema"] = 0] = "Schema";
    PanelType[PanelType["Connect"] = 1] = "Connect";
    PanelType[PanelType["Result"] = 2] = "Result";
    PanelType[PanelType["Row"] = 3] = "Row";
    PanelType[PanelType["Sql"] = 4] = "Sql";
})(PanelType || (PanelType = {}));
class Panel {
    constructor(type, id, heading, notCloseable) {
        this.prepareTemplate();
        this.type = type;
        this.heading = heading;
        this.div = Panel.template.cloneNode(true);
        this.body = document.getElementById("body");
        this.body.appendChild(this.div);
        this.div.id = id;
        this.div.style.display = 'none';
        this.prepareButtons(id, notCloseable);
        let h3 = this.prepareHeading(id);
        h3.onclick = (ev) => {
            ev.stopPropagation();
            HeadingUpdater.show(this.div.id);
        };
        this.setupDragAndDrop();
    }
    getHtml() {
        return this.div;
    }
    prepareTemplate() {
        if (Panel.template != null)
            return;
        Panel.template = document.getElementById("panel");
        Panel.template.remove();
    }
    getHeading() {
        return this.heading;
    }
    setHeading(heading) {
        this.heading = heading;
        this.prepareHeading(this.div.id);
    }
    prepareHeading(parentId) {
        let s = `#${parentId} #panelHeading`;
        let h3 = document.querySelector(s);
        h3.innerHTML = this.heading;
        h3.setAttribute('title', 'click to rename');
        return h3;
    }
    getType() {
        return this.type;
    }
    appendChild(child) {
        this.div.appendChild(child);
    }
    static newZindex() {
        return Panel.zIndex++ + '';
    }
    show() {
        this.div.style.display = 'block';
        this.div.style.zIndex = Panel.newZindex();
    }
    static showPanel(id) {
        for (let i = 0; i < Panel.panels.length; i++) {
            let panel = Panel.panels[i];
            if (panel.getId() === id) {
                panel.show();
                return;
            }
        }
    }
    getId() {
        return this.div.id;
    }
    hide() {
        this.div.style.display = 'none';
    }
    setupDragAndDrop() {
        this.div.draggable = true;
        this.body.ondragover = (ev) => {
            ev.preventDefault();
        };
        this.div.ondragstart = Panel.dragStart;
        this.body.ondrop = Panel.drop;
    }
    static drop(ev) {
        ev.preventDefault();
        let data = ev.dataTransfer.getData("text").split(',');
        let panel = document.getElementById(data[0]);
        let top = (ev.clientY - parseInt(data[1], 10)) + "px";
        let left = (ev.clientX - parseInt(data[2], 10)) + "px";
        panel.style.top = top;
        panel.style.left = left;
        panel.style.zIndex = Panel.newZindex();
    }
    static dragStart(ev) {
        ev.stopPropagation();
        let target = ev.target;
        let topPx = target.style.top.substring(0, target.style.top.length - 2);
        let leftPx = target.style.left.substring(0, target.style.left.length - 2);
        let topOffset = ev.clientY - Number(topPx);
        let leftOffSet = ev.clientX - Number(leftPx);
        let data = `${target.id},${String(topOffset)},${String(leftOffSet)}`;
        let x = ev.dataTransfer;
        x.setData("text", data);
    }
    prepareButtons(parentId, notCloseable) {
        let s = `#${parentId} #closeBttn`;
        let closeButton = document.querySelector(s);
        if (notCloseable)
            closeButton.remove();
        else
            closeButton.onclick = Panel.closePanel;
        s = `#${parentId} #hideBttn`;
        let hider = document.querySelector(s);
        hider.onclick = Panel.hidePanel;
        hider.onmousedown = (ev) => {
            hider.parentElement.setAttribute("draggable", "false");
        };
        hider.onmouseup = (ev) => {
            hider.parentElement.setAttribute("draggable", "true");
        };
    }
    static hidePanel(ev) {
        ev.stopPropagation();
        let button = ev.target;
        button.parentElement.style.display = 'none';
    }
    static closePanel(ev) {
        ev.stopPropagation();
        let button = ev.target;
        let panel = button.parentElement;
        panel.remove();
        Panel.removePanel(panel.id);
    }
    static removePanel(id, removeHtml = false) {
        for (let i = 0; i < Panel.panels.length; i++) {
            let panel = Panel.panels[i];
            if (panel.getId() === id) {
                if (removeHtml && panel.getHtml() != null)
                    panel.getHtml().remove();
                Panel.panels.splice(i, 1);
                return;
            }
        }
    }
    static getPanel(id) {
        for (let i = 0; i < Panel.panels.length; i++) {
            let panel = Panel.panels[i];
            if (panel.getId() === id) {
                return panel;
            }
        }
        return null;
    }
    static getPanels() {
        return Panel.panels;
    }
    static nextPanelNumber() {
        return ++Panel.pnlNumber;
    }
    static savePanel(panel) {
        this.panels.push(panel);
    }
}
Panel.template = null;
Panel.panels = [];
Panel.zIndex = 0;
Panel.pnlNumber = 0;
class HeadingUpdater {
    static show(panelId) {
        HeadingUpdater.panelId = panelId;
        let panel = Panel.getPanel(HeadingUpdater.panelId);
        let html = document.getElementById(HeadingUpdater.id);
        if (html == null) {
            html = (new DOMParser().parseFromString(HeadingUpdater.htmlStr, "text/html").body.firstChild);
            document.body.appendChild(html);
        }
        let s = `#${html.id} #newName`;
        let input = document.querySelector(s);
        input.value = panel.getHeading();
        HeadingUpdater.addEventListeners(html);
        html.style.zIndex = Panel.newZindex();
        html.style.display = 'block';
    }
    static cancel(ev) {
        let html = document.getElementById(HeadingUpdater.id);
        html.style.display = 'none';
    }
    static updateName(ev) {
        let html = document.getElementById(HeadingUpdater.id);
        let s = `#${html.id} #newName`;
        let input = document.querySelector(s);
        let panel = Panel.getPanel(HeadingUpdater.panelId);
        panel.setHeading(input.value);
        html.style.display = 'none';
    }
    static addEventListeners(html) {
        if (this.isConfigured)
            return;
        this.isConfigured = true;
        let s = `#${html.id} #cancel`;
        let cnclBttn = document.querySelector(s);
        cnclBttn.onclick = HeadingUpdater.cancel;
        s = `#${html.id} #update`;
        let updteBttn = document.querySelector(s);
        updteBttn.onclick = HeadingUpdater.updateName;
    }
}
HeadingUpdater.id = 'headingUpdater';
HeadingUpdater.isConfigured = false;
HeadingUpdater.htmlStr = `
        <div id="headingUpdater" class="panel" style='display: none;'>
            <fieldset>
                <legend>Panel Heading Update</legend>
                <input id="newName" name="newName" type="text" size="30" placeholder="type new panel name here" />
            </fieldset>
            <button id="cancel" class="pnlButton">Cancel</button>
            <button id="update" class="pnlButton">Update</button>
        </div>
    `;
class ConnectionPanel extends Panel {
    constructor(id, heading, debug = false) {
        super(PanelType.Connect, id, heading, true);
        Utils.debug = debug;
        JsonAjaxClient.setDebug(debug);
        let x = document.getElementById("connectInputs");
        this.div2 = x.cloneNode(true);
        x.remove();
        super.appendChild(this.div2);
        this.div2.onmousedown = (ev) => {
            this.div2.parentElement.setAttribute("draggable", "false");
        };
        this.div2.onmouseup = (ev) => {
            this.div2.parentElement.setAttribute("draggable", "true");
        };
        let bttn = document.querySelector("#Connect #connectBtn");
        bttn.onclick = (ev) => {
            ev.stopImmediatePropagation();
            this.setUrl();
            let status = document.querySelector('#connectInputs #status');
            status.value = 'Connecting...';
            status.style.color = 'orange';
            status.style.fontWeight = 'bold';
            JsonAjaxClient.get('tables', ConnectionPanel.setResponse, [status]);
        };
        this.show();
    }
    setUrl() {
        let input = document.querySelector("#connectInputs #host");
        let host = input.value;
        input = document.querySelector("#connectInputs #port");
        let port = input.value;
        input = document.querySelector("#connectInputs #context");
        let context = input.value;
        input = document.querySelector("#connectInputs #schema");
        let schema = input.value;
        JsonAjaxClient.setUrl(`http://${host}:${port}/${context}/${schema}/`);
    }
    static setResponse(response, objs) {
        Panel.removePanel(PanelType[PanelType.Schema], true);
        let result = response.results[0];
        let status = objs.pop();
        status.style.fontWeight = 'bold';
        let connException = document.getElementById("connException");
        connException.innerHTML = '';
        if (result.resultType == 'RESULTSET') {
            Panel.getPanel(PanelType[PanelType.Connect]).hide();
            SchemaPanel.getInstance(response).show();
            status.value = 'OK';
            status.style.color = 'green';
        }
        else {
            status.value = 'FAILED';
            status.style.color = 'red';
            if (result.resultType == 'EXCEPTION') {
                connException.innerHTML = "Exception: " + result.toString;
            }
        }
    }
    static getInstance(isStartup = false) {
        let x = Panel.getPanel(PanelType[PanelType.Connect]);
        if (x == null) {
            Panel.nextPanelNumber();
            x = new ConnectionPanel(PanelType[PanelType.Connect], "Connection Panel");
            Panel.savePanel(x);
        }
        if (isStartup)
            new Menu();
        return x;
    }
}
ConnectionPanel.sqlTemplate = "select [first 10] * from {} browse access;";
class TableClickContext {
    constructor(panelId, li) {
        this.panelId = panelId;
        this.li = li;
    }
}
class SchemaPanel extends Panel {
    constructor(id, heading, tables) {
        super(PanelType.Schema, id, JsonAjaxClient.getUrl(), true);
        this.prepareTreeTemplate();
        this.div2 = SchemaPanel.treeTemplate.cloneNode(true);
        this.div2.style.display = 'block';
        super.appendChild(this.div2);
        if (tables != null) {
            let result = tables.results[0];
            let ul = document.querySelector('#' + id + ' #tree');
            while (ul.firstChild)
                ul.removeChild(ul.firstChild);
            result.rows.forEach((value, index, array) => {
                let li = document.createElement("li");
                li.innerHTML = value.values[3] + '=' + value.values[2];
                let attr = document.createAttribute('id');
                attr.value = value.values[2];
                li.attributes.setNamedItem(attr);
                li.classList.add('nsItem');
                li.onclick = (ev) => {
                    JsonAjaxClient.get("tables/" + value.values[2] + "/metaData", SchemaPanel.processTableMetaData, [new TableClickContext(id, li)]);
                };
                ul.appendChild(li);
            });
        }
    }
    static processTableMetaData(metaData, ctxObjs) {
        let ctx = ctxObjs.pop();
        if (Utils.debug)
            console.log("clicked: " + ctx.li.getAttribute('id'));
        let result = metaData.results[0];
        let panel = SqlPanel.getInstance();
        panel.setSql(`select * from ${ctx.li.getAttribute('id')} limit 10`);
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
    }
    prepareTreeTemplate() {
        if (SchemaPanel.treeTemplate == null) {
            SchemaPanel.treeTemplate = document.getElementById("metaTree");
            SchemaPanel.treeTemplate.remove();
        }
    }
    static getInstance(tables) {
        let x = Panel.getPanel(PanelType[PanelType.Schema]);
        if (x == null) {
            Panel.nextPanelNumber();
            x = new SchemaPanel(PanelType[PanelType.Schema], "Schema Panel", tables);
            Panel.savePanel(x);
        }
        return x;
    }
}
SchemaPanel.treeTemplate = null;
class SqlPanel extends Panel {
    constructor(id, heading) {
        super(PanelType.Sql, id, heading);
        if (SqlPanel.sqlTemplate == null) {
            SqlPanel.sqlTemplate = document.getElementById("sqlPanel");
            SqlPanel.sqlTemplate.remove();
        }
        this.div2 = SqlPanel.sqlTemplate.cloneNode(true);
        this.div2.onmousedown = (ev) => {
            this.div2.parentElement.setAttribute("draggable", "false");
        };
        this.div2.onmouseup = (ev) => {
            this.div2.parentElement.setAttribute("draggable", "true");
        };
        super.appendChild(this.div2);
        let selector = `#${id} #statement`;
        this.sql = document.querySelector(selector);
        selector = `#${id} #clear`;
        let bttn = document.querySelector(selector);
        bttn.onclick = (ev) => {
            ev.stopImmediatePropagation();
            this.sql.value = '';
        };
        selector = `#${id} #exec`;
        bttn = document.querySelector(selector);
        bttn.onclick = (ev) => {
            ev.stopImmediatePropagation();
            JsonAjaxClient.post("sql?maxRows=-1", SqlPanel.processResults, "sql=" + this.sql.value, [this.sql.value]);
        };
        this.resultsDisplay = new ResultsDisplay(this, "right-click to copy to SQL");
        this.resultsDisplay.addValueCallback((value) => {
            let sqlText = this.sql.value;
            let cursorPos = this.sql.selectionStart + value.length;
            sqlText = this.sql.value.substr(0, this.sql.selectionStart) + value
                + this.sql.value.substr(this.sql.selectionEnd);
            this.sql.focus();
            this.sql.value = sqlText;
            this.sql.setSelectionRange(cursorPos, cursorPos);
            console.log("called back with: " + value);
        });
    }
    setSql(sql) {
        this.sql.value = sql;
    }
    setStatement(stmnt) {
        this.resultsDisplay.setStatement(stmnt);
    }
    addResults(resp = null, filter) {
        this.resultsDisplay.addResults(resp, filter);
    }
    static processResults(metaData, objs) {
        let sql = objs.pop();
        console.log("executed: " + sql);
        let panel = ResultPanel.getInstance();
        let result = metaData.results[0];
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
    }
    static getInstance() {
        let headTxt = "SQL Panel " + Panel.nextPanelNumber();
        let x = new SqlPanel(PanelType[PanelType.Sql], headTxt);
        Panel.savePanel(x);
        return x;
    }
}
SqlPanel.sqlTemplate = null;
class ResultPanel extends Panel {
    constructor(id, heading) {
        super(PanelType.Result, id, heading);
        this.resultsDisplay = new ResultsDisplay(this);
    }
    setStatement(stmnt) {
        this.resultsDisplay.setStatement(stmnt);
    }
    addResults(resp = null, filter) {
        this.resultsDisplay.addResults(resp, filter);
    }
    static getInstance() {
        let headTxt = "Result Panel " + Panel.nextPanelNumber();
        let x = new ResultPanel(PanelType[PanelType.Result], headTxt);
        Panel.savePanel(x);
        return x;
    }
}
class ResultsDisplay {
    constructor(panel, cellTitle = null) {
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
    setStatement(stmnt) {
        this.stmnt.innerHTML = stmnt;
    }
    addValueCallback(valueCallback) {
        this.valueCallback = valueCallback;
    }
    addResults(resp = null, filter = null) {
        this.updateCnt.innerHTML = '';
        while (this.grid.firstChild)
            this.grid.removeChild(this.grid.firstChild);
        if (resp == null)
            return;
        let result = resp.results[0];
        if (result.updateCount != undefined) {
            this.updateCnt.innerHTML = 'Update Count: ' + String(result.updateCount);
            return;
        }
        let table = this.appendTable();
        let tr = this.appendRow(table);
        let td;
        this.appendHdrCell(tr, "Row");
        result.columnDefinitions.forEach((colDef, i, colDefs) => {
            if (filter == null || (filter != null && filter.indexOf(i) >= 0))
                this.appendHdrCell(tr, colDef.name);
        });
        let rowPanel;
        result.rows.forEach((row, index, rows) => {
            tr = this.createRowHtml(table, row, index, rowPanel, result);
            this.appendCell(tr, String(index));
            row.values.forEach((value, i, values) => {
                if (filter == null || (filter != null && filter.indexOf(i) >= 0)) {
                    td = this.appendCell(tr);
                    this.prepareDataCell(td, value);
                }
            });
        });
    }
    createRowHtml(table, row, index, panel, result) {
        let tr = this.appendRow(table);
        tr.onclick = (ev) => {
            ev.stopPropagation();
            let head = this.panel.getHeading() + ' Row ' + index;
            panel = RowPanel.getInstance();
            panel.addResults(result.columnDefinitions, row);
            panel.show();
            Menu.hideAllMenus();
        };
        return tr;
    }
    prepareDataCell(td, value) {
        td.classList.add('nsItem');
        if (value.length > 32)
            td.innerHTML = value.substr(0, 32) + " ... ";
        else
            td.innerHTML = value;
    }
    appendTable() {
        let table = document.createElement('table');
        this.grid.appendChild(table);
        if (this.valueCallback != null) {
            table.oncontextmenu = (ev) => {
                ev.preventDefault();
                ev.stopImmediatePropagation();
                let tdTgt = ev.target;
                this.valueCallback(tdTgt.innerHTML);
            };
        }
        return table;
    }
    appendRow(table) {
        let tr = document.createElement('tr');
        table.appendChild(tr);
        return tr;
    }
    appendCell(tr, value = null) {
        let td;
        td = document.createElement('td');
        tr.appendChild(td);
        if (value != null)
            td.innerHTML = value;
        if (this.cellTitle != null)
            td.setAttribute('title', this.cellTitle);
        return td;
    }
    appendHdrCell(tr, value = null) {
        let th;
        th = document.createElement('th');
        tr.appendChild(th);
        if (value != null)
            th.innerHTML = value;
        return th;
    }
}
class Menu {
    constructor() {
        let html = document.getElementById("bodyMenu");
        if (html == null) {
            html = (new DOMParser().parseFromString(Menu.bodyMenuStr, "text/html").body.firstChild);
            document.body.appendChild(html);
        }
        html = document.getElementById("panelListMenu");
        if (html == null) {
            html = (new DOMParser().parseFromString(Menu.panelListMenuStr, "text/html").body.firstChild);
            document.body.appendChild(html);
        }
        document.addEventListener("contextmenu", Menu.showContextMenu, false);
        document.addEventListener("click", Menu.hideAllMenus, false);
        let menuItems = document.querySelectorAll('#bodyMenu li');
        for (let i = 1; i < menuItems.length; i++)
            menuItems.item(i).addEventListener('click', Menu.itemClick, false);
    }
    static showContextMenu(ev) {
        ev.preventDefault();
        let menu = document.getElementById('bodyMenu');
        let element = document.getElementById('menuCnnct');
        element.innerHTML = Panel.getPanel(PanelType[PanelType.Connect]).getHeading();
        Menu.showMenu(ev, 'bodyMenu');
    }
    static showMenu(ev, menuName) {
        Menu.hideAllMenus();
        let menu = document.getElementById(menuName);
        menu.style.top = (ev.clientY - 15) + 'px';
        menu.style.left = ev.clientX + 'px';
        menu.style.display = 'block';
        menu.style.zIndex = Panel.newZindex();
    }
    static hideAllMenus() {
        let menus = document.querySelectorAll('.menu');
        for (let i = 0; i < menus.length; i++)
            menus.item(i).style.display = 'none';
    }
    static itemClick(ev) {
        ev.stopPropagation();
        Menu.hideAllMenus();
        let target = ev.target;
        switch (target.getAttribute("data-action")) {
            case PanelType[PanelType.Connect]:
                Panel.showPanel(PanelType[PanelType.Connect]);
                Menu.hideAllMenus();
                break;
            case PanelType[PanelType.Schema]:
                SchemaPanel.getInstance().show();
                Menu.hideAllMenus();
                break;
            case PanelType[PanelType.Sql]:
                SqlPanel.getInstance().show();
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
                let liAttrs = ev.target.attributes;
                let dataAction = liAttrs.getNamedItem('data-action');
                Panel.showPanel(dataAction.value);
        }
    }
    static showPanelListMenu(ev, panelType, menuHeading) {
        let ul = document.getElementById('panelListMenu');
        while (ul.firstChild)
            ul.removeChild(ul.firstChild);
        let li = document.createElement('li');
        li.classList.add('menuHeading');
        li.innerHTML = menuHeading;
        ul.appendChild(li);
        let panels = Panel.getPanels();
        panels.forEach((panel, index, array) => {
            if (panel.getType() === panelType) {
                li = document.createElement('li');
                li.innerHTML = panel.getHeading();
                let dataAction = document.createAttribute('data-action');
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
Menu.bodyMenuStr = `
        <div>
            <ul id="bodyMenu" class="menu">
                <li class="menuHeading">Context Menu</li>
                <li id="menuCnnct" data-action="Connect">Connection Panel</li>
                <li data-action="Schema">Schema Panel</li>
                <li data-action="Sql">New SQL Panel</li>
                <li data-action="sqls">SQL Panels ...</li>
                <li data-action="rows">Row Panels ...</li>
                <li data-action="results">Result Panels ...</li>
            </ul>
        </div>
    `;
Menu.panelListMenuStr = `
        <div>
            <ul id="panelListMenu" class="menu">
                <li class="menuHeading"></li>
                <li data-action="metaPanel1">Meta Panel 1</li>
            </ul>
        </div>
    `;
class RowPanel extends Panel {
    constructor(id, heading) {
        super(PanelType.Row, id, heading);
        this.grid = document.createElement('div');
        super.appendChild(this.grid);
        this.grid.classList.add('scrollable');
    }
    addResults(columnDefinitions, row) {
        while (this.grid.firstChild)
            this.grid.removeChild(this.grid.firstChild);
        let table = document.createElement('table');
        let tr = document.createElement('tr');
        let th = document.createElement('th');
        let td;
        this.grid.appendChild(table);
        table.appendChild(tr);
        tr.appendChild(th);
        th.innerText = 'Column';
        th = document.createElement('th');
        tr.appendChild(th);
        th.innerText = 'Value';
        row.values.forEach((value, i, values) => {
            tr = document.createElement('tr');
            table.appendChild(tr);
            td = document.createElement('td');
            tr.appendChild(td);
            td.innerText = columnDefinitions[i].name;
            td = document.createElement('td');
            tr.appendChild(td);
            td.innerText = value;
        });
    }
    static getInstance() {
        let headTxt = "Row Panel " + Panel.nextPanelNumber();
        let x = new RowPanel(PanelType[PanelType.Row], headTxt);
        Panel.savePanel(x);
        return x;
    }
}
