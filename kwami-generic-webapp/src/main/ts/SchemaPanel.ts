import { Panel, PanelType } from "Panel";
import { Result, JsonResponse, Row, AjaxClient } from "AjaxClient";
import { SqlPanel } from "SqlPanel";
import { Menu } from "Menu";
import { Utils } from "Utils";


class TableClickContext {
    panelId: string;
    li: HTMLLIElement;

    constructor(panelId: string, li: HTMLLIElement) {
        this.panelId = panelId;
        this.li = li;
    }
}

export class SchemaPanel extends Panel {
    private static treeTemplate: HTMLDivElement = null;
    private div2: HTMLDivElement;
    private tables: JsonResponse;

    private constructor(id: string, heading: string) {
        super(PanelType.Schema, id, AjaxClient.url, true);
        this.prepareTreeTemplate();
        this.div2 = <HTMLDivElement>SchemaPanel.treeTemplate.cloneNode(true);
        this.div2.style.display = 'block';
        super.appendChild(this.div2);
        if (this.tables != null) {
            let result: Result = this.tables.results[0];
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
                        AjaxClient.get(
                            "tables/" + value.values[2] + "/metaData",
                            SchemaPanel.processTableMetaData,
                            new TableClickContext(id, li)
                        );
                    };
                    ul.appendChild(li);
                });
        }
    }

    private setTables(tables: JsonResponse): void {
        this.tables = tables;
    }

    public static processTableMetaData(metaData: JsonResponse, ctx: TableClickContext): void {
        if (Utils.debug)
            console.log("clicked: " + ctx.li.getAttribute('id'));
        let result: Result = metaData.results[0];
        let panel: SqlPanel = SqlPanel.getInstance();
        panel.setSql(Utils.getFirstSql(ctx.li.getAttribute('id')));
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

    private prepareTreeTemplate(): void {
        if (SchemaPanel.treeTemplate == null) {
            SchemaPanel.treeTemplate = <HTMLDivElement>document.getElementById("metaTree");
            SchemaPanel.treeTemplate.remove();
        }
    }

    public static getInstance(tables?: JsonResponse): SchemaPanel {
        let x: SchemaPanel = <SchemaPanel>Panel.getPanel(PanelType[PanelType.Schema]);
        if (x == null) {
            Panel.nextPanelNumber();
            x = new SchemaPanel(PanelType[PanelType.Schema], "Schema Panel");
            Panel.savePanel(x);
        }
        if (tables != null)
            x.setTables(tables);
        return x;
    }
}
