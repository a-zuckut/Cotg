package cotg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import cotg.data.Constants;
import cotg.wrappers.Alliance;
import cotg.wrappers.City;
import cotg.wrappers.Player;
import cotg.wrappers.helper.Pair;

public class AttackCreator {
	// Static methods because will be utilized from main method

	public static final int minimum_attacks_per_real_target = 6;
	public static final int minimum_attacks_per_target = 7;

	private static final Random random = new Random();

	/**
	 * 
	 * @param attackers
	 * @param continent
	 * @param targetPlayers
	 * @return a map that contains <Player, <Reals, Fakes>>
	 */
	public static Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> getWaterTargetsByNameAndContinent(
			Map<String, Pair<Integer, Integer>> attackers, int continent, String... targetPlayers) {
		int reals_max = 0;
		int fakes_max = 0;
		for (String pid : attackers.keySet()) {
			Pair<Integer, Integer> p = attackers.get(pid);
			reals_max += p.pairOne();
			fakes_max += p.pairTwo();
		}

		// Note that reals 0-pid1.p1 = reals for pid1 etc (same for fakes)

		ArrayList<City> targets = new ArrayList<>();
		for (String player : targetPlayers) {
			for (Alliance a : Constants.curr_alliances) {
				if (a.players.contains(new Player(player))) {
					for (Player p : a.players) {
						if (p.name.equals(player)) {
							for (City c : p.cities) {
								if (c.isCastle && c.isWater && (c.continent == -1 || c.continent == continent)
										&& c.score >= 6000) {
									targets.add(c);
								}
							}
						}
					}
					break;
				}
			}
		}
		
		// Acquired data

		int reals = 0;
		int max_attacks = (reals_max + fakes_max) * 6;
		while (max_attacks / minimum_attacks_per_target < targets.size()) {
			int x = random.nextInt(targets.size());
			System.out.println("REMOVED " + targets.remove(x));
		}

		reals = reals_max / minimum_attacks_per_real_target;

		int[] real_index = new int[reals];
		for (int i = 0; i < reals; i++)
			real_index[i] = random.nextInt(targets.size());
		removeDup(real_index, targets.size());

		ArrayList<City> r = new ArrayList<City>();

		Arrays.sort(real_index);
		reverse(real_index);

		for (int index : real_index)
			r.add(targets.remove(index));

		System.out.println("REALS");
		printArray(r);

		System.out.println("FAKES");
		printArray(targets);

		System.out.println("\n");

		String[] r2 = new String[reals_max];
		String[] f2 = new String[fakes_max];

		for (int i = 0; i < r2.length; i++)
			r2[i] = "r" + i;
		for (int i = 0; i < f2.length; i++)
			f2[i] = "f" + i;

		// Map<City, ArrayList<String>> generated_attack =
		// generateAttack(r2,f2,r,targets);
		ArrayList<Pair<City, ArrayList<String>>> attacks = generateAttack(r2, f2, r, targets);

		print(attacks);

		// String = ATTACKER PLAYER STRING
		// Pair = ArrayList of reals/fakes = attacks from a certain city
		Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> ret1 = new HashMap<>();

		int curr_real_index = 0;
		int curr_fake_index = 0;
		for (String pid : attackers.keySet()) {
			Pair<Integer, Integer> p = attackers.get(pid);

			ArrayList<ArrayList<Pair<String, City>>> res = new ArrayList<>();
			ArrayList<ArrayList<Pair<String, City>>> fas = new ArrayList<>();
			for (int i = curr_real_index; i < curr_real_index + p.p1; i++) {
				ArrayList<Pair<String, City>> re = new ArrayList<>();
				String temp = r2[i];
				for(Pair<City, ArrayList<String>> p1 : attacks) {
					if(p1.p2.contains(temp)) {
						re.add(new Pair<String, City>("REAL", p1.p1));
					}
					if(p1.p2.contains("f" + temp)) {
						re.add(new Pair<String, City>("FAKE", p1.p1));
					}
				}
				res.add(re);
			}
			curr_real_index += p.p1;

			for (int i = curr_fake_index; i < curr_fake_index + p.p2; i++) {
				ArrayList<Pair<String, City>> fa = new ArrayList<>();
				String temp = f2[i];
				for(Pair<City, ArrayList<String>> p1 : attacks) {
					if(p1.p2.contains(temp)) {
						fa.add(new Pair<String, City>("FAKE", p1.p1));
					}
				}
				fas.add(fa);
			}
			curr_fake_index += p.p2;
			
			Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>> p3 = 
					new Pair<ArrayList<ArrayList<Pair<String,City>>>, ArrayList<ArrayList<Pair<String,City>>>>(res, fas);
			ret1.put(pid, p3);
		}

		return ret1;
	}

