import { Panel, PanelType } from "Panel";
import { ResultsDisplay } from "ResultsDisplay";
import { AjaxClient, Result, JsonResponse } from "AjaxClient";
import { ResultPanel } from "ResultPanel";
import { Menu } from "Menu";
import { Utils } from "Utils";

export class SqlPanel extends Panel {
    private static sqlTemplate: HTMLDivElement = null;
    private div2: HTMLDivElement;
    private resultsDisplay: ResultsDisplay;
    private sql: HTMLTextAreaElement;

    private constructor(id: string, heading: string) {
        super(PanelType.Sql, id, heading);
        if (SqlPanel.sqlTemplate == null) {
            SqlPanel.sqlTemplate = <HTMLDivElement>document.getElementById("sqlPanel");
            SqlPanel.sqlTemplate.remove();
        }
        this.div2 = <HTMLDivElement>SqlPanel.sqlTemplate.cloneNode(true);
        this.div2.onmousedown = (ev: MouseEvent): any => {
            this.div2.parentElement.setAttribute("draggable", "false");
        }
        this.div2.onmouseup = (ev: MouseEvent): any => {
            this.div2.parentElement.setAttribute("draggable", "true");
        }
        super.appendChild(this.div2);
        let selector: string = Utils.interpolate('#{} #statement', id);
        this.sql = <HTMLTextAreaElement>document.querySelector(selector);
        selector = Utils.interpolate('#{} #clear', id);
        let bttn: HTMLElement = <HTMLElement>document.querySelector(selector);
        bttn.onclick = (ev: MouseEvent) => {
            ev.stopImmediatePropagation();
            this.sql.value = '';
        }
        selector = Utils.interpolate('#{} #exec', id);
        bttn = <HTMLElement>document.querySelector(selector);
        bttn.onclick = (ev: MouseEvent) => {
            ev.stopImmediatePropagation();
            AjaxClient.post(
                "sql?maxRows=-1",
                SqlPanel.processResults,
                "sql=" + this.sql.value,
                this.sql.value
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

    public static processResults(metaData: JsonResponse, sql: string): void {
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