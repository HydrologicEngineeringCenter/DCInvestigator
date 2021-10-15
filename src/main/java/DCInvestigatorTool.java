import hec.heclib.dss.DSSPathname;
import hec.heclib.dss.HecDataManager;
import hec.heclib.dss.HecPairedData;
import hec.io.PairedDataContainer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;


public class DCInvestigatorTool {
    private String _dssFilePath;
    private int _lifecyclesPerReal;
    private int _eventsPerLifecycle;

    Set<FailedEvent> _failedEvents = new HashSet<>();

    public DCInvestigatorTool(String _dssFilePath, int _lifecyclesPerReal, int _eventsPerLifecycle) {
        this._dssFilePath = _dssFilePath;
        this._lifecyclesPerReal = _lifecyclesPerReal;
        this._eventsPerLifecycle = _eventsPerLifecycle;
        Investigate();
    }

    public Set<FailedEvent> get_failedEvents() {
        return _failedEvents;
    }

    private void Investigate() {

        Vector<String> pathnames = GetOutputVariablePathnames(_dssFilePath);
        int failedRealization = 0;
        int failedLifecycle = 0;
        int failedEvent = 0;

        HecPairedData pairedData = new HecPairedData();
        pairedData.setDSSFileName(_dssFilePath);

        for (String pathname : pathnames) {
            DSSPathname DSSname = new DSSPathname(pathname);
            PairedDataContainer pdc = new PairedDataContainer();
            pdc.setFullName(pathname);
            pairedData.read(pdc);

            //For most cases we can just loop through all the data
            double[][] yOrds = pdc.getYOridnates();
            int lifecycleCount = 0;
            for (double[] lifecycle : yOrds) {
                lifecycleCount++;
                int eventCount = 0;
                for (double event : lifecycle) {
                    eventCount++;
                    if (event < 0) {
                        failedRealization = Integer.valueOf(DSSname.getCollectionSequence());
                        failedLifecycle = failedRealization*_lifecyclesPerReal + lifecycleCount;
                        failedEvent = eventCount;
                        FailedEvent fail = new FailedEvent(failedRealization, failedLifecycle, failedEvent);
                        _failedEvents.add(fail);
                    }
                }
            }
            //For cases where lifecycles are missing on the ends, we'll have to add those here.
            if (yOrds.length != _lifecyclesPerReal) {
                for (int lifecycleNum = yOrds.length + 1; lifecycleNum <= _lifecyclesPerReal; lifecycleNum++) {
                    for (int eventNum = 1; eventNum <= _eventsPerLifecycle; eventNum++) {
                        failedRealization = Integer.valueOf(DSSname.getCollectionSequence());
                        failedLifecycle = failedRealization*_lifecyclesPerReal + lifecycleNum;
                        failedEvent = eventNum;
                        FailedEvent fail = new FailedEvent(failedRealization, failedLifecycle, failedEvent);
                        _failedEvents.add(fail);
                    }
                }
            }
        }
    }
    public Set<Integer> GrabBadLifecycles(){
        Set<Integer> badLifecycles = new HashSet<>();
        for(FailedEvent fail: _failedEvents){
            badLifecycles.add(fail.getLifecycle());
        }
        return badLifecycles;
    }
    public Set<Integer> GrabBadEvents(int lifecycle){
        Set<Integer> badEvents = new HashSet<>();
        for(FailedEvent fail: _failedEvents){
            if( fail.getLifecycle() == lifecycle){
                badEvents.add(fail.getEvent());
            }
        }
        return badEvents;
    }

    public static Vector<String> GetOutputVariablePathnames(String dssFileName) {
        String dssPartDvalue = "Output Variable";
        String pathWithWildChars = "/*/*/*/*" + dssPartDvalue + "*/*/*/";
        HecDataManager dssManager = new HecDataManager();
        dssManager.setDSSFileName(dssFileName);
        String[] pathnames = dssManager.getCatalog(false, pathWithWildChars);
        HecDataManager.closeAllFiles();
        Vector<String> vector = new Vector<>(Arrays.asList(pathnames));
        return vector;
    }

    public static int GetNumberOfRealizations(String filePath){
        Set<Integer> reals = new HashSet<>();
        Vector<String> OutputVariablePathnames = GetOutputVariablePathnames(filePath);

        for(String pathname: OutputVariablePathnames){
            DSSPathname dssPathname = new DSSPathname(pathname);
            String collectionNum = dssPathname.getCollectionSequence();
            reals.add(Integer.valueOf(collectionNum));
        }
        return reals.size();
    }

