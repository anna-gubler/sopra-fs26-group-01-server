package ch.uzh.ifi.hase.soprafs26.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionAdviceTest {

    private GlobalExceptionAdvice advice;
    private WebRequest webRequest;
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        advice = new GlobalExceptionAdvice();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test");
        httpServletRequest = request;
        webRequest = new ServletWebRequest(request);
    }

    @Test
    void handleConflict_illegalArgumentException_returnsConflict() {
        IllegalArgumentException ex = new IllegalArgumentException("bad argument");

        ResponseEntity<Object> response = advice.handleConflict(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleConflict_illegalStateException_returnsConflict() {
        IllegalStateException ex = new IllegalStateException("bad state");

        ResponseEntity<Object> response = advice.handleConflict(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleTransactionSystemException_returnsConflict() {
        TransactionSystemException ex = new TransactionSystemException("tx error");

        ResponseStatusException result = advice.handleTransactionSystemException(ex, httpServletRequest);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
    }

    @Test
    void handleInternalServerError_returnsInternalServerError() {
        HttpServerErrorException.InternalServerError ex =
                (HttpServerErrorException.InternalServerError) HttpServerErrorException.create(
                        HttpStatus.INTERNAL_SERVER_ERROR, "server error", null, null, null);

        ResponseStatusException result = advice.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }
}
