import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import static org.apache.commons.lang3.ArrayUtils.toArray;

public class DCInvestigatorScript {
    public static void main(String[] args) {
        //String filePathDSS = "\\\\NRC-Ctrl01\\C$\\DCWorkingDir\\Trinity_WAT\\runs\\Existing_Conditions\\Trinity\\Existing_Conditions-Trinity.dss";
        //String filePathDSS = "\\\\NRC-Ctrl02\\C$\\DCWorkingDir\\Trinity_WAT\\runs\\Existing_Conditions\\Trinity\\Existing_Conditions-Trinity.dss";
        //String filePathDSS = "\\\\NRC-Ctrl03\\C$\\DCWorkingDir\\Trinity_WAT\\runs\\Existing_Conditions\\Trinity\\Existing_Conditions-Trinity.dss";
        //String filePathDSS = "\\\\NRC-Ctrl04\\C$\\DCWorkingDir\\Trinity_WAT\\runs\\Existing_Conditions\\Trinity\\Existing_Conditions-Trinity.dss";

        //String filePathDSS = "C:\\DATA\\Grid01\\Lift01\\Existing_Conditions-Trinity.dss";
        //String filePathDSS = "C:\\DATA\\Grid02\\Lift01\\Existing_Conditions-Trinity.dss";
        //String filePathDSS = "C:\\DATA\\Grid02\\Lift01\\Existing_Conditions-Trinity-1and2.dss";
        String filePathDSS = "C:\\DATA\\Grid03\\Lift01\\Existing_Conditions-Trinity.dss";

        int lifecyclesPerReal = 20;
        Set<Integer> badLifecycles = DCInvestigatorTool.GetBadLifecycles(filePathDSS, lifecyclesPerReal);

        //Get rid of realization 75 that sneaks into everything
        for (Integer i = 1501; i <= 1520; i++) {
            badLifecycles.remove(i);
        }

        //Identify Lifecycles that need to be completely rerun
        Set<Integer> lifecycesFailed = new HashSet<>();
        for( Integer lifecycle: badLifecycles){
            Set<Integer> failedEvents = DCInvestigatorTool.GetBadEvents(filePathDSS,lifecycle,lifecyclesPerReal);
            if(failedEvents.size() > 5){
                lifecycesFailed.add(lifecycle);
            }
        }

        //Identify pathnames that have missing lifecycles.
        Vector<String> badpathnames = DCInvestigatorTool.GetIncompleteCollections(filePathDSS, lifecyclesPerReal);

        //Write the error file
        try {
            FileWriter myWriter = new FileWriter("C:\\Temp\\DCInvestigator.txt");
            //write all lifecycles with any missing data
            myWriter.write("DCInvestigator Report for: " + filePathDSS);
            myWriter.write("\n");
            myWriter.write("The following lifecycles have missing data: ");
            myWriter.write("\n");
            myWriter.write(String.valueOf(badLifecycles));
            myWriter.write("\n");
            myWriter.write("The following list missing events for each lifecycle: ");
            //for each of those lifecycles, write which data is missing
            for (Integer eachLifecycle : badLifecycles) {
                myWriter.write(String.valueOf(eachLifecycle));
                myWriter.write(": ");
                myWriter.write(String.valueOf(DCInvestigatorTool.GetBadEvents(filePathDSS, eachLifecycle, lifecyclesPerReal)));
                myWriter.write("\n");
            }
            //write the lifecycles that need to be entirely reran
            myWriter.write("The following lists lifecycles that ought be entirely reran: ");
            myWriter.write("\n");
            myWriter.write(String.valueOf(lifecycesFailed));
            myWriter.write("\n");
            myWriter.write("The following lists collections with missing data: ");
            myWriter.write("\n");
            //write the pathnames that contain errors
            for ( String path: badpathnames){
                myWriter.write(path);
                myWriter.write("\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}

