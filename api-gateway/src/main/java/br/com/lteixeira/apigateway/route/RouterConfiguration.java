package br.com.lteixeira.apigateway.route;

import br.com.lteixeira.apigateway.filter.AuthorizationFilter;
import br.com.lteixeira.apigateway.filter.ResourceAccessFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterConfiguration {

    @Value("${partner.url}")
    private String partnerUrl;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, AuthorizationFilter authorizationFilter, ResourceAccessFilter resourceAccessFilter) {
        return builder.routes()
                .route(p -> p
                        .path("/parceiros/magalu")
                        .filters(f -> f.addRequestHeader("gw-correlation", "gw-1122")
                                .filter(authorizationFilter.apply(new AuthorizationFilter.Config()))
                                .filter(resourceAccessFilter.apply(new ResourceAccessFilter.Config())))
                        .uri(partnerUrl)
                )
                .route(p -> p
                        .path("/parceiros/gpa")
                        .filters(f -> f.addRequestHeader("gw-correlation", "gw-1122")
                                      .filter(authorizationFilter.apply(new AuthorizationFilter.Config()))
                                      .filter(resourceAccessFilter.apply(new ResourceAccessFilter.Config())))
                        .uri(partnerUrl)
                ).build();
    }

}
