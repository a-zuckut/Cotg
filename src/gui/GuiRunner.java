package gui;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import cotg.Parser;

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
		
		Button UPDATE_DATA = new Button("Update DATA from file...");
		UPDATE_DATA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(null);
				Parser.update(chooser.getSelectedFile().getPath());
			}
		});
		Button ATTACK_CREATOR = new Button("Attack Creator...");
		ATTACK_CREATOR.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Goal is to allow for selection to create an 'automated' attack creator w/ options
			}
		});
		main.add(UPDATE_DATA);
		main.add(ATTACK_CREATOR);
		
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setVisible(true);
		main.pack();
	}
	
	public static void main(String[] args) {
		initGUI();
	}

}
