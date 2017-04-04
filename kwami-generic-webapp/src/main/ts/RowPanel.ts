import { Panel, PanelType } from "Panel";
import { ColumnDefinition, Row } from "AjaxClient";

export class RowPanel extends Panel {

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
