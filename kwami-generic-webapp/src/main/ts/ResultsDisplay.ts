import { app, Panel, PanelType } from "Panel";
import { JsonResponse, Result, ColumnDefinition, Row } from "RestConnector";
import { RowPanel } from "RowPanel";
import { Menu } from "Menu";

export class ResultsDisplay {

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
            panel = <RowPanel>app.newPanel(PanelType.Row, head);
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