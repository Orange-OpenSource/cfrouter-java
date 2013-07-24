package cfrouter.client;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import cfrouter.client.impl.PojoMapper;

/**
 *
 */
public class RouterMetricsTest {

    @Test
    public void parse_from_json() throws IOException {
        String json = TestResourceLoader.loadLocalResource("varzResponse.json");
        RouterMetrics metrics = PojoMapper.fromJson(json, RouterMetrics.class);
        Assert.assertEquals(520, metrics.getRequests());
        Assert.assertNotNull(metrics.getConfig());
        Assert.assertEquals(31536000, metrics.getConfig().getStaleThreshold());
        Assert.assertEquals(30, metrics.getConfig().getStartInterval());
        Assert.assertEquals(0, metrics.getConfig().getActiveAppsInterval());
        Assert.assertEquals(30, metrics.getConfig().getPruneInterval());
        Assert.assertEquals("10.114.6.104", metrics.getConfig().getIp());
        Assert.assertEquals(8081, metrics.getConfig().getPort());
    }

}
