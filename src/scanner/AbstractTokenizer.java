package scanner;

import java.util.Iterator;

public abstract class AbstractTokenizer implements Iterator {

	public abstract boolean hasNext();
    
	public abstract Object next();
    
    public abstract void remove();
    
	public abstract long dataRead();
	    
	public abstract int getLine();
	
}
