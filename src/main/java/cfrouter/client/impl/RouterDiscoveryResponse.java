package cfrouter.client.impl;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true) //ignore tags not yet modeled
public class RouterDiscoveryResponse {

    private String host; //to use for REST endpooint
    private String[] credentials;
    private RouterConfig config;

    public RouterDiscoveryResponse() {
    }

    public String[] getCredentials() {
        return credentials;
    }

    public void setCredentials(String[] credentials) {
        this.credentials = credentials;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public RouterConfig getConfig() {
        return config;
    }

    public void setConfig(RouterConfig config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
