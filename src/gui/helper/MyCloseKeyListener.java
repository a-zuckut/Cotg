package gui.helper;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

public class MyCloseKeyListener implements KeyListener {

	private JFrame frame;
	
	public MyCloseKeyListener(JFrame frame) {
		this.frame = frame;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		if(e.getKeyChar() == KeyEvent.VK_ESCAPE) {
			frame.dispose();
		}
	}
	@Override
	public void keyPressed(KeyEvent e) {
	}
	@Override
	public void keyReleased(KeyEvent e) {
	}

}
