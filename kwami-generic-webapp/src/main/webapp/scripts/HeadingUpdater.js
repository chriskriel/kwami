(function (factory) {
    if (typeof module === "object" && typeof module.exports === "object") {
        var v = factory(require, exports);
        if (v !== undefined) module.exports = v;
    }
    else if (typeof define === "function" && define.amd) {
        define(["require", "exports", "Panel"], factory);
    }
})(function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Panel_1 = require("Panel");
    var HeadingUpdater = (function () {
        function HeadingUpdater() {
        }
        HeadingUpdater.show = function (panelId) {
            HeadingUpdater.panelId = panelId;
            var panel = Panel_1.app.getPanel(HeadingUpdater.panelId);
            var html = document.getElementById(HeadingUpdater.id);
            var s = Panel_1.app.interpolate('#{} #{}', html.id, 'newName');
            var input = document.querySelector(s);
            input.value = panel.getHeading();
            HeadingUpdater.addEventListeners(html);
            html.style.zIndex = Panel_1.app.newZindex();
            html.style.display = 'block';
        };
        HeadingUpdater.cancel = function (ev) {
            var html = document.getElementById(HeadingUpdater.id);
            html.style.display = 'none';
        };
        HeadingUpdater.updateName = function (ev) {
            var html = document.getElementById(HeadingUpdater.id);
            var s = Panel_1.app.interpolate('#{} #{}', html.id, 'newName');
            var input = document.querySelector(s);
            var panel = Panel_1.app.getPanel(HeadingUpdater.panelId);
            panel.setHeading(input.value);
            html.style.display = 'none';
        };
        HeadingUpdater.addEventListeners = function (html) {
            if (this.isConfigured)
                return;
            this.isConfigured = true;
            var s = Panel_1.app.interpolate('#{} #{}', html.id, 'cancel');
            var cnclBttn = document.querySelector(s);
            cnclBttn.onclick = HeadingUpdater.cancel;
            s = Panel_1.app.interpolate('#{} #{}', html.id, 'update');
            var updteBttn = document.querySelector(s);
            updteBttn.onclick = HeadingUpdater.updateName;
        };
        return HeadingUpdater;
    }());
    HeadingUpdater.id = 'headingUpdater';
    HeadingUpdater.isConfigured = false;
    exports.HeadingUpdater = HeadingUpdater;
});
