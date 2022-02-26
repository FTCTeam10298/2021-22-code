package us.brainstormz.utils

class MathHelps {
    fun scaleBetween(value: Double, range1: ClosedRange<Double>, range2: ClosedRange<Double>): Double {
        val range1Total = range1.endInclusive - range1.start
        val range2Total = range2.endInclusive - range2.start

        return (((value - range1.start) * range2Total) / range1Total) + range2.start
    }
    fun posOrNeg(num: Double): Int {
        return when {
            num > 0 -> 1
            num < 0 -> -1
            else -> 0
        }
    }
}