package cotg.wrappers;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Player implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String name;
	public String alliance;
	public Set<City> cities;
	
	public int score;
	
	public Player(String name, String alliance) {
		this.name = name;
		this.alliance = alliance;
		cities = new HashSet<>();
	}
	
	public Player(String name) {
		this.name = name;
		generateScore();
	}
	
	public int generateScore() {
		int sum = 0; if(cities==null) return 0;
		for(City c : cities) sum+=c.score;
		score = sum;
		return sum;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Player)) return false;
		Player com = (Player) o;
		return this.name.equals(com.name);
	}
	
	@Override
	public int hashCode() {
		return name.trim().hashCode();
	}
	
	@Override
	public String toString() {
		return name + " has " + cities.size() + " cities " + "score = " + generateScore();
	}

	public boolean onContinent(int continent) {
		if(cities == null) return false;
		for(City c : cities) {
			if(c.continent == continent) return true;
		}
		return false;
	}
	
}
