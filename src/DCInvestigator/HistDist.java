/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DCInvestigator;

import java.util.Arrays;

/**
 *
 * @author Q0HECWPL
 */
public class HistDist {
    private int[] _bins;
    private double _binWidth = 0;
    private double _min;
    private double _max;
    private int _numObs = 0;
    private double _mean = 0;
    private int _convergedIteration =0;
    private boolean _converged = false;
    public HistDist(int numBins, double min, double max)
    {
        if(numBins<=0 || min>max)
        {
            return;
        }
        _min = min;
        _max = max;
        _binWidth = (max-min)/numBins;
        _bins = new int[numBins];
        Arrays.fill(_bins, 0);
    }

    public int getNumObs(){
        return _numObs;
    }
    public double getMin(){return _min;}
    public double getMax(){return _max;}
    public boolean getConverged(){ return _converged;}
    public int getConvergedIteration(){return _convergedIteration;}
    public boolean addObservation(double obs)
    {
        if(_binWidth <= 0 || Double.isInfinite(obs) || Double.isNaN(obs))
        {
            return false;
        }
        //Add bins if necessary
        if(obs<_min)
        {
            int numBinsToAdd = (int)Math.ceil((_min - obs)/_binWidth);
            int[] oldBins = _bins;
            _bins = new int[oldBins.length+numBinsToAdd];
            Arrays.fill(_bins, 0);
            //for(int i=0;i<numBinsToAdd;i++)
            //{
            //	_bins[i]=0;
            //}
            System.arraycopy(oldBins, 0, _bins, numBinsToAdd, oldBins.length);
            _min = _min - numBinsToAdd * _binWidth;
        }
        else if(obs > _max)
        {
            int numBinsToAdd = (int)Math.ceil((obs - _max)/_binWidth);
            int[] oldBins = _bins;
            _bins = new int[oldBins.length+numBinsToAdd];
            Arrays.fill(_bins, 0);
            //for(int i=0;i<numBinsToAdd;i++)
            //{
            //	_bins[oldBins.length+i]=0;
            //}
            System.arraycopy(oldBins, 0, _bins, 0, oldBins.length);
            _max = _max + numBinsToAdd * _binWidth;
        }

        //Add observation to its bin
        int idx = (int)((obs-_min)/_binWidth);
        idx = idx < _bins.length ? idx : _bins.length-1; //when obs == _max
        _bins[idx]++;
        _numObs++;

        recomputeMean(obs);

        return true;
    }

    private void recomputeMean(double obs)
    {
        _mean += (obs-_mean)/_numObs;
    }

    public boolean addObservations(double[] obsArray)
    {
        if(obsArray==null)
        {
            return false;
        }
        int num = obsArray.length;
        for(int i=0;i<num;i++)
        {
            if(!addObservation(obsArray[i])){
                return false;
            }
        }
        return true;
    }

    public double invCDF(double q)
    {
        if(_numObs<=0 || q<=0.0)
        {
            return _min;
        }
        else if(q>=1.0)
        {
            return _max;
        }
        double qObs = q*_numObs;
        if(q<=0.5)
        {
            int idx = 0;
            int binObs = _bins[idx];
            int cumObs = binObs;
            while(qObs>cumObs)
            {
                idx++;
                binObs = _bins[idx];
                cumObs += binObs;
            }
            return _min + _binWidth * (idx+1 - (cumObs - qObs) / binObs);
        }
        else
        {
            int idx = _bins.length-1;
            int binObs = _bins[idx];
            int cumObs = _numObs-binObs;
            while(qObs<cumObs)
            {
                idx--;
                binObs = _bins[idx];
                cumObs -= binObs;
            }
            return _max - _binWidth * (_bins.length-idx + (qObs - cumObs) / binObs);
        }
    }

    public double getCDF(double val)
    {
        if(_numObs<=0 || val<=_min)
        {
            return 0.0;
        }
        else if(val>=_max)
        {
            return 1.0;
        }
        double dIdx = (val-_min)/_binWidth;
        if(dIdx<=0)
        {
            return 0.0;
        }
        else if(dIdx>=_bins.length)
        {
            return 1.0;
        }
        else if(dIdx<=_bins.length/2)
        {
            int idx = (int)Math.floor(dIdx);
            double cumObs = 0.0;
            for(int i=0;i<idx;i++)
            {
                cumObs+=_bins[i];
            }
            cumObs+=(dIdx-idx)*_bins[idx];
            return cumObs/_numObs;
        }
        else
        {
            int idx = (int)Math.floor(dIdx);
            double cumObs = _numObs;
            for(int i=_bins.length-1;i>idx;i--)
            {
                cumObs-=_bins[i];
            }
            cumObs-=(idx+1-dIdx)*_bins[idx];
            return cumObs/_numObs;
        }
    }

    public double getPDF(double val)
    {
        int idx = (int)((val-_min)/_binWidth);
        if(idx<0 || idx>=_bins.length)
        {
            return 0.0;
        }
        return _bins[idx]/(_binWidth*_numObs);
    }
    public boolean testForConvergence(double minConfLimit, double maxConfLimit, double zAlpha, double relativeError)
	{
	if(_converged) return _converged;
        double qVal, qSlope, variance;
        qVal   = invCDF(minConfLimit);
        qSlope = getPDF(qVal);
        variance = (minConfLimit*(1.0-minConfLimit))/(getNumObs()*qSlope*qSlope);
        /////////////////////
        //System.out.println(qVal+" "+qSlope+" "+variance+" "+Math.abs(zAlpha * Math.sqrt(variance) / qVal ));
        /////////////////////
        if(!(Math.abs( zAlpha * Math.sqrt(variance) / qVal ) <= relativeError * 0.5))
        {
            return _converged;
        }
        //High curve converged
        qVal   = invCDF(maxConfLimit);
        qSlope = getPDF(qVal);
        variance = (maxConfLimit*(1.0-maxConfLimit))/(getNumObs()*qSlope*qSlope);
        /////////////////////
        //System.out.println(qVal+" "+qSlope+" "+variance+" "+Math.abs( zAlpha * Math.sqrt(variance) / qVal ));
        /////////////////////
        if(!(Math.abs( zAlpha * Math.sqrt(variance) / qVal ) <= relativeError * 0.5))
        {
            return _converged;
        }
        _converged = true;
        _convergedIteration = _numObs;
		return _converged;
	}
    public int estimateRemainingIterationsForConvergence(double minConfLimit, double maxConfLimit, double zAlpha, double relativeError){
        if(_converged) return 0;
        double p = maxConfLimit;
        double val1 = p*(1-p);
        double z2 = 2*zAlpha;
        double xp = invCDF(p);
        double fxp = getPDF(xp);
        int estimate = (int)Math.abs(Math.ceil(val1*Math.pow(((z2)/xp*relativeError*fxp),2.0)));
        
        p = minConfLimit;
        val1 = p*(1-p);
        z2 = 2*zAlpha;
        xp = invCDF(p);
        fxp = getPDF(xp);
        int estimate2 = (int)Math.abs(Math.ceil(val1*Math.pow(((z2)/xp*relativeError*fxp),2.0)));
        estimate = Math.max(estimate, estimate2);
        
        return estimate-_numObs;
    }
}
