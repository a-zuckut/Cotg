package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;

public class GuiRunner {

	public static final int WIDTH = 1100;
	public static final int HEIGHT = 800;
	
	public static JFrame main;
	public static GUI gui;
	public static Functions functions;
	
	public static void initGUI() {
		main = new JFrame("Testing Data Screen");
		main.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		main.setLayout(new FlowLayout());
		
		gui = new GUI();
		functions = new Functions();
		
		main.add(gui);
		main.add(functions);
		
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setVisible(true);
		main.pack();
	}
	
	public static void main(String[] args) {
		initGUI();
	}

}
