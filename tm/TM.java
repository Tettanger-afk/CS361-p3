package tm;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

// This should be the Turing Machine class.
/**
 * Simple concrete Turing Machine implementation that stores states,
 * an (unbounded) tape using a map, and executes transitions until halting.
 */
public class TM implements TMInterface {

    private final Map<Integer, TMStateInterface> states = new HashMap<>();
    private int blankSymbol = 0;
    private Map<Integer, Integer> tape = new HashMap<>(); // sparse tape
    private int head = 0;
    private int currentState = 0;
    private boolean halted = false;

    public TM() {}

    @Override
    public void initializeTape(int[] input) {
        tape.clear();
        if (input != null) {
            for (int i = 0; i < input.length; i++) {
                tape.put(i, input[i]);
            }
        }
        head = 0;
        halted = false;
    }

    @Override
    public int readTape() {
        return tape.getOrDefault(head, blankSymbol);
    }

    @Override
    public void writeTape(int symbol) {
        if (symbol == blankSymbol) {
            tape.remove(head);
        } else {
            tape.put(head, symbol);
        }
    }

    @Override
    public int getHeadPosition() { return head; }

    @Override
    public void setHeadPosition(int position) { head = position; }

    @Override
    public int getCurrentState() { return currentState; }

    @Override
    public void setCurrentState(int stateId) { currentState = stateId; }

    @Override
    public void step() {
        if (halted) return;
        TMStateInterface state = states.get(currentState);
        if (state == null) { halted = true; return; }
        if (state.isHalting()) { halted = true; return; }

        int read = readTape();
        if (!state.hasTransition(read)) { halted = true; return; }

        int next = state.getNextState(read);
        int write = state.getWriteSymbol(read);
        char dir = state.getDirection(read);

        writeTape(write);
        currentState = next;
        if (dir == 'L') head--;
        else if (dir == 'R') head++;

        TMStateInterface newState = states.get(currentState);
        if (newState != null && newState.isHalting()) halted = true;
    }

    @Override
    public void run(long maxSteps) {
        if (maxSteps <= 0) {
            while (!halted) step();
        } else {
            long cnt = 0;
            while (!halted && cnt < maxSteps) { step(); cnt++; }
        }
    }

    @Override
    public boolean isHalted() { return halted; }

    @Override
    public void reset() { tape.clear(); head = 0; currentState = 0; halted = false; }

    @Override
    public void setBlankSymbol(int blankSymbol) { this.blankSymbol = blankSymbol; }

    @Override
    public int getBlankSymbol() { return blankSymbol; }

    @Override
    public void addState(TMStateInterface state) { states.put(state.getId(), state); }

    @Override
    public TMStateInterface getState(int stateId) { return states.get(stateId); }

    @Override
    public long getOutputAsNumber() {
        // best-effort: compute BigInteger and if it fits in long, return it, else Long.MAX_VALUE
        BigInteger v = getOutputAsBigInteger();
        if (v.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) return Long.MAX_VALUE;
        return v.longValue();
    }

    /** Compute output as BigInteger by concatenating non-blank cells from leftmost to rightmost. */
    public BigInteger getOutputAsBigInteger() {
        if (tape.isEmpty()) return BigInteger.ZERO;
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int k : tape.keySet()) {
            min = Math.min(min, k);
            max = Math.max(max, k);
        }
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        for (int i = min; i <= max; i++) {
            int s = tape.getOrDefault(i, blankSymbol);
            sb.append(Integer.toString(Math.max(0, s)));
        }
        try {
            return new BigInteger(sb.toString());
        } catch (NumberFormatException e) {
            return BigInteger.ZERO;
        }
    }

    @Override
    public int getOutputLength() {
        if (tape.isEmpty()) return 0;
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int k : tape.keySet()) {
            min = Math.min(min, k);
            max = Math.max(max, k);
        }
        return max - min + 1;
    }

    @Override
    public long getSumOfSymbols() {
        long sum = 0;
        for (int v : tape.values()) sum += v;
        return sum;
    }

    // helper: set tape from number of 1s (unary input)
    public void initializeUnaryInput(int ones) {
        tape.clear();
        for (int i = 0; i < ones; i++) tape.put(i, 1);
        head = 0;
        halted = false;
    }

}
