enum PanelType {
    Schema,
    Connect,
    Result,
    Row,
    Sql
}

/********************************************************************/
class Panel {
    private static template: HTMLDivElement = null;
    private static panels: Panel[] = [];
    private static zIndex: number = 0;
    private static pnlNumber: number = 0;
    private htmlStr = `
        <div id="panel" class="panel">
            <h3 id="panelHeading" class="panelHeading"></h3>
            <p class="inlineSpace">&nbsp;&nbsp;&nbsp;</p>
            <button id="closeBttn" class="pnlButton">X</button>
            <button id="hideBttn" class="pnlButton">_</button>
        </div>
    `;
    private type: PanelType;
    private heading: string;
    private div: HTMLDivElement;
    private body: HTMLBodyElement;

    protected constructor(type: PanelType, id: string, heading: string, notCloseable?: boolean) {
        this.prepareTemplate();
        this.type = type;
        this.heading = heading;
        this.div = <HTMLDivElement>Panel.template.cloneNode(true);
        this.body = <HTMLBodyElement>document.getElementById("body");
        this.body.appendChild(this.div);
        this.div.id = id;
        this.div.style.display = 'none';
        this.prepareButtons(id, notCloseable);
        let h3: HTMLElement = this.prepareHeading(id);
        h3.onclick = (ev: MouseEvent) => {
            ev.stopPropagation();
            HeadingUpdater.show(this.div.id);
        }
        this.setupDragAndDrop();
    }

    public getHtml(): HTMLDivElement {
        return this.div;
    }

    private prepareTemplate(): void {
        if (Panel.template != null)
            return;
        Panel.template = Utils.makeDivFromString(this.htmlStr);
    }

    public getHeading(): string {
        return this.heading;
    }

    public setHeading(heading: string): void {
        this.heading = heading;
        this.prepareHeading(this.div.id);
    }

    private prepareHeading(parentId: string): HTMLElement {
        let s: string =`#${parentId} #panelHeading`;
        let h3 = <HTMLElement>document.querySelector(s);
        h3.innerHTML = this.heading;
        h3.setAttribute('title', 'click to rename');
        return h3;
    }

    public getType(): PanelType {
        return this.type;
    }

    public appendChild(child: HTMLElement): void {
        this.div.appendChild(child);
    }

    public static newZindex(): string {
        return Panel.zIndex++ + '';
    }

    public show(): void {
        this.div.style.display = 'block';
        this.div.style.zIndex = Panel.newZindex();
    }

    public static showPanel(id: string) {
        for (let i = 0; i < Panel.panels.length; i++) {
            let panel = Panel.panels[i];
            if (panel.getId() === id) {
                panel.show();
                return;
            }
        }
    }

    public getId(): string {
        return this.div.id;
    }

    public hide(): void {
        this.div.style.display = 'none';
    }

    private setupDragAndDrop(): void {
        this.div.draggable = true;
        this.body.ondragover = (ev: DragEvent): void => {
            ev.preventDefault();
        }
        this.div.ondragstart = Panel.dragStart;
        this.body.ondrop = Panel.drop;
    }

    private static drop(ev: DragEvent): void {
        ev.preventDefault();
        let data: string[] = ev.dataTransfer.getData("text").split(',');
        let panel: HTMLElement = <HTMLElement>document.getElementById(data[0]);
        let top: string = (ev.clientY - parseInt(data[1], 10)) + "px";
        let left = (ev.clientX - parseInt(data[2], 10)) + "px";
        panel.style.top = top;
        panel.style.left = left;
        panel.style.zIndex = Panel.newZindex();
    }

    private static dragStart(ev: DragEvent): void {
        ev.stopPropagation();
        let target: HTMLElement = <HTMLElement>ev.target;
        let topPx = target.style.top.substring(0, target.style.top.length - 2);
        let leftPx = target.style.left.substring(0, target.style.left.length - 2);
        let topOffset = ev.clientY - Number(topPx);
        let leftOffSet = ev.clientX - Number(leftPx);
        let data = `${target.id},${String(topOffset)},${String(leftOffSet)}`;
        let x = ev.dataTransfer;
        x.setData("text", data);

    }

