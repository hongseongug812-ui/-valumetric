package com.valumetric.config;

import com.valumetric.calculator.AhpEngine;
import com.valumetric.calculator.HcroiCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Calculator Bean 설정
 */
@Configuration
public class CalculatorConfig {

    @Bean
    public HcroiCalculator hcroiCalculator() {
        return new HcroiCalculator();
    }

    @Bean
    public AhpEngine ahpEngine() {
        return new AhpEngine();
    }
}
