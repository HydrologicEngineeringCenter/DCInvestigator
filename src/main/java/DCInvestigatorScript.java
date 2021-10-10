import java.util.Set;

public class DCInvestigatorScript {
    public static void main(String[] args) {
        String filePathDSS = "C:\\DATA\\Grid01\\Lift01\\Existing_Conditions-Trinity-G1L1-Partial.dss";
        int lifecyclesPerReal = 20;
        Set<Integer> badLifecycles = DCInvestigatorTool.GetBadLifecycles(filePathDSS, lifecyclesPerReal);
    }
}
