package info.pushkaradhikari.txpd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableConfigurationProperties
@ComponentScan(basePackages = { "info.pushkaradhikari" })
@SpringBootApplication(scanBasePackages = { "info.pushkaradhikari" })
public class TXPDApplication {

    public static boolean runInDefaultMode = true;

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("static")) {
            runInDefaultMode = false;
            StaticRunApplication.main(args);
        } else {
            log.info("Starting TXPDApplication with default configuration...");
            SpringApplication.run(TXPDApplication.class, args);
        }
    }
}
