import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DCInvestigatorSettingsShould {
    String currentDirectory = System.getProperty("user.dir") + "\\src\\test\\resources";
    String propertiesPath = currentDirectory + "\\DCInvestigator.props";
    DCInvestigatorSettings settings = new DCInvestigatorSettings(propertiesPath);

    @Test
    void returnSimulationName() {
        String actual = settings.getSimulation();
        assertEquals("Validation", actual);
    }

    @Test
    void returnLifecyclesPerRealization() {
        int actual = settings.GetLifecyclesPerRealization();
        assertEquals(20, actual);
    }

    @Test
    void returnEventsPerLifecycle() {
        int actual = settings.GetEventsPerLifecycle();
        assertEquals(50, actual);
    }
}