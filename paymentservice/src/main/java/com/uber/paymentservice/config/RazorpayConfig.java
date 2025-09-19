package com.uber.paymentservice.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    @Getter
    @Value("${razorpay.api.key}")
    private String keyId;

    @Value("${razorpay.api.secret}")
    private String keySecret;
    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }
}