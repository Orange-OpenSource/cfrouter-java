package cfrouter.client.impl;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true) //ignore tags not yet modeled
public class RouterConfig {

    @JsonProperty("PublishStartMessageIntervalInSeconds")
    private int startInterval;

    @JsonProperty("DropletStaleThresholdInSeconds")
    private int staleThreshold;

    @JsonProperty("PruneStaleDropletsIntervalInSeconds")
    private int pruneInterval;

    @JsonProperty("PublishActiveAppsIntervalInSeconds")
    private int activeAppsInterval;

    @JsonProperty("Port")
    private int port;

    @JsonProperty("Ip")
    private String ip;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * @return the startInterval
     */
    public int getStartInterval() {
        return startInterval;
    }

    /**
     * @param startInterval
     *            the startInterval to set
     */
    public void setStartInterval(int startInterval) {
        this.startInterval = startInterval;
    }

    /**
     * @return the staleThreshold
     */
    public int getStaleThreshold() {
        return staleThreshold;
    }

    /**
     * @param staleThreshold
     *            the staleThreshold to set
     */
    public void setStaleThreshold(int staleThreshold) {
        this.staleThreshold = staleThreshold;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port
     *            the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip
     *            the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the pruneInterval
     */
    public int getPruneInterval() {
        return pruneInterval;
    }

    /**
     * @param pruneInterval
     *            the pruneInterval to set
     */
    public void setPruneInterval(int pruneInterval) {
        this.pruneInterval = pruneInterval;
    }

    /**
     * @return the activeAppsInterval
     */
    public int getActiveAppsInterval() {
        return activeAppsInterval;
    }

    /**
     * @param activeAppsInterval
     *            the activeAppsInterval to set
     */
    public void setActiveAppsInterval(int activeAppsInterval) {
        this.activeAppsInterval = activeAppsInterval;
    }

}