	private static void print(ArrayList<Pair<City, ArrayList<String>>> attacks) {
		for (Pair<City, ArrayList<String>> p : attacks) {
			System.out.println(p.p1 + " " + p.p2.toString());
		}
	}

	private static ArrayList<Pair<City, ArrayList<String>>> generateAttack(String[] reals, String[] pure_fakes,
			ArrayList<City> real_attacks, ArrayList<City> fake_attacks) {
		ArrayList<Pair<City, ArrayList<String>>> attacks = new ArrayList<>();
		for (City c : real_attacks)
			attacks.add(new Pair<City, ArrayList<String>>(c, new ArrayList<>()));
		for (City c : fake_attacks)
			attacks.add(new Pair<City, ArrayList<String>>(c, new ArrayList<>()));

		// first add min reals to each reals array
		// add fakes per castle until you get min

		for (String s : reals) {
			addToReal(real_attacks.size(), attacks, s);
			boolean rand = random.nextBoolean();
			int total = rand ? 5 : 6;
			for (int i = 0; i < total; i++) {
				addToFake(real_attacks.size(), attacks, "f" + s);
			}
		}

		// next add fakes - fun!
		for (String s : pure_fakes) {
			boolean rand = random.nextBoolean();
			int total = rand ? 6 : 7;
			for (int i = 0; i < total; i++) {
				addFakes(attacks, s);
			}
		}

		return attacks;
	}

	private static void addFakes(ArrayList<Pair<City, ArrayList<String>>> attacks, String s) {
		int[] num_of_attacks = new int[attacks.size()];
		for (int i = 0; i < attacks.size(); i++) {
			num_of_attacks[i] = attacks.get(i).p2.size();
		}

		// select lowest
		int min_index = random.nextInt(num_of_attacks.length);
		for (int i = 0; i < num_of_attacks.length; i++) {
			if (attacks.get(min_index).p2.size() > attacks.get(i).p2.size())
				min_index = i;
		}

		attacks.get(min_index).p2.add(s);
	}

	private static void addToFake(int size, ArrayList<Pair<City, ArrayList<String>>> attacks, String s) {
		int[] num_of_attacks = new int[attacks.size() - size];
		for (int i = 0; i < size; i++) {
			num_of_attacks[i] = attacks.get(i + size).p2.size();
		}

		// select lowest
		int min_index = random.nextInt(num_of_attacks.length);
		for (int i = size; i < num_of_attacks.length + size; i++) {
			if (attacks.get(min_index).p2.size() > attacks.get(i).p2.size() && no_dup(s, attacks.get(i).p2)
					&& ((s.length() > 2) ? no_dup(s.substring(1, s.length()), attacks.get(i).p2) : true))
				min_index = i;
		}

		attacks.get(min_index).p2.add(s);
	}

	private static boolean no_dup(String substring, ArrayList<String> p2) {
		for (String x : p2)
			if (x.equals(substring))
				return false;
		return true;
	}

	private static void addToReal(int size, ArrayList<Pair<City, ArrayList<String>>> attacks, String attack) {
		int[] num_of_attacks = new int[size];
		for (int i = 0; i < size; i++) {
			num_of_attacks[i] = attacks.get(i).p2.size();
		}

		// select lowest
		int min_index = random.nextInt(num_of_attacks.length);
		for (int i = 0; i < num_of_attacks.length; i++) {
			if (attacks.get(min_index).p2.size() > attacks.get(i).p2.size())
				min_index = i;
		}

		attacks.get(min_index).p2.add(attack);
	}

	public static void reverse(int[] array) {
		if (array == null) {
			return;
		}
		int i = 0;
		int j = array.length - 1;
		int tmp;
		while (j > i) {
			tmp = array[j];
			array[j] = array[i];
			array[i] = tmp;
			j--;
			i++;
		}
	}

	private static void removeDup(int[] real_index, int max) {
		Set<Integer> already = new HashSet<>();
		for (int i = 0; i < real_index.length; i++) {
			if (!already.add(real_index[i])) {
				real_index[i] = (real_index[i] - 1 >= 0) ? real_index[i] - 1 : max - 1;
				i--;
			}
		}
	}

	private static void printArray(ArrayList<City> targets) {
		for (City c : targets)
			System.out.println(c);
	}

	public static void main(String[] args) {
		Map<String, Pair<Integer, Integer>> attackers = new HashMap<>();
		attackers.put("888", new Pair<Integer, Integer>(24, 12));
		attackers.put("Byz", new Pair<Integer, Integer>(0,12));
		System.out.println(getWaterTargetsByNameAndContinent(attackers, 23, "azuckut"));
	}

}
