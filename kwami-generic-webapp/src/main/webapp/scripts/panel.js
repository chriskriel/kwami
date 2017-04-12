var PanelType;
(function (PanelType) {
    PanelType[PanelType["Schema"] = 0] = "Schema";
    PanelType[PanelType["Connect"] = 1] = "Connect";
    PanelType[PanelType["Result"] = 2] = "Result";
    PanelType[PanelType["Row"] = 3] = "Row";
    PanelType[PanelType["Sql"] = 4] = "Sql";
})(PanelType || (PanelType = {}));
class Panel {
    constructor(type, id, heading, notCloseable) {
        this.htmlStr = `
        <div id="panel" class="panel">
            <h3 id="panelHeading" class="panelHeading"></h3>
            <p class="inlineSpace">&nbsp;&nbsp;&nbsp;</p>
            <button id="closeBttn" class="pnlButton">X</button>
            <button id="hideBttn" class="pnlButton">_</button>
        </div>
    `;
        this.prepareTemplate();
        this.type = type;
        this.heading = heading;
        this.div = Panel.template.cloneNode(true);
        this.body = document.getElementById("body");
        this.body.appendChild(this.div);
        this.div.id = id;
        this.div.style.display = 'none';
        this.prepareButtons(id, notCloseable);
        let h3 = this.prepareHeading(id);
        h3.onclick = (ev) => {
            ev.stopPropagation();
            HeadingUpdater.show(this.div.id);
        };
        this.setupDragAndDrop();
    }
    getHtml() {
        return this.div;
    }
    prepareTemplate() {
        if (Panel.template != null)
            return;
        Panel.template = Utils.makeDivFromString(this.htmlStr);
    }
    getHeading() {
        return this.heading;
    }
    setHeading(heading) {
        this.heading = heading;
        this.prepareHeading(this.div.id);
    }
    prepareHeading(parentId) {
        let s = `#${parentId} #panelHeading`;
        let h3 = document.querySelector(s);
        h3.innerHTML = this.heading;
        h3.setAttribute('title', 'click to rename');
        return h3;
    }
    getType() {
        return this.type;
    }
    appendChild(child) {
        this.div.appendChild(child);
    }
    static newZindex() {
        return Panel.zIndex++ + '';
    }
    show() {
        this.div.style.display = 'block';
        this.div.style.zIndex = Panel.newZindex();
    }
    static showPanel(id) {
        for (let i = 0; i < Panel.panels.length; i++) {
            let panel = Panel.panels[i];
            if (panel.getId() === id) {
                panel.show();
                return;
            }
        }
    }
    getId() {
        return this.div.id;
    }
    hide() {
        this.div.style.display = 'none';
    }
    setupDragAndDrop() {
        this.div.draggable = true;
        this.body.ondragover = (ev) => {
            ev.preventDefault();
        };
        this.div.ondragstart = Panel.dragStart;
        this.body.ondrop = Panel.drop;
    }
    static drop(ev) {
        ev.preventDefault();
        let data = ev.dataTransfer.getData("text").split(',');
        let panel = document.getElementById(data[0]);
        let top = (ev.clientY - parseInt(data[1], 10)) + "px";
        let left = (ev.clientX - parseInt(data[2], 10)) + "px";
        panel.style.top = top;
        panel.style.left = left;
        panel.style.zIndex = Panel.newZindex();
    }
    static dragStart(ev) {
        ev.stopPropagation();
        let target = ev.target;
        let topPx = target.style.top.substring(0, target.style.top.length - 2);
        let leftPx = target.style.left.substring(0, target.style.left.length - 2);
        let topOffset = ev.clientY - Number(topPx);
        let leftOffSet = ev.clientX - Number(leftPx);
        let data = `${target.id},${String(topOffset)},${String(leftOffSet)}`;
        let x = ev.dataTransfer;
        x.setData("text", data);
    }
    prepareButtons(parentId, notCloseable) {
        let s = `#${parentId} #closeBttn`;
        let closeButton = document.querySelector(s);
        if (notCloseable)
            closeButton.remove();
        else
            closeButton.onclick = Panel.closePanel;
        s = `#${parentId} #hideBttn`;
        let hider = document.querySelector(s);
        hider.onclick = Panel.hidePanel;
        hider.onmousedown = (ev) => {
            hider.parentElement.setAttribute("draggable", "false");
        };
        hider.onmouseup = (ev) => {
            hider.parentElement.setAttribute("draggable", "true");
        };
    }
    static hidePanel(ev) {
        ev.stopPropagation();
        let button = ev.target;
        button.parentElement.style.display = 'none';
    }
    static closePanel(ev) {
        ev.stopPropagation();
        let button = ev.target;
        let panel = button.parentElement;
        panel.remove();
        Panel.removePanel(panel.id);
    }
    static removePanel(id, removeHtml = false) {
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
    static getPanel(id) {
        for (let i = 0; i < Panel.panels.length; i++) {
            let panel = Panel.panels[i];
            if (panel.getId() === id) {
                return panel;
            }
        }
        return null;
    }
    static getPanels() {
        return Panel.panels;
    }
    static nextPanelNumber() {
        return ++Panel.pnlNumber;
    }
    static savePanel(panel) {
        this.panels.push(panel);
    }
}
Panel.template = null;
Panel.panels = [];
Panel.zIndex = 0;
Panel.pnlNumber = 0;
class HeadingUpdater {
    static show(panelId) {
        HeadingUpdater.panelId = panelId;
        let panel = Panel.getPanel(HeadingUpdater.panelId);
        let html = document.getElementById(HeadingUpdater.id);
        if (html == null) {
            html = Utils.makeDivFromString(this.htmlStr);
            document.body.appendChild(html);
        }
        let s = `#${html.id} #newName`;
        let input = document.querySelector(s);
        input.value = panel.getHeading();
        HeadingUpdater.addEventListeners(html);
        html.style.zIndex = Panel.newZindex();
        html.style.display = 'block';
    }
    static cancel(ev) {
        let html = document.getElementById(HeadingUpdater.id);
        html.style.display = 'none';
    }
    static updateName(ev) {
        let html = document.getElementById(HeadingUpdater.id);
        let s = `#${html.id} #newName`;
        let input = document.querySelector(s);
        let panel = Panel.getPanel(HeadingUpdater.panelId);
        panel.setHeading(input.value);
        html.style.display = 'none';
    }
    static addEventListeners(html) {
        if (this.isConfigured)
            return;
        this.isConfigured = true;
        let s = `#${html.id} #cancel`;
        let cnclBttn = document.querySelector(s);
        cnclBttn.onclick = HeadingUpdater.cancel;
        s = `#${html.id} #update`;
        let updteBttn = document.querySelector(s);
        updteBttn.onclick = HeadingUpdater.updateName;
    }
}
HeadingUpdater.id = 'headingUpdater';
HeadingUpdater.isConfigured = false;
HeadingUpdater.htmlStr = `
        <div id="headingUpdater" class="panel" style='display: none;'>
            <fieldset>
                <legend>Panel Heading Update</legend>
                <input id="newName" name="newName" type="text" size="30" placeholder="type new panel name here" />
            </fieldset>
            <button id="cancel" class="pnlButton">Cancel</button>
            <button id="update" class="pnlButton">Update</button>
        </div>
    `;
