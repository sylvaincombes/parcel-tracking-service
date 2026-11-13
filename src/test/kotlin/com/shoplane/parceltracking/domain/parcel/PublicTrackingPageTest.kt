package com.shoplane.parceltracking.domain.parcel

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

class PublicTrackingPageTest {
    @Test
    fun `the link ends with the tracking number`() {
        val link = PublicTrackingPage("https://www.shoplane.example/tracking").linkFor(TrackingNumber.of("CW4400112233FR"))

        assertThat(link).isEqualTo(URI.create("https://www.shoplane.example/tracking/CW4400112233FR"))
    }

    @Test
    fun `a trailing slash in the page url does not double the separator`() {
        val link = PublicTrackingPage("https://www.shoplane.example/tracking/").linkFor(TrackingNumber.of("CW4400112233FR"))

        assertThat(link).isEqualTo(URI.create("https://www.shoplane.example/tracking/CW4400112233FR"))
    }
}
