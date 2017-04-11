class JsonAjaxClient {
    static intercept(response, objs) {
        let jsonResponse = JSON.parse(response);
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
        let callback = (objs.pop());
        callback(jsonResponse, objs);
    }
    static get(url, callback, objs) {
        objs.push(callback);
        AjaxClient.get(url, JsonAjaxClient.intercept, objs);
    }
    static post(url, callback, data, objs) {
        objs.push(callback);
        AjaxClient.post(url, JsonAjaxClient.intercept, data, objs);
    }
    static setDebug(debug) {
        AjaxClient.debug = debug;
    }
    static getUrl() {
        return AjaxClient.url;
    }
    static setUrl(url) {
        AjaxClient.url = url;
    }
}
