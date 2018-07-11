package cotg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import java.util.Random;
import java.util.Set;

import cotg.data.Constants;
import cotg.helpers.ATTACK_TYPES;
import cotg.wrappers.Alliance;
import cotg.wrappers.AttackTypes;
import cotg.wrappers.City;
import cotg.wrappers.Player;
import cotg.wrappers.helper.Pair;

public class AttackCreator {
	// Static methods because will be utilized from main method

	public static final int attacks_per_castle = 6;

	public static final boolean try_senator_siege = true;
	// only used if(try_senator_siege)
	public static final int assault_siege_per_castle = 2;

	// minimum_attacks_per_real_target + 1 <= minimum_attacks_per_target
	public static final int minimum_attacks_per_real_target = 10;
	public static final int minimum_attacks_per_target = minimum_attacks_per_real_target + 1;

	private static final Random GENERATOR = new Random();
	public static Random random;
	public static long seed;

	public static String EXCLUDED_TARGETS = "451:554";

	public static ATTACK_TYPES defaultScout = ATTACK_TYPES.FAKE_SIEGE;

	static {
		seed = GENERATOR.nextLong();
		random = new Random(seed);
	}

	public static Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> getWaterTargetsByAllianceAndContinent(
			Map<String, Pair<Pair<Integer, Integer>, Integer>> attackers, int continent, int x, int y, int rad,
			String alliance) {
		ArrayList<String> playerList = new ArrayList<>();

		Alliance a = Constants.findAlliance(alliance);
		Set<Player> list = a.players;
		Player[] players = new Player[list.size()];
		list.toArray(players);

		for (Player p : players) {
			playerList.add(p.name);
		}

		String[] targets = new String[playerList.size()];
		playerList.toArray(targets);
		return getWaterTargetsByNameAndContinent(attackers, continent, x, y, rad, targets);
	}

	public static Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> getWaterTargetsByAllianceAndContinentV2(
			Map<String, AttackTypes> attackers, int continent, int x, int y, int rad, String alliance) {
		ArrayList<String> playerList = new ArrayList<>();

		Alliance a = Constants.findAlliance(alliance);
		Set<Player> list = a.players;
		Player[] players = new Player[list.size()];
		list.toArray(players);

		for (Player p : players) {
			playerList.add(p.name);
		}

		String[] targets = new String[playerList.size()];
		playerList.toArray(targets);
		return getWaterTargetsByNameAndContinentV2(attackers, continent, x, y, rad, targets);
	}

	public static void printArray(String[] players) {
		for (String xString : players) {
			System.out.print(xString + " ");
		}
	}

	public static Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> getWaterTargetsByNameAndContinent(
			Map<String, Pair<Pair<Integer, Integer>, Integer>> attackers, int continent, int x, int y, int rad,
			String... targetPlayers) {
		Pair<ArrayList<City>, ArrayList<City>> p = getRealsAndFakes(attackers, continent, x, y, rad, targetPlayers);
		return getTargets(attackers, p.p1, p.p2);
	}

	/**
	 * 
	 * @param attackers
	 *            The attackers each containing all attack types
	 * @param continent
	 *            Which continent to attack.
	 * @param x
	 *            If want to attack based on area.
	 * @param y
	 *            If want to attack based on area.
	 * @param rad
	 *            Radius if want to attack based on area
	 * @param targetPlayers
	 *            Players to attack.
	 * @return Returns a Map containing Players to list of attacks - First pair
	 *         is Reals, Second pair is pure fakes
	 */
	public static Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> getWaterTargetsByNameAndContinentV2(
			Map<String, AttackTypes> attackers, int continent, int x, int y, int rad, String... targetPlayers) {
		Map<String, Pair<Pair<Integer, Integer>, Integer>> attackers2 = new HashMap<>();
		for (String player : attackers.keySet()) {
			AttackTypes a = attackers.get(player);
			attackers2.put(player,
					new Pair<Pair<Integer, Integer>, Integer>(new Pair<>(a.getWS(), a.getAssault()), a.getFakes()));
		}

		Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> xMap = getWaterTargetsByNameAndContinent(
				attackers2, continent, x, y, rad, targetPlayers);
		distributeAssaults(xMap, attackers, attackers2);
		return xMap;
	}

