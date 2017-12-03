package cotg;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cotg.data.Constants;
import cotg.wrappers.Alliance;
import cotg.wrappers.Faith;
import cotg.wrappers.helper.Pair;

public class Militarys {

	// Ranking, Continent, Alliance, Military
	public static Map<Integer, Set<Pair<String, Integer>>> military_data;
	public static final File FILE = new File("src/cotg/data/militaryData.csv");

	public static void parseMilitariesData() {
		ArrayList<String> ret = Parser.parseFiles(FILE);
		military_data = new HashMap<Integer, Set<Pair<String, Integer>>>();
		for (String string : ret) {
			if (string.contains("ranking") && string.contains("alliance"))
				continue;
			String[] lineData = string.split(",");
			if (!military_data.containsKey(Integer.valueOf(lineData[1]))) {
				military_data.put(Integer.valueOf(lineData[1]), new HashSet<>());
				military_data.get(Integer.valueOf(lineData[1]))
						.add(new Pair<String, Integer>(lineData[2], Integer.valueOf(lineData[3])));
			} else {
				military_data.get(Integer.valueOf(lineData[1]))
						.add(new Pair<String, Integer>(lineData[2], Integer.valueOf(lineData[3])));
			}
		}
	}
	
	static {
		new Militarys();
	}

	public Militarys() {
		parseMilitariesData();
		loadControlledContinents();
	}

	public static void addMilitariesToAlliances() {
		for(Entry<Integer, Set<Pair<String, Integer>>> xEntry : military_data.entrySet()) {
			for(Pair<String, Integer> t : xEntry.getValue()) {
				Alliance alliance = Constants.findAlliance(t.p1);
				if(alliance==null) continue;
				alliance.military_per_continent.put(xEntry.getKey(), t.p2);
			}
		}
	}
	
	public static Map<Integer, String> allianceControlled;
	
	public static void printAndLoadControlledContinents() {
		List<Entry<Integer, Set<Pair<String, Integer>>>> list = new LinkedList<>(military_data.entrySet());
		allianceControlled = new HashMap<>();
		Collections.sort(list, new Comparator<Map.Entry<Integer, Set<Pair<String, Integer>>>>() {
			@Override
			public int compare(Entry<Integer, Set<Pair<String, Integer>>> o1,
					Entry<Integer, Set<Pair<String, Integer>>> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		System.out.print("\'Controlled\' Continents:\n");
		for (Entry<Integer, Set<Pair<String, Integer>>> x : list) {
			System.out.println("Continent " + x.getKey() + ":");
			boolean comma = false;
			List<Pair<String, Integer>> pa = new LinkedList<>(x.getValue());
			int sum = 0;
			for(Pair<String, Integer> summing : pa) {
				sum += summing.p2;
			}
			
			String one = "";
			for(Pair<String, Integer> deciding : pa) {
				if(deciding.p2.doubleValue()/((double)sum) > .30) {
					if (comma) {
						System.out.print(", " + deciding.p1);
					} else {
						one = deciding.p1;
						System.out.print(" " + deciding.p1);
						comma = true;
					}
				}
			}
			System.out.println(".");
			
			if(comma) allianceControlled.put(x.getKey(), one);
		}
	}
	
	public static void loadControlledContinents() {
		List<Entry<Integer, Set<Pair<String, Integer>>>> list = new LinkedList<>(military_data.entrySet());
		allianceControlled = new HashMap<>();
		Collections.sort(list, new Comparator<Map.Entry<Integer, Set<Pair<String, Integer>>>>() {
			@Override
			public int compare(Entry<Integer, Set<Pair<String, Integer>>> o1,
					Entry<Integer, Set<Pair<String, Integer>>> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		for (Entry<Integer, Set<Pair<String, Integer>>> x : list) {
			boolean comma = false;
			List<Pair<String, Integer>> pa = new LinkedList<>(x.getValue());
			int sum = 0;
			for(Pair<String, Integer> summing : pa) {
				sum += summing.p2;
			}
			
			String one = "";
			for(Pair<String, Integer> deciding : pa) {
				if(deciding.p2.doubleValue()/((double)sum) > .40) {
					if (comma) {
					} else {
						one = deciding.p1;
						comma = true;
					}
				}
			}
			if(comma) allianceControlled.put(x.getKey(), one);
		}
	}

	public static void printAndLoadFaiths() {
		printAndLoadControlledContinents();
		// using allianceControlled and shrinesOnOpenContinents
		Map<String, HashMap<Faith, Integer>> faiths = new HashMap<>();
		for(Entry<Integer, String> entry : allianceControlled.entrySet()) {
			Set<Faith> cont_faith = Shrines.shrines_on_open_continents.get(entry.getKey());
			if(cont_faith == null) continue;
			if(!faiths.containsKey(entry.getValue())) {
				faiths.put(entry.getValue(), new HashMap<Faith, Integer>());
				insertFaiths(faiths.get(entry.getValue()), cont_faith);
			} else {
				insertFaiths(faiths.get(entry.getValue()), cont_faith);
			}
		}
		
		for(Entry<String, HashMap<Faith, Integer>> entry : faiths.entrySet()) {
			System.out.println("FOR ALLIANCE: " + entry.getKey());
			for(Entry<Faith, Integer> entry2 : entry.getValue().entrySet()) {
				System.out.println("\t" + entry2.getKey() + ": " + entry2.getValue());
			}
		}
	}

	private static void insertFaiths(HashMap<Faith, Integer> hashMap, Set<Faith> cont_faith) {
		for(Faith f : cont_faith) {
			if(hashMap.containsKey(f)) {
				hashMap.put(f, hashMap.get(f) + 1);
			} else {
				hashMap.put(f, 1);
			}
		}
	}

	public static void main(String[] args) {
		printAndLoadFaiths();
	}

	private static void printMilitariesPerContinent() {
		List<Entry<Integer, Set<Pair<String, Integer>>>> list = new LinkedList<>(military_data.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Set<Pair<String, Integer>>>>() {
			@Override
			public int compare(Entry<Integer, Set<Pair<String, Integer>>> o1,
					Entry<Integer, Set<Pair<String, Integer>>> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		for (Entry<Integer, Set<Pair<String, Integer>>> x : list) {
			System.out.print("Continent " + x.getKey() + " militaries:");
			boolean comma = false;
			List<Pair<String, Integer>> pa = new LinkedList<>(x.getValue());
			Collections.sort(pa, new Comparator<Pair<String, Integer>>() {
				@Override
				public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
					return o2.p2.compareTo(o1.p2);
				}
			});
			for (Pair<String, Integer> faith : pa) {
				if (comma)
					System.out.print(", " + faith);
				else {
					System.out.print(" " + faith);
					comma = true;
				}
			}

			System.out.println(".");
		}
	}

}
