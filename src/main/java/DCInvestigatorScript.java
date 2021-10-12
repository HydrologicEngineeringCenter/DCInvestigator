import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Set;

import static org.apache.commons.lang3.ArrayUtils.toArray;

public class DCInvestigatorScript {
    public static void main(String[] args) {
        //String filePathDSS = "C:\\DATA\\Grid01\\Lift01\\Existing_Conditions-Trinity-G1L1-Partial.dss";
        String filePathDSS = "C:\\DATA\\Grid01\\Lift02\\Existing_Conditions-Trinity-G1L2.dss";
        int lifecyclesPerReal = 20;
        Set<Integer> badLifecycles = DCInvestigatorTool.GetBadLifecycles(filePathDSS, lifecyclesPerReal);
        for(Integer i = 1501; i <= 1520; i++){
            badLifecycles.remove(i);
        }
        System.out.println(badLifecycles);
    }
}
