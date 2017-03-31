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
    exports.debug = false;
    exports.sqlTemplate = 'select [first 10] * from {} browse access;';
    function getFirstSql(tableName) {
        return this.interpolate(this.sqlTemplate, tableName);
    }
    exports.getFirstSql = getFirstSql;
    function interpolate(template) {
        var values = [];
        for (var _i = 1; _i < arguments.length; _i++) {
            values[_i - 1] = arguments[_i];
        }
        if (template === undefined || template === null)
            return null;
        var parts = template.split("{}");
        var i = 0;
        var result = parts[0];
        for (var j = 1; j < parts.length; j++)
            result += values[i++] + parts[j];
        return result;
    }
    exports.interpolate = interpolate;
});
