import { app, Panel } from "Panel";

export class HeadingUpdater {
    private static panelId: string;
    private static id: string = 'headingUpdater';
    private static isConfigured: boolean = false;

    public static show(panelId: string) {
        HeadingUpdater.panelId = panelId;
        let panel: Panel = app.getPanel(HeadingUpdater.panelId);
        let html = <HTMLDivElement>document.getElementById(HeadingUpdater.id);
        let s: string = app.interpolate('#{} #{}', html.id, 'newName');
        let input = <HTMLInputElement>document.querySelector(s);
        input.value = panel.getHeading();
        HeadingUpdater.addEventListeners(html);
        html.style.zIndex = app.newZindex();
        html.style.display = 'block';
    }

    private static cancel(ev: MouseEvent) {
        let html = <HTMLDivElement>document.getElementById(HeadingUpdater.id);
        html.style.display = 'none';
    }

    private static updateName(ev: MouseEvent) {
        let html = <HTMLDivElement>document.getElementById(HeadingUpdater.id);
        let s: string = app.interpolate('#{} #{}', html.id, 'newName');
        let input = <HTMLInputElement>document.querySelector(s);
        let panel: Panel = app.getPanel(HeadingUpdater.panelId);
        panel.setHeading(input.value);
        html.style.display = 'none';
    }

    private static addEventListeners(html: HTMLDivElement): void {
        if (this.isConfigured)
            return;
        this.isConfigured = true;
        let s: string = app.interpolate('#{} #{}', html.id, 'cancel');
        let cnclBttn = <HTMLElement>document.querySelector(s);
        cnclBttn.onclick = HeadingUpdater.cancel;
        s = app.interpolate('#{} #{}', html.id, 'update');
        let updteBttn = <HTMLElement>document.querySelector(s);
        updteBttn.onclick = HeadingUpdater.updateName;
    }

}