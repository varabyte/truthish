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
internal sealed interface Truncation {
    /** Shows the whole value, e.g. for a diff viewer, which can handle long values better than a report. */
    object None : Truncation

    /** Keeps the start and end of a long collection. */
    object BothEnds : Truncation

    /** Keeps a window at [anchor]. */
    class Anchored(val anchor: Anchor) : Truncation
}

/**
 * Where to truncate a long list, for a window of elements that must stay visible (e.g. where two lists first differ).
 */
internal sealed interface Anchor {
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
    class CenteredOn(val index: Int, val listSizeOverride: Int? = null) : Anchor

    /**
     * A window whose last element is [index], or the end of the list if it's shorter.
     */
    class EndingAt(val index: Int) : Anchor
}

/**
 * Shortens long collections so a report stays readable.
 *
 * The shown elements are kept in a window at an [Anchor], or in two windows at both ends of the collection.
 */
internal class Truncator(val maxElements: Int) {
    companion object {
        /**
         * The truncator reports use, which shows at most 11 elements of a collection.
         */
        // NOTE: An odd number was intentionally chosen so that truncations that happen around an anchor can show the
        // same number of elements before and after.
        val forReports = Truncator(maxElements = 11)
    }

    /**
     * Truncates [values] to a window at [anchor], or keeps them whole if they fit in the limit.
     */
    fun <T> truncate(values: List<T>, anchor: Anchor): List<TruncatedPart<T>> {
        if (values.size <= maxElements) return listOf(TruncatedPart.Shown(values))

        val window = when (anchor) {
            is Anchor.CenteredOn -> findWindow(anchor.listSizeOverride ?: values.size, anchor.index, maxElements)
            is Anchor.EndingAt -> {
                val start = (anchor.index - maxElements + 1).coerceAtLeast(0)
                start until start + maxElements
            }
        }
        return split(values, listOf(window.first..minOf(window.last, values.lastIndex)))
    }

    /**
     * Truncates [values] to one window around each anchor, or keeps them whole if they fit in the limit.
     *
     * The two windows split the limit, the first getting any extra element. Windows that meet become one window
     * instead.
     */
    fun <T> truncate(values: List<T>, firstAnchor: Int, secondAnchor: Int): List<TruncatedPart<T>> {
        require(firstAnchor <= secondAnchor) { "Anchors out of order: $firstAnchor, $secondAnchor" }
        if (values.size <= maxElements) return listOf(TruncatedPart.Shown(values))

        val first = findWindow(values.size, firstAnchor, (maxElements + 1) / 2)
        val second = findWindow(values.size, secondAnchor, maxElements / 2)
        if (second.first > first.last + 1) return split(values, listOf(first, second))

        // The windows overlap or touch, so one full-size window starting at the first one can hold them both
        val start = first.first.coerceAtMost(values.size - maxElements)
        return split(values, listOf(start until start + maxElements))
    }

    /**
     * Finds the window of [count] indices around [anchor] in a collection of [size] elements.
     *
     * Near either end of the collection, the window slides inward so it still has [count] indices. When the anchor
     * can't sit exactly in the middle, the extra index goes toward the nearer end, so early anchors are biased to the
     * start of the collection and later anchors to the end.
     */
    private fun findWindow(size: Int, anchor: Int, count: Int): IntRange {
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
