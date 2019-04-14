package cash.tenant.main;

import java.io.IOException;
import java.net.URL;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import jrain.fw.main.JRainMain;

public class CashTenantServer {

	public static void main(String[] args) throws IOException {
		URL url = Thread.currentThread().getContextClassLoader().getResource("my-log4j2.xml");
		if (url != null) {
			ConfigurationSource source = new ConfigurationSource(url.openStream(), url);
			Configurator.initialize(null, source);
		}
		JRainMain.main(args);
	}
}
