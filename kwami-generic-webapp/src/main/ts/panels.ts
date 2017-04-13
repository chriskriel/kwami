class ConnectionPanel extends Panel {
    public static sqlTemplate: string;
    public static htmlStr = `
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
    private div2: HTMLDivElement;

    private constructor(id: string, heading: string, debug: boolean = false) {
        super(PanelType.Connect, id, heading, true);
        Utils.debug = debug;
        JsonAjaxClient.setDebug(debug);
        let x = Utils.makeDivFromString(ConnectionPanel.htmlStr);
        this.div2 = <HTMLDivElement>x.cloneNode(true);
        super.appendChild(this.div2);
        this.div2.onmousedown = (ev: MouseEvent): any => {
            this.div2.parentElement.setAttribute("draggable", "false");
        }
        this.div2.onmouseup = (ev: MouseEvent): any => {
            this.div2.parentElement.setAttribute("draggable", "true");
        }
        let bttn = <HTMLElement>document.querySelector("#Connect #connectBtn");
        bttn.onclick = (ev: MouseEvent) => {
            ev.stopImmediatePropagation();
            this.setUrl();
            let status = <HTMLInputElement>document.querySelector('#connectInputs #status');
            status.value = 'Connecting...';
            status.style.color = 'orange';
            status.style.fontWeight = 'bold';
            JsonAjaxClient.get('tables', ConnectionPanel.setResponse, [status]);
        }
        this.show();
    }

    public setUrl(): void {
        let input = <HTMLInputElement>document.querySelector("#connectInputs #host");
        let host: string = input.value;
        input = <HTMLInputElement>document.querySelector("#connectInputs #port");
        let port: string = input.value;
        input = <HTMLInputElement>document.querySelector("#connectInputs #context");
        let context: string = input.value;
        input = <HTMLInputElement>document.querySelector("#connectInputs #schema");
        let schema: string = input.value;
        input = <HTMLInputElement>document.querySelector("#connectInputs #sql");
        ConnectionPanel.sqlTemplate = input.value;
        JsonAjaxClient.setUrl(`http://${host}:${port}/${context}/${schema}/`);
    }

    public static setResponse(response: JsonResponse, objs: Object[]): void {
        Panel.removePanel(PanelType[PanelType.Schema], true);
        let result: Result = response.results[0];
        let status = <HTMLInputElement>objs.pop();
        status.style.fontWeight = 'bold';
        let connException = <HTMLParagraphElement>document.getElementById("connException");
        connException.innerHTML = '';
        if (result.resultType == 'RESULTSET') {
            Panel.getPanel(PanelType[PanelType.Connect]).hide();
            SchemaPanel.getInstance(response).show();
            status.value = 'OK';
            status.style.color = 'green';
        } else {
            status.value = 'FAILED';
            status.style.color = 'red';
            if (result.resultType == 'EXCEPTION') {
                connException.innerHTML = "Exception: " + result.toString;
            }
        }
    }

    public static getInstance(isStartup: boolean = false): ConnectionPanel {
        let x: ConnectionPanel = <ConnectionPanel>Panel.getPanel(PanelType[PanelType.Connect]);
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

/********************************************************************/
class TableClickContext {
    panelId: string;
    li: HTMLLIElement;

    constructor(panelId: string, li: HTMLLIElement) {
        this.panelId = panelId;
        this.li = li;
    }
}

/********************************************************************/
class SchemaPanel extends Panel {
    private static htmlStr = `
        <div id="metaTree">
            <ul id="tree" class="scrollable">
                <li id="tanzkw" class="nsItem">TANZKW</li>
                <li id="tanzm" class="nsItem">TANZM</li>
            </ul>
        </div>
    `;
    private static htmlTemplate: HTMLDivElement = null;
    private div2: HTMLDivElement;

    private constructor(id: string, heading: string, tables: JsonResponse) {
        super(PanelType.Schema, id, JsonAjaxClient.getUrl(), true);
        if (SchemaPanel.htmlTemplate == null)
            SchemaPanel.htmlTemplate = Utils.makeDivFromString(SchemaPanel.htmlStr);
        this.div2 = <HTMLDivElement>SchemaPanel.htmlTemplate.cloneNode(true);
        this.div2.style.display = 'block';
        super.appendChild(this.div2);
        if (tables != null) {
            let result: Result = tables.results[0];
            let ul = <HTMLUListElement>document.querySelector('#' + id + ' #tree');
            while (ul.firstChild)
                ul.removeChild(ul.firstChild);
            result.rows.forEach(
                (value: Row, index: number, array: Row[]) => {
                    let li: HTMLLIElement = document.createElement("li");
                    li.innerHTML = value.values[3] + '=' + value.values[2];
                    let attr: Attr = document.createAttribute('id');
                    attr.value = value.values[2];
                    li.attributes.setNamedItem(attr);
                    li.classList.add('nsItem');
                    li.onclick = (ev: MouseEvent) => {
                        JsonAjaxClient.get(
                            "tables/" + value.values[2] + "/metaData",
                            SchemaPanel.processTableMetaData,
                            [new TableClickContext(id, li)]
                        );
                    };
                    ul.appendChild(li);
                });
        }
    }

    public static processTableMetaData(metaData: JsonResponse, ctxObjs: Object[]): void {
        let ctx: TableClickContext = <TableClickContext>ctxObjs.pop();
        if (Utils.debug)
            console.log("clicked: " + ctx.li.getAttribute('id'));
        let result: Result = metaData.results[0];
        let panel: SqlPanel = SqlPanel.getInstance();
        let parts = ConnectionPanel.sqlTemplate.split("{}");
        panel.setSql(parts[0] + ctx.li.getAttribute('id') + parts[1]);
        if (result.resultType == 'EXCEPTION') {
            panel.setStatement("Exception: " + result.toString);
            panel.addResults();
        } else {
            panel.setStatement(ctx.li.getAttribute('id'))
            panel.addResults(metaData, [3, 5, 6, 10]);
        }
        panel.show();
        Menu.hideAllMenus();
    }

    public static getInstance(tables?: JsonResponse): SchemaPanel {
        let x: SchemaPanel = <SchemaPanel>Panel.getPanel(PanelType[PanelType.Schema]);
        if (x == null) {
            Panel.nextPanelNumber();
            x = new SchemaPanel(PanelType[PanelType.Schema], "Schema Panel", tables);
            Panel.savePanel(x);
        }
        return x;
    }
}

/********************************************************************/
class SqlPanel extends Panel {
    private static htmlStr = `
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
    private static sqlTemplate: HTMLDivElement = null;
    private div2: HTMLDivElement;
    private resultsDisplay: ResultsDisplay;
    private sql: HTMLTextAreaElement;

    private constructor(id: string, heading: string) {
        super(PanelType.Sql, id, heading);
        if (SqlPanel.sqlTemplate == null) {
            SqlPanel.sqlTemplate = Utils.makeDivFromString(SqlPanel.htmlStr);
            // SqlPanel.sqlTemplate.remove();
        }
        this.div2 = <HTMLDivElement>SqlPanel.sqlTemplate.cloneNode(true);
        this.div2.onmousedown = (ev: MouseEvent): any => {
            this.div2.parentElement.setAttribute("draggable", "false");
        }
        this.div2.onmouseup = (ev: MouseEvent): any => {
            this.div2.parentElement.setAttribute("draggable", "true");
        }
        super.appendChild(this.div2);
        let selector: string = `#${id} #statement`;
        this.sql = <HTMLTextAreaElement>document.querySelector(selector);
        selector = `#${id} #clear`;
        let bttn: HTMLElement = <HTMLElement>document.querySelector(selector);
        bttn.onclick = (ev: MouseEvent) => {
            ev.stopImmediatePropagation();
            this.sql.value = '';
        }
        selector = `#${id} #exec`;
        bttn = <HTMLElement>document.querySelector(selector);
        bttn.onclick = (ev: MouseEvent) => {
            ev.stopImmediatePropagation();
            JsonAjaxClient.post(
                "sql?maxRows=-1",
                SqlPanel.processResults,
                "sql=" + this.sql.value,
                [this.sql.value]
            );
        }
        this.resultsDisplay = new ResultsDisplay(this, "right-click to copy to SQL");
        this.resultsDisplay.addValueCallback((value: string) => {
            let sqlText = this.sql.value;
            let cursorPos: number = this.sql.selectionStart + value.length;
            sqlText = this.sql.value.substr(0, this.sql.selectionStart) + value
                + this.sql.value.substr(this.sql.selectionEnd);
            this.sql.focus();
            this.sql.value = sqlText;
            this.sql.setSelectionRange(cursorPos, cursorPos);
            console.log("called back with: " + value);
        });
    }

    public setSql(sql: string) {
        this.sql.value = sql;
    }

    public setStatement(stmnt: string) {
        this.resultsDisplay.setStatement(stmnt);
    }

    public addResults(resp: JsonResponse = null, filter?: number[]): void {
        this.resultsDisplay.addResults(resp, filter);
    }

    public static processResults(metaData: JsonResponse, objs: Object[]): void {
        let sql: string = <string>objs.pop();
        console.log("executed: " + sql);
        let panel = ResultPanel.getInstance();
        let result: Result = metaData.results[0];
        if (result.resultType == 'EXCEPTION') {
            panel.setStatement("Exception: " + result.toString);
            panel.addResults();
        } else {
            panel.setStatement("Statement: " + sql);
            panel.addResults(metaData);
        }
        panel.show();
        Menu.hideAllMenus();
    }

    public static getInstance(): SqlPanel {
        let headTxt: string = "SQL Panel " + Panel.nextPanelNumber();
        let x: SqlPanel = new SqlPanel(PanelType[PanelType.Sql], headTxt);
        Panel.savePanel(x);
        return x;
    }
}

