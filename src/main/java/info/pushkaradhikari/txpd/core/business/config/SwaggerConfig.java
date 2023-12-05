package info.pushkaradhikari.txpd.core.business.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfig {
	
	@Bean
	public GroupedOpenApi publicApi() {
		// @formatter:off
		return GroupedOpenApi.builder()
				.group("public")
				.pathsToMatch("/**")
				.build();
		// @formatter:on
	}
	
	@Bean
	public OpenAPI springShopOpenAPI() {
		// @formatter:off
		return new OpenAPI()
				.info(new Info().title("TXPD API")
				.description("TX Process Dashboard")
				.version("v0.0.1")
				.license(new License().name("Apache 2.0").url("https://github.com/pushkar-adhikari/txpd")))
				.externalDocs(new ExternalDocumentation()
				.description("Live TX process miner dashboard using beamline.cloud"));
		// @formatter:on
	  }

}