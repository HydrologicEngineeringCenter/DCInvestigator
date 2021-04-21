import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DCInvestigatorSettingsTest {
    String currentDirectory = System.getProperty("user.dir");
    String propertiesPath = currentDirectory + "\\ConfidenceBuilder.props";
    DCInvestigatorSettings settings = new DCInvestigatorSettings(propertiesPath);

    @Test
    void getSimulation() {
        String actual = settings.getSimulation();
        assertEquals("Validation", actual);
    }
}