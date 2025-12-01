package tm;
/**
 * TMInterface describes the operations a Turing Machine implementation
 * must provide for the simulator and state objects. Methods are kept
 * generic (use primitive arrays or collections in the implementation).
 */
public interface TMInterface     {

	// --- Tape operations ---
	/** Initialize the tape contents from the given input (symbols). */
	void initializeTape(int[] input);

	/** Read the symbol under the head. */
	int readTape();

	/** Write the given symbol at the head position. */
	void writeTape(int symbol);

	/** Get current head position (0-based index on the tape representation). */
	int getHeadPosition();

	/** Set current head position. */
	void setHeadPosition(int position);

	// --- State operations ---
	/** Get the id of the current state. */
	int getCurrentState();

	/** Set the current state by id. */
	void setCurrentState(int stateId);

	// --- Execution control ---
	/** Execute a single transition (one step). */
	void step();

	/**
	 * Run until a halting state is reached. The simulator should not impose
	 * an artificial maximum number of steps; external grading harnesses may
	 * enforce time limits.
	 */
	void run();

	/** True if the machine is in a halting state. */
	boolean isHalted();

	/** Reset machine (tape, head, and state) to the initial configuration. */
	void reset();

	// --- Configuration ---
	/** Set the blank symbol used on uninitialized tape cells. */
	void setBlankSymbol(int blankSymbol);

	/** Get the blank symbol. */
	int getBlankSymbol();

	/** Add a TM state object to the machine. */
	void addState(TMStateInterface state);

	/** Retrieve a state by id; return null if not found. */
	TMStateInterface getState(int stateId);

	// --- Reporting helpers ---
	/**
	 * Return the tape contents interpreted as a (possibly large) number.
	 * Implementations may choose representation; simulator may request
	 * a numeric value and handle overflow/"large number" separately.
	 */
	long getOutputAsNumber();

	/** Return the logical length (number of cells of interest) of the output. */
	int getOutputLength();

	/** Return the sum of symbols currently present on the tape. */
	long getSumOfSymbols();

}
