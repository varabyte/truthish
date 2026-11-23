package com.varabyte.truthish.failure

/**
 * A run of consecutive elements in a truncated collection, either shown or hidden.
 */
internal sealed interface TruncatedPart<out T> {
    data class Shown<T>(val values: List<T>) : TruncatedPart<T>
    data class Hidden(val count: Int) : TruncatedPart<Nothing>
}

/**
 * Shortens long collections so a report stays readable.
 *
 * The shown elements are kept in one window around each anchor, an index that must stay visible (e.g. where two lists
 * first differ). Two anchors on the first and last index show both ends of the collection.
 */
internal class Truncator(private val maxElements: Int) {
    /**
     * Truncates [values] to a window around [anchor], or keeps them whole if they fit in the limit.
     */
    fun <T> truncate(values: List<T>, anchor: Int): List<TruncatedPart<T>> {
        if (values.size <= maxElements) return listOf(TruncatedPart.Shown(values))
        return split(values, listOf(findWindow(values.size, anchor, maxElements)))
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
