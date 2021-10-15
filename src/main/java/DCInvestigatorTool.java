import hec.heclib.dss.DSSPathname;
import hec.heclib.dss.HecDataManager;
import hec.heclib.dss.HecPairedData;
import hec.io.PairedDataContainer;

import java.io.FileWriter;
import java.io.IOException;
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

    public Set<FailedEvent> GetFailedEvents() {
        return _failedEvents;
    }
    public Set<Integer> GetBadLifecycles(){
        Set<Integer> badLifecycles = new HashSet<>();
        for(FailedEvent fail: _failedEvents){
            badLifecycles.add(fail.getLifecycle());
        }
        return badLifecycles;
    }
    public Set<Integer> GetBadEventsPerLifecycle(int lifecycle){
        Set<Integer> badEvents = new HashSet<>();
        for(FailedEvent fail: _failedEvents){
            if( fail.getLifecycle() == lifecycle){
                badEvents.add(fail.getEvent());
            }
        }
        return badEvents;
    }
    public int GetNumberOfRealizations(){
        Set<Integer> reals = new HashSet<>();
        Vector<String> OutputVariablePathnames = GetOutputVariablePathnames();
        for(String pathname: OutputVariablePathnames){
            DSSPathname dssPathname = new DSSPathname(pathname);
            String collectionNum = dssPathname.getCollectionSequence();
            reals.add(Integer.valueOf(collectionNum));
        }
        return reals.size();
    }
    public void WriteReport(String outputFilePath) {
        int totalMissingEvents = 0;
        try {
            FileWriter myWriter = new FileWriter(outputFilePath);
            //write all lifecycles with any missing data
            myWriter.write("DCInvestigator Report for: " + _dssFilePath + "\n");
            myWriter.write("This WAT Results DSS file contains " + GetNumberOfRealizations() + " realizations" + "\n\n");
            myWriter.write("The following lifecycles have missing data: " + "\n");
            myWriter.write(GetBadLifecycles() + "\n\n");
            myWriter.write("The following list missing events for each lifecycle: " + "\n");
            //for each of those lifecycles, write which data is missing
            for (Integer eachLifecycle : GetBadLifecycles()) {
                Set<Integer> failedEvents = GetBadEventsPerLifecycle(eachLifecycle);
                totalMissingEvents += failedEvents.size();
                myWriter.write((eachLifecycle) + ": " + failedEvents + "\n");
            }
            myWriter.write("Total Events Failed = " + totalMissingEvents + "\n\n");
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    private void Investigate() {

        Vector<String> pathnames = GetOutputVariablePathnames();
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
    private Vector<String> GetOutputVariablePathnames() {
        String dssPartDvalue = "Output Variable";
        String pathWithWildChars = "/*/*/*/*" + dssPartDvalue + "*/*/*/";
        HecDataManager dssManager = new HecDataManager();
        dssManager.setDSSFileName(_dssFilePath);
        String[] pathnames = dssManager.getCatalog(false, pathWithWildChars);
        HecDataManager.closeAllFiles();
        Vector<String> vector = new Vector<>(Arrays.asList(pathnames));
        return vector;
    }
}
