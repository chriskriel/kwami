var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
var ElementTester = (function (_super) {
    __extends(ElementTester, _super);
    function ElementTester(element) {
        if (element === void 0) { element = null; }
        var _this = _super.call(this, 'p') || this;
        _this.appendToBody();
        _this.getHtml().innerHTML = "CLICKME";
        var menuCtx = new EventContext(_this);
        menuCtx.handler = function (ev) { _this.handleCtxMenu(menuCtx, ev); };
        _this.getHtml().onclick = function (ev) { _this.handleClick(_this, ev); };
        _this.getHtml().addEventListener('contextmenu', menuCtx.handler, false);
        return _this;
    }
    ElementTester.prototype.handleClick = function (self, ev) {
        console.log("clicked at " + ev.clientX + "," + ev.clientY + " on ID=" + self.getId());
    };
    ElementTester.prototype.handleCtxMenu = function (ctx, ev) {
        ev.preventDefault();
        var self = ctx.self;
        console.log("menu at " + ev.clientX + "," + ev.clientY + " on ID=" + self.getId());
        self.getHtml().removeEventListener("contextmenu", ctx.handler);
    };
    return ElementTester;
}(HtmlElement));
