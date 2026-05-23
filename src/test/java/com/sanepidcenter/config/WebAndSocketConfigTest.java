package com.sanepidcenter.config;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.SimpleBrokerRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.RedirectViewControllerRegistration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebAndSocketConfigTest {
    @Test
    void webConfig_ShouldInvokeRegistryMethods() {
        WebConfig config = new WebConfig();

        ViewControllerRegistry viewRegistry = mock(ViewControllerRegistry.class);
        RedirectViewControllerRegistration viewRegistration = mock(RedirectViewControllerRegistration.class);
        when(viewRegistry.addRedirectViewController("/", "/index")).thenReturn(viewRegistration);
        config.addViewControllers(viewRegistry);
        verify(viewRegistry).addRedirectViewController("/", "/index");

        CorsRegistry corsRegistry = mock(CorsRegistry.class);
        CorsRegistration corsRegistration = mock(CorsRegistration.class);
        when(corsRegistry.addMapping("/**")).thenReturn(corsRegistration);
        when(corsRegistration.allowedOrigins(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedMethods(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedHeaders(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.exposedHeaders(any(String[].class))).thenReturn(corsRegistration);
        config.addCorsMappings(corsRegistry);
        verify(corsRegistry).addMapping("/**");

        ResourceHandlerRegistry resourceRegistry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration resourceRegistration = mock(ResourceHandlerRegistration.class);
        when(resourceRegistry.addResourceHandler(any(String[].class))).thenReturn(resourceRegistration);
        when(resourceRegistration.addResourceLocations(any(String[].class))).thenReturn(resourceRegistration);
        config.addResourceHandlers(resourceRegistry);
        verify(resourceRegistry).addResourceHandler(any(String[].class));
    }

    @Test
    void socketAndOpenApiConfig_ShouldBeConstructible() {
        WebSocketConfig socketConfig = new WebSocketConfig();
        WebSocketMessageBrokerConfigurer cfg = socketConfig;
        assertNotNull(cfg);
        assertNotNull(new OpenApiConfig());

        MessageBrokerRegistry brokerRegistry = mock(MessageBrokerRegistry.class);
        SimpleBrokerRegistration simpleBrokerRegistration = mock(SimpleBrokerRegistration.class);
        when(brokerRegistry.enableSimpleBroker(any(String[].class))).thenReturn(simpleBrokerRegistration);
        socketConfig.configureMessageBroker(brokerRegistry);

        StompEndpointRegistry endpointRegistry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration endpointRegistration = mock(StompWebSocketEndpointRegistration.class);
        when(endpointRegistry.addEndpoint("/ws")).thenReturn(endpointRegistration);
        when(endpointRegistration.setAllowedOriginPatterns(any(String[].class))).thenReturn(endpointRegistration);
        when(endpointRegistration.withSockJS()).thenReturn(null);
        socketConfig.registerStompEndpoints(endpointRegistry);
        verify(endpointRegistry).addEndpoint("/ws");
    }
}
