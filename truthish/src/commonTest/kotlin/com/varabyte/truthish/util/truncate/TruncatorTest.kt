package com.varabyte.truthish.util.truncate

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.util.truncate.Anchor.CenteredOn
import com.varabyte.truthish.util.truncate.Anchor.EndingAt
import com.varabyte.truthish.util.truncate.TruncatedPart.Hidden
import com.varabyte.truthish.util.truncate.TruncatedPart.Shown
import kotlin.test.Test

class TruncatorTest {
    private val zeroToThree = (0..3).toList()
    private val zeroToNinetyNine = (0..99).toList()

    @Test
    fun collectionsThatFitAreShownWhole() {
        val truncator = Truncator(maxElements = 4)

        assertThat(truncator.truncate(zeroToThree, CenteredOn(0), CenteredOn(3))).containsExactly(Shown(listOf(0, 1, 2, 3)))
        assertThat(truncator.truncate(zeroToThree, CenteredOn(2))).containsExactly(Shown(listOf(0, 1, 2, 3)))
    }

    @Test
    fun oddLimitCentersOnAnchor() {
        val truncator = Truncator(maxElements = 11)

        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(49)))
            .containsExactly(Hidden(44), Shown(listOf(44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54)), Hidden(45))
            .inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(50)))
            .containsExactly(Hidden(45), Shown(listOf(45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55)), Hidden(44))
            .inOrder()
    }

    @Test
    fun evenLimitLeansTowardTheNearerEnd() {
        val truncator = Truncator(maxElements = 4)

        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(4)))
            .containsExactly(Hidden(2), Shown(listOf(2, 3, 4, 5)), Hidden(94)).inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(49)))
            .containsExactly(Hidden(47), Shown(listOf(47, 48, 49, 50)), Hidden(49)).inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(50)))
            .containsExactly(Hidden(49), Shown(listOf(49, 50, 51, 52)), Hidden(47)).inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(94)))
            .containsExactly(Hidden(93), Shown(listOf(93, 94, 95, 96)), Hidden(3)).inOrder()
    }

    @Test
    fun windowsSlideInwardNearTheEnds() {
        val truncator = Truncator(maxElements = 4)

        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(0)))
            .containsExactly(Shown(listOf(0, 1, 2, 3)), Hidden(96)).inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(1)))
            .containsExactly(Shown(listOf(0, 1, 2, 3)), Hidden(96)).inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(98)))
            .containsExactly(Hidden(96), Shown(listOf(96, 97, 98, 99))).inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(99)))
            .containsExactly(Hidden(96), Shown(listOf(96, 97, 98, 99))).inOrder()
    }

    @Test
    fun twoAnchorsShowTwoSeparateWindows() {
        val evenTruncator = Truncator(maxElements = 4)
        val oddTruncator = Truncator(maxElements = 11)

        assertThat(evenTruncator.truncate(zeroToNinetyNine, CenteredOn(0), CenteredOn(99)))
            .containsExactly(Shown(listOf(0, 1)), Hidden(96), Shown(listOf(98, 99))).inOrder()
        assertThat(oddTruncator.truncate(zeroToNinetyNine, CenteredOn(0), CenteredOn(99)))
            .containsExactly(Shown(listOf(0, 1, 2, 3, 4, 5)), Hidden(89), Shown(listOf(95, 96, 97, 98, 99)))
            .inOrder()
        assertThat(evenTruncator.truncate(zeroToNinetyNine, CenteredOn(20), CenteredOn(80)))
            .containsExactly(Hidden(19), Shown(listOf(19, 20)), Hidden(59), Shown(listOf(80, 81)), Hidden(18))
            .inOrder()
    }

    @Test
    fun twoAnchorsCanBeOfDifferentKinds() {
        val truncator = Truncator(maxElements = 4)

        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(20), EndingAt(80)))
            .containsExactly(Hidden(19), Shown(listOf(19, 20)), Hidden(58), Shown(listOf(79, 80)), Hidden(19))
            .inOrder()
    }

    @Test
    fun anchorsCanBeInEitherOrder() {
        val truncator = Truncator(maxElements = 5)

        // The first anchor still gets the extra element
        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(80), CenteredOn(20)))
            .containsExactly(Hidden(19), Shown(listOf(19, 20)), Hidden(58), Shown(listOf(79, 80, 81)), Hidden(18))
            .inOrder()
    }

    @Test
    fun windowsThatMeetAreMerged() {
        val truncator = Truncator(maxElements = 4)

        // These windows touch without overlapping
        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(10), CenteredOn(12)))
            .containsExactly(Hidden(9), Shown(listOf(9, 10, 11, 12)), Hidden(87)).inOrder()
        // These windows overlap
        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(10), CenteredOn(11)))
            .containsExactly(Hidden(9), Shown(listOf(9, 10, 11, 12)), Hidden(87)).inOrder()
    }

    @Test
    fun listSizeOverridePlacesTheWindowAsIfTheListWereLonger() {
        val truncator = Truncator(maxElements = 5)
        val zeroToTwentyNine = (0..29).toList()

        // Near the end of the list, the window slides inward
        assertThat(truncator.truncate(zeroToTwentyNine, CenteredOn(28)))
            .containsExactly(Hidden(25), Shown(listOf(25, 26, 27, 28, 29))).inOrder()

        // A longer list compared with it has room to center the window...
        assertThat(truncator.truncate(zeroToNinetyNine, CenteredOn(28)))
            .containsExactly(Hidden(26), Shown(listOf(26, 27, 28, 29, 30)), Hidden(69)).inOrder()
        // ...so the shorter list keeps the same window and shows only the part of it that it has
        assertThat(truncator.truncate(zeroToTwentyNine, CenteredOn(28, listSizeOverride = 100)))
            .containsExactly(Hidden(26), Shown(listOf(26, 27, 28, 29))).inOrder()
    }

    @Test
    fun endingAtShowsTheElementsUpToTheIndex() {
        val truncator = Truncator(maxElements = 4)

        assertThat(truncator.truncate(zeroToNinetyNine, EndingAt(10)))
            .containsExactly(Hidden(7), Shown(listOf(7, 8, 9, 10)), Hidden(89)).inOrder()
        assertThat(truncator.truncate(zeroToNinetyNine, EndingAt(2)))
            .containsExactly(Shown(listOf(0, 1, 2, 3)), Hidden(96)).inOrder()
        // A list that ends before the index shows its last elements
        assertThat(truncator.truncate((0..9).toList(), EndingAt(10)))
            .containsExactly(Hidden(7), Shown(listOf(7, 8, 9))).inOrder()
    }
}