	private static void distributeAssaults(
			Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> xMap,
			Map<String, AttackTypes> attackers, Map<String, Pair<Pair<Integer, Integer>, Integer>> attackers2) {
		Map<City, AttackTypes> attacksForCities = new HashMap<City, AttackTypes>();
		for (String attacker : xMap.keySet()) {
			AttackTypes attackerCastles = attackers.get(attacker);
			Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>> info = xMap
					.get(attacker);
			ArrayList<ArrayList<Pair<String, City>>> reals = info.p1;
			for (ArrayList<Pair<String, City>> T2 : reals) {
				Pair<String, City> real_one = T2.get(0);
				String x = real_one.p1;
				City r1 = real_one.p2;

				AttackTypes current = new AttackTypes();
				if (attacksForCities.containsKey(r1)) {
					current = attacksForCities.get(r1);
				} else {
					attacksForCities.put(r1, current);
				}

				if (real_one.p1.contains("WS")) {
					real_one.p1 = "REAL " + ATTACK_TYPES.WS_SIEGE.name();
					attackerCastles.ws--;
					current.ws++;
				} else if (x.contains("ASS")) {
					if (try_senator_siege) {
						if (current.hasSenator()) {
							if (current.siege < assault_siege_per_castle) {
								if (attackerCastles.useSorc()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.SORC_SIEGE.name();
									current.sorc++;
									current.siege++;
									continue;
								} else if (attackerCastles.useDruid()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.DRUID_SIEGE.name();
									current.druid++;
									current.siege++;
									continue;
								} else if (attackerCastles.useHorse()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.HORSE_SIEGE.name();
									current.horse++;
									current.siege++;
									continue;
								} else if (attackerCastles.useVanq()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.VANQ_SIEGE.name();
									current.vanq++;
									current.siege++;
									continue;
								} else {
									System.out.println("WTF SIEGE???? Error at: " + attacker + x + " " + r1);
								}
							} else { // adding assaults
								if (attackerCastles.useSorc()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.SORC_ASSAULT.name();
									current.sorc++;
									continue;
								} else if (attackerCastles.useDruid()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.DRUID_ASSAULT.name();
									current.druid++;
									continue;
								} else if (attackerCastles.useHorse()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.HORSE_ASSAULT.name();
									current.horse++;
									continue;
								} else if (attackerCastles.useVanq()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.VANQ_ASSAULT.name();
									current.vanq++;
									continue;
								} else {
									System.out.println("WTF ASSAULT1???? Error at: " + x + " " + r1);
								}
							}
						} else {
							// ADD SENATOR IF POSSIBLE
							if (attackerCastles.useSorc(true)) {
								real_one.p1 = "REAL " + ATTACK_TYPES.SORC_SEN_SIEGE.name();
								current.sorc_with_sen++;
								continue;
							} else if (attackerCastles.useDruid(true)) {
								real_one.p1 = "REAL " + ATTACK_TYPES.DRUID_SEN_SIEGE.name();
								current.druid_with_sen++;
								continue;
							} else if (attackerCastles.useHorse(true)) {
								real_one.p1 = "REAL " + ATTACK_TYPES.HORSE_SEN_SIEGE.name();
								current.horse_with_sen++;
								continue;
							} else if (attackerCastles.useVanq(true)) {
								real_one.p1 = "REAL " + ATTACK_TYPES.VANQ_SEN_SIEGE.name();
								current.vanq_with_sen++;
								continue;
							} else if (current.siege < assault_siege_per_castle) {
								if (attackerCastles.useSorc()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.SORC_SIEGE.name();
									current.sorc++;
									current.siege++;
									continue;
								} else if (attackerCastles.useDruid()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.DRUID_SIEGE.name();
									current.druid++;
									current.siege++;
									continue;
								} else if (attackerCastles.useHorse()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.HORSE_SIEGE.name();
									current.horse++;
									current.siege++;
									continue;
								} else if (attackerCastles.useVanq()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.VANQ_SIEGE.name();
									current.vanq++;
									current.siege++;
									continue;
								}
							} else {
								if (attackerCastles.useSorc()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.SORC_ASSAULT.name();
									current.sorc++;
									continue;
								} else if (attackerCastles.useDruid()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.DRUID_ASSAULT.name();
									current.druid++;
									continue;
								} else if (attackerCastles.useHorse()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.HORSE_ASSAULT.name();
									current.horse++;
									continue;
								} else if (attackerCastles.useVanq()) {
									real_one.p1 = "REAL " + ATTACK_TYPES.VANQ_ASSAULT.name();
									current.vanq++;
									continue;
								} else {
									System.out.println("WTF ASSAULT SEN???? Error at: " + attacker + x + " " + r1);
								}
							}
						}
					} else {
						// NO SENATOR SIEGE
						if (attackerCastles.useSorc()) {
							real_one.p1 = "REAL " + ATTACK_TYPES.SORC_ASSAULT.name();
							current.sorc++;
							continue;
						} else if (attackerCastles.useDruid()) {
							real_one.p1 = "REAL " + ATTACK_TYPES.DRUID_ASSAULT.name();
							current.druid++;
							continue;
						} else if (attackerCastles.useHorse()) {
							real_one.p1 = "REAL " + ATTACK_TYPES.HORSE_ASSAULT.name();
							current.horse++;
							continue;
						} else if (attackerCastles.useVanq()) {
							real_one.p1 = "REAL " + ATTACK_TYPES.VANQ_ASSAULT.name();
							current.vanq++;
							continue;
						} else {
							System.out.println("WTF ASSAULT222???? Error at: " + x + " " + r1);
						}
					}
				} else {
					System.out.println("WTF HMM???? Error at: " + x + " " + r1);
				}
			}

			// assert attackers are all 0 ...
		}
	}

	public static Pair<ArrayList<City>, ArrayList<City>> getRealsAndFakes(
			Map<String, Pair<Pair<Integer, Integer>, Integer>> attackers, int continent, int x_, int y_, int rad_,
			String... targetPlayers) {
		int reals_max = 0;
		int fakes_max = 0;
		for (String pid : attackers.keySet()) {
			Pair<Pair<Integer, Integer>, Integer> p = attackers.get(pid);
			reals_max += p.p1.p1 + p.p1.p2;
			fakes_max += p.p2;
		}

		// Note that reals 0-pid1.p1 = reals for pid1 etc (same for fakes) (pid
		// = player
		// id 1) - first attacker
		ArrayList<City> targets = getTargets(continent, x_, y_, rad_, targetPlayers);

		// Acquired data
		int reals = 0;
		int max_attacks = (reals_max + fakes_max) * 6;
		while (max_attacks / minimum_attacks_per_target < targets.size()) {
			int x = random.nextInt(targets.size());
			System.out.println("REMOVED " + targets.remove(x));
		}

		// // Kinda a brute force thing to make sure to only have 888 has reals
		ArrayList<Integer> realTargets = new ArrayList<Integer>();
		realTargets = findRealsNotOnPlayer(targets, targetPlayers, "whitehot");
		System.out.println(realTargets);

		reals = reals_max / minimum_attacks_per_real_target;

		int[] real_index = new int[reals];
		for (int i = 0; i < reals; i++)
			real_index[i] = realTargets.get(random.nextInt(realTargets.size()));
		removeDup(real_index, targets.size());

		ArrayList<City> r = new ArrayList<City>();

		Arrays.sort(real_index);
		reverse(real_index);

		for (int index : real_index)
			r.add(targets.remove(index));

		System.out.println("Number of reals: " + real_index.length);
		for (City real : r) {
			System.out.print(real.coords(City.COORDS_VERSION_PARANTHESIS) + ", ");
		}

		return new Pair<>(r, targets);
	}

	private static ArrayList<Integer> findRealsNotOnPlayer(ArrayList<City> targets, String[] targetPlayers,
			String findPlayer) {
		ArrayList<Integer> ret = new ArrayList<>();

		for (String temp : targetPlayers) {
			if (!temp.equals(findPlayer) || findPlayer.equals(""))
				continue;
			else {
				for (City c : Constants.findPlayer(temp).cities) {
					if (targets.contains(c)) {
						ret.add(targets.indexOf(c));
					}
				}
			}
		}

		return ret;
	}

