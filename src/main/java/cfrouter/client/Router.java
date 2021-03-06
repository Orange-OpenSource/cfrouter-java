package cfrouter.client;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Entry point for controlling the router
 */
public interface Router {

    /**
     * Register to be able to renew expiring routes the router asks them.
     * @param handler the handler that will be notified
     */
    void registerRouterStartMsgHandler(RouterStartHandler handler);

    /**
     * register new routes. If a given vHost is already registered by an existing route, then traffic on this vHost
     * will be load balanced among the hosts of previous and new routes.
     * @param routes
     */
    void addRoutes(List<Route> routes);

    /**
     * register new routes or override existing ones. If vHost in requested routes is already covered by one route r1,
     * then r1 will be unregistered
     * @param routes
     */
    void replaceRoutes(List<Route> routes, long timeout, TimeUnit unit) throws IOException, InterruptedException;

    /**
     * Unregister the specified route (i.e. exact combination of vHost and host/port. If there are other registered host/port
     * for the vHost, they would remain until their expire)
     * @param routes
     */
    void removeRoutes(List<Route> routes);

    /**
     * Get metrics from the first router to respond
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    RouterMetrics getRouterMetrics(long timeout, TimeUnit unit) throws InterruptedException, IOException;

    /**
     * Peeks the active routes from the first router that respond.
     * @return a list of active routes. Notes that vHosts are normalized to use lower case
     */
    List<Route> getActiveRoutes(long timeout, TimeUnit unit) throws IOException, InterruptedException;
}
