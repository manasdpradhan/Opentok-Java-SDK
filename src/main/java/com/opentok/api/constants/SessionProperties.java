package com.opentok.api.constants;

import java.util.HashMap;
import java.util.Map;


public class SessionProperties {


    private String location = null;
    private boolean p2pPreference = false;

    private SessionProperties(Builder builder)
    {
        this.location = builder.location;
        this.p2pPreference = builder.p2pPreference;
    }

    public static class Builder
    {
        private String location = null;
        private boolean p2pPreference = false;
        

        public Builder location(String location)
        {
            this.location = location;
            return this;
        }

        public Builder p2pPreference(boolean p2pPreference)
        {
            this.p2pPreference = p2pPreference;
            return this;
        }

        public SessionProperties build()
        {
            return new SessionProperties(this);
        }
    }
    public String getLocation() {
        return location;
    }
    
    public boolean isP2pPreference() {
        return p2pPreference;
    }

    public Map<String, String> toMap() {
        Map<String, String> params = new HashMap<String, String>();
        if (null != location) {
            params.put("location", location);
        }
        if (p2pPreference) {
            params.put("p2p.preference", "enabled");
        }
        return params;
    }

};