public class FailedEvent {
    int Realization;
    int Lifecycle;
    int Event;

    public int getRealization() {
        return Realization;
    }

    public int getLifecycle() {
        return Lifecycle;
    }

    public int getEvent() {
        return Event;
    }

    public FailedEvent(int realization, int lifecycle, int event) {
        Realization = realization;
        Lifecycle = lifecycle;
        Event = event;
    }
}