	public static Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> getTargets(
			Map<String, Pair<Pair<Integer, Integer>, Integer>> attackers, ArrayList<City> reals,
			ArrayList<City> fakes) {
		int reals_max = 0;
		int fakes_max = 0;
		for (String pid : attackers.keySet()) {
			Pair<Pair<Integer, Integer>, Integer> p = attackers.get(pid);
			reals_max += p.p1.p1 + p.p1.p2;
			fakes_max += p.p2;
		}

		String[] r2 = new String[reals_max];
		String[] f2 = new String[fakes_max];

		for (int i = 0; i < r2.length; i++) {
			r2[i] = "r" + i;
		}
		for (int i = 0; i < f2.length; i++) {
			f2[i] = "f" + i;
		}

		ArrayList<Pair<City, ArrayList<String>>> attacks = generateAttack(r2, f2, reals, fakes);
		// printArray(attacks);
		return ChangeDataStructure(attackers, r2, f2, attacks);
	}

	private static Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> ChangeDataStructure(
			Map<String, Pair<Pair<Integer, Integer>, Integer>> attackers, String[] r2, String[] f2,
			ArrayList<Pair<City, ArrayList<String>>> attacks) {
		Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> ret1 = new HashMap<>();

		int curr_real_index = 0;
		int curr_fake_index = 0;
		for (String pid : attackers.keySet()) {
			Pair<Pair<Integer, Integer>, Integer> p = attackers.get(pid);

			ArrayList<ArrayList<Pair<String, City>>> res = new ArrayList<>();
			ArrayList<ArrayList<Pair<String, City>>> fas = new ArrayList<>();
			for (int i = curr_real_index; i < curr_real_index + p.p1.p1 + p.p1.p2; i++) {
				ArrayList<Pair<String, City>> re = new ArrayList<>();
				String temp = r2[i];
				for (Pair<City, ArrayList<String>> p1 : attacks) {
					if (p1.p2.contains(temp)) {
						re.add(new Pair<String, City>("REAL " + ((i < curr_real_index + p.p1.p1) ? "WS" : "ASS"),
								p1.p1));
					}
					if (p1.p2.contains("f" + temp)) {
						re.add(new Pair<String, City>("FAKE " + ((i < curr_real_index + p.p1.p1) ? "WS" : "ASS"),
								p1.p1));
					}
				}

				re.sort(new Comparator<Pair<String, City>>() {
					@Override
					public int compare(Pair<String, City> o1, Pair<String, City> o2) {
						return o2.p1.compareTo(o1.p1);
					}
				});

				res.add(re);
			}
			curr_real_index += p.p1.p1 + p.p1.p2;

			for (int i = curr_fake_index; i < curr_fake_index + p.p2; i++) {
				ArrayList<Pair<String, City>> fa = new ArrayList<>();
				String temp = f2[i];
				for (Pair<City, ArrayList<String>> p1 : attacks) {
					if (p1.p2.contains(temp)) {
						fa.add(new Pair<String, City>("FAKE", p1.p1));
					}
				}
				fas.add(fa);
			}
			curr_fake_index += p.p2;

			Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>> p3 = new Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>(
					res, fas);
			ret1.put(pid, p3);
		}
		return ret1;
	}

	@SuppressWarnings("unused")
	private static ArrayList<City> getTargets(int continent, String... targetPlayers) {
		return getTargets(continent, -1, -1, -1, targetPlayers);
	}

	private static ArrayList<City> getTargets(int continent, int x, int y, int radius, String... targetPlayers) {
		return getTargets(continent, x, y, radius, 4000, targetPlayers);
	}

	private static ArrayList<City> getTargets(int continent, int x, int y, int radius, int value,
			String... targetPlayers) {
		ArrayList<City> targets = new ArrayList<>();
		for (String player : targetPlayers) {
			for (Alliance a : Constants.curr_alliances) {
				if (a.players.contains(new Player(player))) {
					for (Player p : a.players) {
						if (p.name.equals(player)) {
							for (City c : p.cities) {
								if (c.isCastle && c.isWater && (c.continent == -1 || c.continent == continent 
//										|| (c.continent == 54 && c.score > 8000) /* TODO */
										&& c.score >= value)) {
									if ((x != -1 && y != -1)
											|| Math.abs(c.x_coord - x) > radius && Math.abs(c.y_coord - y) > radius)
										targets.add(c);
								}
							}
						}
					}
					break;
				}
			}
		}
		return targets;
	}

	private static ArrayList<City> getTargets(int continent, int x, int y, int radius, Alliance targetPlayers) {
		ArrayList<City> targets = new ArrayList<>();
		for (Player p : targetPlayers.players) {
			for (City c : p.cities) {
				if (c.isCastle && c.isWater && (c.continent == 54 || c.continent == continent) && c.score >= 5000) {
					if ((x != -1 && y != -1) || Math.abs(c.x_coord - x) > radius && Math.abs(c.y_coord - y) > radius)
						if (EXCLUDED_TARGETS.equals("")
								|| (!EXCLUDED_TARGETS.contains(c.coords(City.COORDS_VERSION_SIMPLE))
										|| !EXCLUDED_TARGETS.contains(c.player))) {
							targets.add(c);
						}
				}
			}

		}
		return targets;
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
			int total = rand ? attacks_per_castle : attacks_per_castle;
			for (int i = 0; i < total; i++) {
				addToFake(attacks.size(), attacks, "f" + s);
			}
		}

