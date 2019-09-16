/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fixerPlugin;
import com.rma.client.Browser;
import com.rma.client.BrowserAction;
import com.rma.io.DssFileManagerImpl;
import com.rma.model.Manager;
import com.rma.model.ManagerProxy;
import com.rma.model.Project;
import hec.heclib.dss.DSSPathname;
import hec.io.PairedDataContainer;
import hec.model.OutputVariable;
import hec2.plugin.AbstractPlugin;
import hec2.plugin.model.ModelAlternative;
import hec2.wat.client.WatFrame;
import hec2.wat.model.FrmSimulation;
import hec2.wat.model.tracking.OutputTracker;
import hec2.wat.model.tracking.OutputVariableImpl;
import hec2.wat.plugin.SimpleWatPlugin;
import hec2.wat.plugin.WatPluginManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import org.apache.commons.lang.ArrayUtils;
import rma.swing.RmaImage;
import rma.util.RMAIO;
/**
 *
 * @author WatPowerUser
 */
public class FrequencyFixerPlugin extends AbstractPlugin implements SimpleWatPlugin {
    public static final String PluginName = "Frequency Fixer Plugin";
    public static final String PluginShortName = "Fixer";
    private static final String _pluginVersion = "1.0.0";
    private static String _simulationName = "Validation";
    private static final String _propertiesPath = "/FrequencyFixer/FrequencyFixer.props";
    private static List<Double> _XOrds;
    private static List<Double> _CI_Vals;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FrequencyFixerPlugin p = new FrequencyFixerPlugin();
    }
    public FrequencyFixerPlugin(){
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
        BrowserAction a = new BrowserAction("Fixer",i,this, "displayApplicationUniqueF");
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
    public boolean displayApplicationUniqueF(){
        return displayApplication();
    }
    @Override
    public boolean displayApplication() {
        Project proj = Browser.getBrowserFrame().getCurrentProject();
        String dir = proj.getProjectDirectory();
        WatFrame fr = null;
        if(dir!=null){
            fr = hec2.wat.WAT.getWatFrame();
        }else{
            return false;
        }
        //read in properties from properties file
        String propertiesFile = dir + _propertiesPath;
        String propertyLine = "";
        BufferedReader brp = null;
        File pf = new File(propertiesFile);
        if(pf.exists()){
            fr.addMessage("Properties found");
            try {
                brp = new BufferedReader(new FileReader(propertiesFile));
                String[] tmp = null;
                while ((propertyLine = brp.readLine()) != null) {
                    tmp = propertyLine.split(",");
                    if(tmp.length==0){continue;}
                    if(tmp[0].equals("SimulationName")){
                        _simulationName = tmp[1];
                    }else if(tmp[0].equals("XOrds")){
                        _XOrds = new ArrayList<>();
                        for(String s: tmp){
                            if(s.equals("XOrds")){continue;}
                            _XOrds.add(1-Double.parseDouble(s));
                            fr.addMessage(s); 
                        }
                        fr.addMessage("There are " + _XOrds.size()); 
                        
                    }else if(tmp[0].equals("CI_Vals")){
                        _CI_Vals = new ArrayList<>();
                        for(String s: tmp){
                            if(s.equals("CI_Vals")){continue;}
                            _CI_Vals.add(Double.parseDouble(s));
                        }                       
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();               
            } finally {
                if (brp != null) {
                     try {
                             brp.close();
                     } catch (IOException e) {
                             e.printStackTrace();
                     }
                }
            }            
        }

        //read in weights from dir/shared/binweights_by_lifecycle.txt
        String weightFile = dir + "/shared/binweights_by_lifecycle.txt";
        String line = "";
        double prob;
        double startProb = 0.0;
        double endProb = 0.0;
        List<Double> weights = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(weightFile));
            startProb = Double.parseDouble(br.readLine().split(",")[1]);//first value is the start of the first bin
            String[] tmp = null;
            while ((line = br.readLine()) != null) {
                tmp = line.split(",");
                if(tmp.length==0){continue;}
                if(tmp[0].equals("above")){
                    endProb = Double.parseDouble(tmp[1]);
                }else{
                    prob = Double.parseDouble(tmp[1]);
                    weights.add(prob);
                }
            }
            fr.addMessage("Weights found");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fr.addMessage("Weights not found, please place the binweights_by_lifecycle.txt file in the shared directory of the WAT project.");
        } catch (IOException e) {
            e.printStackTrace();
            fr.addMessage("Weights not accessable.");               
        } finally {
            if (br != null) {
                 try {
                         br.close();
                 } catch (IOException e) {
                         e.printStackTrace();
                 }
            }
        }
        if(br!=null){
            try {
                    br.close();
            } catch (IOException e) {
                    e.printStackTrace();
            }   
        }
        if(weights.size()==0)return false;
        //process the bin sizes into weights. by dividing by total number of events per bin
        //get the simulation 
        List<ManagerProxy> managerProxyListForType = proj.getManagerProxyListForType(FrmSimulation.class);
        Manager m = null;
        FrmSimulation frm = null;
        OutputTracker ot = null;
        for(ManagerProxy mp : managerProxyListForType){
            if(mp.getName().equals(_simulationName)){
                //do stuff
                m = mp.getManager();
                frm = (FrmSimulation)m;//get the FRM simulation object
                ot =frm.getOutputTracker();//get the outputTracker object
                //fr.addMessage("Found simulation ALTP_NOBA");
            }else{
                //dont do stuff
                
            }
        }
        if(ot!=null){
            fr.addMessage("Output Tracker found");
            List<OutputVariable> outlist = fr.getOutputVariables(frm, true);
            List<OutputVariable> freqList = new ArrayList<>();
            //cycle through all output variables and check to ensure that Frequency Curves Output Variables exist for each output variable. 
            //a frequency output variable must exist for the frequency viewer to view it.
            List<List<OutputVariableImpl>> varListList = ot.getVarListList();
            List<List<OutputVariableImpl>> freqvarListList = new ArrayList<>();
            for(int i = 0;i<varListList.size();i++){
                
                List<OutputVariableImpl> variablesForModel = varListList.get(i);
                List<OutputVariableImpl> freqVarForModel = new ArrayList<>();
                for(int j =0;j<variablesForModel.size();j++){
                    //fr.addMessage("Output Variable Found: " + variablesForModel.get(j).getName());
                    OutputVariableImpl c = (OutputVariableImpl) variablesForModel.get(j).clone();//clone the output variables to have frequency curves created.
                    c.setHasFrequency(true);
                    freqVarForModel.add(c);                    
                }
                freqvarListList.add(freqVarForModel);
            }
            ot.setFreqVarListList(freqvarListList);//now they exist on the list, set the data
            frm.saveData();//not null because ot was retrieved from it...
            //now compute frequency with weights for all frequency curves.
            varListList = ot.getVarListList();
            List<ModelAlternative> models = frm.getAllModelAlternativeList();
            for(int i = 0;i<varListList.size();i++){
                ModelAlternative modelAlt = models.get(i);
                List<OutputVariableImpl> variablesForModel = varListList.get(i);
                if (variablesForModel != null) {
                    int size = variablesForModel.size();
                    for (int j = 0; j < size; j++) {
                        OutputVariableImpl v = variablesForModel.get(j);
                        PairedDataContainer pdc = v.getPairedDataContainer();
                        if (pdc.fileName == null || pdc.fileName.isEmpty()) {
                            String runDir = frm.getSimulationDirectory();
                            runDir = runDir.concat(RMAIO.userNameToFileName(frm.getName())).concat(".dss");
                            pdc.fileName = runDir;
                            //fr.addMessage(runDir);
                        }
                        if (pdc.fullName == null || pdc.fullName.isEmpty()) {
                            DSSPathname path = ot.buildDSSPathname(frm, modelAlt, v);
                            path.setCollectionSequence(0);
                            pdc.fullName = path.getPathname();
                            //fr.addMessage(pdc.fullName);
                        }
                        //fr.addMessage(pdc.fileName);
                        //fr.addMessage(pdc.fullName);
                        //frm.addMessage(frm.getName() + ":" + modelAlt.getProgram() + "-" + modelAlt.getName()
                        //                + ":Computing weighted output variable frequency curve " + (j + 1) + "/" + size);
                        OutputVariableImpl freqVar = ot.getFreqVarForOutputVar(v, i);
                       // fr.addMessage("Computing weighted output variable frequency curve for " + freqVar._name);
                        List<PairedDataContainer> pdcList = v.getAllPairedDataList();
                       // fr.addMessage(freqVar._name + " has " + pdcList.size() + " realizations");
                        freqVar.deleteAllFrequencyPairedData();//tidy up.
                        List<ValueBinIncrementalWeight[]> allData = new ArrayList<>();
                        int realization = 0;
                        for(PairedDataContainer pdci : pdcList){//build the frequency output
                            freqVar.setPairedDataContainer(pdci);
                            ValueBinIncrementalWeight[] tmp = saveVariableFrequencyRealization(freqVar,pdci,frm,startProb,endProb,weights,realization);
                            //saveVariableFrequencyRealization_Thin(freqVar,pdci,frm,startProb,endProb,weights,realization);
                            if(tmp==null){
                                fr.addMessage("aborting frequency curve calculation.");
                                return false;
                            }
                            
                            //sort it to be ascending for the thinned work...
                            //Arrays.sort(tmp);
                            allData.add(tmp);
                            realization++;
                            //fr.addMessage(freqVar._name + " realization " + allData.size() + " computed.");
                            
                        }
                        //double[][] allData = getVariableAllFrequencyData(freqVar,frm);
                        //saveVariableFrequencyPercent(freqVar, allData,frm); //5 and 95 percent

                        ValueBinIncrementalWeight[] fullCurve = saveVariableFrequencyFull(freqVar, allData, frm,endProb,startProb,weights);
                        if(fullCurve!=null){
                            if(_XOrds!=null){
                                saveVariableFrequencyConfidenceLimits(freqVar, fullCurve, frm,endProb,startProb,weights);
                            }
                        
                        }else{
                            //fr.addMessage("Simulation thinning didnt work.");
                            return false;
                        }
                        if(fullCurve!=null){
                        //saveVariableFrequencyFull_Thin(freqVar, fullCurve, frm,endProb,startProb,weights);
                        }else{
                            //fr.addMessage("Simulation thinning didnt work.");
                            return false;
                        }
                    }
                }
            }
        }else{
            fr.addMessage("A WAT simulation named "+_simulationName+" was not found, please check your simulation names, and fix the \\FrequencyFixer\\FrequencyFixer.props file to contain the name of the simulation you wish to destratify.");
            return false;
        }
        return true;
    }
//    private void saveVariableFrequencyRealization_Thin(OutputVariableImpl vv, PairedDataContainer outPdc, FrmSimulation frm,Double startProb, double endProb, List<Double> weights, int real){
//        //BUILD DATA
//        int newOrdinates = frm.getYearsInRealization();
//        double[] newXOrd = new double[newOrdinates];
//        int numlifecycles = frm.getNumberLifeCycles();
//        int numreals = frm.getNumberRealizations();
//
//        int numOrdinates = outPdc.numberOrdinates;//should be number of events in the lifecycles?
//        int numCurves = outPdc.numberCurves; //should be nubmer of lifecycles
//
//        if(numCurves!=(numlifecycles/numreals)){
//            frm.addMessage("there are more curves than lifecycles per real, ignoring old data");
//            numCurves = numlifecycles/numreals;
//        }
//        if(numCurves!=weights.size()){
//            frm.addMessage("Weight count does not match lifecycle count");
//            return ;
//        }
//        double totWeight = startProb;
//        totWeight+=endProb;
//        for (int k = 0; k < weights.size(); k++) { 
//            totWeight+=weights.get(k);
//        }
//        int availOrdinates = numOrdinates * numCurves;
//        double[] newYOrd = new double[Math.max(availOrdinates, newOrdinates)];
//        //String s = "weight, value, bin\n";
//        double cumWeight = startProb;//0.0;
//        double minVal = Double.POSITIVE_INFINITY;
//        double maxVal = Double.NEGATIVE_INFINITY;
//        ValueBinIncrementalWeight[][] binnedData = new ValueBinIncrementalWeight[numCurves][numOrdinates];
//        for (int curve = 0; curve < numCurves; curve++) {
//            double[] yOrd = outPdc.yOrdinates[curve];//this is the number of lifecycles
//            //frm.addMessage("Lifecycle " + + curve +" has weight " + weights.get(curve));
//            for (int ord = 0; ord < numOrdinates; ord++) {
//                int newIdx = curve * numOrdinates + ord;//this is putting the events in a master realization list in order
//                if(yOrd[ord]>maxVal){maxVal=yOrd[ord];}
//                if(yOrd[ord]<minVal){minVal=yOrd[ord];}
//                binnedData[curve][ord] = new ValueBinIncrementalWeight(yOrd[ord],curve,weights.get(curve)/numOrdinates,newIdx);
//                //s += binnedData[curve][ord].getWeight() + ", " + binnedData[curve][ord].getValue() + "\n";
//            }
//            Arrays.sort(binnedData[curve]);
//        }
//        int numOrdsForSmoothing = 100;
//        double delta = (maxVal-minVal)/numOrdsForSmoothing;
//        double smoothVals[] = new double[numOrdsForSmoothing];
//        int prevValueIdx = 0;
//        double[][] binSmoothProbs = new double[numCurves][numOrdsForSmoothing];
//        for(int smooth=0;smooth<numOrdsForSmoothing; smooth++){
//            smoothVals[smooth] = minVal +((smooth+1)*delta);
//        }
//        for (int curve = 0; curve < numCurves; curve++) {
//            prevValueIdx = 0;
//            for(int smooth=0;smooth<numOrdsForSmoothing; smooth++){
//                binSmoothProbs[curve][smooth] = 0;
//                for (int ord = prevValueIdx; ord < numOrdinates; ord++) {
//                    if(binnedData[curve][ord].getValue()>=smoothVals[smooth]){
//                        binSmoothProbs[curve][smooth] = 1.0-((double)ord/(double)numOrdinates);
//                        prevValueIdx = ord;
//                        break;
//                    }
//                }
//            }
//        }
//        double[] xOrdinates = new double[numOrdsForSmoothing];
//        for(int smooth=0;smooth<numOrdsForSmoothing; smooth++){
//            xOrdinates[smooth] = 0;
//            for (int curve = 0; curve < numCurves; curve++) {
//                xOrdinates[smooth] += weights.get(curve)*binSmoothProbs[curve][smooth];
//            }
//        }
//        //SAVE PDC
//        PairedDataContainer freqPdc = vv.getPairedDataContainer();
//        PairedDataContainer thinPdc = new PairedDataContainer();
//        
//        freqPdc.clone(thinPdc);
//        DSSPathname pathname = new DSSPathname(thinPdc.fullName);
//	pathname.setDPart(OutputVariableImpl.LABEL_FREQ_THIN + " Realization");
//
//        thinPdc.numberOrdinates = numOrdsForSmoothing;
//        thinPdc.numberCurves = 1;
//        thinPdc.xOrdinates = xOrdinates;
//        thinPdc.yOrdinates = new double[][]{smoothVals};
//        thinPdc.xparameter = "Probability";
//        thinPdc.labelsUsed = true;
//        thinPdc.labels = new String[1];
//
//        final String sequence = DSSPathname.getCollectionSequence(outPdc.fullName); // first is 000000
//        Integer realization = Integer.valueOf(sequence);
//        realization++;  // plus one b/c we are going to show this to the user.
//        pathname.setCollectionSequence(sequence);
//        thinPdc.fullName = pathname.getPathname(false);
//        
//        thinPdc.labels[0] = "Realization ".concat(realization.toString());
//        //write to csv//
//        //String fileloc = "C:\\Temp\\Weights\\" + vv._name + "_R_" + realization +".txt";
////        try{
////            BufferedWriter bw = new BufferedWriter(new FileWriter(fileloc));
////            bw.write(s);
////            bw.flush();
////            bw.close();
////        } catch (IOException ex) {
////            Logger.getLogger(FrequencyFixerPlugin.class.getName()).log(Level.SEVERE, null, ex);
////        }
//        int zeroOnSuccess = DssFileManagerImpl.getDssFileManager().write(thinPdc);
//        if (zeroOnSuccess != 0) {
//                frm.addWarningMessage("Failed to save PD Output Variable Frequency to "
//                                + outPdc.fileName + ":" + outPdc.fullName + " rv=" + zeroOnSuccess);
//        }
//        return ;
//    }
//    protected void saveVariableFrequencyFull_Thin(OutputVariableImpl vv, ValueBinIncrementalWeight[] fullCurve, FrmSimulation frm, double endProb, double startProb, List<Double> weights) {
//        int realsPerWeightList = 1;
//        double totWeight = startProb;
//        totWeight+=endProb;
//        for (int k = 0; k < weights.size(); k++) { 
//            totWeight+=weights.get(k);
//        }
//        double minVal = Double.POSITIVE_INFINITY;
//        double maxVal = Double.NEGATIVE_INFINITY;
//        frm.addMessage("total weight is " + totWeight); 
//        
//        //sort by bin (ascending
//        ValueBinIncrementalWeight.setSort(false);
//        Arrays.sort(fullCurve);
//        //separate into bin arrays.
//        List<ValueBinIncrementalWeight[]> binnedWeight = new ArrayList<>();
//        int currentBin = fullCurve[0].getBin();
//        int numEventsPerBin =0;
//        for(ValueBinIncrementalWeight event: fullCurve){
//            if(currentBin==event.getBin()){
//                numEventsPerBin++;
//            }else{
//                break;
//            }
//        }
//        binnedWeight.add(new ValueBinIncrementalWeight[numEventsPerBin]);
//        int currentEvent = 0;
//        //sort by value
//        ValueBinIncrementalWeight.setSort(true);
//        
//        for(ValueBinIncrementalWeight event: fullCurve){
//            if(minVal>event.getValue()){minVal = event.getValue();}
//            if(maxVal<event.getValue()){maxVal = event.getValue();}
//            if(currentBin == event.getBin()){
//                //add it to a list
//                binnedWeight.get(currentBin)[currentEvent] = event;
//                currentEvent++;
//            }else{
//                Arrays.sort(binnedWeight.get(currentBin));
//                currentBin = event.getBin();
//                currentEvent = 0;
//                binnedWeight.add(new ValueBinIncrementalWeight[numEventsPerBin]);
//                binnedWeight.get(currentBin)[currentEvent] = event;
//                currentEvent++;
//            }
//        }
//        
//        //create the y values of interest.
//        int numOrdsForSmoothing = 100;
//        double delta = (maxVal-minVal)/numOrdsForSmoothing;
//        double smoothVals[] = new double[numOrdsForSmoothing];
//        int prevValueIdx = 0;
//        double[][] binSmoothProbs = new double[binnedWeight.size()][numOrdsForSmoothing];
//        for(int smooth=0;smooth<numOrdsForSmoothing; smooth++){
//            smoothVals[smooth] = minVal +((smooth+1)*delta);
//        }
//        for (int bin = 0; bin < binnedWeight.size(); bin++) {
//            prevValueIdx = 0;
//            for(int smooth=0;smooth<numOrdsForSmoothing; smooth++){
//                binSmoothProbs[bin][smooth] = 0;
//                for (int ord = prevValueIdx; ord < binnedWeight.get(bin).length; ord++) {
//                    if(binnedWeight.get(bin)[ord].getValue()>=smoothVals[smooth]){
//                        binSmoothProbs[bin][smooth] = 1.0 -((double)ord/(double)numEventsPerBin);
//                        prevValueIdx = ord;
//                        break;
//                    }
//                }
//            }
//        }
//        double[] xOrdinates = new double[numOrdsForSmoothing];
//        for(int smooth=0;smooth<numOrdsForSmoothing; smooth++){
//            xOrdinates[smooth] = 0;
//            for (int bin = 0; bin < binnedWeight.size(); bin++) {
//                xOrdinates[smooth] += weights.get(bin)*binSmoothProbs[bin][smooth];
//            }
//        }
//
//        //SAVE FULL PDCs
//        PairedDataContainer freqPdc = vv.getFullFrequencyPairedData();
//        PairedDataContainer thinPdc = new PairedDataContainer();
//        
//        freqPdc.clone(thinPdc);
//        
//        freqPdc.numberOrdinates = numOrdsForSmoothing;
//        freqPdc.numberCurves = 1;
//        freqPdc.xOrdinates = xOrdinates;
//        freqPdc.yOrdinates = new double[][]{smoothVals};
//        freqPdc.xparameter = "Probability";
//        freqPdc.labelsUsed = true;
//        freqPdc.labels = new String[1];
//        freqPdc.labels[0] = "All Realizations";
//        DSSPathname pathname = new DSSPathname(freqPdc.fullName);
//	pathname.setDPart(OutputVariableImpl.LABEL_FREQ_THIN);
//        freqPdc.fullName = pathname.getPathname(true);
//        int zeroOnSuccess = DssFileManagerImpl.getDssFileManager().write(freqPdc);
//        if (zeroOnSuccess != 0) {
//                frm.addWarningMessage("Failed to save PD Output Variable Frequency to "
//                                + freqPdc.fileName + ":" + freqPdc.fullName + " rv=" + zeroOnSuccess);
//        }
//    }
        private ValueBinIncrementalWeight[] saveVariableFrequencyRealization(OutputVariableImpl vv, PairedDataContainer outPdc, FrmSimulation frm,Double startProb, double endProb, List<Double> weights, int real){
        //BUILD DATA
        int newOrdinates = frm.getYearsInRealization();
        double[] newXOrd = new double[newOrdinates];
        int numlifecycles = frm.getNumberLifeCycles();
        int numreals = frm.getNumberRealizations();

        int numOrdinates = outPdc.numberOrdinates;//should be number of events in the lifecycles?
        int numCurves = outPdc.numberCurves; //should be nubmer of lifecycles

        if(numCurves!=(numlifecycles/numreals)){
            frm.addMessage("there are more curves than lifecycles per real, ignoring old data");
            numCurves = numlifecycles/numreals;
        }
        int realsPerWeightList = 1;
        if(numCurves!=weights.size()){
            frm.addMessage("Weight count does not match lifecycle count");
            double val = (double)weights.size()/(double)numCurves;
            if((val-java.lang.Math.floor(val))>0){
                frm.addMessage("Weight count is not evenly divisible by lifecycle count");
                return null;
            }else{
                realsPerWeightList = (int)val;
            }
        }
        //frm.addMessage("There are " + realsPerWeightList + " realizations per the list of weights");
        double totWeight = startProb;
        totWeight+=endProb;
        for (int k = 0; k < weights.size(); k++) { 
            totWeight+=weights.get(k);
        }
        int availOrdinates = numOrdinates * numCurves;
        double[] newYOrd = new double[Math.max(availOrdinates, newOrdinates)];
        double cumWeight = endProb;
        ValueBinIncrementalWeight[] data= new ValueBinIncrementalWeight[Math.max(availOrdinates, newOrdinates)];
        int shifter = real % realsPerWeightList;
        for (int curve = 0; curve < numCurves; curve++) {
            double[] yOrd = outPdc.yOrdinates[curve];//this is the number of lifecycles
            for (int ord = 0; ord < numOrdinates; ord++) {
                int newIdx = curve * numOrdinates + ord;//this is putting the events in a master realization list in order
                data[newIdx] = new ValueBinIncrementalWeight(yOrd[ord],(shifter*(numCurves))+ curve,weights.get((shifter*(numCurves))+ curve)/numOrdinates, real);
            }
        }
        
        Arrays.sort(data);
        ArrayUtils.reverse(data);
        //String s = "Value, Bin, EventNumber\n";
        for(int i = 0; i<data.length;i++){
            newYOrd[i] = data[i].getValue();
            cumWeight += data[i].getIncrimentalWeight();
            newXOrd[i] = (cumWeight - (data[i].getIncrimentalWeight())/2)/totWeight;//plotting position
            data[i].setPlottingPosition(newXOrd[i]);
            //s += data[i].getValue() + ", " + data[i].getBin() + ", " + data[i].getEventNumber()+ "\n";
        }

        //SAVE PDC
        PairedDataContainer freqPdc = vv.getPairedDataContainer();
        freqPdc.numberOrdinates = newOrdinates;
        freqPdc.numberCurves = 1;
        freqPdc.xOrdinates = newXOrd;
        freqPdc.yOrdinates = new double[][]{newYOrd};
        freqPdc.xparameter = "Probability";
        freqPdc.labelsUsed = true;
        freqPdc.labels = new String[1];

        final String sequence = DSSPathname.getCollectionSequence(outPdc.fullName); // first is 000000
        Integer realization = Integer.valueOf(sequence);
        realization++;  // plus one b/c we are going to show this to the user.

        freqPdc.labels[0] = "Realization ".concat(realization.toString());
        //write to csv//
//        String fileloc = frm.getProject().getProjectDirectory() + "\\Weights_TextFiles\\" + vv._name + "_R_" + realization +".txt";//needs to be stored in the correct location.
//        File destFileDirPath = new File(frm.getProject().getProjectDirectory() + "\\Weights_TextFiles\\");
//        if(!destFileDirPath.exists()){
//            destFileDirPath.mkdirs();
//        }
//        try{
//            BufferedWriter bw = new BufferedWriter(new FileWriter(fileloc));
//            bw.write(s);
//            bw.flush();
//            bw.close();
//        } catch (IOException ex) {
//            Logger.getLogger(FrequencyFixerPlugin.class.getName()).log(Level.SEVERE, null, ex);
//        }
        int zeroOnSuccess = DssFileManagerImpl.getDssFileManager().write(freqPdc);
        if (zeroOnSuccess != 0) {
                frm.addWarningMessage("Failed to save PD Output Variable Frequency to " + outPdc.fileName + ":" + outPdc.fullName + " rv=" + zeroOnSuccess);
        }
        return data;
    }
    protected ValueBinIncrementalWeight[] saveVariableFrequencyFull(OutputVariableImpl vv, List<ValueBinIncrementalWeight[]> allData, FrmSimulation frm, double endProb, double startProb, List<Double> weights) {
        int colSize = allData == null ? 0 : allData.size();//number of realizations?
        int numOrds = colSize <= 0 ? 0 : allData.get(0).length;
        //colSize = number of curves
        //numOrds = number of ordinates in each curve
        int realsPerWeightList = 1;
        int realizations = frm.getNumberRealizations();
        int lifecycles = frm.getNumberLifeCycles();
        lifecycles = lifecycles/realizations;
        if(lifecycles!=weights.size()){
            frm.addMessage("Weight count does not match lifecycle count");
            double val = (double)weights.size()/(double)lifecycles;
            if((val-java.lang.Math.floor(val))>0){
                frm.addMessage("Weight count is not evenly divisible by lifecycle count");
                return null;
            }else{
                realsPerWeightList = (int)val;
            }
        }
        frm.addMessage("There are " + realsPerWeightList + " realizations per the list of weights");
        int fullSize = colSize * numOrds;
        double totWeight = startProb;
        totWeight+=endProb;
        for (int k = 0; k < weights.size(); k++) { 
            totWeight+=weights.get(k);
        }
        
        ValueBinIncrementalWeight[] fullCurve = new ValueBinIncrementalWeight[fullSize];
        for (int i = 0; i < colSize; i++) {
                ValueBinIncrementalWeight[] colData = allData.get(i);
                System.arraycopy(colData, 0, fullCurve, i * numOrds, numOrds);
        }
        allData = null;
        //sort the full curve
        Arrays.sort(fullCurve);
        ArrayUtils.reverse(fullCurve);

        //build full curve and save - new pdc
        double[] xOrdinates = new double[fullSize];
        double[] yOrdinates = new double[fullSize];
        double cumWeight = endProb;
        int scaleFactor =realizations/realsPerWeightList;
        //write to text file for greg
//        String s = "Plotting Position, Value\n";
        for (int k = 0; k < fullSize; k++) {
            cumWeight += fullCurve[k].getIncrimentalWeight()/scaleFactor;
            xOrdinates[k] = (cumWeight-((fullCurve[k].getIncrimentalWeight()/scaleFactor)/2))/totWeight;
            yOrdinates[k] = fullCurve[k].getValue();
//            s += xOrdinates[k] + ", " + yOrdinates[k] + "\n";
        }
//        String fileloc = frm.getProject().getProjectDirectory() + "\\Weights_TextFiles\\" + vv._name + "_Lift_"+ _liftNumber +"_FrequencyFull.txt";
//        File destFileDirPath = new File(frm.getProject().getProjectDirectory() + "\\Weights_TextFiles\\");
//        if(!destFileDirPath.exists()){
//            destFileDirPath.mkdirs();
//        }
//        try{
//            BufferedWriter bw = new BufferedWriter(new FileWriter(fileloc));
//            bw.write(s);
//            bw.flush();
//            bw.close();
//        } catch (IOException ex) {
//            Logger.getLogger(FrequencyFixerPlugin.class.getName()).log(Level.SEVERE, null, ex);
//        }
        //SAVE FULL PDCs
        PairedDataContainer freqPdc = vv.getFullFrequencyPairedData();

        freqPdc.numberOrdinates = fullSize;
        freqPdc.numberCurves = 1;
        freqPdc.xOrdinates = xOrdinates;
        freqPdc.yOrdinates = new double[][]{yOrdinates};
        freqPdc.xparameter = "Probability";
        freqPdc.labelsUsed = true;
        freqPdc.labels = new String[1];
        freqPdc.labels[0] = "All Realizations";
        int zeroOnSuccess = DssFileManagerImpl.getDssFileManager().write(freqPdc);
        if (zeroOnSuccess != 0) {
            frm.addWarningMessage("Failed to save PD Output Variable Frequency to " + freqPdc.fileName + ":" + freqPdc.fullName + " rv=" + zeroOnSuccess);
        }
        return fullCurve;
    }
    public void saveVariableFrequencyConfidenceLimits(OutputVariableImpl vv, ValueBinIncrementalWeight[] fullCurve, FrmSimulation frm, double endProb, double startProb, List<Double> weights){
        //sort by realizatoin (ascending)
        ValueBinIncrementalWeight.setSort(false);
        Arrays.sort(fullCurve);
        //separate into bin arrays.
        List<ValueBinIncrementalWeight[]> realizations = new ArrayList<>();
        int real = fullCurve[0].getRealizationNumber();
        int numEventsPerReal =0;
        List<Integer> proxys = Arrays.asList(62, 87, 125, 175, 225, 262, 287, 337, 375, 425, 475, 512, 550, 612, 637, 675, 725, 775, 825, 862, 887, 925, 962, 999);
        double[] maxs = new double[_XOrds.size()];
        double[] mins = new double[_XOrds.size()];
        for(ValueBinIncrementalWeight event: fullCurve){
            if(real==event.getRealizationNumber()){
                numEventsPerReal++;
            }else{
                break;
            }
        }
        realizations.add(new ValueBinIncrementalWeight[numEventsPerReal]);
        int currentEvent = 0;
        
        //sort by value
        ValueBinIncrementalWeight.setSort(true);
        for(int i = 0; i<_XOrds.size();i++){
            maxs[i] = Double.MIN_VALUE;
            mins[i] = Double.MAX_VALUE;
        }
        //frm.addMessage("working on real " + real);
        for(ValueBinIncrementalWeight event: fullCurve){
            if(real == event.getRealizationNumber()){
                //add it to a list
                realizations.get(real)[currentEvent] = event;
                currentEvent++;
            }else{
                Arrays.sort(realizations.get(real));//sort by value - per real//ascending..
                for(int i = 0; i<_XOrds.size();i++){
                    if(realizations.get(real)[proxys.get(i)].getValue()>maxs[i]){maxs[i] = realizations.get(real)[proxys.get(i)].getValue();}
                    if(realizations.get(real)[proxys.get(i)].getValue()<mins[i]){mins[i] = realizations.get(real)[proxys.get(i)].getValue();}
                }
                real = event.getRealizationNumber();
                //frm.addMessage("working on real " + real);
                currentEvent = 0;
                realizations.add(new ValueBinIncrementalWeight[numEventsPerReal]);
                realizations.get(real)[currentEvent] = event;
                currentEvent++;
            }
        }
        String s = "Real,Event,Value,AEP";
        currentEvent = 0;
        int realization = 0;

        String fileloc = frm.getProject().getProjectDirectory() + "\\Weights_TextFiles\\" + vv._name + "_RawData.txt";
        File destFileDirPath = new File(frm.getProject().getProjectDirectory() + "\\Weights_TextFiles\\");
        if(!destFileDirPath.exists()){
            destFileDirPath.mkdirs();
        }
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileloc));
            bw.write(s + "\n");
            for(ValueBinIncrementalWeight[] events : realizations){
                realization++;
                currentEvent = 0;
                for(ValueBinIncrementalWeight event : events){
                    currentEvent++;
                    bw.write(realization + "," + currentEvent + "," + event.getValue() + "," + (1-event.getPlottingPosition()) + "\n");
                    
                }
            }
            bw.flush();
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(FrequencyFixerPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
        //interpolate to find ploting position of interest and bin
        List<HistDist> verticalSlices = new ArrayList<>();
        int ordcount= 0;
        int bincount = (int)Math.ceil(Math.pow(2.0*realizations.size(),1/3));
        if(bincount<20){bincount=20;}
        
        for(Double d: _XOrds){
            int failureCount=0;//          
            verticalSlices.add(new HistDist(bincount,mins[ordcount],maxs[ordcount]));//how to intelligently get min and max for this prob range?
            //frm.addMessage("Probability: " + d + " with bin Max: " + maxs[ordcount] + " and Min: " + mins[ordcount]);
            ValueBinIncrementalWeight prevVal = null;
            int realcount = 0;
            for(ValueBinIncrementalWeight[] realdata: realizations){
                realcount++;
                boolean foundVal = false;
                for(ValueBinIncrementalWeight obj: realdata ){
                    //should be ascending.
                    if(obj.getPlottingPosition()<=d){
                        //now figure out how to interpolate...
                        foundVal = true;
                        if(prevVal==null){
                            //shit.
                            verticalSlices.get(ordcount).addObservation(obj.getValue());
                        }else{
                            //interpolate
                            double y1 = prevVal.getValue();
                            double y2 = obj.getValue();
                            //-log(-log(p));
                            double x1 = prevVal.getPlottingPosition();
                            double x2 = obj.getPlottingPosition();
                            double ret = y1+((d-x1)*((y1-y2)/(x1-x2))); //is it x1-d or is it d-x1?
                            //frm.addMessage("Max: " + verticalSlices.get(ordcount).getMax() + " Min: " + verticalSlices.get(ordcount).getMin() + " New Value:" + ret);
                            if(!verticalSlices.get(ordcount).addObservation(ret)){
                                failureCount++;
                            }else{
                                if(realcount>100){
                                    verticalSlices.get(ordcount).testForConvergence(.05, .95, .1, .0001);
                                }
                            }
                            break;
                        }
                    }else{
                        //do nothing until coerced..
                        
                    }
                    
                    prevVal = obj;
                }
                if(!foundVal){frm.addMessage("Did not find a value for ord " + ordcount + " which has probability " + d + " on realization " + realcount + " for location " + vv.getName());}
            }
            //verticalSlices.get(ordcount).testForConvergence(.05, .95, .1, .0001);
            //frm.addMessage("Failure Count: " + failureCount);
            frm.addMessage(vv._name + " converged: " + verticalSlices.get(ordcount).getConverged() + " for vertical probabilty slice of " + d);
            if(verticalSlices.get(ordcount).getConverged()){
                frm.addMessage(vv._name + " converged on iter: " + verticalSlices.get(ordcount).getConvergedIteration());
            }else{
                //frm.addMessage(vv._name + " needs more iters: " + verticalSlices.get(ordcount).estimateRemainingIterationsForConvergence);
            }
            ordcount++;  
        }
        double[] xordinates = new double[_XOrds.size()];
        for(int i = 0; i<_XOrds.size();i++){
            xordinates[i] = _XOrds.get(i);
        }
                //SAVE FULL PDCs
            
        for(double o : _CI_Vals){
            double[] vals = new double[_XOrds.size()];
            for(int i= 0; i<verticalSlices.size();i++){
                vals[i] = verticalSlices.get(i).invCDF(o);
            }
            PairedDataContainer freqPdc = vv.getFullFrequencyPairedData();
//            PairedDataContainer thinPdc = new PairedDataContainer();
//            freqPdc.clone(thinPdc);
            freqPdc.numberOrdinates = _XOrds.size();
            freqPdc.numberCurves = 1;
            freqPdc.xOrdinates = xordinates;
            freqPdc.yOrdinates = new double[][]{vals};
            freqPdc.xparameter = "Probability";
            freqPdc.labelsUsed = true;
            freqPdc.labels = new String[1];
            freqPdc.labels[0] = "Confidence Interval - " + o;
            DSSPathname pathname = new DSSPathname(freqPdc.fullName);
            pathname.setDPart(o + " Exceedance Val");
            freqPdc.fullName = pathname.getPathname(true);
            int zeroOnSuccess = DssFileManagerImpl.getDssFileManager().write(freqPdc);
            if (zeroOnSuccess != 0) {
                    frm.addWarningMessage("Failed to save PD Output Variable Frequency to "
                                    + freqPdc.fileName + ":" + freqPdc.fullName + " rv=" + zeroOnSuccess);
            }
        }
    }
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
