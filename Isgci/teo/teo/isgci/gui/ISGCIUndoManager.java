package teo.isgci.gui;

import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;

public class ISGCIUndoManager extends mxUndoManager{
	public ISGCIUndoManager(){
		super();
	}
	
	@Override
	public void undo()
	{
		System.out.println("Before: undo(): " + indexOfNextAdd);	
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
		System.out.println("After: undo(): " + indexOfNextAdd);
	}
	
	public void removeUndos(int amount){
		System.out.println("Before: removeUndos(): " + indexOfNextAdd);
		while(history.size() > 0 && amount > 0){
			mxUndoableEdit edit = history.remove(--indexOfNextAdd);
			edit.die();
			amount--;
		}
		System.out.println("After: removeUndos(): " + indexOfNextAdd);
	}
	
	public int getIndexNext(){
		return indexOfNextAdd;
	}
}