		// next add fakes - fun!
		for (String s : pure_fakes) {
			boolean rand = random.nextBoolean();
			int total = rand ? attacks_per_castle : attacks_per_castle;
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
		for (int i = 0; i < attacks.size() - size; i++) {
			num_of_attacks[i] = attacks.get(i + size).p2.size();
		}

		// select lowest
		int min_index = random.nextInt(size);
		for (int i = 0; i < size; i++) {
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
			int temp = 0;
			ArrayList<String> xArrayList = attacks.get(i).p2;
			for (String s : xArrayList) {
				if (!s.contains("f"))
					temp++;
			}
			num_of_attacks[i] = temp;
		}

		// select lowest
		int min_index = random.nextInt(size);
		for (int i = 0; i < num_of_attacks.length; i++) {
			if (num_of_attacks[i] < num_of_attacks[min_index] && num_of_attacks[i] < minimum_attacks_per_real_target)
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

	private static void printArray(ArrayList<Pair<City, ArrayList<String>>> attacks) {
		for (Pair<City, ArrayList<String>> c : attacks) {
			System.out.println(c.toString());
		}
		System.out.println(attacks.size());
	}

	public static int printDetailedTargets(
			Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> t) {
		int count = 0;
		for (String attacker : t.keySet()) {
			System.out.println(attacker);

			Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>> curr = t
					.get(attacker);
			ArrayList<ArrayList<Pair<String, City>>> reals = curr.p1;
			ArrayList<ArrayList<Pair<String, City>>> fakes = curr.p2;

			System.out.println("REALS");

			for (ArrayList<Pair<String, City>> r : reals) {
				String line = "\t";
				for (Pair<String, City> c : r) {
					count++;
					line += c.p1 + " " + c.p2.simpleString() + ", ";
				}

				System.out.println(line);
			}

			System.out.println("\nFAKES");

			for (ArrayList<Pair<String, City>> r : fakes) {
				String line = "\t";
				for (Pair<String, City> c : r) {
					count++;
					line += c.p2.simpleString() + ", ";
				}

				System.out.println(line);
			}
		}
		return count;
	}

	public static int printSimpleTargets(
			Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> t) {
		int count = 0;
		for (String attacker : t.keySet()) {
			System.out.println(attacker);

			Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>> curr = t
					.get(attacker);
			ArrayList<ArrayList<Pair<String, City>>> reals = curr.p1;
			ArrayList<ArrayList<Pair<String, City>>> fakes = curr.p2;

			System.out.println("REALS");

			for (ArrayList<Pair<String, City>> r : reals) {
				String line = "\t";
				String r9 = "";
				String f9 = "";
				for (Pair<String, City> c : r) {
					count++;
					if (c.p1.contains("REAL")) {
						String x = c.p1.replace("REAL", "").trim();
						r9 += x + " " + c.p2.simpleString() + ", ";
					} else {
						String x = c.p1.replace("FAKE", "").trim();
						f9 += x + " " + c.p2.simpleString() + ", ";
					}
				}

				line += "REAL: " + r9 + "FAKE: " + f9;

				convertToCFunky(line);
				// System.out.println(line);
			}

			if (fakes.size() > 0) {
				System.out.println("\nFAKES");

				for (ArrayList<Pair<String, City>> r : fakes) {
					String line = "\tFAKE: ";
					for (Pair<String, City> c : r) {
						count++;
						line += c.p2.simpleString() + ", ";
					}

					convertToCFunky(line);
				}
			}
		}
		return count;
	}

	public static void printAttacksByTargetsHidden(
			Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> waterTargetsByNameAndContinent) {
		Map<City, ArrayList<String>> temp = new HashMap<>();
		for (Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>> x : waterTargetsByNameAndContinent
				.values()) {
			for (ArrayList<Pair<String, City>> arr : x.p1) {
				for (Pair<String, City> p : arr) {
					if (!temp.containsKey(p.p2)) {
						ArrayList<String> y = new ArrayList<>();
						y.add(p.p1);
						temp.put(p.p2, y);
					} else {
						temp.get(p.p2).add(p.p1);
					}
				}
			}

			for (ArrayList<Pair<String, City>> arr : x.p2) {
				for (Pair<String, City> p : arr) {
					if (!temp.containsKey(p.p2)) {
						ArrayList<String> y = new ArrayList<>();
						y.add(p.p1);
						temp.put(p.p2, y);
					} else {
						temp.get(p.p2).add(p.p1);
					}
				}
			}
		}

		printArray(temp);
	}

	@SuppressWarnings("unused")
	private static void printCoords(Map<City, ArrayList<String>> temp) {
		int count = 1;
		for (City c : temp.keySet()) {
			System.out.println("Target " + (count++) + ": " + c);
			for (String coord : temp.get(c)) {
				System.out.println("\t" + coord);
			}
		}
	}

	public static void printAttacksByTargets(
			Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> waterTargetsByNameAndContinent) {
		Map<City, ArrayList<String>> temp = new HashMap<>();
		for (Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>> x : waterTargetsByNameAndContinent
				.values()) {
			for (ArrayList<Pair<String, City>> arr : x.p1) {
				for (Pair<String, City> p : arr) {
					if (!temp.containsKey(p.p2)) {
						ArrayList<String> y = new ArrayList<>();
						y.add(p.p1);
						temp.put(p.p2, y);
					} else {
						temp.get(p.p2).add(p.p1);
					}
				}
			}

			for (ArrayList<Pair<String, City>> arr : x.p2) {
				for (Pair<String, City> p : arr) {
					if (!temp.containsKey(p.p2)) {
						ArrayList<String> y = new ArrayList<>();
						y.add(p.p1);
						temp.put(p.p2, y);
					} else {
						temp.get(p.p2).add(p.p1);
					}
				}
			}
		}

		printArray(temp);
	}

	public static void printArray(Map<City, ArrayList<String>> temp) {
		for (City c : temp.keySet()) {
			System.out.println(c + " " + temp.get(c).size() + "" + temp.get(c));
		}
	}

	public static void useSeed(long seed) {
		AttackCreator.random = new Random(seed);
		AttackCreator.seed = seed;
	}

	public static ByteArrayOutputStream gatherPrintlnOutput() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		System.setOut(ps);
		return baos;
	}

	public static int extra_ws = 0;
	public static int extra_ass = 0;

	public static Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> simpleAttack(String alliance,
			int continent, Map<String, AttackTypes> attacks, int warships, int assaults) {
		return simpleAttack(alliance, continent, attacks, warships, assaults, false);
	}

	public static Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> simpleAttack(String alliance,
			int continent, Map<String, AttackTypes> attacks, int warships, int assaults, boolean senator_assault) {
		return simpleAttack(
				Arrays.stream(Constants.playerAlliance(alliance, EXCLUDED_TARGETS))
						.filter(s -> (s != null && s.length() > 0)).toArray(String[]::new),
				continent, attacks, warships, assaults, 4000, senator_assault);
	}

	public static Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> simpleAttack(String alliance,
			int continent, Map<String, AttackTypes> attacks, int warships, int assaults, int value,
			boolean senator_assault) {
		return simpleAttack(
				Arrays.stream(Constants.playerAlliance(alliance, EXCLUDED_TARGETS))
						.filter(s -> (s != null && s.length() > 0)).toArray(String[]::new),
				continent, attacks, warships, assaults, value, senator_assault);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> simpleAttack(String[] players,
			int continent, Map<String, AttackTypes> attacks, int warships, int assaults, int value,
			boolean senator_assault) {
		ArrayList<City> cities = getTargets(continent, -1, -1, -1, value, players);
		System.out.println(cities.size());

		Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> target = new HashMap<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>>();

		int ws = 0;
		int ass = 0;
		for (AttackTypes at : attacks.values()) {
			ws += at.getWS();
			ass += at.getAssault();
		}

		int num_of_reals = 0;
		if (assaults == 0)
			num_of_reals = Math.min(ws / warships, cities.size());
		else if (warships == 0)
			num_of_reals = Math.min(ass / assaults, cities.size());
		else
			num_of_reals = Math.min(ass / assaults, Math.min(ws / warships, cities.size()));

		System.out.println("Number of reals: " + num_of_reals);

		Map<City, AttackTypes> reals = new HashMap<City, AttackTypes>();
		Map<City, AttackTypes> fakes = new HashMap<City, AttackTypes>();

		// TODO: THIS IS AN EXTREME ADJUSTMENT...
		while (num_of_reals > 0) {
			int index = random.nextInt(cities.size());
			for (int i = 0; i < cities.size(); i++) {
				index = i;
				break;
			}
			
			reals.put(cities.remove(index), new AttackTypes());
			num_of_reals--;
		}
		// TODO: THIS IS AN EXTREME ADJUSTMENT...

		while (!cities.isEmpty()) {
			if(fakes.size() > 40) break;
			fakes.put(cities.remove(0), new AttackTypes());
		}

		assert (cities.isEmpty());
		// initialize final data...
		for (String p : attacks.keySet()) {
			target.put(p, new ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>());
		}

		// now need to add attacks:
		// go through each player take their number of WS and add it to all
		// targets...
		// go through each player take their number of assault troops and
		// distribute...
		// for each castle add fakes to go along with it... randomly...
		for (Entry<String, AttackTypes> x : attacks.entrySet()) {

			// Dealing with WSs
			if (x.getValue().getWS() > 0) {
				// Add reals and fakes
				// first dealing with WS/SEN
				for (int i = 0; i < x.getValue().ws_with_sen; i++) {
					if(!senator_assault)
						wsAddSenSiegeOrSiege(reals, fakes, warships, x.getKey(), target);
					else
						wsAddSiege(reals, fakes, warships + 1, x.getKey(), target);
				}

				x.getValue().ws_with_sen = 0;

				// next dealing with pure WS
				for (int i = 0; i < x.getValue().ws + x.getValue().ws_with_sen; i++) {
					wsAddSiege(reals, fakes, (senator_assault?warships+1:warships), x.getKey(), target);
				}
				x.getValue().ws = 0;

			}
			if (x.getValue().getAssault() > 0) {

				// Add reals and fakes
				// first dealing with VANQ
				if (x.getValue().vanq_with_sen > 0 && senator_assault) {
					for (int i = 0; i < x.getValue().vanq_with_sen; i++) {
						vanqAddSenatorOrAssaultReal(reals, fakes, assaults, x.getKey(), target);
					}
					x.getValue().vanq_with_sen = 0;
				}
				for (int i = 0; i < x.getValue().vanq + x.getValue().vanq_with_sen; i++) {
					vanqAddAssaultReal(reals, fakes, assaults, x.getKey(), target, senator_assault);
				}
				x.getValue().vanq = 0;
				x.getValue().vanq_with_sen = 0;

				if (x.getValue().sorc_with_sen > 0 && senator_assault) {
					for (int i = 0; i < x.getValue().sorc_with_sen; i++) {
						sorcAddSenatorOrAssaultReal(reals, fakes, assaults, x.getKey(), target);
					}
					x.getValue().sorc_with_sen = 0;
				}
				for (int i = 0; i < x.getValue().sorc + x.getValue().sorc_with_sen; i++) {
					sorcAddAssaultReal(reals, fakes, assaults, x.getKey(), target, senator_assault);
				}
				x.getValue().sorc = 0;
				x.getValue().sorc_with_sen = 0;

				if (x.getValue().horse_with_sen > 0 && senator_assault) {
					for (int i = 0; i < x.getValue().horse_with_sen; i++) {
						horseAddSenatorOrAssaultReal(reals, fakes, assaults, x.getKey(), target);
					}
					x.getValue().horse_with_sen = 0;
				}
				for (int i = 0; i < x.getValue().horse + x.getValue().horse_with_sen; i++) {
					horseAddAssaultReal(reals, fakes, assaults, x.getKey(), target, senator_assault);
				}
				x.getValue().horse_with_sen = 0;
				x.getValue().horse = 0;

				if (x.getValue().druid_with_sen > 0 && senator_assault) {
					for (int i = 0; i < x.getValue().druid_with_sen; i++) {
						druidAddSenatorOrAssaultReal(reals, fakes, assaults, x.getKey(), target);
					}
					x.getValue().druid_with_sen = 0;
				}
				for (int i = 0; i < x.getValue().druid + x.getValue().druid_with_sen; i++) {
					druidAddAssaultReal(reals, fakes, assaults, x.getKey(), target, senator_assault);
				}
				x.getValue().druid_with_sen = 0;
				x.getValue().druid = 0;

			}

			if (x.getValue().getFakes() > 0) {
				for (int i = 0; i < x.getValue().fakes; i++) {
					addFakes(reals, fakes, x.getKey(), target);
				}
			}
		}

		System.out.println(reals);
		System.out.println(fakes);

		System.out.println("Extra ws: " + extra_ws);
		System.out.println("Extra ass: " + extra_ass);

		System.out.println("Total attacks: " + sumMaps(reals, fakes));

		return target;

	}

	private static void addFakes(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes, String string,
			Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> target) {
		Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(null, new ArrayList<City>());
		addFakesForFakes(reals, fakes, string, pair);
		target.get(string).add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.FAKE, pair));
	}

