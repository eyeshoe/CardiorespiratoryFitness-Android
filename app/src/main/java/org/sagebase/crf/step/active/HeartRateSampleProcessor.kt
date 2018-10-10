package org.sagebase.crf.step.active

//  Copyright © 2018 Sage Bionetworks. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// 1.  Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// 2.  Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation and/or
// other materials provided with the distribution.
//
// 3.  Neither the name of the copyright holder(s) nor the names of any contributors
// may be used to endorse or promote products derived from this software without
// specific prior written permission. No license is granted to the trademarks of
// the copyright holders even if such marks are included in this software.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

/// Frame rates supported by this processor, with the preferred framerate listed first.
val SUPPORTED_FRAME_RATES = arrayOf(60.0)

/// The number of seconds for the window used to calculate the heart rate.
const val HEART_RATE_WINDOW_IN_SECONDS: Double = 10.0

internal class HeartRateSampleProcessor {

    // --- Code ported from Matlab
    private val fs: Double = SUPPORTED_FRAME_RATES.first()              // frames / second
    private val window: Double = HEART_RATE_WINDOW_IN_SECONDS           // seconds
    private val windowLength: Int = Math.round(fs * window).toInt()

    // number of frames in the window
    /// channel, 60fps, 10sec window
    internal fun findHeartRateValues(channel: List<Double>): List<CalculatedHeartRate> {
        val nframes = Math.floor(channel.size.toDouble() / (windowLength.toDouble() / 2.0)).toInt() - 1
        if (nframes < 1) {
            return listOf()
        }
        val output = mutableListOf<CalculatedHeartRate>()
        for (frame_no in 1..nframes) {
            val lower = (1 + ((frame_no - 1) * windowLength / 2)) - 1
            val upper = ((frame_no + 1) * windowLength / 2) - 1
            val currframe = channel.subList(lower, upper + 1).toTypedArray()
            output.add(calculateHeartRate(currframe))
        }
        return output
    }


    internal data class CalculatedHeartRate(val heartRate: Long, val confidence: Double)

    /**
     * For a given window return the calculated heart rate and confidence.
     * @note The calculated heart rate is rounded.
     */
    internal fun calculateHeartRate(input: Array<Double>): CalculatedHeartRate {
        //% Preprocess and find the autocorrelation function
        val filteredValues = bandpassFiltered(input)
        val xCorrValues = xcorr(filteredValues)
        //% To just remove the repeated part of the autocorr function (since it is even)
        val maxRet = maxSplice(xCorrValues)
        val x = maxRet.v2

        //% HR ranges from 40-200 BPM, so consider only that part of the autocorr
        //% function
        val lower = Math.round(60.0 * fs / 200.0).toInt()
        val upper = Math.round(60.0 * fs / 40.0).toInt()
        val retVal = x.zeroReplace(lower-1, upper-1).seekMax()
        val value = retVal.value
        val pos = retVal.index
        val heartRate = Math.round(60.0 * fs / (pos.toDouble() + 1.0))
        return CalculatedHeartRate(heartRate, value / maxRet.maxValue)
    }

    // Used privately in place of a tuple. Does not need to implement hash and equality.
    internal data class MaxAndSplice(val maxValue: Double, val v2: Array<Double>)

    internal fun maxSplice(input: Array<Double>): MaxAndSplice {
        //% To just remove the repeated part of the autocorr function (since it is even)
        val ret = input.seekMax()
        return MaxAndSplice(ret.value, input.copyOfRange(ret.index, input.size))
    }

    internal fun bandpassFiltered(input: Array<Double>): Array<Double> {
        // % Setting no. of samples as per a max HR of 220 BPM
        val nsamples = Math.round(60 * fs / 220).toInt()
        // % b1 = fir1(128,[1/30, 25/30], 'bandpass');
        val b1: Array<Double> = arrayOf(-0.000506610984132016, 0.000281340196104213, -0.000453477478785663, 0.000175433848479960, 5.78571000126717e-19, -0.000200178238070410, 0.000588479261901569, -0.000412615808534457, 0.000832401037231464, -4.84818239396100e-19, 0.000465554741153073, 0.00102165166976478, -0.000118534274769341, 0.00192609062899124, -2.40024436102973e-18, 0.00182952606970045, 0.00135480554590726, 0.000748599044261129, 0.00319643179850945, -2.30788276369201e-19, 0.00382994518525259, 0.00107470141262219, 0.00233017559097417, 0.00376919225339987, -8.21109764793137e-18, 0.00568709829032464, -0.000418547259970266, 0.00430878547299781, 0.00234096774958672, -1.06597329751523e-17, 0.00589948032626289, -0.00345001874823703, 0.00577085280898743, -0.00228532700432350, -3.81044085438483e-18, 0.00263801974428747, -0.00769131382422690, 0.00531148463293734, -0.0104990208677403, 1.62815935886881e-17, -0.00558417076326117, -0.0119241848598587, 0.00134611898423683, -0.0212997771796790, -2.07091826506435e-17, -0.0192845505914200, -0.0139952617851127, -0.00760318790070690, -0.0320397640632609, -3.05719612807051e-18, -0.0378997870775431, -0.0106518977344771, -0.0232807805994706, -0.0382418951609459, 1.64113172833343e-17, -0.0611787321852445, 0.00471988055056295, -0.0517540592057603, -0.0305770938728010, 3.42293636763843e-17, -0.100426633129967, 0.0729786483544900, -0.170609488045242, 0.125861208906484, 0.800308136102957, 0.125861208906484, -0.170609488045242, 0.0729786483544900, -0.100426633129967, 3.42293636763843e-17, -0.0305770938728010, -0.0517540592057603, 0.00471988055056295, -0.0611787321852445, 1.64113172833343e-17, -0.0382418951609459, -0.0232807805994706, -0.0106518977344771, -0.0378997870775431, -3.05719612807051e-18, -0.0320397640632609, -0.00760318790070690, -0.0139952617851127, -0.0192845505914200, -2.07091826506435e-17, -0.0212997771796790, 0.00134611898423683, -0.0119241848598587, -0.00558417076326117, 1.62815935886881e-17, -0.0104990208677403, 0.00531148463293734, -0.00769131382422690, 0.00263801974428747, -3.81044085438483e-18, -0.00228532700432350, 0.00577085280898743, -0.00345001874823703, 0.00589948032626289, -1.06597329751523e-17, 0.00234096774958672, 0.00430878547299781, -0.000418547259970266, 0.00568709829032464, -8.21109764793137e-18, 0.00376919225339987, 0.00233017559097417, 0.00107470141262219, 0.00382994518525259, -2.30788276369201e-19, 0.00319643179850945, 0.000748599044261129, 0.00135480554590726, 0.00182952606970045, -2.40024436102973e-18, 0.00192609062899124, -0.000118534274769341, 0.00102165166976478, 0.000465554741153073, -4.84818239396100e-19, 0.000832401037231464, -0.000412615808534457, 0.000588479261901569, -0.000200178238070410, 5.78571000126717e-19, 0.000175433848479960, -0.000453477478785663, 0.000281340196104213, -0.000506610984132016)
        // Normalize the input
        val meanValue = input.average()
        val normalizedValues = input.map { (it - meanValue) }
        //% Preprocess and find the autocorrelation function
        return meanfilter(normalizedValues.toTypedArray(), 2 * nsamples + 1, b1)
    }

