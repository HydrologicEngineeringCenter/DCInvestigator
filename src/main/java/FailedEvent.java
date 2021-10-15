public class FailedEvent {
    private int Realization;
    private int Lifecycle;
    private int Event;

    public FailedEvent(int realization, int lifecycle, int event) {
        Realization = realization;
        Lifecycle = lifecycle;
        Event = event;
    }

    public int getRealization() {
        return Realization;
    }

    public int getLifecycle() {
        return Lifecycle;
    }

    public int getEvent() {
        return Event;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null) return false;
        if(getClass() != obj.getClass()) return false;
        FailedEvent other = (FailedEvent) obj;
        if(Realization != other.getRealization()) return false;
        if(Lifecycle != other.getLifecycle()) return false;
        if(Event != other.getEvent()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Realization;
        result = prime * result + Lifecycle;
        result = prime * result + Event;
        return result;
    }
}