    private prepareButtons(parentId: string, notCloseable?: boolean): void {
        let s: string = `#${parentId} #closeBttn`;
        let closeButton = <HTMLElement>document.querySelector(s);
        if (notCloseable)
            closeButton.remove();
        else
            closeButton.onclick = Panel.closePanel;
        s = `#${parentId} #hideBttn`;
        let hider = <HTMLElement>document.querySelector(s);
        hider.onclick = Panel.hidePanel;
        hider.onmousedown = (ev: MouseEvent): any => {
            hider.parentElement.setAttribute("draggable", "false");
        }
        hider.onmouseup = (ev: MouseEvent): any => {
            hider.parentElement.setAttribute("draggable", "true");
        }
    }

    private static hidePanel(ev: MouseEvent): void {
        ev.stopPropagation();
        let button = <Element>ev.target;
        (<HTMLElement>button.parentElement).style.display = 'none';
    }

    private static closePanel(ev: MouseEvent): void {
        ev.stopPropagation();
        let button = <HTMLElement>ev.target;
        let panel = <HTMLElement>button.parentElement;
        panel.remove();
        Panel.removePanel(panel.id);
    }

    public static removePanel(id: string, removeHtml: boolean = false): void {
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

    public static getPanel(id: string): Panel {
        for (let i = 0; i < Panel.panels.length; i++) {
            let panel = Panel.panels[i];
            if (panel.getId() === id) {
                return panel;
            }
        }
        return null;
    }

    public static getPanels(): Panel[] {
        return Panel.panels;
    }

    public static nextPanelNumber(): number {
        return ++Panel.pnlNumber;
    }

    public static savePanel(panel: Panel) {
        this.panels.push(panel);
    }

}

/********************************************************************/
class HeadingUpdater {
    private static panelId: string;
    private static id: string = 'headingUpdater';
    private static isConfigured: boolean = false;
    private static htmlStr: string = `
        <div id="headingUpdater" class="panel" style='display: none;'>
            <fieldset>
                <legend>Panel Heading Update</legend>
                <input id="newName" name="newName" type="text" size="30" placeholder="type new panel name here" />
            </fieldset>
            <button id="cancel" class="pnlButton">Cancel</button>
            <button id="update" class="pnlButton">Update</button>
        </div>
    `;

    public static show(panelId: string) {
        HeadingUpdater.panelId = panelId;
        let panel: Panel = Panel.getPanel(HeadingUpdater.panelId);
        let html = <HTMLDivElement>document.getElementById(HeadingUpdater.id);
        if (html == null) {
            html = Utils.makeDivFromString(this.htmlStr);
            document.body.appendChild(html);
        }
        let s: string = `#${html.id} #newName`;
        let input = <HTMLInputElement>document.querySelector(s);
        input.value = panel.getHeading();
        HeadingUpdater.addEventListeners(html);
        html.style.zIndex = Panel.newZindex();
        html.style.display = 'block';
    }

    private static cancel(ev: MouseEvent) {
        let html = <HTMLDivElement>document.getElementById(HeadingUpdater.id);
        html.style.display = 'none';
    }

    private static updateName(ev: MouseEvent) {
        let html = <HTMLDivElement>document.getElementById(HeadingUpdater.id);
        let s: string = `#${html.id} #newName`;
        let input = <HTMLInputElement>document.querySelector(s);
        let panel: Panel = Panel.getPanel(HeadingUpdater.panelId);
        panel.setHeading(input.value);
        html.style.display = 'none';
    }

    private static addEventListeners(html: HTMLDivElement): void {
        if (this.isConfigured)
            return;
        this.isConfigured = true;
        let s: string = `#${html.id} #cancel`;
        let cnclBttn = <HTMLElement>document.querySelector(s);
        cnclBttn.onclick = HeadingUpdater.cancel;
        s = `#${html.id} #update`;
        let updteBttn = <HTMLElement>document.querySelector(s);
        updteBttn.onclick = HeadingUpdater.updateName;
    }
}

