import { Panel, PanelType } from "Panel";
import { JsonResponse } from "RestConnector";
import { ResultsDisplay } from "ResultsDisplay";

export class ResultPanel extends Panel {

    private resultsDisplay: ResultsDisplay;

    constructor(id: string, heading: string) {
        super(PanelType.Result, id, heading);
        this.resultsDisplay = new ResultsDisplay(this);
    }

    public setStatement(stmnt: string) {
        this.resultsDisplay.setStatement(stmnt);
    }

    public addResults(resp: JsonResponse = null, filter?: number[]): void {
        this.resultsDisplay.addResults(resp, filter);
    }
}