type ResponseCallback = (response: JsonResponse, objs?: Object[]) => void;

interface Row {
    values: string[];
}

interface ColumnDefinition {
    length: number;
    name: string;
    sqlType: string;
}

interface Result {
    resultType: string;
    updateCount: number;
    toString: string;
    columnDefinitions: ColumnDefinition[];
    rows: Row[];
}

interface JsonResponse {
    results: Result[];
}


abstract class JsonAjaxClient {

    public static intercept(response: string, objs: Object[]) {
        let jsonResponse: JsonResponse = JSON.parse(response);
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
        let callback: ResponseCallback = <ResponseCallback>(objs.pop());
        callback(jsonResponse, objs);
    }

    public static get(url: string, callback: ResponseCallback, objs: Object[]): void {
        objs.push(callback);
        AjaxClient.get(url, JsonAjaxClient.intercept, objs);
    }

    public static post(url: string, callback: ResponseCallback, data: string, objs: Object[]): void {
        objs.push(callback);
        AjaxClient.post(url, JsonAjaxClient.intercept, data, objs);
    }

    public static setDebug(debug: boolean): void {
        AjaxClient.debug = debug;
    }

    public static getUrl(): string {
        return AjaxClient.url;
    }

    public static setUrl(url: string): void {
        AjaxClient.url = url;
    }
}
