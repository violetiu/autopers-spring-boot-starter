package org.violetime.autopers.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "autopers")
public class AutopersProperties {

    private String model="development";//project,development

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

}
