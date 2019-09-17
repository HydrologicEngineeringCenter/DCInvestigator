/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DCInvestigator;

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
    private ArrayList<Double> _Convergence_Ordinates;
    private boolean _vertical; //vertical or horizontal criteria.
    public String getSimulation(){
        return _SimulationName;
    }
    public ArrayList<Double> getOrdinatesForConvergence(){
        return _Convergence_Ordinates;
    }
    public boolean isVerticalSlices(){
        return _vertical;
    }
    public DCInvestigatorSettings(String propertiesPath){
        //read in the properties file?
        WatFrame fr = null;
        fr = hec2.wat.WAT.getWatFrame();
        String propertiesFile = propertiesPath;
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
                        _SimulationName = tmp[1];
                    }else if(tmp[0].equals("XOrds")){
                        _Convergence_Ordinates = new ArrayList<>();
                        for(String s: tmp){
                            if(s.equals("XOrds")){continue;}
                            _Convergence_Ordinates.add(1-Double.parseDouble(s));
                            fr.addMessage(s); 
                        }
                        fr.addMessage("There are " + _Convergence_Ordinates.size() + " ordinates for testing convergence.");                 
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
        }else{
            fr.addMessage("No properties file found at " + propertiesPath);
        }

    }
    public DCInvestigatorSettings(String simName, ArrayList<Double> ords, boolean vert){
        _SimulationName = simName;
        _Convergence_Ordinates = ords;
        _vertical = vert;
    }
}
