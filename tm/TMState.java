package tm;
// This should be the Turing Machine State class.
public class TMState implements TMStateInterface {

    @Override
    public int getId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getId'");
    }

    @Override
    public void setId(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setId'");
    }

    @Override
    public boolean isHalting() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isHalting'");
    }

    @Override
    public void setHalting(boolean halting) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setHalting'");
    }

    @Override
    public void addTransition(int readSymbol, int nextStateId, int writeSymbol, char direction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addTransition'");
    }

    @Override
    public boolean hasTransition(int readSymbol) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasTransition'");
    }

    @Override
    public int getNextState(int readSymbol) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNextState'");
    }

    @Override
    public int getWriteSymbol(int readSymbol) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWriteSymbol'");
    }

    @Override
    public char getDirection(int readSymbol) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDirection'");
    }

    
} // end of TMState class
