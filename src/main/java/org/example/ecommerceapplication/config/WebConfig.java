package org.example.ecommerceapplication.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path productUploadDirectory = Paths.get("uploads", "products")
                .toAbsolutePath()
                .normalize();
        Path profileUploadDirectory = Paths.get("uploads", "profiles")
                .toAbsolutePath()
                .normalize();

        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(productUploadDirectory.toUri().toString());

        registry.addResourceHandler("/uploads/profiles/**")
                .addResourceLocations(profileUploadDirectory.toUri().toString());
    }
}
