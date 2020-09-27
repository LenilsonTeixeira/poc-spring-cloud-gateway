package br.com.lteixeira.apigateway.filter;

import br.com.lteixeira.apigateway.representation.AccessToken;
import br.com.lteixeira.apigateway.representation.GrantType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    @Value("${authorization.url}")
    private String authorizationUrl;

    @Override
    public GatewayFilter apply(Config config) {
        return (((exchange, chain) -> {

            final String clientId = getClientId(exchange.getRequest());

            final String clientSecret = getClientSecret(exchange.getRequest());

            return WebClient.create(authorizationUrl)
                    .post()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .bodyValue(buildRequestBody(clientId, clientSecret, GrantType.CLIENT_CREDENTIALS))
                    .retrieve()
                    .bodyToMono((AccessToken.class))
                    .flatMap(tokenResponse -> {

                        log.info("Autenticação realizada com sucesso no Keycloak.");

                        log.info("Access Token : {} ", tokenResponse);

                        final ServerHttpRequest serverHttpRequest = exchange.getRequest().mutate().header("authenticate_id", tokenResponse.getAccessToken()).build();

                        exchange.mutate().request(serverHttpRequest);

                        return chain.filter(exchange);

                    }).onErrorResume(throwable -> onError(exchange, HttpStatus.UNAUTHORIZED));

        }));

    }

    private Mono<Void> onError(final ServerWebExchange serverWebExchange, final HttpStatus httpStatus) {

        final ServerHttpResponse serverHttpResponse = serverWebExchange.getResponse();

        serverHttpResponse.setStatusCode(httpStatus);

        return serverHttpResponse.setComplete();
    }

    private String getClientId(final ServerHttpRequest serverHttpRequest) {
        return serverHttpRequest.getHeaders().getFirst("client_id");
    }

    private String getClientSecret(final ServerHttpRequest serverHttpRequest) {
        return serverHttpRequest.getHeaders().getFirst("client_secret");
    }

    private String buildRequestBody(final String clientId, final String secret, final String grantType) {
        return new StringBuilder("grant_type")
                .append("=")
                .append(grantType)
                .append("&")
                .append("client_id")
                .append("=")
                .append(clientId)
                .append("&")
                .append("client_secret")
                .append("=")
                .append(secret).toString();
    }

    public static class Config { }

}
