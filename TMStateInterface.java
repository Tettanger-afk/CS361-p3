/**
 * TMStateInterface models a single state of a Turing Machine. Implementations
 * should store transitions that map a read symbol to (nextStateId, writeSymbol,
 * direction).
 */
public interface TMStateInterface {

	/** Return the unique id for this state. */
	int getId();

	/** Set the unique id for this state. */
	void setId(int id);

	/** True if this is a halting state (machine should stop when entered). */
	boolean isHalting();

	/** Mark or unmark this state as halting. */
	void setHalting(boolean halting);

	/**
	 * Add a transition for when the head reads `readSymbol`.
	 * direction should be 'L' (left), 'R' (right), or 'N' (no move).
	 */
	void addTransition(int readSymbol, int nextStateId, int writeSymbol, char direction);

	/** Return true if a transition exists for the given read symbol. */
	boolean hasTransition(int readSymbol);

	/** Get the next state id for the given read symbol. */
	int getNextState(int readSymbol);

	/** Get the symbol to write for the given read symbol. */
	int getWriteSymbol(int readSymbol);

	/** Get the direction ('L'/'R'/'N') for the given read symbol. */
	char getDirection(int readSymbol);

}
