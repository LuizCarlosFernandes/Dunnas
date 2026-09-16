package br.com.dunnastecnologia.chamados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class GerenciadorChamadosApplication {

	public static void main(String[] args) {
		configureApplicationTimeZone();
		SpringApplication.run(GerenciadorChamadosApplication.class, args);
	}

	private static void configureApplicationTimeZone() {
		String configuredTimeZone = firstNonBlank(
				System.getenv("APP_TIMEZONE"),
				System.getenv("TZ")
		);

		if (configuredTimeZone == null) {
			return;
		}

		TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(configuredTimeZone)));
	}

	private static String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value.trim();
			}
		}
		return null;
	}

}
