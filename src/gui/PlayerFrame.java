package gui;

import java.awt.Dimension;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import cotg.wrappers.Player;
import gui.helper.MyCloseKeyListener;

public class PlayerFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;

	public PlayerFrame(Player player) {
		this.setPreferredSize(new Dimension(400, 600));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.addKeyListeners(new MyCloseKeyListener(this));
		
		// adding components
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private void addKeyListeners(KeyListener keyListener) {
		this.addKeyListener(keyListener);
	}
	
}
