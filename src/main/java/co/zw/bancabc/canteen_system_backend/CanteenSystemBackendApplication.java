package co.zw.bancabc.canteen_system_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CanteenSystemBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CanteenSystemBackendApplication.class, args);
	}
}