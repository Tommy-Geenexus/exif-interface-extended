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

import static com.tomg.exifinterfaceextended.ExifInterfaceExtended.IFD_FORMAT_DOUBLE;
import static com.tomg.exifinterfaceextended.ExifInterfaceExtended.IFD_FORMAT_SINGLE;
import static com.tomg.exifinterfaceextended.ExifInterfaceExtended.IFD_FORMAT_SLONG;
import static com.tomg.exifinterfaceextended.ExifInterfaceExtended.IFD_FORMAT_SSHORT;
import static com.tomg.exifinterfaceextended.ExifInterfaceExtended.IFD_FORMAT_ULONG;
import static com.tomg.exifinterfaceextended.ExifInterfaceExtended.IFD_FORMAT_UNDEFINED;
import static com.tomg.exifinterfaceextended.ExifInterfaceExtended.IFD_FORMAT_USHORT;

/**
 * A class for indicating EXIF tag.
 */
class ExifTag {

    private final int mNumber;
    private final String mName;
    private final int mPrimaryFormat;
    private final int mSecondaryFormat;

    ExifTag(String name, int number, int format) {
        mName = name;
        mNumber = number;
        mPrimaryFormat = format;
        mSecondaryFormat = -1;
    }

    ExifTag(String name, int number, int primaryFormat, int secondaryFormat) {
        mName = name;
        mNumber = number;
        mPrimaryFormat = primaryFormat;
        mSecondaryFormat = secondaryFormat;
    }

    public boolean isFormatCompatible(int format) {
        if (mPrimaryFormat == IFD_FORMAT_UNDEFINED || format == IFD_FORMAT_UNDEFINED) {
            return true;
        } else if (mPrimaryFormat == format || mSecondaryFormat == format) {
            return true;
        } else if ((mPrimaryFormat == IFD_FORMAT_ULONG || mSecondaryFormat == IFD_FORMAT_ULONG)
                && format == IFD_FORMAT_USHORT) {
            return true;
        } else if ((mPrimaryFormat == IFD_FORMAT_SLONG || mSecondaryFormat == IFD_FORMAT_SLONG)
                && format == IFD_FORMAT_SSHORT) {
            return true;
        } else {
            return (mPrimaryFormat == IFD_FORMAT_DOUBLE ||
                    mSecondaryFormat == IFD_FORMAT_DOUBLE) && format == IFD_FORMAT_SINGLE;
        }
    }

    public int getNumber() {
        return mNumber;
    }

    public String getName() {
        return mName;
    }

    public int getPrimaryFormat() {
        return mPrimaryFormat;
    }

    public int getSecondaryFormat() {
        return mSecondaryFormat;
    }
}
