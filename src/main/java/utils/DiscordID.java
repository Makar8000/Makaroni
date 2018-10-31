package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DiscordID {
	public static final long PROGRAMMING = 176944717987053568L;
	public static final long GAMING = 91676956352868352L;
	public static final long MAPLE_SYRUP = 127575428217962496L;
	public static final String ADMIN_ID = "85924030661533696";
	public static final String LIN = "91683230192791552";
	public static final String BECCA = "91694618898362368";
	public static final String KYSIS = "175172897155973120";
	public static final String KAGA = "173302124656984064";
	public static final String VENUS = "374995210591272960";
	public static HashMap<String, ArrayList<String>> EXCLUSIONS;
	static {
		EXCLUSIONS = new HashMap<String, ArrayList<String>>();
		EXCLUSIONS.put(LIN, new ArrayList<>(Arrays.asList(BECCA)));
		EXCLUSIONS.put(BECCA, new ArrayList<>(Arrays.asList(LIN)));
		EXCLUSIONS.put(KYSIS, new ArrayList<>(Arrays.asList(VENUS)));
		EXCLUSIONS.put(KAGA, new ArrayList<>(Arrays.asList(VENUS)));
		EXCLUSIONS.put(VENUS, new ArrayList<>(Arrays.asList(KYSIS, KAGA)));
	}
}