/********************************************************************/
class ResultPanel extends Panel {

    private resultsDisplay: ResultsDisplay;

    private constructor(id: string, heading: string) {
        super(PanelType.Result, id, heading);
        this.resultsDisplay = new ResultsDisplay(this);
    }

    public setStatement(stmnt: string) {
        this.resultsDisplay.setStatement(stmnt);
    }

    public addResults(resp: JsonResponse = null, filter?: number[]): void {
        this.resultsDisplay.addResults(resp, filter);
    }

    public static getInstance(): ResultPanel {
        let headTxt: string = "Result Panel " + Panel.nextPanelNumber();
        let x: ResultPanel = new ResultPanel(PanelType[PanelType.Result], headTxt);
        Panel.savePanel(x);
        return x;
    }
}

/********************************************************************/
class ResultsDisplay {

    private stmnt: HTMLParagraphElement;
    private panel: Panel;
    private grid: HTMLDivElement;
    private updateCnt: HTMLParagraphElement;
    private valueCallback: (value: string) => void;
    private cellTitle: string;

    constructor(panel: Panel, cellTitle: string = null) {
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

    public setStatement(stmnt: string): void {
        this.stmnt.innerHTML = stmnt;
    }

    public addValueCallback(valueCallback: (value: string) => void) {
        this.valueCallback = valueCallback;
    }

    public addResults(resp: JsonResponse = null, filter: number[] = null): void {
        this.updateCnt.innerHTML = '';
        while (this.grid.firstChild)
            this.grid.removeChild(this.grid.firstChild);
        if (resp == null)
            return;
        let result: Result = resp.results[0];
        if (result.updateCount != undefined) {
            this.updateCnt.innerHTML = 'Update Count: ' + String(result.updateCount);
            return;
        }
        let table: HTMLTableElement = this.appendTable();
        let tr: HTMLTableRowElement = this.appendRow(table);
        let td: HTMLTableCellElement;
        this.appendHdrCell(tr, "Row");
        result.columnDefinitions.forEach((colDef: ColumnDefinition, i: number, colDefs: ColumnDefinition[]) => {
            if (filter == null || (filter != null && filter.indexOf(i) >= 0))
                this.appendHdrCell(tr, colDef.name);
        });
        let rowPanel: RowPanel;
        result.rows.forEach((row: Row, index: number, rows: Row[]) => {
            tr = this.createRowHtml(table, row, index, rowPanel, result);
            this.appendCell(tr, String(index));
            row.values.forEach((value: string, i: number, values: string[]) => {
                if (filter == null || (filter != null && filter.indexOf(i) >= 0)) {
                    td = this.appendCell(tr);
                    this.prepareDataCell(td, value);
                }
            });
        });
    }

    private createRowHtml(table: HTMLTableElement, row: Row, index: number, panel: RowPanel, result: Result): HTMLTableRowElement {
        let tr: HTMLTableRowElement = this.appendRow(table);
        tr.onclick = (ev: MouseEvent) => {
            ev.stopPropagation();
            let head: string = this.panel.getHeading() + ' Row ' + index;
            panel = RowPanel.getInstance();
            panel.addResults(result.columnDefinitions, row);
            panel.show();
            Menu.hideAllMenus();
        };
        return tr;
    }

    private prepareDataCell(td: HTMLTableCellElement, value: string): void {
        td.classList.add('nsItem');
        if (value.length > 32)
            td.innerHTML = value.substr(0, 32) + " ... ";
        else
            td.innerHTML = value;
    }

    private appendTable(): HTMLTableElement {
        let table: HTMLTableElement = document.createElement('table');
        this.grid.appendChild(table);
        if (this.valueCallback != null) {
            table.oncontextmenu = (ev: PointerEvent) => {
                ev.preventDefault();
                ev.stopImmediatePropagation();
                let tdTgt = <HTMLElement>ev.target;
                this.valueCallback(tdTgt.innerHTML);
            }
        }
        return table;
    }

    private appendRow(table: HTMLTableElement): HTMLTableRowElement {
        let tr: HTMLTableRowElement = document.createElement('tr');
        table.appendChild(tr);
        return tr;
    }

    private appendCell(tr: HTMLTableRowElement, value: string = null): HTMLTableCellElement {
        let td: HTMLTableCellElement;
        td = document.createElement('td');
        tr.appendChild(td);
        if (value != null)
            td.innerHTML = value;
        if (this.cellTitle != null)
            td.setAttribute('title', this.cellTitle);
        return td;
    }

    private appendHdrCell(tr: HTMLTableRowElement, value: string = null) {
        let th: HTMLTableHeaderCellElement;
        th = document.createElement('th');
        tr.appendChild(th);
        if (value != null)
            th.innerHTML = value;
        return th;
    }
}

/********************************************************************/
class Menu {
    private static bodyMenuStr: string = `
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
    private static panelListMenuStr: string = `
        <div>
            <ul id="panelListMenu" class="menu">
                <li class="menuHeading"></li>
                <li data-action="metaPanel1">Meta Panel 1</li>
            </ul>
        </div>
    `;


    public constructor() {
        let html = <HTMLDivElement>document.getElementById("bodyMenu");
        if (html == null) {
            html = Utils.makeDivFromString(Menu.bodyMenuStr);
            document.body.appendChild(html);
        }
        html = <HTMLDivElement>document.getElementById("panelListMenu");        
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

    private static showContextMenu(ev: MouseEvent): void {
        ev.preventDefault();
        let menu = document.getElementById('bodyMenu');
        let element = <HTMLElement>document.getElementById('menuCnnct');
        element.innerHTML = Panel.getPanel(PanelType[PanelType.Connect]).getHeading();
        Menu.showMenu(ev, 'bodyMenu');
    }

    private static showMenu(ev: MouseEvent, menuName: string): void {
        Menu.hideAllMenus();
        let menu = document.getElementById(menuName);
        menu.style.top = (ev.clientY - 15) + 'px';
        menu.style.left = ev.clientX + 'px';
        menu.style.display = 'block';
        menu.style.zIndex = Panel.newZindex();
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
                let liAttrs = (<Element>ev.target).attributes;
                let dataAction: Attr = liAttrs.getNamedItem('data-action');
                Panel.showPanel(dataAction.value);
        }
    }

    private static showPanelListMenu(ev: MouseEvent, panelType: PanelType, menuHeading: string) {
        let ul: HTMLUListElement = <HTMLUListElement>document.getElementById('panelListMenu');
        while (ul.firstChild)
            ul.removeChild(ul.firstChild);
        let li: HTMLLIElement = document.createElement('li');
        li.classList.add('menuHeading');
        li.innerHTML = menuHeading;
        ul.appendChild(li);
        let panels: Panel[] = Panel.getPanels();
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

/********************************************************************/
class RowPanel extends Panel {

    private grid: HTMLDivElement;

    private constructor(id: string, heading: string) {
        super(PanelType.Row, id, heading);
        this.grid = document.createElement('div');
        super.appendChild(this.grid);
        this.grid.classList.add('scrollable');
    }

    public addResults(columnDefinitions: ColumnDefinition[], row: Row): void {
        while (this.grid.firstChild)
            this.grid.removeChild(this.grid.firstChild);
        let table: HTMLTableElement = document.createElement('table');
        let tr: HTMLTableRowElement = document.createElement('tr');
        let th: HTMLTableHeaderCellElement = document.createElement('th');
        let td: HTMLTableCellElement;
        this.grid.appendChild(table);
        table.appendChild(tr);
        tr.appendChild(th);
        th.innerText = 'Column';
        th = document.createElement('th');
        tr.appendChild(th);
        th.innerText = 'Value';
        row.values.forEach(
            (value: string, i: number, values: string[]) => {
                tr = document.createElement('tr');
                table.appendChild(tr);
                td = document.createElement('td');
                tr.appendChild(td);
                td.innerText = columnDefinitions[i].name;
                td = document.createElement('td');
                tr.appendChild(td);
                td.innerText = value;
            }
        );
    }

    public static getInstance(): RowPanel {
        let headTxt: string = "Row Panel " + Panel.nextPanelNumber();
        let x: RowPanel = new RowPanel(PanelType[PanelType.Row], headTxt);
        Panel.savePanel(x);
        return x;
    }
}
