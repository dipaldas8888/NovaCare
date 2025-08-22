// src/main/java/com/dipal/NovaCare/config/ImageKitConfig.java
package com.dipal.NovaCare.config;

import io.imagekit.sdk.ImageKit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageKitConfig {

    @Value("${imagekit.public-key}")
    private String publicKey;

    @Value("${imagekit.private-key}")
    private String privateKey;

    @Value("${imagekit.url-endpoint}")
    private String urlEndpoint;

    @Bean
    public ImageKit imageKit() {
        ImageKit ik = ImageKit.getInstance();
        // ✅ fully-qualify the ImageKit Configuration class here
        ik.setConfig(new io.imagekit.sdk.config.Configuration(publicKey, privateKey, urlEndpoint));
        return ik;
    }
}
