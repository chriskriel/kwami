var Utils = (function () {
    function Utils() {
    }
    Utils.makeDivFromString = function (html) {
        return (new DOMParser().parseFromString(html, "text/html").body.firstChild);
    };
    return Utils;
}());
Utils.debug = false;