    public static Vector<String> GetIncompleteCollections(String filePath, int lifecyclesPerReal, int numMissingEvents){
        Vector<String> incompleteRealizations = new Vector<>();
        Vector<String> OutputVariablePathnames = GetOutputVariablePathnames(filePath);
        PairedDataContainer mypdc = new PairedDataContainer();
        HecPairedData pairedData = new HecPairedData();
        pairedData.setDSSFileName(filePath);


        for(String pathname: OutputVariablePathnames){
            Boolean realizationIsIncomplete = false;
            mypdc.setFullName(pathname);
            pairedData.read(mypdc);
            if(mypdc.getNumberCurves() != lifecyclesPerReal){
                incompleteRealizations.add(pathname);
                continue;
            }
            double[][] yOrds = mypdc.getYOridnates();
            for(double[] lifecycle: yOrds){
                int failedEventCount = 0;
                for(double event: lifecycle){
                    if(event < 0 ){
                        failedEventCount++;
                    }
                    if (failedEventCount > numMissingEvents){
                        incompleteRealizations.add(pathname);
                        realizationIsIncomplete = true;
                        break;
                    }
                }
                if(realizationIsIncomplete){
                    break;
                }
            }
        }
        return incompleteRealizations;
    }



    @Deprecated
    public static Set<Integer> IdentifyBadLifecycles(String filePath, int lifecyclesPerRealization){
        PairedDataContainer pdc = new PairedDataContainer();
        HecPairedData pairedData = new HecPairedData();
        pairedData.setDSSFileName(filePath);
        Vector<String> pathnames = GetOutputVariablePathnames(filePath);
        Set<Integer> errorList = new HashSet<>();
        for(String pathname: pathnames){
            pdc.setFullName(pathname);
            pairedData.read(pdc);
            DSSPathname tmp = new DSSPathname(pdc.fullName);
            String collectionNum = tmp.getCollectionSequence();
            int real = Integer.valueOf(collectionNum);

            if(pdc.getNumberCurves() != lifecyclesPerRealization){
                for(int lifecycle = pdc.getNumberCurves()+1; lifecycle <= lifecyclesPerRealization; lifecycle++){
                    errorList.add(real*lifecyclesPerRealization+lifecycle);
                }
            }
            double[][] yOrds = pdc.yOrdinates;
            int lifecycleCount = 0;
            for(double[] lifecycle: yOrds){
                lifecycleCount++;
                Boolean lifecycleHasErrors = false;
                int eventCount = 0;
                for(double event: lifecycle){
                    eventCount++;
                    if(event < 0){
                        lifecycleHasErrors = true;
                        break;
                    }
                }
                if(lifecycleHasErrors){
                    errorList.add(real*lifecyclesPerRealization+lifecycleCount);
                }
            }
        }
        return errorList;
    }
    @Deprecated
    public static Set<Integer> IdentifyBadEvents(String filePath, int lifecycle, int lifecyclesPerRealization ){
        PairedDataContainer pdc = new PairedDataContainer();
        HecPairedData pairedData = new HecPairedData();
        pairedData.setDSSFileName(filePath);

        Set<Integer> badEvents = new HashSet<>();

        int collectionMemberNumber;
        int lifecycleIndex;
        if(lifecycle%lifecyclesPerRealization==0) {
            collectionMemberNumber = lifecycle / lifecyclesPerRealization - 1;
            lifecycleIndex = 19;
        }
        else{
            collectionMemberNumber = lifecycle/lifecyclesPerRealization;
            lifecycleIndex = lifecycle%lifecyclesPerRealization-1;
        }

        Vector<String> pathnames = GetOutputVariablePathnames(filePath);
        for(String pathname: pathnames){
            DSSPathname name = new DSSPathname(pathname);
            if(Integer.valueOf(name.getCollectionSequence()) != collectionMemberNumber){
                continue;
            }
            pdc.setFullName(pathname);
            pairedData.read(pdc);
            double[][]yOrds = pdc.getYOridnates();
            if(pdc.getNumberCurves() <= lifecycleIndex){
                for(int i = 1; i<= pdc.getNumberOrdinates(); i++ ){
                    badEvents.add(i);
                }
            }
            else{
                for(int i = 0; i < yOrds[lifecycleIndex].length; i++){
                    if(yOrds[lifecycleIndex][i] < 0){
                        badEvents.add(i+1);
                    }
                }
            }

        }
        return badEvents;

    }


}
