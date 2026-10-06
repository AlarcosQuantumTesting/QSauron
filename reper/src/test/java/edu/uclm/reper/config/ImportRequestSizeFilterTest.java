package edu.uclm.reper.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class ImportRequestSizeFilterTest {

    private ImportRequestSizeFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ImportRequestSizeFilter(33554432); // 32 MB
    }

    @Test
    void testPost33MbTooLargeReturns413() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/projects/import");
        request.addHeader("Content-Length", 33L * 1024 * 1024);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(413, response.getStatus());
        assertTrue(response.getContentAsString().contains("TOTAL_TOO_LARGE"));
    }

    @Test
    void testPost7MbPassesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/projects/import");
        request.addHeader("Content-Length", 7L * 1024 * 1024);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    void testPostSmallPassesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/projects/import");
        request.addHeader("Content-Length", 1024);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    void testGetFormatsPassesFilter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/projects/import/formats");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }
}
