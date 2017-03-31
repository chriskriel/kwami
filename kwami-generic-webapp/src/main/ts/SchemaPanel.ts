import { app, Panel, PanelType } from "Panel";
import { Result, JsonResponse, RestConnector, Row } from "RestConnector";
import { SqlPanel } from "SqlPanel";
import { Menu } from "Menu";

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

    constructor(id: string, heading: string) {
        super(PanelType.Schema, id, RestConnector.url, true);
        this.prepareTreeTemplate();
        this.div2 = <HTMLDivElement>SchemaPanel.treeTemplate.cloneNode(true);
        this.div2.style.display = 'block';
        super.appendChild(this.div2);
        if (RestConnector.tables != null) {
            let result: Result = RestConnector.tables.results[0];
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
                        RestConnector.ajaxGet(
                            "tables/" + value.values[2] + "/metaData",
                            SchemaPanel.processTableMetaData,
                            new TableClickContext(id, li)
                        );
                    };
                    ul.appendChild(li);
                });
        }
    }

    public static processTableMetaData(metaData: JsonResponse, ctx: TableClickContext): void {
        if (app.getDebug())
            console.log("clicked: " + ctx.li.getAttribute('id'));
        let result: Result = metaData.results[0];
        let panel = <SqlPanel>app.newPanel(PanelType.Sql);
        panel.setSql(app.getFirstSql(ctx.li.getAttribute('id')));
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
}
