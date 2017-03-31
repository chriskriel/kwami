export class EventContext {
    self: HtmlElement;
    handler: (ev: Event) => void;

    public constructor(self: HtmlElement) {
        this.self = self;
    }
}

export class HtmlElement {
    private static idIncr: number = 0;
    private static objects: HtmlElement[] = [];
    private parent: HtmlElement = null;
    private html: HTMLElement;

    public constructor(tag: string, html: HTMLElement = null) {
        this.html = html;
        if (html == null)
            this.html = document.createElement(tag);
        if (this.html.id == undefined || this.html.id == null || this.html.id.length == 0)
            this.html.id = 'E' + HtmlElement.idIncr++;
        HtmlElement.objects.push(this);
    }

    public static constructFromHtml(html: HTMLElement, parent: HtmlElement = null): HtmlElement {
        if (html.nodeName == '#text' || html.nodeName == '#comment'
            || html.nodeName == 'SCRIPT')
            return;
        let me = new HtmlElement(null, html);
        me.parent = parent;
        for (let i = 0; i < html.children.length; i++) {
            HtmlElement.constructFromHtml(<HTMLElement>html.children.item(i), me);
        }
        return me;
    }
    
    public getId(): string {
        return this.html.id;
    }
    
    public getHtml(): HTMLElement {
        return this.html;
    }
    
    public getParent(): HtmlElement {
        return this.parent;
    }

    public static findWithId(id: string): HtmlElement {
        for (let i = 0; i < HtmlElement.objects.length; i++)
            if (HtmlElement.objects[i].html.id == id)
                return HtmlElement.objects[i];
    }

    public appendChild(child: HtmlElement) {
        child.parent = this;
        this.html.appendChild(child.html);
    }

    public insertBefore(newChild: HtmlElement, refChild: HtmlElement) {
        newChild.parent = this;
        this.html.insertBefore(newChild.html, refChild.html);
    }

    public appendTo(parent: HtmlElement) {
        this.parent = parent;
        parent.html.appendChild(this.html);
    }

    public appendToBody() {
        this.html.querySelector('body').appendChild(this.html);
    }
}