package cotg.wrappers.helper;

/**
 * Pair DS - simple <Object, Object>
 */
public class Pair<T, U> {

	// Can access either from methods or directly from variables
	public T p1;
	public U p2;
	
	public Pair(T p1, U p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
	
	public Pair<U,T> swap() {
		return new Pair<U,T>(p2,p1);
	}
	
	@Override
	public String toString() {
		return "{ " + p1 + ", " + p2 + " }";
	}
	
}
