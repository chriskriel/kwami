import { Panel, PanelType } from "Panel";
import { RestConnector } from "RestConnector";
import { RowPanel } from "RowPanel";
import { SchemaPanel } from "SchemaPanel";
import { Menu } from "Menu";
import { SqlPanel } from "SqlPanel";
import { ResultPanel } from "ResultPanel";

export class Application {

    private debug: boolean = false;
    private sqlTemplate: string = 'select [first 10] * from {} browse access;';
    // private currentMenu: string;
    private zIndex: number = 0;
    private pnlNumber: number = 0;
    private panels: Panel[];
    private menu: Menu;

    constructor() {
        this.panels = [];
        this.newPanel(PanelType.Connect).show();
        this.menu = new Menu();
    }

    public newPanel(type: PanelType, heading?: string): Panel {
        this.pnlNumber++;
        let id: string = PanelType[type] + String(this.pnlNumber);
        let headTxt: string;
        let x: Panel;
        switch (type) {
            case PanelType.Connect:
                headTxt = heading == null ? "Connection" : heading;
                x = new RestConnector(PanelType[type], headTxt);
                break;
            case PanelType.Row:
                headTxt = heading == null ? "Row Panel " + this.pnlNumber : heading;
                x = new RowPanel(id, headTxt);
                break;
            case PanelType.Schema:
                x = this.getPanel(PanelType[type]);
                if (x != null)
                    return x;
                headTxt = heading == null ? "Schema Panel" : heading;
                x = new SchemaPanel(PanelType[type], headTxt);
                break;
            case PanelType.Sql:
                headTxt = heading == null ? "SQL Panel " + this.pnlNumber : heading;
                x = new SqlPanel(id, headTxt);
                break;
            case PanelType.Result:
                headTxt = heading == null ? "Result Panel " + this.pnlNumber : heading;
                x = new ResultPanel(id, headTxt);
                break;
        }
        this.panels.push(x);
        return x;
    }

    public getFirstSql(tableName: string): string {
        return this.interpolate(this.sqlTemplate, tableName);
    }

    public showPanel(id: string) {
        for (let i = 0; i < this.panels.length; i++) {
            let panel = this.panels[i];
            if (panel.getId() === id) {
                panel.show();
                return;
            }
        }
    }

    public removePanel(id: string, removeHtml: boolean = false): void {
        for (let i = 0; i < this.panels.length; i++) {
            let panel = this.panels[i];
            if (panel.getId() === id) {
                if (removeHtml && panel.getHtml() != null)
                    panel.getHtml().remove();
                this.panels.splice(i, 1);
                return;
            }
        }
    }

    public getPanel(id: string): Panel {
        for (let i = 0; i < this.panels.length; i++) {
            let panel = this.panels[i];
            if (panel.getId() === id) {
                return panel;
            }
        }
        return null;
    }

    public getPanels(): Panel[] {
        return this.panels;
    }

    public interpolate(template: string, ...values: string[]) {
        if (template === undefined || template === null)
            return null;
        var parts = template.split("{}");
        var i = 0;
        var result = parts[0];
        for (var j = 1; j < parts.length; j++)
            result += values[i++] + parts[j];
        return result;
    }

    public newZindex(): string {
        return this.zIndex++ + '';
    }

    public getDebug(): boolean {
        return this.debug;
    }

    public setDebug(debug: boolean) {
        this.debug = debug;
    }

    public getSqlTemplate(): string {
        return this.sqlTemplate;
    }

    public setSqlTemplate(sql: string) {
        this.sqlTemplate = sql;
    }
}
