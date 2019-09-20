/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DCInvestigator;

import java.util.Objects;

/**
 *
 * @author Q0HECWPL
 */
class ErrorLocation {
    private String _modelAlt;
    private Integer _lifecycleNumber;
    private String _lcOrReal;
    String getMAlt(){
        return _modelAlt;
    }
    Integer getLifeCycleNumber(){
        return _lifecycleNumber;
    }
    public ErrorLocation(String modelAlt, Integer lifecycle, boolean isRealization){
        _modelAlt = modelAlt;
        _lifecycleNumber = lifecycle;
        if(isRealization){
            _lcOrReal = "Realization";
        }else{
            _lcOrReal = "LifeCycle";
        }
    }
    public boolean IsRealizationError(){
        if(_lcOrReal.equals("Realization")){
            return true;
        }else{
            return false;
        }
    }    
    @Override
    public boolean equals(Object o) {
        if(o.getClass()==this.getClass()){
            ErrorLocation lcm = (ErrorLocation)o;
            if(this.getMAlt().equals(lcm.getMAlt())){
                if(Objects.equals(this.getLifeCycleNumber(), lcm.getLifeCycleNumber())){
                    if(this._lcOrReal.equals(lcm._lcOrReal)){
                        return true;
                    }else{
                        return false;
                    }
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }else{
            return false;
        }
    }
    public String Write(){
        if(_lcOrReal.equals("LifeCycle")){
            return "Rerun " + _lcOrReal + " " + getLifeCycleNumber() + " because: ";
        }else{
            return "Investigate " + _lcOrReal + " " + getLifeCycleNumber() + " because: ";
        }
    }
}
