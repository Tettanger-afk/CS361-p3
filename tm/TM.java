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
    // array-backed tape: logical index i maps to tapeArray[tapeOrigin + i]
    private int[] tapeArray = null;
    private int tapeOrigin = 0; // offset in array corresponding to logical index 0
    private int head = 0;
    private int currentState = 0;
    private boolean halted = false;
    // Optional packed transition table for fast lookup: indexed by (state * symbolsPerState + symbol)
    private int[] transitionTable = null;
    private int symbolsPerState = 0;
    private boolean[] haltingStates = null;
    private boolean useTransitionTable = false;
    // track visited tape indices (inclusive)
    private int minVisited = Integer.MAX_VALUE;
    private int maxVisited = Integer.MIN_VALUE;

    public TM() {}

    private void ensureTapeCapacityForIndex(int arrayIndex) {
        if (tapeArray == null) {
            int cap = Math.max(64, 16);
            tapeArray = new int[cap];
            for (int i = 0; i < cap; i++) tapeArray[i] = blankSymbol;
            tapeOrigin = cap / 4;
        }
        if (arrayIndex >= 0 && arrayIndex < tapeArray.length) return;
        int need = Math.max(arrayIndex + 1, tapeArray.length * 2);
        int newCap = Math.max(need, tapeArray.length * 2);
        int[] na = new int[newCap];
        for (int i = 0; i < newCap; i++) na[i] = blankSymbol;
        int oldLen = tapeArray.length;
        int newOrigin = (newCap - oldLen) / 2;
        System.arraycopy(tapeArray, 0, na, newOrigin, oldLen);
        tapeOrigin = newOrigin + tapeOrigin;
        tapeArray = na;
    }

    /**
     * Build a packed transition table for fast runtime lookups.
     * nStates is the total number of states, symbolsPerState is |Γ| (including blank 0).
     */
    public void buildTransitionTable(int nStates, int symbolsPerState) {
        this.symbolsPerState = symbolsPerState;
        int len = nStates * symbolsPerState;
        transitionTable = new int[len];
        java.util.Arrays.fill(transitionTable, -1);
        haltingStates = new boolean[nStates];
        for (int s = 0; s < nStates; s++) {
            TMStateInterface st = states.get(s);
            if (st == null) continue;
            haltingStates[s] = st.isHalting();
            for (int sym = 0; sym < symbolsPerState; sym++) {
                if (!st.hasTransition(sym)) continue;
                int next = st.getNextState(sym);
                int write = st.getWriteSymbol(sym);
                char dir = st.getDirection(sym);
                int dirBit = (dir == 'R') ? 1 : 0;
                int packed = (next << 8) | (write << 1) | dirBit;
                int idx = s * symbolsPerState + sym;
                if (idx >= 0 && idx < len) transitionTable[idx] = packed;
            }
        }
        useTransitionTable = true;
    }

    @Override
    public void initializeTape(int[] input) {
        int cap = Math.max(64, (input == null ? 0 : input.length) * 4 + 16);
        tapeArray = new int[cap];
        for (int i = 0; i < cap; i++) tapeArray[i] = blankSymbol;
        tapeOrigin = cap / 4;
        if (input != null) {
            for (int i = 0; i < input.length; i++) tapeArray[tapeOrigin + i] = input[i];
            if (input.length > 0) {
                minVisited = 0;
                maxVisited = input.length - 1;
            } else {
                minVisited = Integer.MAX_VALUE;
                maxVisited = Integer.MIN_VALUE;
            }
        } else {
            minVisited = Integer.MAX_VALUE;
            maxVisited = Integer.MIN_VALUE;
        }
        head = 0;
        halted = false;
    }

    @Override
    public int readTape() {
        updateVisited();
        if (tapeArray == null) return blankSymbol;
        int ai = tapeOrigin + head;
        if (ai < 0 || ai >= tapeArray.length) return blankSymbol;
        return tapeArray[ai];
    }

    @Override
    public void writeTape(int symbol) {
        updateVisited();
        int ai = tapeOrigin + head;
        ensureTapeCapacityForIndex(ai);
        // tapeOrigin may have changed during ensure; recompute array index
        ai = tapeOrigin + head;
        ensureTapeCapacityForIndex(ai);
        tapeArray[ai] = symbol;
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
        if (useTransitionTable && transitionTable != null) {
            if (haltingStates != null && currentState >= 0 && currentState < haltingStates.length && haltingStates[currentState]) { halted = true; return; }
            int read = readTape();
            int idx = currentState * symbolsPerState + read;
            if (idx < 0 || idx >= transitionTable.length) { halted = true; return; }
            int packed = transitionTable[idx];
            if (packed == -1) { halted = true; return; }
            int next = packed >>> 8;
            int write = (packed >>> 1) & 0x7F;
            int dirBit = packed & 1;

            writeTape(write);
            currentState = next;
            if (dirBit == 0) head--; else head++;

            updateVisited();

            if (currentState >= 0 && haltingStates != null && currentState < haltingStates.length && haltingStates[currentState]) halted = true;
            return;
        }

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

        updateVisited();

        TMStateInterface newState = states.get(currentState);
        if (newState != null && newState.isHalting()) halted = true;
    }

    @Override
    public void run() {
        while (!halted) step();
    }

    @Override
    public boolean isHalted() { return halted; }

    @Override
    public void reset() { tapeArray = null; head = 0; currentState = 0; halted = false; }
    
    // ensure reset also clears visited range
    public void fullReset() { reset(); minVisited = Integer.MAX_VALUE; maxVisited = Integer.MIN_VALUE; }

    private void updateVisited() {
        if (head < minVisited) minVisited = head;
        if (head > maxVisited) maxVisited = head;
    }

    /** Return visited tape content from leftmost visited to rightmost visited as a string. */
    public String getVisitedContentString() {
        if (minVisited == Integer.MAX_VALUE) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = minVisited; i <= maxVisited; i++) {
            int s;
            if (tapeArray == null) s = blankSymbol;
            else {
                int ai = tapeOrigin + i;
                if (ai < 0 || ai >= tapeArray.length) s = blankSymbol; else s = tapeArray[ai];
            }
            sb.append(Integer.toString(Math.max(0, s)));
        }
        return sb.toString();
    }

    /** Return number of visited tape squares (inclusive). */
    public int getVisitedLength() {
        if (minVisited == Integer.MAX_VALUE) return 0;
        return maxVisited - minVisited + 1;
    }

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
        if (tapeArray == null) return BigInteger.ZERO;
        int minIdx = Integer.MAX_VALUE, maxIdx = Integer.MIN_VALUE;
        for (int ai = 0; ai < tapeArray.length; ai++) {
            if (tapeArray[ai] != blankSymbol) {
                minIdx = Math.min(minIdx, ai);
                maxIdx = Math.max(maxIdx, ai);
            }
        }
        if (minIdx == Integer.MAX_VALUE) return BigInteger.ZERO;
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        for (int ai = minIdx; ai <= maxIdx; ai++) {
            int s = tapeArray[ai];
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
        if (tapeArray == null) return 0;
        int minIdx = Integer.MAX_VALUE, maxIdx = Integer.MIN_VALUE;
        for (int ai = 0; ai < tapeArray.length; ai++) {
            if (tapeArray[ai] != blankSymbol) {
                minIdx = Math.min(minIdx, ai);
                maxIdx = Math.max(maxIdx, ai);
            }
        }
        if (minIdx == Integer.MAX_VALUE) return 0;
        return maxIdx - minIdx + 1;
    }

    @Override
    public long getSumOfSymbols() {
        if (tapeArray == null) return 0L;
        long sum = 0;
        for (int v : tapeArray) if (v != blankSymbol) sum += v;
        return sum;
    }

    // helper: set tape from number of 1s (unary input)
    public void initializeUnaryInput(int ones) {
        int cap = Math.max(64, ones * 4 + 16);
        tapeArray = new int[cap];
        for (int i = 0; i < cap; i++) tapeArray[i] = blankSymbol;
        tapeOrigin = cap / 4;
        for (int i = 0; i < ones; i++) tapeArray[tapeOrigin + i] = 1;
        head = 0;
        halted = false;
        if (ones <= 0) {
            minVisited = Integer.MAX_VALUE;
            maxVisited = Integer.MIN_VALUE;
        } else {
            minVisited = 0;
            maxVisited = ones - 1;
        }
    }

    /**
     * Create a fresh TM instance that contains the same state/transition
     * definitions (a template copy) but with an empty tape and reset head.
     */
    public TM cloneTemplate() {
        TM copy = new TM();
        copy.blankSymbol = this.blankSymbol;
        for (TMStateInterface s : this.states.values()) {
            if (s instanceof TMState) {
                TMState src = (TMState) s;
                TMState dst = new TMState(src.getId());
                dst.setHalting(src.isHalting());
                src.copyTo(dst);
                copy.addState(dst);
            } else {
                TMState dst = new TMState(s.getId());
                dst.setHalting(s.isHalting());
                copy.addState(dst);
            }
        }
        return copy;
    }

}
