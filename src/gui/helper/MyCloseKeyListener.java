package gui.helper;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTextField;

public class MyCloseKeyListener implements KeyListener {

	public JFrame frame;
	private JTextField field;
	@SuppressWarnings("rawtypes")
	private JList list;
	
	@SuppressWarnings("rawtypes")
	public MyCloseKeyListener(JFrame frame, JTextField field, JList list) {
		this.frame = frame;
		this.list = list;
		this.field = field;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			frame.dispose();
		}
		
		if(field == null || list == null) return;
		if(e.getSource().equals(field)) {
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				list.requestFocus();
				list.setSelectedIndex(0);
			}
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
	}

}
