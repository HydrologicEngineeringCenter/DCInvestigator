import hec.heclib.dss.DSSPathname;
import hec.heclib.dss.HecDSSUtilities;
import hec.heclib.dss.HecDataManager;
import hec.heclib.dss.HecPairedData;
import hec.io.PairedDataContainer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;


public class DCInvestigatorTool {
    String _filePath;
    int _lifecyclesPerRealization;

    public DCInvestigatorTool(String _filePath, int _lifecyclesPerRealization) {
        this._filePath = _filePath;
        this._lifecyclesPerRealization = _lifecyclesPerRealization;
    }

    public static Vector<String> getOutputVariablePathnames(String dssFileName) {
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
        Vector<String> pathnames = getOutputVariablePathnames(filePath);
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

}
