package gui.helper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import cotg.Militarys;
import cotg.data.Constants;

public class ButtonActionListener implements ActionListener {

	JTextField continent;
	JTextField string;
	int i;

	boolean running = false;

	public ButtonActionListener(JTextField continent, JTextField string) {
		i = -1;
		this.continent = continent;
		this.string = string;
	}

	private ButtonActionListener(int i, JTextField continent, JTextField string) {
		this.i = i;
		this.continent = continent;
		this.string = string;
	}

	public ButtonActionListener set(int i) {
		return new ButtonActionListener(i, continent, string);
	}
	
	public ByteArrayOutputStream gatherPrintlnOutput() {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos);
	    System.setOut(ps);
	    return baos;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (running)
			return;
		running = true;
		int parsedInt = -1;
		String parsedString = "";

		if (!continent.getText().trim().equals("")) {
			try {
				parsedInt = Integer.parseInt(continent.getText().trim());
				parsedString = string.getText().trim();
			} catch (NumberFormatException t) {
				running = false;
				System.out.println("error");
				return;
			}
		}

		PrintStream old = System.out;
		ByteArrayOutputStream x = gatherPrintlnOutput();
		switch (i) {
		case 1: // Constants.printCityCountOnContinent
			Constants.printCityCountOnContinent(parsedInt);
			JOptionPane.showMessageDialog(null, x.toString(), "City Count on " + parsedInt, JOptionPane.INFORMATION_MESSAGE);
			break;
		case 2: // Constants.printScoreOfPlayersOnContinent
			Constants.printScoreOfPlayersOnContinent(parsedInt);
			JOptionPane.showMessageDialog(null, x.toString(), "Score on " + parsedInt, JOptionPane.INFORMATION_MESSAGE);
			break;
		case 3: // Constants.printWaterCastlesForPlayerOnContinent
			Constants.printWaterCastlesForPlayerOnContinent(parsedString, parsedInt);
			JOptionPane.showMessageDialog(null, x.toString(), "Water Castles for " + parsedString + " on " + parsedInt, JOptionPane.INFORMATION_MESSAGE);
			break;
		case 4: // Constants.printAlliance
			Constants.printAlliance(parsedString);
			JOptionPane.showMessageDialog(null, x.toString(), "Alliance " + parsedString, JOptionPane.INFORMATION_MESSAGE);
			break;
		case 5: // Constants.printAlliances
			Constants.printAlliances();
			JOptionPane.showMessageDialog(null, x.toString(), "Alliances", JOptionPane.INFORMATION_MESSAGE);
			break;
		case 6: // Militarys.printAndLoadFaiths
			Militarys.printAndLoadFaiths();
			JOptionPane.showMessageDialog(null, x.toString(), "Faiths", JOptionPane.INFORMATION_MESSAGE);
			break;
		case 7: // Militarys.printAndLoadControlledContinents
			Militarys.printAndLoadControlledContinents();
			JOptionPane.showMessageDialog(null, x.toString(), "Controlled Continents", JOptionPane.INFORMATION_MESSAGE);
			break;
		case 8: // Militarys.printAllianceControllingContinents
			Militarys.printAllianceControllingContinents();
			JOptionPane.showMessageDialog(null, x.toString(), "Controlling Continents", JOptionPane.INFORMATION_MESSAGE);
			break;
		case 9: // Constants.printWaterCastlesForPlayer
			Constants.printWaterCastlesForPlayer(parsedString);
			JOptionPane.showMessageDialog(null, x.toString(), "Water Castles for " + parsedString, JOptionPane.INFORMATION_MESSAGE);
			break;
		}
		System.setOut(old);
		running = false;
	}

}
