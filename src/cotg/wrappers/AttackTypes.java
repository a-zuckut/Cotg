package cotg.wrappers;

public class AttackTypes {

	public int vanq_with_sen, sorc_with_sen, horse_with_sen, druid_with_sen, ws_with_sen;
	public int vanq, sorc, horse, druid;
	public int ws;

	public int fakes;

	public int siege = 0;

	public AttackTypes() {
		// default
	}

	public AttackTypes(int vanq_sen, int vanq, int sorc_sen, int sorc, int horse_sen, int horse, int druid_sen,
			int druid, int ws_sen, int ws, int fakes) {
		this.vanq_with_sen = vanq_sen;
		this.vanq = vanq;
		this.sorc_with_sen = sorc_sen;
		this.sorc = sorc;
		this.horse_with_sen = horse_sen;
		this.horse = horse;
		this.druid_with_sen = druid_sen;
		this.druid = druid;
		this.ws_with_sen = ws_sen;
		this.ws = ws;
		this.fakes = fakes;
	}

	public int getAssault() {
		return vanq_with_sen + vanq + sorc + horse + druid + sorc_with_sen + horse_with_sen + druid_with_sen;
	}

	public int getWS() {
		return ws + ws_with_sen;
	}

	public int getFakes() {
		return fakes;
	}

	public boolean hasSenator() {
		return vanq_with_sen + sorc_with_sen + horse_with_sen + druid_with_sen > 0;
	}

	public boolean useSorc() {
		return useSorc(false);
	}

	public boolean useSorc(boolean senator) {
		if (sorc_with_sen > 0) {
			sorc_with_sen--;
			return true;
		}

		if (senator)
			return false;

		if (sorc > 0) {
			sorc--;
			return true;
		}
		return false;
	}

	public boolean useVanq() {
		return useVanq(false);
	}

	public boolean useVanq(boolean senator) {
		if (vanq_with_sen > 0) {
			vanq_with_sen--;
			return true;
		}

		if (senator)
			return false;

		if (vanq > 0) {
			vanq--;
			return true;
		}
		return false;
	}

	public boolean useHorse() {
		return useHorse(false);
	}

	public boolean useHorse(boolean senator) {
		if (horse_with_sen > 0) {
			horse_with_sen--;
			return true;
		}

		if (senator)
			return false;

		if (horse > 0) {
			horse--;
			return true;
		}
		return false;
	}

	public boolean useDruid() {
		return useDruid(false);
	}

	public boolean useDruid(boolean senator) {
		if (druid_with_sen > 0) {
			druid_with_sen--;
			return true;
		}

		if (senator)
			return false;

		if (druid > 0) {
			druid--;
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "Vanq With Sen: " + vanq_with_sen + ", Vanq: " + vanq + ", Sorc With Sen: " + sorc_with_sen + ", Sorc: " + sorc + ", Horse with Sen: " + horse_with_sen + ", Horse: " + horse + ", Druid With Sen: " + druid_with_sen + ", Druid: " + druid + ", WS with Sen: " + ws_with_sen + ", WS: " + ws + "\n";
	}

}
