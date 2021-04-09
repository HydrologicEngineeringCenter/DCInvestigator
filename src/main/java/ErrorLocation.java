/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.Objects;

/**
 *
 * @author Q0HECWPL
 */
class ErrorLocation {
    private String _modelAlt;
    private Integer _index;
    private ErrorEnum _lcOrReal;
    String getMAlt(){
        return _modelAlt;
    }
    Integer getIndex(){
        return _index;
    }
    public ErrorLocation(String modelAlt, Integer index, boolean isRealization){
        _modelAlt = modelAlt;
        _index = index;
        if(isRealization){
            _lcOrReal = ErrorEnum.Realization;
        }else{
            _lcOrReal = ErrorEnum.LifeCycle;
        }
    }
    public boolean IsRealizationError(){
        if(_lcOrReal.equals(ErrorEnum.Realization)){
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
                if(Objects.equals(this.getIndex(), lcm.getIndex())){
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
        if(_lcOrReal.equals(ErrorEnum.LifeCycle)){
            return "Rerun " + _lcOrReal.toString() + " " + getIndex() + " because: ";
        }else{
            return "Investigate " + _lcOrReal.toString() + " " + getIndex() + " because: ";
        }
    }
}
