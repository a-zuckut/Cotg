package gui.helper;

import java.util.ArrayList;

public class Map {

	private ArrayList<Integer> continents;
	private ArrayList<Integer> score;
	
	public Map() {
		continents = new ArrayList<>();
		score = new ArrayList<>();
	}
	
	public void addCity(int cont, int score) {
		if(continents.contains(cont)) {
			int index = continents.indexOf(cont);
			int curr = this.score.get(continents.indexOf(cont));
			
			this.score.set(index, curr + score);
		} else {
			continents.add(cont);
			this.score.add(score);
		}
	}
	
	public Integer[] getScore() {
		return score.toArray(new Integer[score.size()]);
	}
	
	public Integer[] getContinents() {
		return continents.toArray(new Integer[continents.size()]);
	}
	
}
