package br.com.lteixeira.apigateway.function;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JWTDecoderFunction {

    public static String getResourceAccess(final String token) {

        final DecodedJWT jwt = JWT.decode(token);

        return jwt.getClaim("azp").asString();

    }
}
