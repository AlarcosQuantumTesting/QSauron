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
        filter = new ImportRequestSizeFilter(6291456); // 6 MB
    }

    @Test
    void testPostTooLargeReturns413() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/projects/import");
        byte[] largeContent = new byte[7 * 1024 * 1024];
        request.setContent(largeContent);


        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(413, response.getStatus());
        assertTrue(response.getContentAsString().contains("TOTAL_TOO_LARGE"));
    }

    @Test
    void testPostSmallPassesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/projects/import");
        byte[] content = new byte[1024];
        request.setContent(content);
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
