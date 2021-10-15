/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Q0HECWPL
 */
public class DCInvestigatorSettings {
    private String _SimulationName;//the simulation the user wishes to check on
    private int _lifecyclesPerRealization;
    private int _eventsPerLifecycle;

    public String getSimulation(){
        return _SimulationName;
    }
    public int GetLifecyclesPerRealization() {return _lifecyclesPerRealization; }
    public int GetEventsPerLifecycle(){return _eventsPerLifecycle; }



    public DCInvestigatorSettings(String propertiesPath){
        //read in the properties file?
        String propertiesFile = propertiesPath;
        String propertyLine = "";
        BufferedReader brp = null;
        File pf = new File(propertiesFile);
        if(pf.exists()){
            try {
                brp = new BufferedReader(new FileReader(propertiesFile));
                String[] tmp = null;
                while ((propertyLine = brp.readLine()) != null) {
                    tmp = propertyLine.split(",");
                    if(tmp.length==0){continue;}
                    if(tmp[0].equals("SimulationName")){
                        _SimulationName = tmp[1];
                    }
                    if(tmp[0].equals("LifecyclesPerRealization")){
                        _lifecyclesPerRealization = Integer.valueOf(tmp[1]);
                    }
                    if(tmp[0].equals("EventsPerLifecycle")){
                        _eventsPerLifecycle = Integer.valueOf(tmp[1]);
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
    }
}
