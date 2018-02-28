package net.kwami.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public abstract class RuntimeExec {
	private static final MyLogger logger = new MyLogger(RuntimeExec.class);

	public static boolean issue(String cmd) {
		boolean success = true;
		String[] cmdParts = cmd.split(" ");
		List<String> outputLines = new ArrayList<>();
		try {
			Process result = Runtime.getRuntime().exec(cmdParts);
			result.waitFor();
			try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(result.getInputStream()))) {
				String line;
				while ((line = outputReader.readLine()) != null) {
					outputLines.add(line);
				}
				if (outputLines.size() == 0)
					outputLines.add("Successfully executed with no output");
			}
		} catch (IOException | InterruptedException e) {
			success = false;
			outputLines.add(e.toString());
		}
		StringBuilder output = new StringBuilder();
		for (String line : outputLines) {
			output.append(line);
		}
		logger.info("Command execution successful = %b:\n%s\n%s", success, cmd, output.toString());
		return success;
	}

}
