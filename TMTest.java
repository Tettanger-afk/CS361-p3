import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import tm.TMInterface;
import tm.TMStateInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple JUnit tests that provide tiny concrete/dummy implementations
 * of the TM interfaces and assert basic behavior. This lets you compile
 * and run tests even though full TM logic is not implemented yet.
 */
public class TMTest {

    // Minimal TMState implementation for testing
    static class DummyTMState implements TMStateInterface {
        private int id;
        private boolean halting;
        private final Map<Integer, Transition> transitions = new HashMap<>();

        static class Transition {
            int nextStateId;
            int writeSymbol;
            char direction;
            Transition(int n, int w, char d) { nextStateId = n; writeSymbol = w; direction = d; }
        }

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
    }

    // Minimal TM implementation for testing
    static class DummyTM implements TMInterface {
        private int[] tape = new int[0];
        private int head = 0;
        private int currentState = 0;
        private boolean halted = false;
        private int blank = 0;
        private final Map<Integer, TMStateInterface> states = new HashMap<>();
        private long steps = 0;

        @Override
        public void initializeTape(int[] input) {
            tape = input == null ? new int[0] : input.clone();
            head = 0;
        }

        @Override
        public int readTape() {
            if (tape.length == 0 || head < 0 || head >= tape.length) return blank;
            return tape[head];
        }

        @Override
        public void writeTape(int symbol) {
            if (tape.length == 0) tape = new int[1];
            if (head < 0) throw new IndexOutOfBoundsException("head < 0");
            if (head >= tape.length) {
                int[] nt = new int[head+1];
                System.arraycopy(tape, 0, nt, 0, tape.length);
                tape = nt;
            }
            tape[head] = symbol;
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
        public void step() { steps++; halted = true; }

        @Override
        public void run(long maxSteps) { if (maxSteps <= 0) { step(); } else { for (long i=0;i<maxSteps;i++) step(); } }

        @Override
        public boolean isHalted() { return halted; }

        @Override
        public void reset() { head = 0; currentState = 0; halted = false; steps = 0; }

        @Override
        public void setBlankSymbol(int blankSymbol) { blank = blankSymbol; }

        @Override
        public int getBlankSymbol() { return blank; }

        @Override
        public void addState(TMStateInterface state) { states.put(state.getId(), state); }

        @Override
        public TMStateInterface getState(int stateId) { return states.get(stateId); }

        @Override
        public long getOutputAsNumber() {
            long val = 0;
            for (int i=0;i<tape.length;i++) {
                val = val*10 + Math.max(0, tape[i]);
            }
            return val;
        }

        @Override
        public int getOutputLength() { return tape.length; }

        @Override
        public long getSumOfSymbols() {
            long sum = 0;
            for (int v : tape) sum += v;
            return sum;
        }
    }

    @Test
    public void dummyStateTransitionsWork() {
        DummyTMState s = new DummyTMState();
        s.setId(2);
        assertEquals(2, s.getId());
        assertFalse(s.isHalting());
        s.setHalting(true);
        assertTrue(s.isHalting());

        s.addTransition(1, 3, 9, 'R');
        assertTrue(s.hasTransition(1));
        assertEquals(3, s.getNextState(1));
        assertEquals(9, s.getWriteSymbol(1));
        assertEquals('R', s.getDirection(1));

        assertFalse(s.hasTransition(0));
        assertEquals(-1, s.getNextState(0));
    }

    @Test
    public void dummyTMReadsWritesAndReports() {
        DummyTM tm = new DummyTM();
        tm.setBlankSymbol(7);
        assertEquals(7, tm.getBlankSymbol());

        tm.initializeTape(new int[]{1,2,3});
        assertEquals(1, tm.readTape());
        assertEquals(3, tm.getOutputLength());
        assertEquals(123, tm.getOutputAsNumber());
        assertEquals(6, tm.getSumOfSymbols());

        tm.setHeadPosition(1);
        assertEquals(2, tm.readTape());
        tm.writeTape(5);
        assertEquals(5, tm.readTape());

        tm.reset();
        assertFalse(tm.isHalted());
        tm.step();
        assertTrue(tm.isHalted());
    }
}
