package com.PayMoney.service;

import com.PayMoney.Service.RateLimitFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private FilterChain filterChain;

    @Test
    void doFilter_shouldPassThrough_whenPathIsNotRateLimited() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(redisTemplate);

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/v1/wallet");
        request.setServletPath("/api/v1/wallet");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void doFilter_shouldReturn503_whenRedisGivesNoRequestCount() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(redisTemplate);

        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setServletPath("/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertEquals(503, response.getStatus());
        verifyNoInteractions(filterChain);
    }
}