	private static void vanqAddSenatorOrAssaultReal(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes,
			int assaults, String string,
			Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> target) {

		// first go through trying to set sen siege...
		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (!e.getValue().hasSenator()) {
				e.getValue().vanq_with_sen++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.VANQ_SEN_SIEGE, pair));
				return;
			}
		}

		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (e.getValue().getAssault() < assaults) {
				e.getValue().vanq++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.VANQ_ASSAULT, pair));
				return;
			}
		}

		// If only require fakes left...
		Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(null, new ArrayList<City>());
		addFakesForFakes(reals, fakes, string, pair);
		target.get(string).add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.VANQ_ASSAULT, pair));
		extra_ass++;
	}

	private static void sorcAddSenatorOrAssaultReal(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes,
			int assaults, String string,
			Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> target) {

		// first go through trying to set sen siege...
		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (!e.getValue().hasSenator()) {
				e.getValue().sorc_with_sen++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.SORC_SEN_SIEGE, pair));
				return;
			}
		}

		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (e.getValue().getAssault() < assaults) {
				e.getValue().sorc++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.SORC_ASSAULT, pair));
				return;
			}
		}

		// If only require fakes left...
		Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(null, new ArrayList<City>());
		addFakesForFakes(reals, fakes, string, pair);
		target.get(string).add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.SORC_ASSAULT, pair));
		extra_ass++;
	}

	private static void druidAddSenatorOrAssaultReal(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes,
			int assaults, String string,
			Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> target) {

		// first go through trying to set sen siege...
		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (!e.getValue().hasSenator()) {
				e.getValue().druid_with_sen++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.DRUID_SEN_SIEGE, pair));
				return;
			}
		}

		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (e.getValue().getAssault() < assaults) {
				e.getValue().druid++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.DRUID_ASSAULT, pair));
				return;
			}
		}

		// If only require fakes left...
		Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(null, new ArrayList<City>());
		addFakesForFakes(reals, fakes, string, pair);
		target.get(string).add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.DRUID_ASSAULT, pair));
		extra_ass++;
	}

	private static void horseAddSenatorOrAssaultReal(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes,
			int assaults, String string,
			Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> target) {

		// first go through trying to set sen siege...
		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (!e.getValue().hasSenator()) {
				e.getValue().horse_with_sen++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.HORSE_SEN_SIEGE, pair));
				return;
			}
		}

		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (e.getValue().getAssault() < assaults) {
				e.getValue().horse++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.HORSE_ASSAULT, pair));
				return;
			}
		}

		// If only require fakes left...
		Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(null, new ArrayList<City>());
		addFakesForFakes(reals, fakes, string, pair);
		target.get(string).add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.HORSE_ASSAULT, pair));
		extra_ass++;
	}

	private static int sumMaps(@SuppressWarnings("unchecked") Map<City, AttackTypes>... reals) {
		int ret = 0;
		for (int i = 0; i < reals.length; i++) {
			for (AttackTypes t : reals[i].values()) {
				ret += t.getTotal();
			}
		}
		return ret;
	}

	private static void horseAddAssaultReal(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes, int assaults,
			String string, Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> target,
			boolean senatorCheck) {
		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (e.getValue().getAssault() < assaults) {
				e.getValue().horse++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.HORSE_ASSAULT, pair));
				return;
			}
		}

		// If only require fakes left...
		Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(null, new ArrayList<City>());
		addFakesForFakes(reals, fakes, string, pair);
		target.get(string).add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.HORSE_ASSAULT, pair));
		extra_ass++;
	}

	private static void druidAddAssaultReal(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes, int assaults,
			String string, Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> target,
			boolean senatorCheck) {
		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (e.getValue().getAssault() < assaults) {
				e.getValue().druid++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.DRUID_ASSAULT, pair));
				return;
			}
		}

		// If only require fakes left...
		Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(null, new ArrayList<City>());
		addFakesForFakes(reals, fakes, string, pair);
		target.get(string).add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.DRUID_ASSAULT, pair));
		extra_ass++;
	}

	private static void vanqAddAssaultReal(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes, int assaults,
			String string, Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> target,
			boolean senatorCheck) {
		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (e.getValue().getAssault() < assaults) {
				e.getValue().vanq++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.VANQ_ASSAULT, pair));
				return;
			}
		}

		// If only require fakes left...
		Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(null, new ArrayList<City>());
		addFakesForFakes(reals, fakes, string, pair);
		target.get(string).add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.VANQ_ASSAULT, pair));
		extra_ass++;
	}

	private static void sorcAddAssaultReal(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes, int assaults,
			String string, Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> target,
			boolean senatorCheck) {
		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (e.getValue().getAssault() < assaults) {
				e.getValue().sorc++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.SORC_ASSAULT, pair));
				return;
			}
		}

		// If only require fakes left...
		Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(null, new ArrayList<City>());
		addFakesForFakes(reals, fakes, string, pair);
		target.get(string).add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.SORC_ASSAULT, pair));
		extra_ass++;
	}

	private static void wsAddSiege(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes, int warships,
			String string, Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> target) {
		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (e.getValue().ws < warships - 1) {
				e.getValue().ws++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.WS_SIEGE, pair));
				return;
			}
		}

		// If only require fakes left...
		Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(null, new ArrayList<City>());
		addFakesForFakes(reals, fakes, string, pair);
		target.get(string).add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.WS_SIEGE, pair));
		extra_ws++;
	}

	private static void print(Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> target) {
		for (Entry<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> e : target.entrySet()) {
			System.out.println(e.getKey() + ":");
			for (Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>> p : e.getValue()) {
				if (p.p2.p1 != null) {
					String x = p.p1.name() + " REAL: " + p.p2.p1.coords() + " FAKE: " + print(p.p2.p2);
					// System.out.println(x);
					convertToCFunky(x);
				} else {
					String x = p.p1.name() + " FAKE: " + print(p.p2.p2);
					// System.out.println(x);
					convertToCFunky(x);
				}
			}
		}
	}

	private static String print(ArrayList<City> p2) {
		String ret = "";
		for (City c : p2)
			ret += c.coords() + ", ";
		return ret;
	}

	private static void wsAddSenSiegeOrSiege(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes, int warships,
			String string, Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> target) {

		// first go through trying to set sen siege...
		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (e.getValue().ws_with_sen == 0) {
				e.getValue().ws_with_sen++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.WS_SEN_SIEGE, pair));
				return;
			}
		}

		// If no more sen sieges to set...
		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (e.getValue().ws < warships - 1) {
				e.getValue().ws++;
				Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(e.getKey(), new ArrayList<City>());
				addFakesForReal(reals, fakes, string, pair);
				target.get(string)
						.add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.WS_SIEGE, pair));
				return;
			}
		}

		// If only require fakes left...
		Pair<City, ArrayList<City>> pair = new Pair<City, ArrayList<City>>(null, new ArrayList<City>());
		addFakesForFakes(reals, fakes, string, pair);
		target.get(string).add(new Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>(ATTACK_TYPES.WS_SIEGE, pair));
		extra_ws++;
	}

	private static void addFakesForFakes(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes, String string,
			Pair<City, ArrayList<City>> pair) {
		int num = attacks_per_castle;
		for (int i = 0; i < num; i++) {
			City temp = lowestIndex(reals, fakes);
			pair.p2.add(temp);
			incrementFakes(temp, reals, fakes);
		}
	}

	private static void addFakesForReal(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes, String string,
			Pair<City, ArrayList<City>> target) {
		int num = attacks_per_castle - 1;
		for (int i = 0; i < num; i++) {
			City temp = lowestIndex(reals, fakes);
			target.p2.add(temp);
			incrementFakes(temp, reals, fakes);
		}
	}

	private static void incrementFakes(City temp, Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes) {
		AttackTypes s = reals.get(temp);
		if (s != null) {
			s.fakes++;
			return;
		}
		s = fakes.get(temp);
		s.fakes++;
		return;
	}

	private static City lowestIndex(Map<City, AttackTypes> reals, Map<City, AttackTypes> fakes) {
		City lowest = null;
		int value = Integer.MAX_VALUE; // start with fakes to fill them up
										// first...
		for (Entry<City, AttackTypes> e : fakes.entrySet()) {
			int i = e.getValue().getTotal();
			if(i < value) {
				lowest = e.getKey();
				value = i;
			}
			
		}
		
		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (e.getValue().getTotal() < value) {
				lowest = e.getKey();
				value = e.getValue().getTotal();
			}
		}

		return lowest;
	}

	@SuppressWarnings("unused")
	private static boolean wsNotDone(Map<City, AttackTypes> reals, int ws) {
		for (Entry<City, AttackTypes> e : reals.entrySet()) {
			if (e.getValue().getWS() < ws)
				return true;
		}
		return false;
	}

	public static void main(String[] args) {

		// Scanner scanner = new Scanner(System.in);

		// All the attackers inputs. Can do this with csv sheet. Need to test.
		Map<String, AttackTypes> attackersV2 = new HashMap<>();
		// 123 = only assaults
		// 1234 = only WS
		attackersV2 = csvToAttackers("src/cotg/data/DATA.csv", -1);

		Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> x = null;
		int warships = 0;
		int assults = 13;
		int continent_to_attack = 22;
		int number_of_attacks = 0;
		x = simpleAttack("ALLIANCE", continent_to_attack, attackersV2, warships, assults, number_of_attacks, true);

		// x = simpleAttack(new String[] { "whitehot", "NardosASR", "Wrothar",
		// "Archemenos", "ReiDeDeuses", "dinamik",
		// "Lordziggy", "phuddydhuddy" }, 54, attackersV2, 0, 6, true);
		// useSeed(-6160508199884610223L);
		// attackersV2 = csvToAttackers("src/cotg/data/DATA.csv", 1234);
		// EXCLUDED_TARGETS = "whitehot, NardosASR, Wrothar, Archemenos,
		// ReiDeDeuses, dinamik, Lordziggy, phuddydhuddy";
		// x = simpleAttack(Constants.BSR, 54, attackersV2, 4, 6, false);

		// combine x and x2
		ByteArrayOutputStream os = gatherPrintlnOutput();
		print(x);
		System.out.println(AttackCreator.seed);

		try {
			Parser.printToFile(os.toString(), new File("src/cotg/data/output.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// String input = "", rString = "";
		// while (!(rString = scanner.nextLine()).equals("q")) {
		// if (rString.equals("c")) {
		// convertToCFunky(input);
		// input = "";
		// }
		// input += rString + "\n";
		// }
		//
		// convertToCFunky(input);
		//
		// scanner.close();

	}

	private static Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> combineOrders(
			Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> x,
			Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> x2) {
		Map<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>> ret = new HashMap<String, ArrayList<Pair<ATTACK_TYPES, Pair<City, ArrayList<City>>>>>();

		for (String player : x.keySet()) {
			ret.put(player, x.get(player));
			ret.get(player).addAll(x2.get(player));
		}

		return ret;
	}

	public final static String month = "03";
	public final static String day = "03";
	public final static int hour = 10;
	public final static String min = "00";

	public static void convertToCFunky(String input) {
		String[] lines = input.split("\n");
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].matches(".*[(][0-9].*[)].*")) {
				int real = lines[i].indexOf("REAL: ");

				String attackType = findAttackType(lines[i]);

				int fake = lines[i].indexOf("FAKE:");

				int currentIndex = real;
				int count = count(lines[i], '(');
				int[] type = new int[count];
				String[] x = new String[count], y = new String[count];
				int index = 0;

				// getting all real attacks...
				while (lines[i].indexOf("(", currentIndex) < fake) {
					currentIndex = lines[i].indexOf("(", currentIndex);
					int endIndex = lines[i].indexOf(")", currentIndex);

					String[] xStrings = lines[i].substring(currentIndex + 1, endIndex).replace(" ", "").split(",");

					type[index] = 1; // real
					x[index] = "\"" + xStrings[0] + "\"";
					y[index] = "\"" + xStrings[1] + "\"";
					index++;

					currentIndex = endIndex;
				}

				while (lines[i].indexOf("(", currentIndex) > 0) {
					currentIndex = lines[i].indexOf("(", currentIndex);
					int endIndex = lines[i].indexOf(")", currentIndex);

					String[] xStrings = lines[i].substring(currentIndex + 1, endIndex).replace(" ", "").split(",");
					x[index] = "\"" + xStrings[0] + "\"";
					y[index] = "\"" + xStrings[1] + "\"";
					index++;

					currentIndex = endIndex;
				}

				String json = attackType + "\t{\"type\": " + Arrays.toString(type) + ", \"y\": " + Arrays.toString(y)
						+ ", \"x\": " + Arrays.toString(x) + ", \"time\": [\"" + hour + "\", \"" + min
						+ "\", \"00\", \"" + month + "/" + day + "/2018\"]}";
				System.out.println(json);
			}
		}
	}

	private static String findAttackType(String string) {
		if (string.contains(ATTACK_TYPES.SORC_ASSAULT.name()))
			return ATTACK_TYPES.SORC_ASSAULT.name();
		if (string.contains(ATTACK_TYPES.SORC_SIEGE.name()))
			return ATTACK_TYPES.SORC_SIEGE.name();
		if (string.contains(ATTACK_TYPES.SORC_SEN_SIEGE.name()))
			return ATTACK_TYPES.SORC_SEN_SIEGE.name();

		if (string.contains(ATTACK_TYPES.VANQ_ASSAULT.name()))
			return ATTACK_TYPES.VANQ_ASSAULT.name();
		if (string.contains(ATTACK_TYPES.VANQ_SIEGE.name()))
			return ATTACK_TYPES.VANQ_SIEGE.name();
		if (string.contains(ATTACK_TYPES.VANQ_SEN_SIEGE.name()))
			return ATTACK_TYPES.VANQ_SEN_SIEGE.name();

		if (string.contains(ATTACK_TYPES.HORSE_ASSAULT.name()))
			return ATTACK_TYPES.HORSE_ASSAULT.name();
		if (string.contains(ATTACK_TYPES.HORSE_SIEGE.name()))
			return ATTACK_TYPES.HORSE_SIEGE.name();
		if (string.contains(ATTACK_TYPES.HORSE_SEN_SIEGE.name()))
			return ATTACK_TYPES.HORSE_SEN_SIEGE.name();

		if (string.contains(ATTACK_TYPES.DRUID_ASSAULT.name()))
			return ATTACK_TYPES.DRUID_ASSAULT.name();
		if (string.contains(ATTACK_TYPES.DRUID_SIEGE.name()))
			return ATTACK_TYPES.DRUID_SIEGE.name();
		if (string.contains(ATTACK_TYPES.DRUID_SEN_SIEGE.name()))
			return ATTACK_TYPES.DRUID_SEN_SIEGE.name();

		if (string.contains(ATTACK_TYPES.WS_SEN_SIEGE.name()))
			return ATTACK_TYPES.WS_SEN_SIEGE.name();
		if (string.contains(ATTACK_TYPES.WS_SIEGE.name()))
			return ATTACK_TYPES.WS_SIEGE.name();

		if (string.contains(ATTACK_TYPES.FAKE_ASSAULT.name()))
			return ATTACK_TYPES.FAKE_ASSAULT.name();
		if (string.contains(ATTACK_TYPES.FAKE_SCOUT.name()))
			return ATTACK_TYPES.FAKE_SCOUT.name();
		if (string.contains(ATTACK_TYPES.FAKE_SIEGE.name()))
			return ATTACK_TYPES.FAKE_SIEGE.name();
		if (string.contains(ATTACK_TYPES.FAKE.name()))
			return ATTACK_TYPES.FAKE.name();

		return "";
	}

	private static int count(String string, char c) {
		int i = 0;
		for (char h : string.toCharArray()) {
			if (h == c)
				i++;
		}
		return i;
	}

	/*
	 * 0 Timestamp 1 In-Game Name 2 Vanquisher/Galley Castles [Senator Capable]
	 * 3 Vanquisher/Galley Castles [Not Senator Capable] 4 Horseman/Galley
	 * Castles [Senator Capable] 5 Horseman/Galley Castles [Not Senator Capable]
	 * 6 Sorcerer/Galley Castles [Senator Capable] 7 Sorcerer/Galley Castles
	 * [Not Senator Capable] 8 Druid/Galley Castles [Senator Capable] 9
	 * Druid/Galley Castles [Not Senator Capable] 10 Warship Castles [Senator
	 * Capable] 11 Warship Castles [Not Senator Capable] 12 Sorcerer/Galley
	 * Castles [Not Senator Capable]
	 */
	public static Map<String, AttackTypes> csvToAttackers(String file, int type) {
		ArrayList<String> rows = Parser.parseFiles(file);
		Map<String, AttackTypes> ret = new HashMap<String, AttackTypes>();
		// int count = 0;

		for (int i = 1; i < rows.size(); i++) {
			rows.set(i, rows.get(i).replaceAll(",", " , "));
			String[] column = rows.get(i).split(",");
			for (int j = 0; j < column.length; j++) {
				column[j] = column[j].trim();
			}
			String name = column[1];

			int vanq_sen = getInt(column[2]);
			int vanq = getInt(column[3]);
			int sorc_sen = getInt(column[6]);
			int sorc = getInt(column[7]);
			int horse_sen = getInt(column[4]);
			int horse = getInt(column[5]);
			int druid_sen = getInt(column[8]);
			int druid = getInt(column[9]);
			int ws_sen = getInt(column[10]);
			int ws = getInt(column[11]);
			int fake = getInt(column[12]) + getInt(column[13]);

			AttackTypes curr = new AttackTypes(vanq_sen, vanq, sorc_sen, sorc, horse_sen, horse, druid_sen, druid,
					ws_sen, ws, fake);
			// ONLY ASSAULTS
			if (type == 1) {
				curr = new AttackTypes(vanq_sen, vanq, sorc_sen, sorc, horse_sen, horse, druid_sen, druid, ws_sen, ws,
						0);
			}
			if (type == 123) {
				curr = new AttackTypes(vanq_sen, vanq, sorc_sen, sorc, horse_sen, horse, druid_sen, druid, 0, 0, 0);
			}
			// ONLY WS && FAKES
			if (type == 1234) {
				curr = new AttackTypes(0, 0, 0, 0, 0, 0, 0, 0, ws_sen, ws, fake);
			}
			ret.put(name, curr);
			// count+=curr.getAssault();
		}
		// System.out.println(count);

		return ret;
	}

	public static int getInt(String s) {
		if (s.equals(""))
			return 0;
		if (s.matches("^[0-9]*$")) {
			return Integer.valueOf(s);
		} else
			return 0;
	}
}
