package ru.thelenskyy.limitsservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LimitsServiceController {
    @Autowired
    private Configuration configuration;

    @GetMapping("/limits")
    public LimitConfiguration limitConfiguration(){
        return new LimitConfiguration(configuration.getMinimum(), configuration.getMaximum());
    }
}
