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

import android.content.res.TypedArray;

class ExpectedValue {

    // Thumbnail information.
    private final boolean mHasThumbnail;
    private final int mThumbnailWidth;
    private final int mThumbnailHeight;
    private final boolean mIsThumbnailCompressed;
    private final int mThumbnailOffset;
    private final int mThumbnailLength;

    // GPS information.
    private final boolean mHasLatLong;
    private final float mLatitude;
    private final int mLatitudeOffset;
    private final int mLatitudeLength;
    private final float mLongitude;
    private final float mAltitude;

    // Make information
    private final boolean mHasMake;
    private final int mMakeOffset;
    private final int mMakeLength;
    private final String mMake;

    // Values.
    private final String mModel;
    private final float mAperture;
    private final String mDateTimeOriginal;
    private final float mExposureTime;
    private final float mFlash;
    private final String mFocalLength;
    private final String mGpsAltitude;
    private final String mGpsAltitudeRef;
    private final String mGpsDatestamp;
    private final String mGpsLatitude;
    private final String mGpsLatitudeRef;
    private final String mGpsLongitude;
    private final String mGpsLongitudeRef;
    private final String mGpsProcessingMethod;
    private final String mGpsTimestamp;
    private final int mImageLength;
    private final int mImageWidth;
    private final String mIso;
    private final int mOrientation;
    private final int mWhiteBalance;

    // XMP information.
    private final boolean mHasXmp;
    private final int mXmpOffset;
    private final int mXmpLength;

    private final boolean mHasExtendedXmp;
    private final boolean mHasIccProfile;
    private final boolean mHasPhotoshopImageResources;

    private static String getString(TypedArray typedArray, int index) {
        String stringValue = typedArray.getString(index);
        if (stringValue == null || stringValue.equals("")) {
            return null;
        }
        return stringValue.trim();
    }

    ExpectedValue(TypedArray typedArray) {
        int index = 0;

        // Reads thumbnail information.
        mHasThumbnail = typedArray.getBoolean(index++, false);
        mThumbnailOffset = typedArray.getInt(index++, -1);
        mThumbnailLength = typedArray.getInt(index++, -1);
        mThumbnailWidth = typedArray.getInt(index++, 0);
        mThumbnailHeight = typedArray.getInt(index++, 0);
        mIsThumbnailCompressed = typedArray.getBoolean(index++, false);

        // Reads GPS information.
        mHasLatLong = typedArray.getBoolean(index++, false);
        mLatitudeOffset = typedArray.getInt(index++, -1);
        mLatitudeLength = typedArray.getInt(index++, -1);
        mLatitude = typedArray.getFloat(index++, 0f);
        mLongitude = typedArray.getFloat(index++, 0f);
        mAltitude = typedArray.getFloat(index++, 0f);

        // Reads Make information.
        mHasMake = typedArray.getBoolean(index++, false);
        mMakeOffset = typedArray.getInt(index++, -1);
        mMakeLength = typedArray.getInt(index++, -1);
        mMake = getString(typedArray, index++);

        // Reads values.
        mModel = getString(typedArray, index++);
        mAperture = typedArray.getFloat(index++, 0f);
        mDateTimeOriginal = getString(typedArray, index++);
        mExposureTime = typedArray.getFloat(index++, 0f);
        mFlash = typedArray.getFloat(index++, 0f);
        mFocalLength = getString(typedArray, index++);
        mGpsAltitude = getString(typedArray, index++);
        mGpsAltitudeRef = getString(typedArray, index++);
        mGpsDatestamp = getString(typedArray, index++);
        mGpsLatitude = getString(typedArray, index++);
        mGpsLatitudeRef = getString(typedArray, index++);
        mGpsLongitude = getString(typedArray, index++);
        mGpsLongitudeRef = getString(typedArray, index++);
        mGpsProcessingMethod = getString(typedArray, index++);
        mGpsTimestamp = getString(typedArray, index++);
        mImageLength = typedArray.getInt(index++, 0);
        mImageWidth = typedArray.getInt(index++, 0);
        mIso = getString(typedArray, index++);
        mOrientation = typedArray.getInt(index++, 0);
        mWhiteBalance = typedArray.getInt(index++, 0);

        // Reads XMP information.
        mHasXmp = typedArray.getBoolean(index++, false);
        mXmpOffset = typedArray.getInt(index++, 0);
        mXmpLength = typedArray.getInt(index++, 0);

        mHasExtendedXmp = typedArray.getBoolean(index++, false);
        mHasIccProfile = typedArray.getBoolean(index++, false);
        mHasPhotoshopImageResources = typedArray.getBoolean(index, false);

        typedArray.recycle();
    }

    public boolean hasThumbnail() {
        return mHasThumbnail;
    }

    public int getThumbnailWidth() {
        return mThumbnailWidth;
    }

    public int getThumbnailHeight() {
        return mThumbnailHeight;
    }

    public boolean isIsThumbnailCompressed() {
        return mIsThumbnailCompressed;
    }

    public int getThumbnailOffset() {
        return mThumbnailOffset;
    }

    public int getThumbnailLength() {
        return mThumbnailLength;
    }

    public boolean hasLatLong() {
        return mHasLatLong;
    }

    public float getLatitude() {
        return mLatitude;
    }

    public int getLatitudeOffset() {
        return mLatitudeOffset;
    }

    public int getLatitudeLength() {
        return mLatitudeLength;
    }

    public float getLongitude() {
        return mLongitude;
    }

    public float getAltitude() {
        return mAltitude;
    }

    public boolean hasMake() {
        return mHasMake;
    }

    public int getMakeOffset() {
        return mMakeOffset;
    }

    public int getMakeLength() {
        return mMakeLength;
    }

    public String getMake() {
        return mMake;
    }

    public String getModel() {
        return mModel;
    }

    public float getAperture() {
        return mAperture;
    }

    public String getDateTimeOriginal() {
        return mDateTimeOriginal;
    }

    public float getExposureTime() {
        return mExposureTime;
    }

    public float getFlash() {
        return mFlash;
    }

    public String getFocalLength() {
        return mFocalLength;
    }

    public String getGpsAltitude() {
        return mGpsAltitude;
    }

    public String getGpsAltitudeRef() {
        return mGpsAltitudeRef;
    }

    public String getGpsDatestamp() {
        return mGpsDatestamp;
    }

    public String getGpsLatitude() {
        return mGpsLatitude;
    }

    public String getGpsLatitudeRef() {
        return mGpsLatitudeRef;
    }

    public String getGpsLongitude() {
        return mGpsLongitude;
    }

    public String getGpsLongitudeRef() {
        return mGpsLongitudeRef;
    }

    public String getGpsProcessingMethod() {
        return mGpsProcessingMethod;
    }

    public String getGpsTimestamp() {
        return mGpsTimestamp;
    }

    public int getImageLength() {
        return mImageLength;
    }

    public int getImageWidth() {
        return mImageWidth;
    }

    public String getIso() {
        return mIso;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public int getWhiteBalance() {
        return mWhiteBalance;
    }

    public boolean hasXmp() {
        return mHasXmp;
    }

    public int getXmpOffset() {
        return mXmpOffset;
    }

    public int getXmpLength() {
        return mXmpLength;
    }

    public boolean hasExtendedXmp() {
        return mHasExtendedXmp;
    }

    public boolean hasIccProfile() {
        return mHasIccProfile;
    }

    public boolean hasPhotoshopImageResources() {
        return mHasPhotoshopImageResources;
    }
}
