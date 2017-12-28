package cotg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
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

	public static final int attacks_per_castle = 7;

	public static final boolean try_senator_siege = false;
	// only used if(try_senator_siege)
	public static final int assault_siege_per_castle = 1;

	// minimum_attacks_per_real_target + 1 <= minimum_attacks_per_target
	public static final int minimum_attacks_per_real_target = 12;
	public static final int minimum_attacks_per_target = minimum_attacks_per_real_target + 1;

	private static final Random GENERATOR = new Random();
	public static Random random;
	public static long seed;

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

		return new Pair<>(r, targets);
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
		printArray(attacks);
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

	private static ArrayList<City> getTargets(int continent, String... targetPlayers) {
		return getTargets(continent, -1, -1, -1, targetPlayers);
	}

	private static ArrayList<City> getTargets(int continent, int x, int y, int radius, String... targetPlayers) {
		ArrayList<City> targets = new ArrayList<>();
		for (String player : targetPlayers) {
			for (Alliance a : Constants.curr_alliances) {
				if (a.players.contains(new Player(player))) {
					for (Player p : a.players) {
						if (p.name.equals(player)) {
							for (City c : p.cities) {
								if (c.isCastle && c.isWater && (c.continent == -1 || c.continent == continent)
										&& c.score >= 6000) {
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
			int total = rand ? attacks_per_castle - 1 : attacks_per_castle;
			for (int i = 0; i < total; i++) {
				addToFake(real_attacks.size(), attacks, "f" + s);
			}
		}

		// next add fakes - fun!
		for (String s : pure_fakes) {
			boolean rand = random.nextBoolean();
			int total = rand ? attacks_per_castle : attacks_per_castle + 1;
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

	private static void printArray(ArrayList<Pair<City, ArrayList<String>>> attacks) {
		for (Pair<City, ArrayList<String>> c : attacks) {
			System.out.println(c.toString());
		}
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

				System.out.println(line);
			}

			System.out.println("\nFAKES");

			for (ArrayList<Pair<String, City>> r : fakes) {
				String line = "\tFAKE: ";
				for (Pair<String, City> c : r) {
					count++;
					line += c.p2.simpleString() + ", ";
				}

				System.out.println(line);
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

	public static void main(String[] args) {

		// useSeed(-5509912525257982219L);

		Scanner scanner = new Scanner(System.in);

		// All the attackers inputs. Can do this with csv sheet. Need to test.
		Map<String, AttackTypes> attackersV2 = new HashMap<>();
		// attackersV2 = csvToAttackers("src/cotg/data/DATA.csv");

		int x = -1, y = -1, radius = -1;
		Map<String, Pair<ArrayList<ArrayList<Pair<String, City>>>, ArrayList<ArrayList<Pair<String, City>>>>> waterTargetsByNameAndContinent = getWaterTargetsByAllianceAndContinentV2(
				attackersV2, 22, x, y, radius, Constants.DMC);
		printAttacksByTargets(waterTargetsByNameAndContinent);

		System.out.println(printSimpleTargets(waterTargetsByNameAndContinent));

		// System.out.println(AttackCreator.seed);

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

		scanner.close();

	}

	public final static int month = 12;
	public final static int day = 24;

	public static void convertToCFunky(String input) {
		String[] lines = input.split("\n");
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].matches(".*[(][0-9].*[)].*")) {
				int real = lines[i].indexOf("REAL: ");

				String attackType = findAttackType(lines[i]);

				int fake = lines[i].indexOf("FAKE: ");

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
						+ ", \"x\": " + Arrays.toString(x) + ", \"time\": [\"10\", \"00\", \"00\", \"" + month + "/"
						+ day + "/2017\"]}";
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
	public static Map<String, AttackTypes> csvToAttackers(String file) {
		ArrayList<String> rows = Parser.parseFiles(file);
		Map<String, AttackTypes> ret = new HashMap<String, AttackTypes>();

		for (int i = 1; i < rows.size(); i++) {
			String[] column = rows.get(i).split(",");
			String name = column[1];

			int vanq_sen = Integer.valueOf(column[2]);
			int vanq = Integer.valueOf(column[3]);
			int sorc_sen = Integer.valueOf(column[6]);
			int sorc = Integer.valueOf(column[7]);
			int horse_sen = Integer.valueOf(column[4]);
			int horse = Integer.valueOf(column[5]);
			int druid_sen = Integer.valueOf(column[8]);
			int druid = Integer.valueOf(column[9]);
			int ws_sen = 0;
			int ws = Integer.valueOf(column[10]) + Integer.valueOf(column[11]);
			int fakes = Integer.valueOf(column[12]);

			AttackTypes curr = new AttackTypes(vanq_sen, vanq, sorc_sen, sorc, horse_sen, horse, druid_sen, druid,
					ws_sen, ws, fakes);
			ret.put(name, curr);
		}

		return ret;
	}
}
