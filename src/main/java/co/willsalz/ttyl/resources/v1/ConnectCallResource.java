package co.willsalz.ttyl.resources.v1;

import co.willsalz.ttyl.middleware.CsrfFilter;
import com.codahale.metrics.annotation.Timed;
import com.twilio.twiml.Dial;
import com.twilio.twiml.Number;
import com.twilio.twiml.Say;
import com.twilio.twiml.TwiML;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("v1/connectCall")
@Produces(MediaType.APPLICATION_XML)
@PermitAll
@CsrfFilter.CsrfFilterBypass
public class ConnectCallResource {

    private static final Logger logger = LoggerFactory.getLogger(ConnectCallResource.class);

    @POST
    @Timed
    public TwiML connectCall(@Context HttpServletRequest req) throws TwiMLException {

        // TODO(wjs): lookup ephemeral call [ phone #, zip, etc ]

        // Build our response
        final VoiceResponse voiceResponse = new VoiceResponse.Builder()
                .say(
                        new Say.Builder("Connecting you now…")
                                .voice(Say.Voice.ALICE)
                                .build()
                )
                .dial(
                        new Dial.Builder()
                                .number(
                                        new Number.Builder("+15005550006")
                                                .build()
                                )
                                .build()
                )
                .build();

        // Return TwiML Response
        return voiceResponse;
    }
}
