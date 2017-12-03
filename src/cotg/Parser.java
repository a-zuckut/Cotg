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
import java.util.Iterator;
import java.util.Map;

import cotg.data.Constants;
import cotg.wrappers.Alliance;
import cotg.wrappers.City;
import cotg.wrappers.Player;

public class Parser {

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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static Map<String, Map<String, ArrayList<City>>> parseIntoMaps(String string) {

		ArrayList<String> in = parseFiles(string);
		Map<String, Map<String, ArrayList<City>>> ret = new HashMap<>();

		for (String input : in) {

			String[] split = input.split(",");
			if (split[0].toLowerCase().equals("timestamp"))
				continue;
			String alliance = split[2].trim();
			String player = split[1].trim();

			if (alliance.trim().equals("") || player.trim().equals(""))
				continue;

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
		Map<String, Map<String, ArrayList<City>>> data = parseIntoMaps(s);
		
		Alliance[] alliances = Constants.curr_alliances;
		
		for(String s1 : data.keySet()) {
			int index = -1;
			if((index = alliancesContains(s1)) != -1) {
				for(String p : data.get(s1).keySet()) {
					Player new_player = new Player(p, s1);
					new_player.cities.addAll(data.get(s1).get(p));
					new_player.generateScore();
					if(!alliances[index].players.add(new_player)) {
						// updates by replacing... 
						alliances[index].players.remove(new_player);
						alliances[index].players.add(new_player);
					} else {
						System.out.println("Added new player");
						alliances[index].players.add(new_player);
					}
				}
				alliances[index].generateScore();
			}
		}
	}

	public static int alliancesContains(String alliance) {
		for (int i = 0 ; i < Constants.curr_alliances.length ; i++) {
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

	public static void main(String[] args) {
		update("src/cotg/data/playerData.csv");
	}

	public static ArrayList<String> parseFiles(String file) {
		return parseFiles(new File(file));
	}

}
