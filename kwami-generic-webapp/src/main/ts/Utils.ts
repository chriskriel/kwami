export class Utils {
    public static debug: boolean = false;
    public static sqlTemplate: string = 'select [first 10] * from {} browse access;';

    public static getFirstSql(tableName: string): string {
        return this.interpolate(this.sqlTemplate, tableName);
    }

    public static interpolate(template: string, ...values: string[]) {
        if (template === undefined || template === null)
            return null;
        var parts = template.split("{}");
        var i = 0;
        var result = parts[0];
        for (var j = 1; j < parts.length; j++)
            result += values[i++] + parts[j];
        return result;
    }
}
