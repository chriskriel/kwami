class ConnectionPanel extends Panel {
    constructor(id, heading, debug = false) {
        super(PanelType.Connect, id, heading, true);
        Utils.debug = debug;
        JsonAjaxClient.setDebug(debug);
        let x = Utils.makeDivFromString(ConnectionPanel.htmlStr);
        this.div2 = x.cloneNode(true);
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
        input = document.querySelector("#connectInputs #sql");
        ConnectionPanel.sqlTemplate = input.value;
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
ConnectionPanel.htmlStr = `
        <div id="connectInputs">
            <fieldset>
                <legend>Connection Parameters</legend>
                <label for="host">Host: </label>
                <input id="host" type="text" size="30" value="localhost" />
                <br>
                <label for="port">Port: </label>
                <input id="port" type="number" min="4096" max="65535" value="18080" />
                <br>
                <label for="context">Context: </label>
                <input id="context" type="text" size="30" value="sqlmx" />
                <br>
                <label for="schema">Schema: </label>
                <input id="schema" type="text" size="30" value="employees" />
                <br>
                <label for="sql">Sampler: </label>
                <input id="sql" type="text" size="30" value="select * from {} limit 10" />
                <br>
                <label for="status">Status: </label>
                <input id="status" type="text" value="Not tried" disabled="disabled" />
                <br>
                <button id="connectBtn" class="pnlButton">Connect</button>
            </fieldset>
            <p id="connException"></p>
        </div>
    `;
class TableClickContext {
    constructor(panelId, li) {
        this.panelId = panelId;
        this.li = li;
    }
}
class SchemaPanel extends Panel {
    constructor(id, heading, tables) {
        super(PanelType.Schema, id, JsonAjaxClient.getUrl(), true);
        if (SchemaPanel.htmlTemplate == null)
            SchemaPanel.htmlTemplate = Utils.makeDivFromString(SchemaPanel.htmlStr);
        this.div2 = SchemaPanel.htmlTemplate.cloneNode(true);
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
        let parts = ConnectionPanel.sqlTemplate.split("{}");
        panel.setSql(parts[0] + ctx.li.getAttribute('id') + parts[1]);
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
SchemaPanel.htmlStr = `
        <div id="metaTree">
            <ul id="tree" class="scrollable">
                <li id="tanzkw" class="nsItem">TANZKW</li>
                <li id="tanzm" class="nsItem">TANZM</li>
            </ul>
        </div>
    `;
SchemaPanel.htmlTemplate = null;
class SqlPanel extends Panel {
    constructor(id, heading) {
        super(PanelType.Sql, id, heading);
        if (SqlPanel.sqlTemplate == null) {
            SqlPanel.sqlTemplate = Utils.makeDivFromString(SqlPanel.htmlStr);
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
SqlPanel.htmlStr = `
        <div id="sqlPanel">
            <p class="blankLine">&nbsp;</p>
            <textarea id="statement" placeholder="Type your SQL statement here ..."></textarea>
            <div id="sqlInput">
                <button id="exec">execute</button>
                <button id="clear">clear</button>
            </div>
            <p class="blankLine">&nbsp;</p>
            <div id="sqlResults"></div>
        </div>
    `;
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
            html = Utils.makeDivFromString(Menu.bodyMenuStr);
            document.body.appendChild(html);
        }
        html = document.getElementById("panelListMenu");
        if (html == null) {
            html = Utils.makeDivFromString(Menu.panelListMenuStr);
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
