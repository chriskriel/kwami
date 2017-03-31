export let debug: boolean = false;
export let sqlTemplate: string = 'select [first 10] * from {} browse access;';

export function getFirstSql(tableName: string): string {
    return this.interpolate(this.sqlTemplate, tableName);
}

export function interpolate(template: string, ...values: string[]) {
    if (template === undefined || template === null)
        return null;
    var parts = template.split("{}");
    var i = 0;
    var result = parts[0];
    for (var j = 1; j < parts.length; j++)
        result += values[i++] + parts[j];
    return result;
}
