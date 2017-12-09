package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import cotg.wrappers.City;
import cotg.wrappers.Player;
import gui.helper.Map;
import gui.helper.MyCloseKeyListener;
import gui.helper.PieChart;

public class PlayerFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private Player player;
	private PieChart chart;
	private Map continents;
	
	private static MyCloseKeyListener keyListener = new MyCloseKeyListener(null, null, null);
	
	private JTextArea textField;
	
	public PlayerFrame(Player player) {
		keyListener.frame = this;
		this.player = player;
		this.continents = new Map();
		
		this.setPreferredSize(new Dimension(400, 600));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		initComponents();
		this.addKeyListeners(keyListener);
		this.setBackground(Color.WHITE);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private void initComponents() {
		
		// Initialize continents
		for(City c : player.cities) continents.addCity(c.continent, c.score);
		
		// Initialize scores
		chart = new PieChart(toDouble(continents.getScore()), toString(continents.getContinents()));
		
		this.setLayout(new FlowLayout());
		
		textField = new JTextArea("Player: " + player.name + "\nScore: " + player.score);
		textField.setEditable(false);
		this.add(textField);
	}
	
	public void paint(Graphics g) {
		super.paintComponents(g);
		chart.paint(g);
	}

	private String[] toString(Integer[] continents2) {
		String[] ret = new String[continents2.length];
		for(int i = 0; i < continents2.length; i++) {
			ret[i] = continents2[i].toString();
		}
		return ret;
	}

	private double[] toDouble(Integer[] score) {
		double[] ret = new double[score.length];
		for(int i = 0; i < score.length; i++) {
			ret[i] = score[i];
		}
		return ret;
	}

	private void addKeyListeners(KeyListener keyListener) {
		textField.addKeyListener(keyListener);
		this.addKeyListener(keyListener);
	}
	
}
