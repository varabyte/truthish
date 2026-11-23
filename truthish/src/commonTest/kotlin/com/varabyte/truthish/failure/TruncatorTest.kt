package com.varabyte.truthish.failure

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.failure.TruncatedPart.Hidden
import com.varabyte.truthish.failure.TruncatedPart.Shown
import kotlin.test.Test

class TruncatorTest {
    private val zeroToThree = (0..3).toList()
    private val zeroToNinetyNine = (0..99).toList()

    @Test
    fun collectionsThatFitAreShownWhole() {
        val truncator = Truncator(maxElements = 4)

        assertThat(truncator.truncate(zeroToThree, 0, 3)).containsExactly(Shown(listOf(0, 1, 2, 3)))
        assertThat(truncator.truncate(zeroToThree, 2)).containsExactly(Shown(listOf(0, 1, 2, 3)))
    }

    @Test
    fun oddLimitCentersOnAnchor() {
        val truncator = Truncator(maxElements = 11)

        assertThat(truncator.truncate(zeroToNinetyNine, 49))
            .containsExactly(Hidden(44), Shown(listOf(44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54)), Hidden(45))
            .inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, 50))
            .containsExactly(Hidden(45), Shown(listOf(45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55)), Hidden(44))
            .inOrder()
    }

    @Test
    fun evenLimitLeansTowardTheNearerEnd() {
        val truncator = Truncator(maxElements = 4)

        assertThat(truncator.truncate(zeroToNinetyNine, 4))
            .containsExactly(Hidden(2), Shown(listOf(2, 3, 4, 5)), Hidden(94)).inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, 49))
            .containsExactly(Hidden(47), Shown(listOf(47, 48, 49, 50)), Hidden(49)).inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, 50))
            .containsExactly(Hidden(49), Shown(listOf(49, 50, 51, 52)), Hidden(47)).inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, 94))
            .containsExactly(Hidden(93), Shown(listOf(93, 94, 95, 96)), Hidden(3)).inOrder()
    }

    @Test
    fun windowsSlideInwardNearTheEnds() {
        val truncator = Truncator(maxElements = 4)

        assertThat(truncator.truncate(zeroToNinetyNine, 0))
            .containsExactly(Shown(listOf(0, 1, 2, 3)), Hidden(96)).inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, 1))
            .containsExactly(Shown(listOf(0, 1, 2, 3)), Hidden(96)).inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, 98))
            .containsExactly(Hidden(96), Shown(listOf(96, 97, 98, 99))).inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, 99))
            .containsExactly(Hidden(96), Shown(listOf(96, 97, 98, 99))).inOrder()
    }

    @Test
    fun twoAnchorsShowTwoSeparateWindows() {
        val evenTruncator = Truncator(maxElements = 4)
        val oddTruncator = Truncator(maxElements = 11)

        assertThat(evenTruncator.truncate(zeroToNinetyNine, 0, 99))
            .containsExactly(Shown(listOf(0, 1)), Hidden(96), Shown(listOf(98, 99))).inOrder()
        assertThat(oddTruncator.truncate(zeroToNinetyNine, 0, 99))
            .containsExactly(Shown(listOf(0, 1, 2, 3, 4, 5)), Hidden(89), Shown(listOf(95, 96, 97, 98, 99)))
            .inOrder()
        assertThat(evenTruncator.truncate(zeroToNinetyNine, 20, 80))
            .containsExactly(Hidden(19), Shown(listOf(19, 20)), Hidden(59), Shown(listOf(80, 81)), Hidden(18))
            .inOrder()
    }

    @Test
    fun windowsThatMeetAreMerged() {
        val truncator = Truncator(maxElements = 4)

        // The windows touch without overlapping
        assertThat(truncator.truncate(zeroToNinetyNine, 10, 12))
            .containsExactly(Hidden(9), Shown(listOf(9, 10, 11, 12)), Hidden(87)).inOrder()
        // The windows overlap
        assertThat(truncator.truncate(zeroToNinetyNine, 10, 11))
            .containsExactly(Hidden(9), Shown(listOf(9, 10, 11, 12)), Hidden(87)).inOrder()
    }
}
