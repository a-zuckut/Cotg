package gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gui.helper.ButtonActionListener;

public class Functions extends JPanel {
	private static final long serialVersionUID = 1L;

	// Variables
	JTextField continent;
	JTextField player;

	// Functions
	private JButton printCityCountOnContinent;
	private JButton printScoreOfPlayersOnContinent;
	private JButton printWaterCastlesForPlayerOnContinent;
	private JButton printWaterCastlesForPlayer;

	private JButton printAlliance;
	private JButton printAlliances;

	private JButton printAndLoadFaiths;
	private JButton printAllianceControllingContinents;
	private JButton printAndLoadControlledContinents;

	private ButtonActionListener buttonActionListener;

	public Functions() {
		this.setPreferredSize(new Dimension(900, 400));
		initComponents();
		setUpLayout();
	}

	private void initComponents() {
		continent = new JTextField("", 10);
		player = new JTextField("", 20);
		buttonActionListener = new ButtonActionListener(continent, player);

		// Requires continent
		printCityCountOnContinent = new JButton("City Count (continent)");
		printCityCountOnContinent.addActionListener(buttonActionListener.set(1));

		// Requires continent
		printScoreOfPlayersOnContinent = new JButton("Scores (continent)");
		printScoreOfPlayersOnContinent.addActionListener(buttonActionListener.set(2));

		// Requires player and continent
		printWaterCastlesForPlayerOnContinent = new JButton("Player Water Castles On Cont (player, continent)");
		printWaterCastlesForPlayerOnContinent.addActionListener(buttonActionListener.set(3));
		
		// Requires player
		printWaterCastlesForPlayer = new JButton("Player Water Castles (player)");
		printWaterCastlesForPlayer.addActionListener(buttonActionListener.set(9));

		// Requires string (alliance)
		printAlliance = new JButton("Print Specific Alliance (alliance)");
		printAlliance.addActionListener(buttonActionListener.set(4));

		// Requires N/A
		printAlliances = new JButton("Print Alliances ()");
		printAlliances.addActionListener(buttonActionListener.set(5));

		// Requires N/A
		printAndLoadFaiths = new JButton("Print Faiths ()");
		printAndLoadFaiths.addActionListener(buttonActionListener.set(6));

		// Requires N/A
		printAndLoadControlledContinents = new JButton("Controlled Continents ()");
		printAndLoadControlledContinents.addActionListener(buttonActionListener.set(7));

		// Requires N/A
		printAllianceControllingContinents = new JButton("Alliance Contients ()");
		printAllianceControllingContinents.addActionListener(buttonActionListener.set(8));
	}

	private void setUpLayout() {
		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.CENTER;
		this.add(new JLabel("Continent"), c);
		
		c.gridx = 2;
		this.add(continent, c);
		
		c.gridx = 0;
		c.gridy = 1;
		this.add(new JLabel("String (alliance/player)"), c);

		c.gridx = 2;
		this.add(player, c);
		
		c.gridx = 0;
		c.gridy = 2;
		this.add(printCityCountOnContinent, c);
		
		c.gridx = 1;
		this.add(printScoreOfPlayersOnContinent, c);
		
		c.gridx = 2;
		this.add(printWaterCastlesForPlayerOnContinent, c);
		
		c.gridx = 3;
		this.add(printWaterCastlesForPlayer, c);
		
		c.gridx = 0;
		c.gridy = 3;
		this.add(printAlliance, c);
		
		c.gridx = 1;
		this.add(printAlliances, c);
		
		c.gridy = 4;
		c.gridx = 0;
		this.add(printAndLoadControlledContinents, c);
		
		c.gridx = 1;
		this.add(printAndLoadFaiths, c);
		
		c.gridx = 2;
		this.add(printAllianceControllingContinents, c);
	}

}
