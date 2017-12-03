package cotg.wrappers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Alliance implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String name;
	public int score;
	public Set<Player> players;
	
	public Map<Integer, Integer> military_per_continent = new HashMap<>();
	
	public Alliance(String name) {
		this.name = name;
		players = new HashSet<>();
	}
	
	public int generateScore() {
		int sum = 0;
		for(Player p:players) sum += p.generateScore();
		score = sum;
		return sum;
	}
	
	@Override
	public String toString() {
		generateScore();
		return String.format("%-50s", "\"" + name + "\"" + " with score: " + generateScore()) + "Players: " + players; 
	}
}
