package cfrouter.client.impl;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import nats.client.Message;
import nats.client.MessageHandler;
import nats.client.Nats;
import nats.client.Subscription;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cfrouter.client.Route;
import cfrouter.client.Router;
import cfrouter.client.RouterMetrics;
import cfrouter.client.RouterStartHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HostAndPort;

/**
 *
 */
public class RouterImpl implements Router {
    private static final Logger logger = LoggerFactory.getLogger(RouterImpl.class);


    private static final String ROUTER_REGISTER = "router.register";
    private static final String ROUTER_UNREGISTER = "router.unregister";
    private static final String VARZ_ENDPOINT = "/varz";
    private static final String ROUTES_ENDPOINT = "/routes";
    private nats.client.Nats nats;

    public RouterImpl(Nats nats) {
        this.nats = nats;
    }

    @Override
    public void registerRouterStartMsgHandler(final RouterStartHandler handler) {
        Subscription subscription = nats.subscribe("router.start", new MessageHandler() {
            @Override
            public void onMessage(Message message) {
                List<Route> routes = handler.fetchAllActiveRoutes();
                for (Route route : routes) {
                    nats.publish(ROUTER_REGISTER, route.toJson());
                }
            }
        });
    }

    @Override
    public void addRoutes(List<Route> routes) {
        for (Route route : routes) {
            nats.publish(ROUTER_REGISTER, route.toJson());
        }
    }

    @Override
    public void replaceRoutes(List<Route> routes, long timeout, TimeUnit unit) throws IOException, InterruptedException {
        List<Route> activeRoutes = getActiveRoutes(timeout, unit);
        Set<String> vHostsToSet = new HashSet<String>();
        for (Route route : routes) {
            vHostsToSet.addAll(asList(route.getVirtualHosts()));
        }
        Set<Route> routesToUnregister = new HashSet<Route>();

        for (Route activeRoute : activeRoutes) {
            for (String activeVirtualHost : activeRoute.getVirtualHosts()) {
                if (vHostsToSet.contains(activeVirtualHost)) {
                    routesToUnregister.add(activeRoute);
                }
            }
        }
        if (! routesToUnregister.isEmpty()) {
            removeRoutes(new ArrayList<Route>(routesToUnregister));
        }
        addRoutes(routes);
    }

    @Override
    public void removeRoutes(List<Route> routes) {
        for (Route route : routes) {
            nats.publish(ROUTER_UNREGISTER, route.toJson());
        }
    }

    private RouterDiscoveryResponse getRouterDiscoveryResponse(long timeout, TimeUnit unit) throws InterruptedException, IOException {
        BlockingDeque<Message> discoverReplyMsgs = new LinkedBlockingDeque<Message>();
        nats.request("vcap.component.discover", timeout, unit, new RecordingMessageHandler("vcap.component.discover", discoverReplyMsgs));
        Message discoverReply = discoverReplyMsgs.takeFirst();
        discoverReplyMsgs.clear();
        RouterDiscoveryResponse response = PojoMapper.fromJson(discoverReply.getBody(), RouterDiscoveryResponse.class);
        logger.debug("Router discovery response: " + response);
        return response;
    }

    private BasicHttpContext workAroundApparentLackOfAuthChallengeInResponse(String host, int port) {
        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local
        // auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(new HttpHost(host, port), basicAuth);

        // Add AuthCache to the execution context
        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
        return localcontext;
    }

    private String invokeRouterEndPoint(RouterDiscoveryResponse r, String endPoint) throws IOException {

        String hostPort = r.getHost();
        HostAndPort hp = HostAndPort.fromString(hostPort).withDefaultPort(80);
        String host = hp.getHostText();
        int port = hp.getPort();

        DefaultHttpClient httpclient = new DefaultHttpClient();

        String endPointResponse;
        try {
            httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope(host, port),
                    new UsernamePasswordCredentials(r.getCredentials()[0], r.getCredentials()[1]));
            BasicHttpContext localContext = workAroundApparentLackOfAuthChallengeInResponse(host, port);

            HttpGet httpget = new HttpGet("http://" + hostPort + endPoint);

            endPointResponse = httpclient.execute(httpget, new BasicResponseHandler(), localContext);
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return endPointResponse;
    }

    @Override
    public RouterMetrics getRouterMetrics(long timeout, TimeUnit unit) throws InterruptedException, IOException {
        RouterDiscoveryResponse r = getRouterDiscoveryResponse(timeout, unit);

        String endPointResponse = invokeRouterEndPoint(r, VARZ_ENDPOINT);
        return PojoMapper.fromJson(endPointResponse, RouterMetrics.class);
    }

    public List<Route> parseRouteDumpIntoDtos(String json) throws IOException {
        List<Route> parsedRoutes = new ArrayList<Route>();
        ObjectMapper mapper = new ObjectMapper();
        Map<String,List<String>> routes = mapper.readValue(json, Map.class);
        for (Map.Entry<String, List<String>> entry : routes.entrySet()) {
            String vHost = entry.getKey();
            List<String> hostPorts = entry.getValue();
            for (String hostPort : hostPorts) {
                HostAndPort hp = HostAndPort.fromString(hostPort).withDefaultPort(80);
                Route r = new Route(hp.getHostText(), hp.getPort(), new String[]{vHost});
                parsedRoutes.add(r);
            }
        }
        return parsedRoutes;
    }

    @Override
    public List<Route> getActiveRoutes(long timeout, TimeUnit unit) throws IOException, InterruptedException {
        RouterDiscoveryResponse r = getRouterDiscoveryResponse(timeout, unit);

        String endPointResponse = invokeRouterEndPoint(r, ROUTES_ENDPOINT);
        List<Route> routes = parseRouteDumpIntoDtos(endPointResponse);
        return routes;
    }
}
