package net.kwami.ppfe;

import java.util.Properties;

import net.kwami.utils.Configurator;

public class RelayApplication extends PpfeApplication {
	private PpfeContainer container;

	public RelayApplication(PpfeContainer container) throws Exception {
		super(container);
		this.container = container;
	}

	public void process(PpfeMessage message) {
		Properties properties = Configurator.get(Properties.class);
		container.sendRequest(properties.getProperty("nextServerPath"), message, 2000);
	}
}
