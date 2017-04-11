class H1Component {
    private text: string;
    private html: string = `
        <h1>${this.text}</h1>
    `
    constructor(text: string) {
        this.text = text;
    }
}