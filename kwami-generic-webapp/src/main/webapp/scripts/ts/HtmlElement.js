var EventContext = (function () {
    function EventContext(self) {
        this.self = self;
    }
    return EventContext;
}());
var HtmlElement = (function () {
    function HtmlElement(tag, html) {
        if (html === void 0) { html = null; }
        this.parent = null;
        this.html = html;
        if (html == null)
            this.html = document.createElement(tag);
        if (this.html.id == undefined || this.html.id == null || this.html.id.length == 0)
            this.html.id = 'E' + HtmlElement.idIncr++;
        HtmlElement.objects.push(this);
    }
    HtmlElement.constructFromHtml = function (html, parent) {
        if (parent === void 0) { parent = null; }
        if (html.nodeName == '#text' || html.nodeName == '#comment'
            || html.nodeName == 'SCRIPT')
            return;
        var me = new HtmlElement(null, html);
        me.parent = parent;
        for (var i = 0; i < html.children.length; i++) {
            HtmlElement.constructFromHtml(html.children.item(i), me);
        }
        return me;
    };
    HtmlElement.prototype.getId = function () {
        return this.html.id;
    };
    HtmlElement.prototype.getHtml = function () {
        return this.html;
    };
    HtmlElement.prototype.getParent = function () {
        return this.parent;
    };
    HtmlElement.findWithId = function (id) {
        for (var i = 0; i < HtmlElement.objects.length; i++)
            if (HtmlElement.objects[i].html.id == id)
                return HtmlElement.objects[i];
    };
    HtmlElement.prototype.appendChild = function (child) {
        child.parent = this;
        this.html.appendChild(child.html);
    };
    HtmlElement.prototype.insertBefore = function (newChild, refChild) {
        newChild.parent = this;
        this.html.insertBefore(newChild.html, refChild.html);
    };
    HtmlElement.prototype.appendTo = function (parent) {
        this.parent = parent;
        parent.html.appendChild(this.html);
    };
    HtmlElement.prototype.appendToBody = function () {
        this.html.querySelector('body').appendChild(this.html);
    };
    return HtmlElement;
}());
HtmlElement.idIncr = 0;
HtmlElement.objects = [];
