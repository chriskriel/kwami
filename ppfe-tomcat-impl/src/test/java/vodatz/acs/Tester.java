package vodatz.acs;

import java.util.Properties;

public class Tester {

	public static void main(String[] args) {
		AcsConfig config = new AcsConfig();
		config.setLatencyThresholdMilliSecs(20000);
		config.setPathsendTimeoutDeciSecs(2000);
		Properties ussdMap = config.getUriToServerMappings();
		ussdMap.put("/acs/airtime", "$QUNC1.ACS-AIRTIME");
		ussdMap.put("/acs/bundles", "$QUNC1.ACS-BUNDLES");
		System.out.println(config.toString());
	}
}
