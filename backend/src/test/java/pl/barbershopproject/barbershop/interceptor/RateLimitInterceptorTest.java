package pl.barbershopproject.barbershop.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import pl.barbershopproject.barbershop.annotation.RateLimited;

import java.lang.reflect.Method;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitInterceptorTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() {
        rateLimitInterceptor = new RateLimitInterceptor(redisTemplate);
    }

    @Test
    void preHandle_AllowsRequest_WhenHandlerIsNotHandlerMethod() throws Exception {
        // given
        MockHttpServletRequest request = createRequest("/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

        // then
        assertTrue(result);
        assertEquals(200, response.getStatus());

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void preHandle_AllowsRequest_WhenMethodIsNotRateLimited() throws Exception {
        // given
        MockHttpServletRequest request = createRequest("/not-limited");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = createHandlerMethod("notLimitedEndpoint");

        // when
        boolean result = rateLimitInterceptor.preHandle(request, response, handlerMethod);

        // then
        assertTrue(result);
        assertEquals(200, response.getStatus());

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void preHandle_AllowsRequestAndSetsExpiration_WhenFirstRequestIsWithinLimit() throws Exception {
        // given
        MockHttpServletRequest request = createRequest("/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = createHandlerMethod("limitedEndpoint");

        String expectedKey = "rate_limit:127.0.0.1:/login";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(expectedKey)).thenReturn(1L);

        // when
        boolean result = rateLimitInterceptor.preHandle(request, response, handlerMethod);

        // then
        assertTrue(result);
        assertEquals(200, response.getStatus());

        verify(valueOperations).increment(expectedKey);
        verify(redisTemplate).expire(expectedKey, Duration.ofSeconds(60));
        verify(redisTemplate, never()).getExpire(anyString());
    }

    @Test
    void preHandle_AllowsRequestWithoutSettingExpiration_WhenRequestIsNotFirstAndStillWithinLimit() throws Exception {
        // given
        MockHttpServletRequest request = createRequest("/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = createHandlerMethod("limitedEndpoint");

        String expectedKey = "rate_limit:127.0.0.1:/login";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(expectedKey)).thenReturn(2L);

        // when
        boolean result = rateLimitInterceptor.preHandle(request, response, handlerMethod);

        // then
        assertTrue(result);
        assertEquals(200, response.getStatus());

        verify(valueOperations).increment(expectedKey);
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
        verify(redisTemplate, never()).getExpire(anyString());
    }

    @Test
    void preHandle_BlocksRequestAndSetsRetryAfterHeader_WhenLimitIsExceeded() throws Exception {
        // given
        MockHttpServletRequest request = createRequest("/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = createHandlerMethod("limitedEndpoint");

        String expectedKey = "rate_limit:127.0.0.1:/login";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(expectedKey)).thenReturn(3L);
        when(redisTemplate.getExpire(expectedKey)).thenReturn(42L);

        // when
        boolean result = rateLimitInterceptor.preHandle(request, response, handlerMethod);

        // then
        assertFalse(result);
        assertEquals(429, response.getStatus());
        assertEquals("42", response.getHeader("Retry-After"));
        assertEquals("text/plain;charset=UTF-8", response.getContentType());
        assertTrue(response.getContentAsString().contains("Przekroczono limit zapytań."));

        verify(valueOperations).increment(expectedKey);
        verify(redisTemplate).getExpire(expectedKey);
    }

    @Test
    void preHandle_UsesRateLimitWindowAsRetryAfter_WhenRedisExpireTimeIsUnavailable() throws Exception {
        // given
        MockHttpServletRequest request = createRequest("/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = createHandlerMethod("limitedEndpoint");

        String expectedKey = "rate_limit:127.0.0.1:/login";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(expectedKey)).thenReturn(3L);
        when(redisTemplate.getExpire(expectedKey)).thenReturn(-1L);

        // when
        boolean result = rateLimitInterceptor.preHandle(request, response, handlerMethod);

        // then
        assertFalse(result);
        assertEquals(429, response.getStatus());
        assertEquals("60", response.getHeader("Retry-After"));
        assertTrue(response.getContentAsString().contains("Przekroczono limit zapytań."));

        verify(valueOperations).increment(expectedKey);
        verify(redisTemplate).getExpire(expectedKey);
    }

    @Test
    void preHandle_UsesFirstIpFromXForwardedForHeader_WhenHeaderExists() throws Exception {
        // given
        MockHttpServletRequest request = createRequest("/login");
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = createHandlerMethod("limitedEndpoint");

        String expectedKey = "rate_limit:203.0.113.10:/login";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(expectedKey)).thenReturn(1L);

        // when
        boolean result = rateLimitInterceptor.preHandle(request, response, handlerMethod);

        // then
        assertTrue(result);
        assertEquals(200, response.getStatus());

        verify(valueOperations).increment(expectedKey);
        verify(redisTemplate).expire(expectedKey, Duration.ofSeconds(60));
    }

    private static MockHttpServletRequest createRequest(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setRemoteAddr("127.0.0.1");

        return request;
    }

    private static HandlerMethod createHandlerMethod(String methodName) throws NoSuchMethodException {
        TestController testController = new TestController();
        Method method = TestController.class.getMethod(methodName);

        return new HandlerMethod(testController, method);
    }

    @SuppressWarnings("unused")
    private static class TestController {

        @RateLimited(limit = 2)
        public void limitedEndpoint() {
            // Intentionally empty. This method is used only as HandlerMethod metadata in interceptor tests.
        }

        public void notLimitedEndpoint() {
            // Intentionally empty. This method is used only as HandlerMethod metadata in interceptor tests.
        }
    }
}