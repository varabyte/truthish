package com.varabyte.truthish.util.diff

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import com.varabyte.truthish.util.diff.LineDiff.Line.Added
import com.varabyte.truthish.util.diff.LineDiff.Line.Kept
import com.varabyte.truthish.util.diff.LineDiff.Line.Removed
import kotlin.test.Test

class LineDiffTest {
    @Test
    fun identicalStringsKeepEveryLine() {
        assertThat(LineDiff("a\nb\nc", "a\nb\nc").lines)
            .containsExactly(Kept("a"), Kept("b"), Kept("c"))
            .inOrder()
    }

    @Test
    fun linesAreSplitOnEitherLineEnding() {
        assertThat(LineDiff("a\r\nb", "a\nb").lines)
            .containsExactly(Kept("a"), Kept("b"))
            .inOrder()
    }

    @Test
    fun changedLineIsRemovedThenAdded() {
        assertThat(LineDiff("a\nb\nc", "a\nB\nc").lines)
            .containsExactly(Kept("a"), Removed("b"), Added("B"), Kept("c"))
            .inOrder()
    }

    @Test
    fun addedAndRemovedLines() {
        assertThat(LineDiff("a\nb\nc", "a\nc\nd").lines)
            .containsExactly(Kept("a"), Removed("b"), Kept("c"), Added("d"))
            .inOrder()
    }

    @Test
    fun movedLineIsRemovedAndAddedWhileTheOthersAreKept() {
        assertThat(LineDiff("a\nb\nc\nd", "a\nc\nd\nb").lines)
            .containsExactly(Kept("a"), Removed("b"), Kept("c"), Kept("d"), Added("b"))
            .inOrder()
    }

    @Test
    fun changedBlockRemovesAllItsLinesBeforeAddingAny() {
        assertThat(LineDiff("a\nb\nc\nd", "a\nB\nC\nd").lines)
            .containsExactly(Kept("a"), Removed("b"), Removed("c"), Added("B"), Added("C"), Kept("d"))
            .inOrder()
    }

    @Test
    fun completelyDifferentStringsRemoveEveryLineThenAddEveryLine() {
        assertThat(LineDiff("a\nb", "c\nd").lines)
            .containsExactly(Removed("a"), Removed("b"), Added("c"), Added("d"))
            .inOrder()
    }

    @Test
    fun shorterExpectedString() {
        assertThat(LineDiff("a\nb", "a\nb\nc\nd\ne").lines)
            .containsExactly(Kept("a"), Kept("b"), Added("c"), Added("d"), Added("e"))
            .inOrder()

        assertThat(LineDiff("a\ne", "a\nb\nc\nd\ne").lines)
            .containsExactly(Kept("a"), Added("b"), Added("c"), Added("d"), Kept("e"))
            .inOrder()
    }

    @Test
    fun shorterActualString() {
        assertThat(LineDiff("a\nb\nc\nd\ne", "a\nb").lines)
            .containsExactly(Kept("a"), Kept("b"), Removed("c"), Removed("d"), Removed("e"))
            .inOrder()

        assertThat(LineDiff("a\nb\nc\nd\ne", "a\ne").lines)
            .containsExactly(Kept("a"), Removed("b"), Removed("c"), Removed("d"), Kept("e"))
            .inOrder()
    }

    @Test
    fun extraCopyOfARepeatedLineIsAdded() {
        assertThat(LineDiff("a\na", "a\na\na").lines)
            .containsExactly(Kept("a"), Kept("a"), Added("a"))
            .inOrder()
    }

    @Test
    fun hugeChangedRegionIsNotDiffed() {
        // Changing the first and last lines leaves 1001 x 1001 lines to compare, which is over the table size limit
        val expected = (1..1001).joinToString("\n")
        val actual = buildList {
            add("first")
            (2..1000).mapTo(this) { "$it" }
            add("last")
        }.joinToString("\n")

        assertThat(LineDiff.tryCreate(expected, actual)).isNull()
        assertThrows<IllegalArgumentException> { LineDiff(expected, actual) }
    }

    @Test
    fun emptyExpectedString() {
        assertThat(LineDiff("", "a\nb").lines)
            .containsExactly(Added("a"), Added("b"))
            .inOrder()
    }

    @Test
    fun emptyActualString() {
        assertThat(LineDiff("a\nb", "").lines)
            .containsExactly(Removed("a"), Removed("b"))
            .inOrder()
    }

    @Test
    fun finalNewlineDoesNotStartAnotherLine() {
        assertThat(LineDiff("a\nb\n", "a\nb").lines)
            .containsExactly(Kept("a"), Kept("b"))
            .inOrder()
    }

    @Test
    fun emptyLinesAreLines() {
        assertThat(LineDiff("\n\n\n", "a\nb\nc\n").lines)
            .containsExactly(Removed(""), Removed(""), Removed(""), Added("a"), Added("b"), Added("c"))
            .inOrder()
    }

    @Test
    fun ignoringCaseKeepsLinesThatDifferOnlyInCase() {
        // A kept line always picks the expected line's text
        assertThat(LineDiff("a\nb\nc", "A\nB\nd", ignoreCase = true).lines)
            .containsExactly(Kept("a"), Kept("b"), Removed("c"), Added("d"))
            .inOrder()
    }

    @Test
    fun ignoringCaseAlsoKeepsLinesBetweenChanges() {
        assertThat(LineDiff("a\nb\nc", "x\nB\ny", ignoreCase = true).lines)
            .containsExactly(Removed("a"), Added("x"), Kept("b"), Removed("c"), Added("y"))
            .inOrder()
    }
}
