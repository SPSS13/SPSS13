package teo.isgci.gui;

import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;

/**
 * ISGCIUndomanager necessary for extended methods from mxUndoManager of JGraphX;
 * changed undo-Methods
 * @author Matthias Miller
 *
 */
public class ISGCIUndoManager extends mxUndoManager{
    private boolean significant = true;
    
	public ISGCIUndoManager(){
		super();
	}
	
	/**
	 * Undo-Method, which undos last Changes
	 */
	@Override
	public void undo()
	{
//		System.out.println("Before: undo(): " + indexOfNextAdd);	
		while (indexOfNextAdd > 0)
		{
			mxUndoableEdit edit = history.get(--indexOfNextAdd);
			edit.undo();

			if (edit.isSignificant())
			{
				fireEvent(new mxEventObject(mxEvent.UNDO, "edit", edit));
				break;
			}
		}
//		System.out.println("After: undo(): " + indexOfNextAdd);
	}
	
	/**
	 * removes amount number of last undos from Stack, until Stack is empty or amount is met
	 * @param amount
	 */
	public void removeUndos(int amount){
//		System.out.println("Before: removeUndos(): " + indexOfNextAdd);
		while(history.size() > 0 && amount > 0){
			mxUndoableEdit edit = history.remove(--indexOfNextAdd);
			edit.die();
			amount--;
		}
//		System.out.println("After: removeUndos(): " + indexOfNextAdd);
	}
	
	/**
	 * mark only significant edits
	 */
	@Override
	public void undoableEditHappened(mxUndoableEdit undoableEdit){
	    if(significant)
	        super.undoableEditHappened(undoableEdit);
	}
	/**
	 * Gives next Index from Stack of next to adding undo action
	 * @return
	 */
	public int getIndexNext(){
		return indexOfNextAdd;
	}

    public boolean isSignificant() {
        return significant;
    }

    public void setSignificant(boolean significant) {
        this.significant = significant;
    }
}
