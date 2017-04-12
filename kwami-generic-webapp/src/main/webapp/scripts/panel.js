var PanelType;
(function (PanelType) {
    PanelType[PanelType["Schema"] = 0] = "Schema";
    PanelType[PanelType["Connect"] = 1] = "Connect";
    PanelType[PanelType["Result"] = 2] = "Result";
    PanelType[PanelType["Row"] = 3] = "Row";
    PanelType[PanelType["Sql"] = 4] = "Sql";
})(PanelType || (PanelType = {}));
var Panel = (function () {
    function Panel(type, id, heading, notCloseable) {
        var _this = this;
        this.htmlStr = "\n        <div id=\"panel\" class=\"panel\">\n            <h3 id=\"panelHeading\" class=\"panelHeading\"></h3>\n            <p class=\"inlineSpace\">&nbsp;&nbsp;&nbsp;</p>\n            <button id=\"closeBttn\" class=\"pnlButton\">X</button>\n            <button id=\"hideBttn\" class=\"pnlButton\">_</button>\n        </div>\n    ";
        this.prepareTemplate();
        this.type = type;
        this.heading = heading;
        this.div = Panel.template.cloneNode(true);
        this.body = document.getElementById("body");
        this.body.appendChild(this.div);
        this.div.id = id;
        this.div.style.display = 'none';
        this.prepareButtons(id, notCloseable);
        var h3 = this.prepareHeading(id);
        h3.onclick = function (ev) {
            ev.stopPropagation();
            HeadingUpdater.show(_this.div.id);
        };
        this.setupDragAndDrop();
    }
    Panel.prototype.getHtml = function () {
        return this.div;
    };
    Panel.prototype.prepareTemplate = function () {
        if (Panel.template != null)
            return;
        Panel.template = Utils.makeDivFromString(this.htmlStr);
    };
    Panel.prototype.getHeading = function () {
        return this.heading;
    };
    Panel.prototype.setHeading = function (heading) {
        this.heading = heading;
        this.prepareHeading(this.div.id);
    };
    Panel.prototype.prepareHeading = function (parentId) {
        var s = "#" + parentId + " #panelHeading";
        var h3 = document.querySelector(s);
        h3.innerHTML = this.heading;
        h3.setAttribute('title', 'click to rename');
        return h3;
    };
    Panel.prototype.getType = function () {
        return this.type;
    };
    Panel.prototype.appendChild = function (child) {
        this.div.appendChild(child);
    };
    Panel.newZindex = function () {
        return Panel.zIndex++ + '';
    };
    Panel.prototype.show = function () {
        this.div.style.display = 'block';
        this.div.style.zIndex = Panel.newZindex();
    };
    Panel.showPanel = function (id) {
        for (var i = 0; i < Panel.panels.length; i++) {
            var panel = Panel.panels[i];
            if (panel.getId() === id) {
                panel.show();
                return;
            }
        }
    };
    Panel.prototype.getId = function () {
        return this.div.id;
    };
    Panel.prototype.hide = function () {
        this.div.style.display = 'none';
    };
    Panel.prototype.setupDragAndDrop = function () {
        this.div.draggable = true;
        this.body.ondragover = function (ev) {
            ev.preventDefault();
        };
        this.div.ondragstart = Panel.dragStart;
        this.body.ondrop = Panel.drop;
    };
    Panel.drop = function (ev) {
        ev.preventDefault();
        var data = ev.dataTransfer.getData("text").split(',');
        var panel = document.getElementById(data[0]);
        var top = (ev.clientY - parseInt(data[1], 10)) + "px";
        var left = (ev.clientX - parseInt(data[2], 10)) + "px";
        panel.style.top = top;
        panel.style.left = left;
        panel.style.zIndex = Panel.newZindex();
    };
    Panel.dragStart = function (ev) {
        ev.stopPropagation();
        var target = ev.target;
        var topPx = target.style.top.substring(0, target.style.top.length - 2);
        var leftPx = target.style.left.substring(0, target.style.left.length - 2);
        var topOffset = ev.clientY - Number(topPx);
        var leftOffSet = ev.clientX - Number(leftPx);
        var data = target.id + "," + String(topOffset) + "," + String(leftOffSet);
        var x = ev.dataTransfer;
        x.setData("text", data);
    };
    Panel.prototype.prepareButtons = function (parentId, notCloseable) {
        var s = "#" + parentId + " #closeBttn";
        var closeButton = document.querySelector(s);
        if (notCloseable)
            closeButton.remove();
        else
            closeButton.onclick = Panel.closePanel;
        s = "#" + parentId + " #hideBttn";
        var hider = document.querySelector(s);
        hider.onclick = Panel.hidePanel;
        hider.onmousedown = function (ev) {
            hider.parentElement.setAttribute("draggable", "false");
        };
        hider.onmouseup = function (ev) {
            hider.parentElement.setAttribute("draggable", "true");
        };
    };
    Panel.hidePanel = function (ev) {
        ev.stopPropagation();
        var button = ev.target;
        button.parentElement.style.display = 'none';
    };
    Panel.closePanel = function (ev) {
        ev.stopPropagation();
        var button = ev.target;
        var panel = button.parentElement;
        panel.remove();
        Panel.removePanel(panel.id);
    };
    Panel.removePanel = function (id, removeHtml) {
        if (removeHtml === void 0) { removeHtml = false; }
        for (var i = 0; i < Panel.panels.length; i++) {
            var panel = Panel.panels[i];
            if (panel.getId() === id) {
                if (removeHtml && panel.getHtml() != null)
                    panel.getHtml().remove();
                Panel.panels.splice(i, 1);
                return;
            }
        }
    };
    Panel.getPanel = function (id) {
        for (var i = 0; i < Panel.panels.length; i++) {
            var panel = Panel.panels[i];
            if (panel.getId() === id) {
                return panel;
            }
        }
        return null;
    };
    Panel.getPanels = function () {
        return Panel.panels;
    };
    Panel.nextPanelNumber = function () {
        return ++Panel.pnlNumber;
    };
    Panel.savePanel = function (panel) {
        this.panels.push(panel);
    };
    return Panel;
}());
Panel.template = null;
Panel.panels = [];
Panel.zIndex = 0;
Panel.pnlNumber = 0;
var HeadingUpdater = (function () {
    function HeadingUpdater() {
    }
    HeadingUpdater.show = function (panelId) {
        HeadingUpdater.panelId = panelId;
        var panel = Panel.getPanel(HeadingUpdater.panelId);
        var html = document.getElementById(HeadingUpdater.id);
        if (html == null) {
            html = Utils.makeDivFromString(this.htmlStr);
            document.body.appendChild(html);
        }
        var s = "#" + html.id + " #newName";
        var input = document.querySelector(s);
        input.value = panel.getHeading();
        HeadingUpdater.addEventListeners(html);
        html.style.zIndex = Panel.newZindex();
        html.style.display = 'block';
    };
    HeadingUpdater.cancel = function (ev) {
        var html = document.getElementById(HeadingUpdater.id);
        html.style.display = 'none';
    };
    HeadingUpdater.updateName = function (ev) {
        var html = document.getElementById(HeadingUpdater.id);
        var s = "#" + html.id + " #newName";
        var input = document.querySelector(s);
        var panel = Panel.getPanel(HeadingUpdater.panelId);
        panel.setHeading(input.value);
        html.style.display = 'none';
    };
    HeadingUpdater.addEventListeners = function (html) {
        if (this.isConfigured)
            return;
        this.isConfigured = true;
        var s = "#" + html.id + " #cancel";
        var cnclBttn = document.querySelector(s);
        cnclBttn.onclick = HeadingUpdater.cancel;
        s = "#" + html.id + " #update";
        var updteBttn = document.querySelector(s);
        updteBttn.onclick = HeadingUpdater.updateName;
    };
    return HeadingUpdater;
}());
HeadingUpdater.id = 'headingUpdater';
HeadingUpdater.isConfigured = false;
HeadingUpdater.htmlStr = "\n        <div id=\"headingUpdater\" class=\"panel\" style='display: none;'>\n            <fieldset>\n                <legend>Panel Heading Update</legend>\n                <input id=\"newName\" name=\"newName\" type=\"text\" size=\"30\" placeholder=\"type new panel name here\" />\n            </fieldset>\n            <button id=\"cancel\" class=\"pnlButton\">Cancel</button>\n            <button id=\"update\" class=\"pnlButton\">Update</button>\n        </div>\n    ";
