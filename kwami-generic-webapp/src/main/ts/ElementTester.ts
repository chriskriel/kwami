import { HtmlElement, EventContext } from "HtmlElement";

class ElementTester extends HtmlElement {

    public constructor(element: HTMLElement = null) {
        super('p');
        this.appendToBody();
        this.getHtml().innerHTML = "CLICKME";
        let menuCtx = new EventContext(this);
        menuCtx.handler = (ev: PointerEvent) => { this.handleCtxMenu(menuCtx, ev); };
        this.getHtml().onclick = (ev: MouseEvent) => { this.handleClick(this, ev); };
        this.getHtml().addEventListener('contextmenu', menuCtx.handler, false);
    }

    private handleClick(self: ElementTester, ev: MouseEvent) {
        console.log("clicked at " + ev.clientX + "," + ev.clientY + " on ID=" + self.getId());
    }

    private handleCtxMenu(ctx: EventContext, ev: PointerEvent) {
        ev.preventDefault();
        let self = ctx.self;
        console.log("menu at " + ev.clientX + "," + ev.clientY + " on ID=" + self.getId());
        self.getHtml().removeEventListener("contextmenu", ctx.handler);
    }
}