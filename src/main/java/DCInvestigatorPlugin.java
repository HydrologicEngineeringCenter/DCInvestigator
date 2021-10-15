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
import hec2.plugin.AbstractPlugin;
import hec2.wat.client.WatFrame;
import hec2.wat.model.FrmSimulation;
import hec2.wat.model.ModelComputeTracker;
import hec2.wat.plugin.SimpleWatPlugin;
import hec2.wat.plugin.WatPluginManager;

import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;

import rma.swing.RmaImage;

public class DCInvestigatorPlugin extends AbstractPlugin implements SimpleWatPlugin {
    public static final String PluginName = "Distributed Compute Investigator Plugin";
    public static final String PluginShortName = "DCInvestigator";
    private static final String _pluginVersion = "1.0.0";
    private DCInvestigatorSettings _settings;

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

    protected void addToToolsToolbar() {
        Icon i = RmaImage.getImageIcon("Images/Workstation.gif");
        BrowserAction a = new BrowserAction("Fixer",i,this, "displayDCInvestigator");
        a.putValue(Action.SHORT_DESCRIPTION, getName());
        Browser.getBrowserFrame().getToolsMenu().insert(a,3);
        ((WatFrame)Browser.getBrowserFrame()).getToolsToolbarGroup().add(a);
    }

    @Override
    public boolean createProject(Project prjct) {return true;}
    @Override
    public boolean openProject(Project prjct) {return true;}
    @Override
    public boolean close(boolean bln) {return true;}
    @Override
    public String getProjectName() {return "";}
    @Override
    public boolean saveProject() {return true;}
    @Override
    public String getLogfile() {return null;}
    @Override
    public boolean displayApplication() {
        Project proj = Browser.getBrowserFrame().getCurrentProject();
        String dir = proj.getProjectDirectory();
        if (dir != null) {
            _settings = new DCInvestigatorSettings(dir + "\\" + PluginShortName + "\\" + PluginShortName + ".props");
        } else {
            return false;
        }

        //get the simulation 
        List<ManagerProxy> managerProxyListForType = proj.getManagerProxyListForType(FrmSimulation.class);
        Manager m = null;
        FrmSimulation frm = null;
        for (ManagerProxy mp : managerProxyListForType) {
            if (mp.getName().equals(_settings.getSimulation())) {
                m = mp.getManager();
                frm = (FrmSimulation) m;//get the FRM simulation object
            }
        }

        ModelComputeTracker mct = new ModelComputeTracker(frm);
        DCInvestigatorTool detective = new DCInvestigatorTool(frm.getSimulationDssFile(),_settings.GetLifecyclesPerRealization(), _settings.GetEventsPerLifecycle());
        for(FailedEvent fail: detective.GetFailedEvents()){
            mct.add("RAS", fail.getRealization(), fail.getLifecycle(), fail.getEvent(), frm.getRunTimeWindow(), false);
        }
        mct.saveData();
        return true;
    }
    @Override
    public String getVersion() {return _pluginVersion;}
    @Override
    public String getDirectory() {return "";}
    private boolean isAppInstalled() {return true;}
}
