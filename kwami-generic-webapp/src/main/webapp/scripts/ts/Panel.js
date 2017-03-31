(function (factory) {
    if (typeof module === "object" && typeof module.exports === "object") {
        var v = factory(require, exports);
        if (v !== undefined) module.exports = v;
    }
    else if (typeof define === "function" && define.amd) {
        define(["require", "exports"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var PanelType;
    (function (PanelType) {
        PanelType[PanelType["Schema"] = 0] = "Schema";
        PanelType[PanelType["Connect"] = 1] = "Connect";
        PanelType[PanelType["Result"] = 2] = "Result";
        PanelType[PanelType["Row"] = 3] = "Row";
        PanelType[PanelType["Sql"] = 4] = "Sql";
    })(PanelType = exports.PanelType || (exports.PanelType = {}));
    var Panel = (function () {
        function Panel(type, id, heading, notCloseable) {
            var _this = this;
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
            Panel.template = document.getElementById("panel");
            Panel.template.remove();
        };
        Panel.prototype.getHeading = function () {
            return this.heading;
        };
        Panel.prototype.setHeading = function (heading) {
            this.heading = heading;
            this.prepareHeading(this.div.id);
        };
        Panel.prototype.prepareHeading = function (parentId) {
            var s = app.interpolate('#{} #{}', parentId, 'panelHeading');
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
        Panel.prototype.show = function () {
            this.div.style.display = 'block';
            this.div.style.zIndex = app.newZindex();
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
            panel.style.zIndex = app.newZindex();
        };
        Panel.dragStart = function (ev) {
            ev.stopPropagation();
            var target = ev.target;
            var topPx = target.style.top.substring(0, target.style.top.length - 2);
            var leftPx = target.style.left.substring(0, target.style.left.length - 2);
            var topOffset = ev.clientY - Number(topPx);
            var leftOffSet = ev.clientX - Number(leftPx);
            var data = app.interpolate('{},{},{}', target.id, String(topOffset), String(leftOffSet));
            var x = ev.dataTransfer;
            x.setData("text", data);
        };
        Panel.prototype.prepareButtons = function (parentId, notCloseable) {
            var s = app.interpolate('#{} #{}', parentId, 'closeBttn');
            var closeButton = document.querySelector(s);
            if (notCloseable)
                closeButton.remove();
            else
                closeButton.onclick = Panel.closePanel;
            s = app.interpolate('#{} #{}', parentId, 'hideBttn');
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
            app.removePanel(panel.id);
        };
        return Panel;
    }());
    Panel.template = null;
    exports.Panel = Panel;
});
