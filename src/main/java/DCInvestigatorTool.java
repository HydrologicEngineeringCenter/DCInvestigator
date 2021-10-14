import hec.heclib.dss.DSSPathname;
import hec.heclib.dss.HecDataManager;
import hec.heclib.dss.HecPairedData;
import hec.io.PairedDataContainer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;


public class DCInvestigatorTool {
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
    public static Set<Integer> GetBadLifecycles(String filePath, int lifecyclesPerRealization){
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
    public static Set<Integer> GetBadEvents(String filePath, int lifecycle, int lifecyclesPerRealization ){
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
}
