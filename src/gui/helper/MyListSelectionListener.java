package gui.helper;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MyListSelectionListener<V> implements ListSelectionListener {

	public V selected;
	
	public MyListSelectionListener() {
		selected = null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting()) return;
		
		
		selected = (V) ((JList<V>)e.getSource()).getSelectedValue();
	}

}
