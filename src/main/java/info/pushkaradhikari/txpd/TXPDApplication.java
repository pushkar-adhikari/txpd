package info.pushkaradhikari.txpd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@EnableConfigurationProperties
@ComponentScan(basePackages = { "info.pushkaradhikari" })
@SpringBootApplication(scanBasePackages = { "info.pushkaradhikari" })
public class TXPDApplication {

	public static void main(String[] args) {
		SpringApplication.run(TXPDApplication.class, args);
	}

}
