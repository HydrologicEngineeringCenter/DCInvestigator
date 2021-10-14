/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.rma.client.Browser;
import com.rma.client.BrowserAction;
import com.rma.model.Manager;
import com.rma.model.ManagerProxy;
import com.rma.model.Project;
import hec.io.PairedDataContainer;
import hec.model.OutputVariable;
import hec2.plugin.AbstractPlugin;
import hec2.plugin.model.ModelAlternative;
import hec2.wat.client.WatFrame;
import hec2.wat.model.FailedModelComputeTracker;
import hec2.wat.model.FrmSimulation;
import hec2.wat.model.ModelComputeTracker;
import hec2.wat.model.tracking.OutputTracker;
import hec2.wat.model.tracking.OutputVariableImpl;
import hec2.wat.plugin.SimpleWatPlugin;
import hec2.wat.plugin.WatPluginManager;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;

import rma.swing.RmaImage;

/**
 * The purpose of this plugin is to do the following processes
 * 1. Check to see if each Output Variable exists
 * 2. Check to see if any Event level outputs are missing and generate a report (possibly check the report against the modelResults.xml file)
 *    2.a - make sure that only valid data is in each paired data record, truncate the collections to the length of the lifecycle.
 * 3. If all data is good, ensure frequency curves have been created."
 * 4. Check for convergence criteria and report out what has converged. 
 * @author WatPowerUser
 */
