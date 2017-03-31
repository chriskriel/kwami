import { Application } from "Application";
import { HeadingUpdater } from "HeadingUpdater";

export let app: Application;  // all classes of this application depends on this global variable

export enum PanelType {
    Schema,
    Connect,
    Result,
    Row,
    Sql
}

export class Panel {
    private static template: HTMLDivElement = null;
    private type: PanelType;
    private heading: string;
    private div: HTMLDivElement;
    private body: HTMLBodyElement;

    public constructor(type: PanelType, id: string, heading: string,
        notCloseable?: boolean) {
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
        Panel.template = <HTMLDivElement>document.getElementById("panel");
        Panel.template.remove();
    }

    public getHeading(): string {
        return this.heading;
    }

    public setHeading(heading: string): void {
        this.heading = heading;
        this.prepareHeading(this.div.id);
    }

    private prepareHeading(parentId: string): HTMLElement {
        let s: string = app.interpolate('#{} #{}', parentId, 'panelHeading');
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

    public show(): void {
        this.div.style.display = 'block';
        this.div.style.zIndex = app.newZindex();
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
        panel.style.zIndex = app.newZindex();
    }

    private static dragStart(ev: DragEvent): void {
        ev.stopPropagation();
        let target: HTMLElement = <HTMLElement>ev.target;
        let topPx = target.style.top.substring(0, target.style.top.length - 2);
        let leftPx = target.style.left.substring(0, target.style.left.length - 2);
        let topOffset = ev.clientY - Number(topPx);
        let leftOffSet = ev.clientX - Number(leftPx);
        let data = app.interpolate('{},{},{}', target.id, String(topOffset), String(leftOffSet));
        let x = ev.dataTransfer;
        x.setData("text", data);

    }

    private prepareButtons(parentId: string, notCloseable?: boolean): void {
        let s: string = app.interpolate('#{} #{}', parentId, 'closeBttn');
        let closeButton = <HTMLElement>document.querySelector(s);
        if (notCloseable)
            closeButton.remove();
        else
            closeButton.onclick = Panel.closePanel;
        s = app.interpolate('#{} #{}', parentId, 'hideBttn');
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
        app.removePanel(panel.id);
    }
}
