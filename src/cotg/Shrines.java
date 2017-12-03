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

import cotg.wrappers.Faith;

public class Shrines {

	public Shrines() {
		parseShrinesData();
	}

	public static Map<Integer, Set<Faith>> shrines_on_open_continents;
	public static final File FILE = new File("src/cotg/data/shrineContinentData.csv");

	public static void parseShrinesData() {
		ArrayList<String> ret = Parser.parseFiles(FILE);
		shrines_on_open_continents = new HashMap<Integer, Set<Faith>>();
		String[] headers = ret.get(0).split(",");
		for (String s : ret) {
			if (s.split(",")[0].equals(headers[0]))
				continue;
			String[] lineData = s.split(",");
			if (!shrines_on_open_continents.containsKey(Integer.valueOf(lineData[1]))) {
				shrines_on_open_continents.put(Integer.valueOf(lineData[1]), new HashSet<Faith>());
				for (int i = 4; i < lineData.length - 1; i++) {
					if (!lineData[i].equals("-")) {
						shrines_on_open_continents.get(Integer.valueOf(lineData[1])).add(Faith.valueOf(headers[i]));
					}
				}
			} else {
				for (int i = 4; i < lineData.length - 1; i++) {
					if (!lineData[i].equals("-")) {
						shrines_on_open_continents.get(Integer.valueOf(lineData[1])).add(Faith.valueOf(headers[i]));
					}
				}
			}
		}
	}

	static {
		new Shrines();
	}

	public static void printShrinesOnOpenContinents() {
		List<Entry<Integer, Set<Faith>>> list = new LinkedList<Map.Entry<Integer, Set<Faith>>>(
				shrines_on_open_continents.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Set<Faith>>>() {
			@Override
			public int compare(Entry<Integer, Set<Faith>> o1, Entry<Integer, Set<Faith>> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		for (Entry<Integer, Set<Faith>> x : list) {
			System.out.print("Continent " + x.getKey() + " has shrines:");
			boolean comma = false;
			for (Faith faith : x.getValue()) {
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
