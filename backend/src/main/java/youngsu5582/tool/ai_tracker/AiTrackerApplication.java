package youngsu5582.tool.ai_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiTrackerApplication.class, args);
    }

}
