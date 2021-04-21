/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import hec2.wat.client.WatFrame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Q0HECWPL
 */
public class DCInvestigatorSettings {
    private String _SimulationName;//the simulation the user wishes to check on
    public String getSimulation(){
        return _SimulationName;
    }
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
