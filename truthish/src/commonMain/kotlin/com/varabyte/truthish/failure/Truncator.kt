package com.varabyte.truthish.failure

/**
 * A run of consecutive elements in a truncated collection, either shown or hidden.
 */
internal sealed interface TruncatedPart<out T> {
    data class Shown<T>(val values: List<T>) : TruncatedPart<T>
    data class Hidden(val count: Int) : TruncatedPart<Nothing>
}

/**
 * How to shorten a long value so a report stays readable.
 */
sealed interface Truncation {
    /** Shows the whole value, e.g. for a diff viewer, which can handle long values better than a report. */
    object None : Truncation

    /** Keeps the start and end of a long collection. */
    object BothEnds : Truncation

    /**
     * Keeps a window at [firstAnchor], and another at [secondAnchor] if set.
     *
     * Two anchors result in two windows, each window sharing half of the amount of allowed elements, the first getting
     * any extra element. Windows that meet become one window instead.
     */
    class Anchored(val firstAnchor: Anchor, val secondAnchor: Anchor?) : Truncation {
        constructor(anchor: Anchor) : this(anchor, null)
    }
}

/**
 * Where to truncate a long list, for a window of elements that must stay visible (e.g. where two lists first differ).
 */
sealed interface Anchor {
    /** The index the window is relative to. */
    val index: Int

    /**
     * A window centered on [index].
     *
     * If the window can't fit the same number of elements on both sides of [index] (e.g. a limit of 10 leaves 9 spots
     * besides [index] itself), the extra element goes toward whichever end of the list [index] is closer to.
     *
     * If [index] is too close to the start or end of the list to center on, the window starts or ends there instead,
     * so it still shows the full number of elements. For example, with a limit of 11, `CenteredOn(2)` shows elements 0
     * through 10 instead of 0 through 7.
     *
     * @param listSizeOverride Place the window as if the list had this many elements, e.g. the size of the longer of
     *   two lists being compared, so both get the same window.
     */
    // NOTE: `listSizeOverride` is internal because we don't expect a third-party use case for it. We can make it public
    // later if we become aware of one.
    class CenteredOn internal constructor(override val index: Int, internal val listSizeOverride: Int?) : Anchor {
        constructor(index: Int) : this(index, listSizeOverride = null)
    }

    /**
     * A window whose last element is [index], or the end of the list if it's shorter.
     */
    class EndingAt(override val index: Int) : Anchor
}

/**
 * Shortens long collections (or strings, as lists of characters) so a report stays readable.
 *
 * The shown elements are kept in a window at each [Anchor].
 */
internal class Truncator(val maxElements: Int) {
    companion object {
        /**
         * The truncator reports use, which shows at most 11 elements of a collection.
         */
        // NOTE: An odd number was intentionally chosen so that truncations that happen around an anchor can show the
        // same number of elements before and after.
        val forReports = Truncator(maxElements = 11)

        /**
         * The truncator reports use for strings, which shows at most 100 characters.
         */
        val forStrings = Truncator(maxElements = 100)
    }

    /**
     * Truncates [values] to a window at [anchor].
     *
     * Truncation will not happen if the whole list fits into the element limit.
     */
    fun <T> truncate(values: List<T>, anchor: Anchor) = truncate(values, anchor, null)

    /**
     * Truncates [values] to a window at [firstAnchor], and another at [secondAnchor] if set.
     *
     * Truncation will not happen if the whole list fits into the element limit.
     *
     * Two windows split the limit, [firstAnchor] getting any extra element. Windows that meet become one window
     * instead. The anchors can be in either order.
     */
    fun <T> truncate(values: List<T>, firstAnchor: Anchor, secondAnchor: Anchor?): List<TruncatedPart<T>> {
        if (values.size <= maxElements) return listOf(TruncatedPart.Shown(values))

        if (secondAnchor == null) {
            val window = firstAnchor.findWindow(values.size, maxElements)
            return split(values, listOf(window.first..minOf(window.last, values.lastIndex)))
        }

        val (earlier, later) = listOf(
            firstAnchor.findWindow(values.size, (maxElements + 1) / 2),
            secondAnchor.findWindow(values.size, maxElements / 2),
        ).sortedBy { it.first }
        if (later.first > earlier.last + 1) {
            return split(values, listOf(earlier, later).map { it.first..minOf(it.last, values.lastIndex) })
        }

        // The windows overlap or touch, so one full-size window starting at the earlier one can hold them both
        val start = earlier.first.coerceAtMost(values.size - maxElements)
        return split(values, listOf(start until start + maxElements))
    }

    /**
     * Finds the window of [count] indices at this anchor in a collection of [size] elements.
     */
    private fun Anchor.findWindow(size: Int, count: Int): IntRange = when (this) {
        is Anchor.CenteredOn -> findCenteredWindow(listSizeOverride ?: size, index, count)
        is Anchor.EndingAt -> {
            val start = (index - count + 1).coerceAtLeast(0)
            start until start + count
        }
    }

    /**
     * Finds the window of [count] indices around [anchor] in a collection of [size] elements.
     *
     * Near either end of the collection, the window slides inward so it still has [count] indices. When the anchor
     * can't sit exactly in the middle, the extra index goes toward the nearer end, so early anchors are biased to the
     * start of the collection and later anchors to the end.
     */
    private fun findCenteredWindow(size: Int, anchor: Int, count: Int): IntRange {
        val others = count - 1
        val isInFirstHalf = anchor <= (size - 1) / 2
        val before = if (isInFirstHalf) (others + 1) / 2 else others / 2
        val start = (anchor - before).coerceIn(0, size - count)
        return start until start + count
    }

    // Splits `values` into the given ranges, in increasing order, with the gaps between them hidden
    private fun <T> split(values: List<T>, shownRanges: List<IntRange>): List<TruncatedPart<T>> = buildList {
        var nextIndex = 0
        for (range in shownRanges) {
            if (range.first > nextIndex) add(TruncatedPart.Hidden(range.first - nextIndex))
            add(TruncatedPart.Shown(values.subList(range.first, range.last + 1)))
            nextIndex = range.last + 1
        }
        if (nextIndex < values.size) add(TruncatedPart.Hidden(values.size - nextIndex))
    }
}
