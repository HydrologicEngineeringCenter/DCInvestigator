/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DCInvestigator;

/**
 *
 * @author WatPowerUser
 */
public class ValueBinIncrementalWeight implements Comparable<ValueBinIncrementalWeight> {
    private final int _bin;
    private final double _incrementialWeight;
    private final double _value;
    private final int _realizationNumber;
    private double _plottingPosition;
    private static boolean _sortByValue = true;
    public ValueBinIncrementalWeight(double v, int b, double w, int realizatoinNumber){
        _value = v;
        _bin = b;
        _incrementialWeight = w;
        _realizationNumber = realizatoinNumber;
    }
    public double getValue(){
        return _value;
    }
    public int getRealizationNumber(){
        return _realizationNumber;
    }
    public double getIncrimentalWeight(){
        return _incrementialWeight;
    }
    public void setPlottingPosition(double pp){
        _plottingPosition = pp;
    }
    public double getPlottingPosition(){
        return _plottingPosition;
    }
    public int getBin(){
        return _bin;
    }
    public static void setSort(boolean byValue){
        _sortByValue = byValue;
    }
    @Override
    public int compareTo(ValueBinIncrementalWeight o) {
        if(_sortByValue){
            if(this.getValue()>o.getValue()){
                return 1;
            }else if(this.getValue()==o.getValue()){
                return 0;
            }else{
                return -1;
            }            
        }else{
            if(this.getRealizationNumber()>o.getRealizationNumber()){
                return 1;
            }else if(this.getRealizationNumber()==o.getRealizationNumber()){
                return 0;
            }else{
                return -1;
            } 
        }
    }
}
