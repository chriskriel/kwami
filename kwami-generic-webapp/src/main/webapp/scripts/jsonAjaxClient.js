var JsonAjaxClient = (function () {
    function JsonAjaxClient() {
    }
    JsonAjaxClient.intercept = function (response, objs) {
        var jsonResponse = JSON.parse(response);
        if (AjaxClient.debug) {
            if (jsonResponse != null && jsonResponse.results[0] != null) {
                if (jsonResponse.results[0].updateCount != null)
                    console.log("JSON update count: " + jsonResponse.results[0].updateCount);
                if (jsonResponse.results[0].columnDefinitions != null)
                    console.log("JSON contained " + jsonResponse.results[0].columnDefinitions.length + " column definitions");
                if (jsonResponse.results[0].rows != null)
                    console.log("JSON contained " + jsonResponse.results[0].rows.length + " rows");
            }
        }
        var callback = (objs.pop());
        callback(jsonResponse, objs);
    };
    JsonAjaxClient.get = function (url, callback, objs) {
        objs.push(callback);
        AjaxClient.get(url, JsonAjaxClient.intercept, objs);
    };
    JsonAjaxClient.post = function (url, callback, data, objs) {
        objs.push(callback);
        AjaxClient.post(url, JsonAjaxClient.intercept, data, objs);
    };
    JsonAjaxClient.setDebug = function (debug) {
        AjaxClient.debug = debug;
    };
    JsonAjaxClient.getUrl = function () {
        return AjaxClient.url;
    };
    JsonAjaxClient.setUrl = function (url) {
        AjaxClient.url = url;
    };
    return JsonAjaxClient;
}());
