import { Panel, PanelType } from "Panel";
import { JsonResponse } from "AjaxClient";
import { ResultsDisplay } from "ResultsDisplay";

export class ResultPanel extends Panel {

    private resultsDisplay: ResultsDisplay;

    private constructor(id: string, heading: string) {
        super(PanelType.Result, id, heading);
        this.resultsDisplay = new ResultsDisplay(this);
    }

    public setStatement(stmnt: string) {
        this.resultsDisplay.setStatement(stmnt);
    }

    public addResults(resp: JsonResponse = null, filter?: number[]): void {
        this.resultsDisplay.addResults(resp, filter);
    }

    public static getInstance(): ResultPanel {
        let headTxt: string = "Result Panel " + Panel.nextPanelNumber();
        let x: ResultPanel = new ResultPanel(PanelType[PanelType.Result], headTxt);
        Panel.savePanel(x);
        return x;
    }
}