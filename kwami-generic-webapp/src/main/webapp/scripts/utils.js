class Utils {
    static makeDivFromString(html) {
        return (new DOMParser().parseFromString(html, "text/html").body.firstChild);
    }
}
Utils.debug = false;
