package cotg.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import cotg.Parser;
import cotg.wrappers.Alliance;
import cotg.wrappers.Player;

public class Constants {

	public static final File alliance_file = new File("src/cotg/data/all_data.txt");
	public static Alliance[] curr_alliances;
	
	static {
		try {
			curr_alliances = getAllianceFromFile(alliance_file);
			if(curr_alliances == null) {
				System.out.println("Parsing");
				curr_alliances = Parser.parseIntoAlliances("src/cotg/data/playerData.csv", 0);
				Parser.storeData(curr_alliances);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Alliance[] getAllianceFromFile(File file) throws Exception {
		if(!file.exists()) return null;
		Alliance[] e;
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		e = (Alliance[]) ois.readObject();
		ois.close();
		fis.close();
		System.out.println("Map<Date, Double> obtained from file");
		return e;
	}
	
	public static void printAlliances() {
		System.out.println("Printing alliances");
		for(Alliance alliance : curr_alliances) {
			System.out.println(alliance.name);
		}
	}
	
	public static void printAlliance(String s) {
		Alliance alliance = findAlliance(s);
		
		System.out.println("Alliance: " + alliance.name + " with score of " + alliance.score);
		for(Player player : alliance.players) {
			System.out.println("\t" + player);
		}
	}
	
	public static Alliance findAlliance(String s) {
		for(Alliance alliance : curr_alliances) {
			if(alliance.name.equals(s)) return alliance;
		}
		return null;
	}
	
}
