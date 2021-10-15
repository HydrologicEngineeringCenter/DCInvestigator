import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DCInvestigatorToolShould {
    static String currentDirectory = System.getProperty("user.dir") + "\\src\\test\\resources";
    String _filepath = currentDirectory + "\\Existing_Conditions-Trinity.dss";
    int _lifecyclesPerReal = 20;
    int _eventsPerLifecycle = 50;
    static String outputFilePath = currentDirectory + "\\DCInvestigatorReport.txt";
    static String referenceReport = currentDirectory + "\\references\\DCInvestigatorReport.txt";

    DCInvestigatorTool tool = new DCInvestigatorTool(_filepath,_lifecyclesPerReal,_eventsPerLifecycle);

    @Test
    void returnAllFailedLifecycles(){
        Set<Integer> actual = tool.GetBadLifecycles();
        ArrayList<Integer> expected = new ArrayList<Integer>(Arrays.asList(320, 195, 201, 202, 204, 205, 78, 207, 79, 208, 339, 211, 212, 213, 214, 215, 216, 217, 219, 220, 94, 479, 480, 98, 40, 296, 237, 498, 57, 250, 59, 380, 319));
        assertTrue(actual.containsAll(expected));
    }
    @Test
    void returnCorrectNumberOfFailedEvents(){
        int actual = tool.GetFailedEvents().size();
        int expected = 770;
        assertEquals(expected,actual);
    }
    @Test
    void returnCorrectFailedEventsForALifecycle(){
        Set<Integer> actual = tool.GetBadEventsPerLifecycle(237);
        Set<Integer> expected = new HashSet<>(Arrays.asList(6,38));
        assertEquals(expected,actual);
    }
    @Test
    void writesCorrectReport(){
        tool.WriteReport(outputFilePath);
        File expected = new File(referenceReport);
        File actual = new File(outputFilePath);
        assertEquals(expected.getTotalSpace(), actual.getTotalSpace());
        assertEquals(expected.length(), expected.length());
    }
    @AfterAll
    static void deleteReport(){
        File expected = new File(outputFilePath);
        expected.delete();
    }
}
