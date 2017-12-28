package cotg.data;

import java.awt.print.Printable;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import cotg.Parser;
import cotg.wrappers.Alliance;
import cotg.wrappers.City;
import cotg.wrappers.Player;
import cotg.wrappers.helper.Pair;

public class Constants {

	public static final File alliance_file = new File("src/cotg/data/all_data.txt");
	public static Alliance[] curr_alliances;

	static {
		try {
			curr_alliances = getAllianceFromFile(alliance_file);
			if (curr_alliances == null) {
				System.out.println("Parsing");
				curr_alliances = Parser.parseIntoAlliances("src/cotg/data/playerData.csv", 0);
				Parser.storeData(curr_alliances);
			} else {
				// Try to update! :)
				sortByScore(curr_alliances);
				Parser.update("src/cotg/data/playerData.csv");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Alliance[] getAllianceFromFile(File file) throws Exception {
		if (!file.exists())
			return null;
		Alliance[] e;
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		e = (Alliance[]) ois.readObject();
		ois.close();
		fis.close();
		System.out.println("Map<Date, Double> obtained from file");
		return e;
	}

	private static void sortByScore(Alliance[] curr_alliances2) {
		Arrays.sort(curr_alliances2, new Comparator<Alliance>() {
			@Override
			public int compare(Alliance o1, Alliance o2) {
				return new Integer(o2.score).compareTo(new Integer(o1.score));
			}
		});
	}

	public static void printAlliances() {
		System.out.println("Printing alliances");
		for (Alliance alliance : curr_alliances) {
			System.out.println(alliance.name);
		}
	}

	public static void printAlliance(String s) {
		Alliance alliance = findAlliance(s);

		System.out.println("Alliance: " + alliance.name + " with score of " + alliance.score);
		for (Player player : alliance.players) {
			System.out.println("\t" + player);
		}
	}

	public static Alliance findAlliance(String s) {
		for (Alliance alliance : curr_alliances) {
			if (alliance.name.equals(s))
				return alliance;
		}
		return null;
	}

	public static Player findPlayer(String s) {
		for (Alliance a : curr_alliances) {
			for (Player p : a.players) {
				if (p.name.equals(s)) {
					return p;
				}
			}
		}
		return null;
	}
	
	public static final String WPH = "Wolf Pack & Horizon";
	public static final String DMC = "Dirty Mastiff Cartel";
	public static final String BSR = "Black Sail Reapers";

	public static void main(String[] args) {
//		printLandCastlesForAllianceContinent(DMC, 03);
		printWaterCastlesForAllianceContinent(DMC, 03);
//		printWaterCastlesForAllianceContinent(BSR, 54);
	}

	public static void printWaterCastlesForPlayerOnContinent(String player, int continent) {
		Player p = null;
		if ((p = findPlayer(player)) == null)
			return;

		int count = 0;
		for (City c : p.cities)
			if (c.continent == continent && c.isCastle && c.isWater) {
				System.out.println(c);
				count++;
			}

		System.out.println(count);
	}

	public static void printCastlesInContinentNOTAlliance(String alliance, int continent) {
		Alliance alliance2 = findAlliance(alliance);

		if (alliance2 == null)
			return;

		int count = 0;
		for (Alliance a : curr_alliances) {
			if (!a.equals(alliance2)) {
				for (Player p : a.players) {
					for (City c : p.cities) {
						if (c.continent == continent && c.isCastle) {
							System.out.println(p.name + "\t" + c.coords());
							count++;
						}
					}
				}
			}
		}
		System.out.println(count);

	}

	public static void printCastlesInContinentForAlliance(String alliance, int continent) {
		Alliance alliance2 = findAlliance(alliance);

		if (alliance2 == null)
			return;

		int count = 0;
		for (Player p : alliance2.players) {
			for (City c : p.cities) {
				if (c.continent == continent && c.isCastle) {
					System.out.println(c);
					count++;
				}
			}
		}
		System.out.println(count);
	}

	public static void printWaterCastlesForPlayer(String player) {
		Player p = null;
		if ((p = findPlayer(player)) == null)
			return;
		int count = 0;
		for (City c : p.cities)
			if (c.isCastle && c.isWater) {
				System.out.println(p.name + "\t" + c.coords());
				count++;
			}

		System.out.println(count);
	}
	
	public static void printWaterCastlesForAllianceContinent(String alliance, int continent) {
		Alliance alliance2 = findAlliance(alliance);

		if (alliance2 == null)
			return;

		int count = 0;
		for (Player p : alliance2.players) {
			for (City c : p.cities) {
				if (c.continent == continent && c.isCastle && c.isWater) {
					System.out.println(p.name + "\t" + c.coords());
					count++;
				}
			}
		}
		System.out.println(count);
	}
	
	public static void printLandCastlesForAllianceContinent(String alliance, int continent) {
		Alliance alliance2 = findAlliance(alliance);

		if (alliance2 == null)
			return;

		int count = 0;
		for (Player p : alliance2.players) {
			for (City c : p.cities) {
				if (c.continent == continent && c.isCastle && !c.isWater) {
					System.out.println(p.name + "\t" + c.coords());
					count++;
				}
			}
		}
		System.out.println(count);
	}

	public static void printCityCountOnContinent(int continent) {
		int count = 0;
		for (Alliance a : curr_alliances) {
			for (Player p : a.players) {
				for (City c : p.cities) {
					if (c.continent == continent)
						count++;
				}
			}
		}

		System.out.println("Continent " + continent + " has " + count + " cities.");
	}

	public static int addAlliance(String s1) {
		System.out.println("ADD ALLIANCE: " + s1);
		Alliance[] alliances = new Alliance[curr_alliances.length + 1];
		for (int i = 0; i < curr_alliances.length; i++) {
			alliances[i] = curr_alliances[i];
		}
		alliances[curr_alliances.length] = new Alliance(s1);
		System.out.println("Added Alliance " + s1);
		curr_alliances = alliances;
		return curr_alliances.length - 1;
	}

	public static void printScoreOfPlayersOnContinent(int continent) {
		int maxLength = Integer.MAX_VALUE;
		Map<String, Pair<Integer, Integer>> players = new TreeMap<String, Pair<Integer, Integer>>();
		for (int i = 0; i < curr_alliances.length; i++) {
			for (Player p : curr_alliances[i].players) {
				int score = 0, cities = 0;
				for (City c : p.cities) {
					if (c.continent == continent) {
						cities++;
						score += c.score;
					}
				}
				if (cities > 0) {
					players.put(p.name, new Pair<Integer, Integer>(cities, score));
					maxLength = Math.max(maxLength, p.name.length());
				}
			}
		}

		SortedSet<Map.Entry<String, Pair<Integer, Integer>>> sortedset = new TreeSet<Map.Entry<String, Pair<Integer, Integer>>>(
				new Comparator<Map.Entry<String, Pair<Integer, Integer>>>() {
					@Override
					public int compare(Map.Entry<String, Pair<Integer, Integer>> e1,
							Map.Entry<String, Pair<Integer, Integer>> e2) {
						return e2.getValue().p2.compareTo(e1.getValue().p2);
					}
				});

		sortedset.addAll(players.entrySet());

		System.out.println("Scores for Continent " + continent);
		print(sortedset, maxLength);
	}

	private static void print(SortedSet<Map.Entry<String, Pair<Integer, Integer>>> sortedset, int maxLength) {
		System.out.printf("%15s\t%15s\t%15s\t\n", "Name", "Score", "Cities");
		for (Map.Entry<String, Pair<Integer, Integer>> e : sortedset) {
			System.out.printf("%15s\t%15s\t%15s\t\n", e.getKey(), e.getValue().p2, e.getValue().p1);
		}
	}

}
