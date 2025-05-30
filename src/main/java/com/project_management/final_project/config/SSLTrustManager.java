package com.project_management.final_project.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Component
public class SSLTrustManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SSLTrustManager.class);
    
    @PostConstruct
    public void init() {
        try {
            TrustManager[] trustAllCertificates = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };
            
            // Install the trust manager
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            
            // Create host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            
            logger.info("Custom SSL Trust Manager initialized successfully");
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.error("Failed to initialize SSL Trust Manager", e);
        }
    }
} 