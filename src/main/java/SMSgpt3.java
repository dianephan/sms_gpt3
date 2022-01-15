import java.util.ArrayList;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.engine.Engine;

import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;
import static spark.Spark.*;
import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Region;
import com.github.alexdlaird.ngrok.protocol.Tunnel;

public class SMSgpt3 {
    public static void main(String... args) {
        port(80);
        String token = System.getenv("OPENAI_TOKEN");
        OpenAiService service = new OpenAiService(token);
        Engine davinci = service.getEngine("davinci");
        ArrayList<CompletionChoice> storyArray = new ArrayList<CompletionChoice>();

        post("/sms", (req, res) -> {
            res.type("application/xml");
            System.out.println("\nBrewing up a story...");

            CompletionRequest completionRequest = CompletionRequest.builder()
                    .prompt("The following is a spooky story written for kids, just in time for Halloween. Everyone always talks about the old house at the end of the street, but I couldn’t believe what happened when I went inside.")
                    .temperature(0.7)
                    .maxTokens(96)
                    .topP(1.0)
                    .frequencyPenalty(0.0)
                    .presencePenalty(0.3)
                    .echo(true)
                    .build();
            service.createCompletion("davinci", completionRequest).getChoices().forEach(line -> {storyArray.add(line);});
    //        System.out.println(storyArray);
            String SMSElement = storyArray.toString();
            System.out.println(SMSElement);

            Body body = new Body
                    .Builder(SMSElement)
                    .build();
            Message sms = new Message
                    .Builder()
                    .body(body)
                    .build();
            MessagingResponse twiml = new MessagingResponse
                    .Builder()
                    .message(sms)
                    .build();
            return twiml.toXml();
        });

        final NgrokClient ngrokClient = new NgrokClient.Builder().build();
        final CreateTunnel createTunnel = new CreateTunnel.Builder()
                .build();
        final Tunnel tunnel = ngrokClient.connect(createTunnel);
    }
}
