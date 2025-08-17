package youngsu5582.tool.ai_tracker.api.proxy;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/proxy")
public class ProxyController {

    public static class GeminiRequestData {
        public String uri;
        public String method;
        public Object body;
    }

    @PostMapping("/requests")
    public void logGeminiRequest(@RequestBody GeminiRequestData data) {
        System.out.println("===== Received Gemini Request from Proxy =====");
        System.out.println("URI: " + data.uri);
        System.out.println("Method: " + data.method);
        System.out.println("Body: " + data.body.toString());
        System.out.println("==============================================");

        // You can add logic here to save to a database or perform other actions.
    }
}
