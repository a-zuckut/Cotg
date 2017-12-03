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

	public Militarys() {
		parseMilitariesData();
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
	
	public static void printControlledContinents() {
		List<Entry<Integer, Set<Pair<String, Integer>>>> list = new LinkedList<>(military_data.entrySet());
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
			
			for(Pair<String, Integer> deciding : pa) {
				if(deciding.p2.doubleValue()/((double)sum) > .40) {
					if (comma)
						System.out.print(", " + deciding.p1);
					else {
						System.out.print(" " + deciding.p1);
						comma = true;
					}
				}
			}
			System.out.println(".");
		}
	}

	public static void main(String[] args) {
		new Militarys();
		printControlledContinents();
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
