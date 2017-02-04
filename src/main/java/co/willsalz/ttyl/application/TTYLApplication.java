package co.willsalz.ttyl.application;

import co.willsalz.ttyl.configuration.TTYLConfiguration;
import co.willsalz.ttyl.gateways.RepresentativeLookupGateway;
import co.willsalz.ttyl.healthchecks.TwilioHealthCheck;
import co.willsalz.ttyl.resources.v1.CallResource;
import co.willsalz.ttyl.resources.v1.ConnectCallResource;
import co.willsalz.ttyl.resources.v1.RepresentativeResource;
import co.willsalz.ttyl.serialization.TwimlMessageBodyWriter;
import co.willsalz.ttyl.service.PhoneService;
import com.twilio.http.TwilioRestClient;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.CsrfProtectionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;

public class TTYLApplication extends Application<TTYLConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(TTYLApplication.class);

    public static void main(final String[] args) throws Exception {
        new TTYLApplication().run(args);
    }

    @Override
    public String getName() {
        return "ttyl";
    }

    @Override
    public void initialize(final Bootstrap<TTYLConfiguration> bootstrap) {
        super.initialize(bootstrap);

        // Allow Environment Variables to be used in YAML
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(true)
                )
        );

        // Views
        bootstrap.addBundle(new AssetsBundle(
                "/static/",
                "/",
                "index.html"
        ));
    }

    public void run(final TTYLConfiguration cfg, final Environment env) throws Exception {

        // Register Middleware
        env.jersey().register(new TwimlMessageBodyWriter());
        env.jersey().register(new CsrfProtectionFilter());

        // Gateways
        final Client client = new JerseyClientBuilder(env)
                .using(cfg.getJerseyClientConfiguration())
                .build(getName());

        final RepresentativeLookupGateway representativeLookupGateway = new RepresentativeLookupGateway(
                client,
                cfg.getRepresentativeClientConfiguration().getBaseUri(),
                env.getObjectMapper()
        );

        // Services
        final TwilioRestClient twilio = cfg.getTwilioConfiguration().build(env);
        final PhoneService phoneService = new PhoneService(
                twilio,
                cfg.getTwilioConfiguration().getPhoneNumber()
        );


        // Register Resources
        env.jersey().register(new CallResource(phoneService));
        env.jersey().register(new ConnectCallResource());
        env.jersey().register(new RepresentativeResource(representativeLookupGateway));

        // Register Healthchecks
        env.healthChecks().register("twilio", new TwilioHealthCheck(twilio));

    }
}
