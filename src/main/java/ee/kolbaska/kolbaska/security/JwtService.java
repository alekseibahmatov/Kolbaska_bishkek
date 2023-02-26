package ee.kolbaska.kolbaska.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Value("${jwt.valid.for}")
    private Integer JWT_VALID;

    @Value("${jwt.issuer}")
    private String JWT_ISSUER;

    public String extractUserEmail(String token) {
        return JWT.decode(token).getSubject();
    }

    public String createToken(UserDetails userDetails) {
        return createToken(new HashMap<>(), userDetails);
    }

    public String createToken(
            Map<String, Object> claims,
            UserDetails userDetails
    ) {
        return JWT
                .create()
                .withPayload(claims)
                .withSubject(userDetails.getUsername())
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + JWT_VALID))
                .withIssuer(JWT_ISSUER)
                .sign(Algorithm.HMAC512(JWT_SECRET));
    }

    private Map<String, Claim> extractClaims(String token) {
        return JWT.decode(token).getClaims();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            JWT.require(Algorithm.HMAC512(JWT_SECRET))
                    .withSubject(userDetails.getUsername())
                    .withIssuer(JWT_ISSUER)
                    .build()
                    .verify(token);

            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaims(token).get("exp").asDate();
    }
}
