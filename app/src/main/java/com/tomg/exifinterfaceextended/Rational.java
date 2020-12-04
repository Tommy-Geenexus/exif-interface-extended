/*
 * Copyright 2018 The Android Open Source Project
 * Copyright 2020 Tom Geiselmann <tomgapplicationsdevelopment@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tomg.exifinterfaceextended;

import androidx.annotation.NonNull;

/**
 * A class for indicating EXIF rational type.
 */
class Rational {

    private final long mNumerator;
    private final long mDenominator;

    Rational(double value) {
        this((long) (value * 10000), 10000);
    }

    Rational(long numerator, long denominator) {
        // Handle erroneous case
        if (denominator == 0) {
            mNumerator = 0;
            mDenominator = 1;
        } else {
            mNumerator = numerator;
            mDenominator = denominator;
        }
    }

    @Override
    @NonNull
    public String toString() {
        return mNumerator + "/" + mDenominator;
    }

    public double calculate() {
        return (double) mNumerator / mDenominator;
    }

    public long getNumerator() {
        return mNumerator;
    }

    public long getDenominator() {
        return mDenominator;
    }
}