public class DCInvestigatorPlugin extends AbstractPlugin implements SimpleWatPlugin {
    public static final String PluginName = "Distributed Compute Investigator Plugin";
    public static final String PluginShortName = "DCInvestigator";
    private static final String _pluginVersion = "1.0.0";
    private DCInvestigatorSettings _settings;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DCInvestigatorPlugin p = new DCInvestigatorPlugin();
    }
    public DCInvestigatorPlugin(){
        super();
        setName(PluginShortName);
        WatPluginManager.register(this);
        if ( isAppInstalled())
        {
                addToToolsToolbar();
        }
    }
    protected void addToToolsToolbar()
    {
        Icon i = RmaImage.getImageIcon("Images/Workstation.gif");
        BrowserAction a = new BrowserAction("Fixer",i,this, "displayDCInvestigator");
        a.putValue(Action.SHORT_DESCRIPTION, getName());
        Browser.getBrowserFrame().getToolsMenu().insert(a,3);
        ((WatFrame)Browser.getBrowserFrame()).getToolsToolbarGroup().add(a);
    }
    @Override
    public boolean createProject(Project prjct) {
        return true;
    }
    @Override
    public boolean openProject(Project prjct) {
        return true;//called when the user is asked to open an existing project.
    }
    @Override
    public boolean close(boolean bln) {
        return true;
    }
    @Override
    public String getProjectName() {
        return "";
    }
    @Override
    public boolean saveProject() {
        return true;
    }
    @Override
    public String getLogfile() {
        return null;
    }
    public boolean displayDCInvestigator(){
        return displayApplication();
    }
    @Override
    public boolean displayApplication() {
        Project proj = Browser.getBrowserFrame().getCurrentProject();
        String dir = proj.getProjectDirectory();
        WatFrame fr = hec2.wat.WAT.getWatFrame();
        if(dir!=null){
            _settings = new DCInvestigatorSettings(dir + "\\" + PluginShortName + "\\" + PluginShortName + ".props");
        }else{
            return false;
        }

        /*//process the bin sizes into weights. by dividing by total number of events per bin
        //get the simulation 
        List<ManagerProxy> managerProxyListForType = proj.getManagerProxyListForType(FrmSimulation.class);
        Manager m = null;
        FrmSimulation frm = null;
        OutputTracker ot = null;
        for(ManagerProxy mp : managerProxyListForType){
            if(mp.getName().equals(_settings.getSimulation())){
                //do stuff
                m = mp.getManager();
                frm = (FrmSimulation)m;//get the FRM simulation object
                ot =frm.getOutputTracker();//get the outputTracker object
                //fr.addMessage("Found simulation ALTP_NOBA");
            }else{
                //dont do stuff
                
            }
        }

        FailedModelComputeTracker fmct = new FailedModelComputeTracker(frm);
        fmct.saveData();

        if(ot!=null){
            fr.addMessage("Output Tracker found");
            List<OutputVariable> outlist = fr.getOutputVariables(frm, true);
            //cycle through all output variables and check to ensure that the data exists.
            List<List<OutputVariableImpl>> varListList = ot.getVarListList();
            List<ModelAlternative> models = frm.getAllModelAlternativeList();//frm is not null because ot was retrieved from it.

            ErrorReport masterList = new ErrorReport();
            for(int i = 0;i<varListList.size();i++){
                ModelAlternative modelAlt = models.get(i);//varlistlist is an array of output variables stored by model alternative.
                List<OutputVariableImpl> variablesForModel = varListList.get(i);
                if (variablesForModel != null) {
                    int size = variablesForModel.size();
                    for (int j = 0; j < size; j++) {
                        OutputVariableImpl v = variablesForModel.get(j);
                        PairedDataContainer pdc = v.getPairedDataContainer();
                        List<PairedDataContainer> pdcList = v.getAllPairedDataList();
                        ArrayList<Integer> totalReal = new ArrayList<>();
                        int propReal = Integer.parseInt(_settings.getTotalRealizations());
                        int realization = 0;
                        for(PairedDataContainer pdci : pdcList){
                            //check the validity of the output

                            //Extracts realization number from dss record
                            String initialSim= pdci.toString();
                            String[] simParse = initialSim.split("/",0);
                            String realCollection = simParse[6].substring(2,8);
                            Integer curRealization = Integer.parseInt(realCollection);

                            //Stores realization number in arraylist
                            totalReal.add(curRealization);

                            ErrorReport err = checkValidityofOutputVariable(v,pdci,frm,realization,modelAlt);
                            if(err.HasErrors()){
                                masterList.BulkAdd(err);
                            }
                            realization++;
                        }
                        if(totalReal.size() != propReal) {
                            fr.addMessage("Number of computed Realizations, " + totalReal.size() + ", is not equal to your set realizations, " + propReal + ", for " + v + ". " +
                                    "Review your simulation .dss file or your properties file");
                        }

                    }
                }
            }
            masterList.WriteReport(fr);
            masterList.WriteLifecycleReport(fr);
        }
        else{
            fr.addMessage("A WAT simulation named "+_settings.getSimulation()+" was not found, please check your simulation names, and fix the \\DCInvestigator\\DCInvestigator.props file to contain the name of the simulation you wish to destratify.");
            return false;
        }*/
        return true;
    }
  /*  private ErrorReport checkValidityofOutputVariable(OutputVariableImpl vv, PairedDataContainer outPdc, FrmSimulation frm, int real, ModelAlternative modelAlt){
        ErrorReport errors = new ErrorReport();
        int numlifecycles = frm.getNumberLifeCycles();
        int numreals = frm.getNumberRealizations();
        int numLifecyclesPerReal = numlifecycles/numreals;
        
        int numEventsperLifecycle = frm.getYearsInRealization()/numLifecyclesPerReal;
        int numOrdinates = outPdc.numberOrdinates;//should be number of events in the lifecycles?
        int numCurves = outPdc.numberCurves; //should be nubmer of lifecycles
        if(numOrdinates!=numEventsperLifecycle){
            //errors.AddErrorReport("there is an inconsistency between the number of events per lifecycle", new LifeCycleMAlt(modelAlt.getName(), real));
            //frm.addMessage("there is an inconsistency between the number of events per lifecycle there should be and how many ordinates are stored.");
        }
        if(numCurves!=(numLifecyclesPerReal)){
            errors.AddErrorReport("the number of collection memebers(" + numCurves +") does not match lifecycles (" + numLifecyclesPerReal +"), check realization " + real + " for variable " + vv.getName(),new ErrorLocation(modelAlt.getName(), real, true));
            //frm.addMessage("there are more curves than lifecycles per real, ignoring old data");
            //numCurves = numLifecyclesPerReal;
        }
        int prevLifecycles = real*numLifecyclesPerReal;

        //Extracts realization number from dss record
        String initialSim= outPdc.toString();
        String[] simParse = initialSim.split("/",0);
        String realCollection = simParse[6].substring(2,8);

        for (int curve = 0; curve < numCurves; curve++) {
            double[] yOrd = outPdc.yOrdinates[curve];//this is the number of lifecycles
            String lab = outPdc.labels[curve];
            String[] lifecycleParse = lab.split(" ", 2);
            Integer lifecycle = Integer.parseInt(lifecycleParse[1]);
            for (int ord = 0; ord < numOrdinates; ord++) {
                if(yOrd[ord]==Double.NaN || yOrd[ord]==0){
                    //bad
                    errors.AddErrorReport("Variable " + vv.getName() + " has a NaN at realization " + (realCollection) + " " + (lab) + " event " + (ord +1),new ErrorLocation(modelAlt.getName(), lifecycle, false));
                }else if(yOrd[ord]<0){
                    errors.AddErrorReport("Variable " + vv.getName() + " has a value less than zero at realization " + (realCollection) + " " + (lab) + " event " + (ord +1),new ErrorLocation(modelAlt.getName(), lifecycle, false));
                }
                else{
                    //good
                } 
            }
        }
        return errors;
    }*/
    @Override
    public String getVersion() {
        return _pluginVersion;
    }
    @Override
    public String getDirectory() {
        return "";
    }
    private boolean isAppInstalled() {
            return true;
    }
}
