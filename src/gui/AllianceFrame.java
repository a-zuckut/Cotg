package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import cotg.wrappers.Alliance;
import gui.helper.MyCloseKeyListener;

public class AllianceFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	public static final int ALLIANCE_HEIGHT = 400;
	public static final int ALLIANCE_WIDTH = 600;
	
	private MyCloseKeyListener keyListener;
	
	private PlayersPanel pPanel;

	public AllianceFrame(Alliance a) {
		this.setPreferredSize(new Dimension(ALLIANCE_WIDTH, ALLIANCE_HEIGHT));
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setLocationRelativeTo(GUI.frame);
		this.setLayout(new BorderLayout());
		
		pPanel = new PlayersPanel(a.players, getWidth()/2, getHeight()/2);
		pPanel.frame = this;
		pPanel.addKeyListeners();
		
		this.add(pPanel, BorderLayout.CENTER);
		
		this.addKeyListener(keyListener);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}
	
}
