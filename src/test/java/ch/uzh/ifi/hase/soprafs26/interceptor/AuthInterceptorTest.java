package ch.uzh.ifi.hase.soprafs26.interceptor;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthInterceptorTest {

    private AuthInterceptor authInterceptor;
    private UserService userService;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        userService = mock(UserService.class);
        authInterceptor = new AuthInterceptor(userService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        return user;
    }

    // --- OPTIONS request handling ---

    @Test
    public void givenOptionsRequest_whenPreHandle_thenReturnTrue() {
        when(request.getMethod()).thenReturn("OPTIONS");

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    // --- Missing Authorization header ---

    @Test
    public void givenNullAuthorizationHeader_whenPreHandle_thenThrowUnauthorized() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authInterceptor.preHandle(request, response, new Object()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Missing Authorization header"));
    }

    @Test
    public void givenBlankAuthorizationHeader_whenPreHandle_thenThrowUnauthorized() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("   ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authInterceptor.preHandle(request, response, new Object()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Missing Authorization header"));
    }

    // --- Invalid Authorization header format ---

    @Test
    public void givenInvalidAuthorizationHeaderFormat_whenPreHandle_thenThrowUnauthorized() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authInterceptor.preHandle(request, response, new Object()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Invalid Authorization header"));
    }

    @Test
    public void givenWrongBearerPrefix_whenPreHandle_thenThrowUnauthorized() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Token abc123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authInterceptor.preHandle(request, response, new Object()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Invalid Authorization header"));
    }

    // --- Empty token ---

    @Test
    public void givenEmptyToken_whenPreHandle_thenThrowUnauthorized() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authInterceptor.preHandle(request, response, new Object()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Missing token"));
    }

    @Test
    public void givenBearerWithOnlyWhitespace_whenPreHandle_thenThrowUnauthorized() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer    ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authInterceptor.preHandle(request, response, new Object()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Missing token"));
    }

    // --- Valid token ---

    @Test
    public void givenValidAuthorizationHeader_whenPreHandle_thenReturnTrueAndSetUser() {
        User user = buildUser();
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token-123");
        when(userService.getUserByToken("valid-token-123")).thenReturn(user);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(request).setAttribute("authenticatedUser", user);
    }

    @Test
    public void givenValidTokenWithWhitespace_whenPreHandle_thenTrimAndReturnTrue() {
        User user = buildUser();
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer   valid-token-123   ");
        when(userService.getUserByToken("valid-token-123")).thenReturn(user);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(userService).getUserByToken("valid-token-123");
        verify(request).setAttribute("authenticatedUser", user);
    }

    // --- Invalid token (user service throws exception) ---

    @Test
    public void givenInvalidToken_whenPreHandle_thenThrowUnauthorizedFromUserService() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(userService.getUserByToken("invalid-token"))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authInterceptor.preHandle(request, response, new Object()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

}
