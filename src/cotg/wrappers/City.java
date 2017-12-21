package cotg.wrappers;

import java.io.Serializable;

public class City implements Serializable {
	private static final long serialVersionUID = 1L;

	public int continent;
	
	public int score;
	public boolean isCastle;
	public boolean isWater;
	public boolean isTemple;
	
	public int x_coord;
	public int y_coord;
	
	private String player;
	private String alliance;
	private String city_name;
	
	public City(int con, int sc, boolean castle, boolean water, boolean temple, int x, int y, String name) {
		continent = con;
		score = sc;
		isCastle = castle;
		isWater = water;
		isTemple = temple;
		x_coord = x;
		y_coord = y;
	}
	
	public City(String csvLine) {
		// ex: 1511549438919,888,Tiny Alliance,8814,42,0,1,0,262,456,32,29884678
		String[] values = csvLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		
		player = values[1];
		alliance = values[2];
		
		score = Integer.valueOf(values[3]);
		continent = Integer.valueOf(values[4]);
		
		isCastle = values[5].trim().equals("1");
		isWater = values[6].trim().equals("1");
		isTemple = values[7].trim().equals("1");
		
		x_coord = Integer.valueOf(values[8]);
		y_coord = Integer.valueOf(values[9]);
		
		city_name = values[10];
		
	}
	
	public String getAlliance() {
		return alliance;
	}
	
	public String getPlayer() {
		return player;
	}
	
	@Override
	public String toString() {
		return String.format("%s at %d(%d, %d) with score %d", city_name != null ? city_name : "City", continent, x_coord, y_coord, score) + " is a " + (isWater ? "water " : "land ") + (!isTemple ? (isCastle ? "castle " : "city ") : "temple ");
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof City)) {
			return false;
		}
		City com = (City) o;
		return this.x_coord == com.x_coord && this.y_coord == com.y_coord && this.continent == com.continent;
	}
	
	@Override
	public int hashCode() {
		return x_coord * 600 + y_coord;
	}

	public String simpleString() {
		return city_name + " " + String.format("(%d, %d)", x_coord, y_coord);
	}
	
	public String coords() {
		return String.format("(%d, %d)", x_coord, y_coord);
	}
}
