import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DCInvestigatorScript {
    public static void main(String[] args) {

        //String filePathDSS = "\\\\NRC-Ctrl01\\C$\\DCWorkingDir\\Trinity_WAT\\runs\\Existing_Conditions\\Trinity\\Existing_Conditions-Trinity.dss";
        //String filePathDSS = "\\\\NRC-Ctrl02\\C$\\DCWorkingDir\\Trinity_WAT\\runs\\Existing_Conditions\\Trinity\\Existing_Conditions-Trinity.dss";
        //String filePathDSS = "\\\\NRC-Ctrl03\\C$\\DCWorkingDir\\Trinity_WAT\\runs\\Existing_Conditions\\Trinity\\Existing_Conditions-Trinity.dss";
        //String filePathDSS = "\\\\NRC-Ctrl04\\C$\\DCWorkingDir\\Trinity_WAT\\runs\\Existing_Conditions\\Trinity\\Existing_Conditions-Trinity.dss";

        //String filePathDSS = "C:\\DATA\\Grid01\\Lift03\\Existing_Conditions-Trinity.dss";
        //String filePathDSS = "C:\\DATA\\Grid02\\Lift03\\Existing_Conditions-Trinity.dss";
        //String filePathDSS = "C:\\DATA\\Grid03\\Lift03\\Existing_Conditions-Trinity.dss";
        //String filePathDSS = "C:\\DATA\\Grid04\\Lift02\\Existing_Conditions-Trinity.dss";

        String filePathDSS = "C:\\DCWorkingDir\\Trinity_WAT\\runs\\Existing_Conditions\\Trinity\\Existing_Conditions-Trinity.dss";

        int lifecyclesPerReal = 20;
        int eventsPerLifecycle = 50;
        String outputFilePath = "C:\\Temp\\DCInvestigatorReport.txt";
        String outputXMLPath = "C:\\Temp\\DCInvestigatorReport.xml";

        DCInvestigatorTool tool = new DCInvestigatorTool(filePathDSS,lifecyclesPerReal,eventsPerLifecycle);
        tool.WriteReport(outputFilePath);
        tool.WriteToXML(outputXMLPath);

//        Vector<String> incompletes = tool.GetIncompleteCollections();
//        for(String path:incompletes){
//            System.out.println(path);
        }
    }


