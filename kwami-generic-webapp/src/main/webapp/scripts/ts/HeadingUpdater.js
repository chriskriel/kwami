var HeadingUpdater = (function () {
    function HeadingUpdater() {
    }
    HeadingUpdater.show = function (panelId) {
        HeadingUpdater.panelId = panelId;
        var panel = app.getPanel(HeadingUpdater.panelId);
        var html = document.getElementById(HeadingUpdater.id);
        var s = app.interpolate('#{} #{}', html.id, 'newName');
        var input = document.querySelector(s);
        input.value = panel.getHeading();
        HeadingUpdater.addEventListeners(html);
        html.style.zIndex = app.newZindex();
        html.style.display = 'block';
    };
    HeadingUpdater.cancel = function (ev) {
        var html = document.getElementById(HeadingUpdater.id);
        html.style.display = 'none';
    };
    HeadingUpdater.updateName = function (ev) {
        var html = document.getElementById(HeadingUpdater.id);
        var s = app.interpolate('#{} #{}', html.id, 'newName');
        var input = document.querySelector(s);
        var panel = app.getPanel(HeadingUpdater.panelId);
        panel.setHeading(input.value);
        html.style.display = 'none';
    };
    HeadingUpdater.addEventListeners = function (html) {
        if (this.isConfigured)
            return;
        this.isConfigured = true;
        var s = app.interpolate('#{} #{}', html.id, 'cancel');
        var cnclBttn = document.querySelector(s);
        cnclBttn.onclick = HeadingUpdater.cancel;
        s = app.interpolate('#{} #{}', html.id, 'update');
        var updteBttn = document.querySelector(s);
        updteBttn.onclick = HeadingUpdater.updateName;
    };
    return HeadingUpdater;
}());
HeadingUpdater.id = 'headingUpdater';
HeadingUpdater.isConfigured = false;
