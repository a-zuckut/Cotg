package cotg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cotg.data.Constants;
import cotg.wrappers.Alliance;
import cotg.wrappers.City;
import cotg.wrappers.Player;

public class Parser {
	
	public static final String fileName = "src/cotg/data/playerData.csv";

	/**
	 * Simple file parser
	 * 
	 * @param string
	 *            File name.
	 * @return The file in form of Strings for each line in the file
	 */
	public static ArrayList<String> parseFiles(File file) {
		ArrayList<String> ret = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String x = null;
			while ((x = br.readLine()) != null) {
				ret.add(x);
			}
			br.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static Map<String, Map<String, ArrayList<City>>> parseIntoMaps(String string) {

		ArrayList<String> in = parseFiles(string);
		
		if(in == null) return null;
		Map<String, Map<String, ArrayList<City>>> ret = new HashMap<>();

		for (String input : in) {

			String[] split = input.split(",");
			if (split[0].toLowerCase().equals("timestamp"))
				continue;
			String alliance = split[2].trim();
			String player = split[1].trim();

			City ne = new City(input);
			if (ret.containsKey(alliance)) {
				if (ret.get(alliance).containsKey(player)) {
					ret.get(alliance).get(player).add(ne);
				} else {
					ArrayList<City> n = new ArrayList<>();
					n.add(ne);
					ret.get(alliance).put(player, n);
				}
			} else {
				ret.put(alliance, new HashMap<>());
				ArrayList<City> n = new ArrayList<>();
				n.add(ne);
				ret.get(alliance).put(player, n);
			}
		}

		return ret;
	}

	public static Alliance[] parseIntoAlliances(String s, int score_min) {
		Map<String, Map<String, ArrayList<City>>> data = parseIntoMaps(s);
		if(data == null) return null;

		ArrayList<Alliance> TEMP = new ArrayList<Alliance>();
		
		for (String alliance : data.keySet()) {
			Map<String, ArrayList<City>> players = data.get(alliance);

			Alliance a = new Alliance(alliance);

			for (String pn : players.keySet()) {
				ArrayList<City> pc = players.get(pn);
				Player fP = new Player(pn, alliance);
				for (City c : pc)
					fP.cities.add(c);
				a.players.add(fP);
			}

			if (a.generateScore() >= score_min) {
				TEMP.add(a);
			}

		}

		Alliance[] ret = new Alliance[TEMP.size()];
		ret = TEMP.toArray(ret);
		return ret;
	}

	public static void update(String s) {
		System.out.println("Updating");
		Map<String, Map<String, ArrayList<City>>> data = parseIntoMaps(s);

		if(data == null) return;

		for (String s1 : data.keySet()) {
			int index = -1;
			
			if ((index = alliancesContains(s1)) == -1) {
				index = Constants.addAlliance(s1);
			}
			
			for (String p : data.get(s1).keySet()) {
				Player new_player = new Player(p, s1);
				new_player.cities.addAll(data.get(s1).get(p));
				new_player.generateScore();

				removePlayer(new_player, index);

				if (!Constants.curr_alliances[index].players.add(new_player)) {
					// updates by replacing...
					Constants.curr_alliances[index].players.remove(new_player);
					Constants.curr_alliances[index].players.add(new_player);
				} else {
					Constants.curr_alliances[index].players.add(new_player);
				}
			}
			Constants.curr_alliances[index].generateScore();
		}

		storeData(Constants.curr_alliances);
	}

	public static Alliance[] removeEmptyAlliances(Alliance[] alliances) {
		int num = 0;
		for (int i = 0; i < alliances.length; i++) {
			if (alliances[i].players.isEmpty())
				num++;
		}

		if (num == 0)
			return alliances;

		System.out.println("Removing " + num + " alliance(s).");

		Alliance[] ret = new Alliance[alliances.length - num];

		int index = 0;
		for (Alliance alliance : alliances) {
			if (!alliance.players.isEmpty())
				ret[index++] = alliance;
		}

		return ret;
	}

	private static void removePlayer(Player new_player, int index) {
		for (Alliance a : Constants.curr_alliances) {
			if (a.equals(Constants.curr_alliances[index]))
				continue;
			List<Player> remove = new LinkedList<>();
			for (Player player : a.players) {
				if (player.equals(new_player)) {
					remove.add(player);
				}
			}

			a.players.removeAll(remove);
		}
	}

	public static int alliancesContains(String alliance) {
		for (int i = 0; i < Constants.curr_alliances.length; i++) {
			if (Constants.curr_alliances[i].name.equals(alliance.trim()))
				return i;
		}
		return -1;
	}

	public static void storeData(Alliance[] p) {
		try {
			System.out.println("Stored data");
			storeAllianceArray(p, Constants.alliance_file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void print() {
		Alliance[] x = Constants.curr_alliances;
		String str = "";
		for (Alliance a : x) {
			str += a + "\n";
		}

		File f = new File("src/cotg/data/alliances.txt");
		try {
			f.createNewFile();
			FileWriter fw = new FileWriter(f);
			fw.write(str);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void storeAllianceArray(Alliance[] all, File file) throws Exception {
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file, false);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(all);
		oos.close();
		fos.close();
		System.out.println("Map<String, String> stored");
	}
	
	public static void printAlliances(Alliance[] a) {
		System.out.println("Printing alliances");
		for(Alliance alliance : a) {
			System.out.println("\"" + alliance.name + "\"");
		}
	}

	public static void main(String[] args) {
		Constants.printAlliances();
	}

	public static ArrayList<String> parseFiles(String file) {
		return parseFiles(new File(file));
	}

}
