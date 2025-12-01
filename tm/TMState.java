package tm;
// This should be the Turing Machine State class.
public class TMState implements TMStateInterface {

    private int id;
    private boolean halting = false;

    // transitions keyed by read symbol
    private java.util.Map<Integer, Transition> transitions = new java.util.HashMap<>();

    private static class Transition {
        int nextStateId;
        int writeSymbol;
        char direction; // 'L','R','N'

        Transition(int nextStateId, int writeSymbol, char direction) {
            this.nextStateId = nextStateId;
            this.writeSymbol = writeSymbol;
            this.direction = direction;
        }
    }

    public TMState() {}

    public TMState(int id) { this.id = id; }

    @Override
    public int getId() { return id; }

    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public boolean isHalting() { return halting; }

    @Override
    public void setHalting(boolean halting) { this.halting = halting; }

    @Override
    public void addTransition(int readSymbol, int nextStateId, int writeSymbol, char direction) {
        transitions.put(readSymbol, new Transition(nextStateId, writeSymbol, direction));
    }

    @Override
    public boolean hasTransition(int readSymbol) { return transitions.containsKey(readSymbol); }

    @Override
    public int getNextState(int readSymbol) {
        Transition t = transitions.get(readSymbol);
        return t == null ? -1 : t.nextStateId;
    }

    @Override
    public int getWriteSymbol(int readSymbol) {
        Transition t = transitions.get(readSymbol);
        return t == null ? -1 : t.writeSymbol;
    }

    @Override
    public char getDirection(int readSymbol) {
        Transition t = transitions.get(readSymbol);
        return t == null ? 'N' : t.direction;
    }

    /**
     * Copy transitions from this state into dest. Used when cloning machine templates.
     */
    public void copyTo(TMState dest) {
        for (java.util.Map.Entry<Integer, Transition> e : transitions.entrySet()) {
            Transition t = e.getValue();
            dest.transitions.put(e.getKey(), new Transition(t.nextStateId, t.writeSymbol, t.direction));
        }
    }

}
