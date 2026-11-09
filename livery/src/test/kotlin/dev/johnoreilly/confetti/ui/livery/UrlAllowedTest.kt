package dev.johnoreilly.confetti.ui.livery

import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class UrlAllowedTest {
    private val hosts = setOf("droidcon.com", "kotlinconf.com")

    @Test fun allowsTheHostsAndTheirSubdomains() {
        assertTrue(urlAllowed("https://droidcon.com/party", hosts))
        assertTrue(urlAllowed("https://london.droidcon.com/party?x=1", hosts))
        assertTrue(urlAllowed("https://KotlinConf.com:443/workshops", hosts))
    }

    @Test fun judgesTheHostTheUrlReallyReaches() {
        assertFalse(urlAllowed("https://evil.example#.droidcon.com", hosts))
        assertFalse(urlAllowed("https://evil.example?.droidcon.com", hosts))
        assertFalse(urlAllowed("https://evil.example/.droidcon.com", hosts))
        assertFalse(urlAllowed("https://droidcon.com@evil.example/", hosts))
        assertFalse(urlAllowed("https://evil.example\\.droidcon.com", hosts))
        assertFalse(urlAllowed("https://notdroidcon.com/", hosts))
    }

    @Test fun httpsOnly() {
        assertFalse(urlAllowed("http://droidcon.com/", hosts))
        assertFalse(urlAllowed("javascript:alert(1)//droidcon.com", hosts))
    }
}