    /**
     * Mean filter which emphasizes the maxima in a specified window length (n), but de-emphasizes
     * everything else in that window.
     */
    private fun meanfilter(input: Array<Double>, n: Int, b1: Array<Double>): Array<Double> {
        val x = conv(input, b1, ConvolutionType.SAME).centerSplice(65)
        val output = x.copyOf()
        for (nn in ((n + 1) / 2)..(x.size - (n - 1) / 2)) {
            val lower = (nn - (n - 1) / 2) - 1
            val upper = (nn + (n - 1) / 2) - 1
            val currwin = x.copyOfRange(lower, upper + 1).sortedArray()
            output[nn - 1] = x[nn - 1] - ((currwin.sum() - currwin.max()!!) / (n - 1).toDouble())
        }
        return output
    }

    /**
     * autocorrelation
     */
    internal fun xcorr(x: Array<Double>): Array<Double> {
        val xflip = x.reversedArray()
        return conv(x, xflip)
    }

    /**
     * convolution
     * https://www.mathworks.com/help/matlab/ref/conv.html#bucr92l-2
     */
    enum class ConvolutionType {
        FULL,
        SAME
    }

    internal fun conv(u: Array<Double>, v: Array<Double>, convolutionType: ConvolutionType = ConvolutionType.FULL) : Array<Double> {
        return when (convolutionType) {
            ConvolutionType.SAME ->
                outputConv(u, v, outputLength = u.count())
            ConvolutionType.FULL ->
                outputConv(u, v, outputLength = -1)
        }
    }

    private fun outputConv(u: Array<Double>, v: Array<Double>, outputLength: Int): Array<Double> {
        val m = u.size
        val n = v.size
        val range = Array(m + n - 1) { ii -> ii + 1 }
        val output: List<Double> = range.map { k ->
            var sum = 0.0
            for (j in Math.max(1, k + 1 - n)..Math.min(k, m)) {
                sum += u[j - 1] * v[k - j]
            }
            return@map sum
        }

        return when (outputLength > 0) {
            true -> {
                val center = Math.floor(output.size.toDouble() / 2.0).toInt()
                val halfU = Math.floor(outputLength.toDouble() / 2.0).toInt()
                val start = center - halfU
                val end = start + outputLength
                output.subList(start, end).toTypedArray()
            }
            false ->
                output.toTypedArray()
        }
    }

}

internal data class ValueAndIndex(val value: Double, val index: Int)

/**
 * Returns the max value and index of that value.
 */
internal fun Array<Double>.seekMax(): ValueAndIndex {
    val value = this.max()!!
    val index = this.indexOf(value)
    return ValueAndIndex(value, index)
}

/**
 * Replace the elements of the array with the given number of zeros on the left and clipped on the
 * right.
 */
internal fun Array<Double>.zeroReplace(lowerBounds: Int, upperBounds: Int): Array<Double> {
    val dropCount = this.count() - upperBounds - 1
    val y = if (dropCount > 0) this.dropLast(dropCount).toTypedArray() else this.copyOf()
    if (lowerBounds >= 0) {
        y.fill(0.0, 0, lowerBounds)
    }
    return y
}

/**
 * Pad the array with zeros before the value.
 */
internal fun Array<Double>.zeroPadBefore(count: Int): Array<Double> {
    val output = MutableList(count) { 0.0 }
    output.addAll(this)
    return output.toTypedArray()
}

/**
 * Pad the array with zeros before the value.
 */
internal fun Array<Double>.zeroPadAfter(count: Int): Array<Double> {
    val output = this.toMutableList()
    output.addAll(Array(count) { 0.0 })
    return output.toTypedArray()
}

/**
 * Return the center of the range minus the ends to endCount.
 */
internal fun Array<Double>.centerSplice(endCount: Int): Array<Double> =
        this.slice((endCount - 1) until (this.count() - endCount)).toTypedArray()