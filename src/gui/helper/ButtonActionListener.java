package gui.helper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (running)
			return;
		running = true;
		int parsedInt = -1;
		String parsedString = "";
		try {
			parsedInt = Integer.parseInt(continent.getText().trim());
			parsedString = string.getText().trim();
		} catch (NumberFormatException t) {
			running = false;
			System.out.println("error");
			return;
		}
		
		System.out.println("RUNNING WITH INPUTS " + parsedInt + " " + parsedString);
		switch (i) {
		case 1: // Constants.printCityCountOnContinent
			Constants.printCityCountOnContinent(parsedInt);
			break;
		case 2: // Constants.printScoreOfPlayersOnContinent
			Constants.printScoreOfPlayersOnContinent(parsedInt);
			break;
		case 3: // Constants.printWaterCastlesForPlayerOnContinent
			Constants.printWaterCastlesForPlayerOnContinent(parsedString, parsedInt);
			break;
		case 4: // Constants.printAlliance
			Constants.printAlliance(parsedString);
			break;
		case 5: // Constants.printAlliances
			Constants.printAlliances();
			break;
		case 6: // Militarys.printAndLoadFaiths
			Militarys.printAndLoadFaiths();
			break;
		case 7: // Militarys.printAndLoadControlledContinents
			Militarys.printAndLoadControlledContinents();
			break;
		case 8: // Militarys.printAllianceControllingContinents
			Militarys.printAllianceControllingContinents();
			break;
		case 9: // Constants.printWaterCastlesForPlayer
			Constants.printWaterCastlesForPlayer(parsedString);
			break;
		}
		running = false;
	}

}
