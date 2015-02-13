package common.utils;

import common.functional.Action;

public class DebugUtils {
	public static void elapse(Action act) {
		long prev = System.currentTimeMillis();
		act.call();
		System.out.println("Elapsed: " + (System.currentTimeMillis() - prev)  + " Ms");
	}
}
