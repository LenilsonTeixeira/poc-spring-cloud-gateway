package br.com.lteixeira.apigateway.filter;

import br.com.lteixeira.apigateway.function.JWTDecoderFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ResourceAccessFilter extends AbstractGatewayFilterFactory<ResourceAccessFilter.Config> {

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {

            final String authenticateId= exchange.getRequest().getHeaders().getFirst("authenticate_id");

            log.info("Token authenticate_id: {}", authenticateId);

            final String infoJwt = JWTDecoderFunction.getResourceAccess(authenticateId);

            log.info("Valor recuperado do JWT: {}", infoJwt);

            if(!getPath(exchange.getRequest()).contains(infoJwt)) {
                return onError(exchange, HttpStatus.FORBIDDEN);
            }

            return chain.filter(exchange);
        });
    }

    private Mono<Void> onError(final ServerWebExchange serverWebExchange, final HttpStatus httpStatus) {

        final ServerHttpResponse serverHttpResponse = serverWebExchange.getResponse();

        serverHttpResponse.setStatusCode(httpStatus);

        return serverHttpResponse.setComplete();
    }

    private String getPath(final ServerHttpRequest serverHttpRequest) {
        return serverHttpRequest.getPath().toString();
    }

    public static class Config { }
}
