/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DCInvestigator;

import hec2.wat.client.WatFrame;
import java.util.ArrayList;

/**
 *
 * @author Q0HECWPL
 */
class ErrorReport {
    private final ArrayList<String> Errors;
    private final ArrayList<ErrorLocation> LCMALT;
    public ErrorReport(){
        Errors = new ArrayList();
        LCMALT = new ArrayList<>();
    }
    public void AddErrorReport(String report, ErrorLocation lcma){
        Errors.add(report);
        LCMALT.add(lcma);
    }
    public void BulkAdd(ErrorReport otherReport){
        Integer counter = 0;
        for(ErrorLocation newLoc : otherReport.LCMALT){
            if(!this.LCMALT.contains(newLoc)){
                this.AddErrorReport(otherReport.Errors.get(counter), otherReport.LCMALT.get(counter));
            }
            counter ++;
        }
    }
    public boolean HasErrors(){
        return Errors.size()>0;
    }
    void WriteReport(WatFrame fr) {
        Integer counter = 0;
        fr.addMessage("There are " + LCMALT.size() + " error reports.");
        for(ErrorLocation el : LCMALT){
            fr.addMessage(el.Write() + Errors.get(counter));
            counter++;
        }
        fr.addMessage("Reported " + LCMALT.size() + " error reports.");
    }
}
