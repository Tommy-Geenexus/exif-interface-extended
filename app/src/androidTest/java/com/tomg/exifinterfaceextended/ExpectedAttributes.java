/*
 * Copyright 2024 The Android Open Source Project
 * Copyright 2024 Tom Geiselmann <tomgapplicationsdevelopment@gmail.com>
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

import androidx.annotation.Nullable;

import com.tomg.exifinterfaceextended.test.R;

/** Expected Exif attributes for test images in the res/raw/ directory. */
final class ExpectedAttributes {

    /** Expected attributes for {@link R.raw#jpeg_with_exif_byte_order_ii}. */
    public static final ExpectedAttributes JPEG_WITH_EXIF_BYTE_ORDER_II =
            new Builder()
                    .setThumbnailOffsetAndLength(3500, 6265)
                    .setThumbnailSize(512, 288)
                    .setIsThumbnailCompressed(true)
                    .setMake("SAMSUNG")
                    .setMakeOffsetAndLength(160, 8)
                    .setModel("SM-N900S")
                    .setAperture(2.2f)
                    .setDateTimeOriginal("2016:01:29 18:32:27")
                    .setExposureTime(0.033f)
                    .setFocalLength("413/100")
                    .setImageSize(640, 480)
                    .setIso("50")
                    .setOrientation(ExifInterfaceExtended.ORIENTATION_ROTATE_90)
                    .build();

    /**
     * Expected attributes for {@link R.raw#jpeg_with_exif_byte_order_ii} when only the Exif data is
     * read using {@link ExifInterfaceExtended#STREAM_TYPE_EXIF_DATA_ONLY}.
     */
    public static final ExpectedAttributes JPEG_WITH_EXIF_BYTE_ORDER_II_STANDALONE =
            JPEG_WITH_EXIF_BYTE_ORDER_II
                    .buildUpon()
                    .setThumbnailOffset(JPEG_WITH_EXIF_BYTE_ORDER_II.getThumbnailOffset() - 6)
                    .setMakeOffset(JPEG_WITH_EXIF_BYTE_ORDER_II.getMakeOffset() - 6)
                    .build();

    /** Expected attributes for {@link R.raw#jpeg_with_exif_byte_order_mm}. */
    public static final ExpectedAttributes JPEG_WITH_EXIF_BYTE_ORDER_MM =
            new Builder()
                    .setLatitudeOffsetAndLength(584, 24)
                    .setLatLong(0, 0)
                    .setAltitude(0)
                    .setMake("LGE")
                    .setMakeOffsetAndLength(414, 4)
                    .setModel("Nexus 5")
                    .setAperture(2.4f)
                    .setDateTimeOriginal("2016:01:29 15:44:58")
                    .setExposureTime(0.017f)
                    .setFocalLength("3970/1000")
                    .setGpsAltitude("0/1000")
                    .setGpsAltitudeRef("0")
                    .setGpsDatestamp("1970:01:01")
                    .setGpsLatitude("0/1,0/1,0/10000")
                    .setGpsLatitudeRef("N")
                    .setGpsLongitude("0/1,0/1,0/10000")
                    .setGpsLongitudeRef("E")
                    .setGpsProcessingMethod("GPS")
                    .setGpsTimestamp("00:00:00")
                    .setImageSize(144, 176)
                    .setIso("146")
                    .build();

    /**
     * Expected attributes for {@link R.raw#jpeg_with_exif_byte_order_mm} when only the Exif data is
     * read using {@link ExifInterfaceExtended#STREAM_TYPE_EXIF_DATA_ONLY}.
     */
    public static final ExpectedAttributes JPEG_WITH_EXIF_BYTE_ORDER_MM_STANDALONE =
            JPEG_WITH_EXIF_BYTE_ORDER_MM
                    .buildUpon()
                    .setLatitudeOffset(JPEG_WITH_EXIF_BYTE_ORDER_MM.getLatitudeOffset() - 6)
                    .setMakeOffset(JPEG_WITH_EXIF_BYTE_ORDER_MM.getMakeOffset() - 6)
                    .setImageSize(0, 0)
                    .build();

    /** Expected attributes for {@link R.raw#jpeg_with_exif_invalid_offset}. */
    public static final ExpectedAttributes JPEG_WITH_EXIF_INVALID_OFFSET =
            JPEG_WITH_EXIF_BYTE_ORDER_MM
                    .buildUpon()
                    .setAperture(0)
                    .setDateTimeOriginal(null)
                    .setExposureTime(0)
                    .setFocalLength(null)
                    .setIso(null)
                    .build();

    /** Expected attributes for {@link R.raw#dng_with_exif_with_xmp}. */
    public static final ExpectedAttributes DNG_WITH_EXIF_WITH_XMP =
            new Builder()
                    .setThumbnailOffsetAndLength(12570, 15179)
                    .setThumbnailSize(256, 144)
                    .setIsThumbnailCompressed(true)
                    .setLatitudeOffsetAndLength(12486, 24)
                    .setLatLong(53.834507f, 10.69585f)
                    .setAltitude(0)
                    .setMake("LGE")
                    .setMakeOffsetAndLength(102, 4)
                    .setModel("LG-H815")
                    .setAperture(1.8f)
                    .setDateTimeOriginal("2015:11:12 16:46:18")
                    .setExposureTime(0.0040f)
                    .setFocalLength("442/100")
                    .setGpsDatestamp("1970:01:17")
                    .setGpsLatitude("53/1,50/1,423/100")
                    .setGpsLatitudeRef("N")
                    .setGpsLongitude("10/1,41/1,4506/100")
                    .setGpsLongitudeRef("E")
                    .setGpsTimestamp("18:08:10")
                    .setImageSize(600, 337)
                    .setIso("800")
                    .setXmpOffsetAndLength(826, 10067)
                    .build();

    /** Expected attributes for {@link R.raw#jpeg_with_exif_with_xmp}. */
    public static final ExpectedAttributes JPEG_WITH_EXIF_WITH_XMP =
            DNG_WITH_EXIF_WITH_XMP
                    .buildUpon()
                    .clearThumbnail()
                    .setLatitudeOffset(1692)
                    .setMakeOffset(84)
                    .setOrientation(ExifInterfaceExtended.ORIENTATION_NORMAL)
                    .setXmpOffsetAndLength(1809, 13197)
                    .build();

    /** Expected attributes for {@link R.raw#png_with_exif_byte_order_ii}. */
    public static final ExpectedAttributes PNG_WITH_EXIF_BYTE_ORDER_II =
            JPEG_WITH_EXIF_BYTE_ORDER_II
                    .buildUpon()
                    .setThumbnailOffset(212271)
                    .setMakeOffset(211525)
                    .setFocalLength("41/10")
                    .setXmpOffsetAndLength(352, 1409)
                    .build();

    /** Expected attributes for {@link R.raw#webp_with_exif}. */
    public static final ExpectedAttributes WEBP_WITH_EXIF =
            JPEG_WITH_EXIF_BYTE_ORDER_II
                    .buildUpon()
                    .setThumbnailOffset(9646)
                    .setMakeOffset(6306)
                    .build();

    /** Expected attributes for {@link R.raw#invalid_webp_with_jpeg_app1_marker}. */
    public static final ExpectedAttributes INVALID_WEBP_WITH_JPEG_APP1_MARKER =
            new Builder()
                    .setOrientation(ExifInterfaceExtended.ORIENTATION_ROTATE_270)
                    .setHasIccProfile(true)
                    .build();

    /** Expected attributes for {@link R.raw#heif_with_exif} when read on a device below API 31. */
    public static final ExpectedAttributes HEIF_WITH_EXIF_BELOW_API_31 =
            new Builder()
                    .setMake("LGE")
                    .setMakeOffsetAndLength(3519, 4)
                    .setModel("Nexus 5")
                    .setImageSize(1920, 1080)
                    .setOrientation(ExifInterfaceExtended.ORIENTATION_NORMAL)
                    .build();

    /**
     * Expected attributes for {@link R.raw#heif_with_exif} when read on a device running API 31 or
     * above.
     */
    public static final ExpectedAttributes HEIF_WITH_EXIF_API_31_AND_ABOVE =
            HEIF_WITH_EXIF_BELOW_API_31.buildUpon().setXmpOffsetAndLength(3721, 3020).build();

    /** Expected attributes for {@link R.raw#jpeg_with_icc_with_exif_with_extended_xmp}. */
    public static final ExpectedAttributes JPEG_WITH_ICC_WITH_EXIF_WITH_EXTENDED_XMP =
            new Builder()
                    .setMake("Google")
                    .setMakeOffsetAndLength(170, 7)
                    .setModel("Pixel 3a")
                    .setAperture(1.8f)
                    .setDateTimeOriginal("2021:01:07 11:38:23")
                    .setExposureTime(Float.parseFloat("5.11e-4"))
                    .setFocalLength("4440/1000")
                    .setImageSize(4032, 3024)
                    .setIso("63")
                    .setOrientation(ExifInterfaceExtended.ORIENTATION_NORMAL)
                    .setXmpOffsetAndLength(955, 322)
                    .setHasExtendedXmp(true)
                    .setHasIccProfile(true)
                    .setHasPhotoshopImageResources(false)
                    .build();

    /** Expected attributes for {@link R.raw#jpeg_with_exif_with_photoshop_with_xmp}. */
    public static final ExpectedAttributes JPEG_WITH_EXIF_WITH_PHOTSHOP_WITH_XMP =
            new Builder()
                    .setThumbnailSize(127, 160)
                    .setThumbnailOffsetAndLength(464, 6473)
                    .setImageSize(300, 379)
                    .setOrientation(ExifInterfaceExtended.ORIENTATION_NORMAL)
                    .setXmpOffsetAndLength(15680, 19087)
                    .setHasExtendedXmp(false)
                    .setHasIccProfile(true)
                    .setHasPhotoshopImageResources(true)
                    .build();

    /** Expected attributes for {@link R.raw#webp_with_icc_with_exif_with_xmp}. */
    public static final ExpectedAttributes WEBP_WITH_ICC_WITH_EXIF_WITH_XMP =
            new Builder()
                    .setMake("Google")
                    .setMakeOffsetAndLength(3285288, 7)
                    .setModel("Pixel 3a")
                    .setAperture(1.8f)
                    .setDateTimeOriginal("2021:01:07 11:38:23")
                    .setExposureTime(Float.parseFloat("5.11e-4"))
                    .setFocalLength("4440/1000")
                    .setImageSize(4032, 3024)
                    .setIso("63")
                    .setOrientation(ExifInterfaceExtended.ORIENTATION_NORMAL)
                    .setXmpOffsetAndLength(3286048, 322)
                    .setHasExtendedXmp(false)
                    .setHasIccProfile(true)
                    .setHasPhotoshopImageResources(false)
                    .build();

    public static class Builder {
        // Thumbnail information.
        private boolean mHasThumbnail;
        private long mThumbnailOffset;
        private long mThumbnailLength;
        private int mThumbnailWidth;
        private int mThumbnailHeight;
        private boolean mIsThumbnailCompressed;

        // GPS information.
        private boolean mHasLatLong;
        private long mLatitudeOffset;
        private long mLatitudeLength;
        private float mLatitude;
        private float mLongitude;
        private float mAltitude;

        // Make information
        private boolean mHasMake;
        private long mMakeOffset;
        private long mMakeLength;
        @Nullable private String mMake;

        // Values.
        @Nullable private String mModel;
        private float mAperture;
        @Nullable private String mDateTimeOriginal;
        private float mExposureTime;
        private float mFlash;
        @Nullable private String mFocalLength;
        @Nullable private String mGpsAltitude;
        @Nullable private String mGpsAltitudeRef;
        @Nullable private String mGpsDatestamp;
        @Nullable private String mGpsLatitude;
        @Nullable private String mGpsLatitudeRef;
        @Nullable private String mGpsLongitude;
        @Nullable private String mGpsLongitudeRef;
        @Nullable private String mGpsProcessingMethod;
        @Nullable private String mGpsTimestamp;
        private int mImageLength;
        private int mImageWidth;
        @Nullable private String mIso;
        private int mOrientation;
        private int mWhiteBalance;

        // XMP information.
        private boolean mHasXmp;
        private long mXmpOffset;
        private long mXmpLength;
        private boolean mHasExtendedXmp;
        private boolean mHasIccProfile;
        private boolean mHasPhotoshopImageResources;

        Builder() {}

        private Builder(ExpectedAttributes attributes) {
            mHasThumbnail = attributes.hasThumbnail();
            mThumbnailOffset = attributes.getThumbnailOffset();
            mThumbnailLength = attributes.getThumbnailLength();
            mThumbnailWidth = attributes.getThumbnailWidth();
            mThumbnailHeight = attributes.getThumbnailHeight();
            mIsThumbnailCompressed = attributes.isIsThumbnailCompressed();
            mHasLatLong = attributes.hasLatLong();
            mLatitude = attributes.getLatitude();
            mLatitudeOffset = attributes.getLatitudeOffset();
            mLatitudeLength = attributes.getLatitudeLength();
            mLongitude = attributes.getLongitude();
            mAltitude = attributes.getAltitude();
            mHasMake = attributes.hasMake();
            mMakeOffset = attributes.getMakeOffset();
            mMakeLength = attributes.getMakeLength();
            mMake = attributes.getMake();
            mModel = attributes.getModel();
            mAperture = attributes.getAperture();
            mDateTimeOriginal = attributes.getDateTimeOriginal();
            mExposureTime = attributes.getExposureTime();
            mFocalLength = attributes.getFocalLength();
            mGpsAltitude = attributes.getGpsAltitude();
            mGpsAltitudeRef = attributes.getGpsAltitudeRef();
            mGpsDatestamp = attributes.getGpsDatestamp();
            mGpsLatitude = attributes.getGpsLatitude();
            mGpsLatitudeRef = attributes.getGpsLatitudeRef();
            mGpsLongitude = attributes.getGpsLongitude();
            mGpsLongitudeRef = attributes.getGpsLongitudeRef();
            mGpsProcessingMethod = attributes.getGpsProcessingMethod();
            mGpsTimestamp = attributes.getGpsTimestamp();
            mImageLength = attributes.getImageLength();
            mImageWidth = attributes.getImageWidth();
            mIso = attributes.getIso();
            mOrientation = attributes.getOrientation();
            mHasXmp = attributes.hasXmp();
            mXmpOffset = attributes.getXmpOffset();
            mXmpLength = attributes.getXmpLength();
            mHasExtendedXmp = attributes.hasExtendedXmp();
            mHasIccProfile = attributes.hasIccProfile();
            mHasPhotoshopImageResources = attributes.hasPhotoshopImageResources();
        }

        public Builder setThumbnailSize(int width, int height) {
            mHasThumbnail = true;
            mThumbnailWidth = width;
            mThumbnailHeight = height;
            return this;
        }

        public Builder setIsThumbnailCompressed(boolean isThumbnailCompressed) {
            mHasThumbnail = true;
            mIsThumbnailCompressed = isThumbnailCompressed;
            return this;
        }

        public Builder setThumbnailOffsetAndLength(long offset, long length) {
            mHasThumbnail = true;
            mThumbnailOffset = offset;
            mThumbnailLength = length;
            return this;
        }

        public Builder setThumbnailOffset(long offset) {
            if (!mHasThumbnail) {
                throw new IllegalStateException(
                        "Thumbnail position in the file must first be set with "
                                + "setThumbnailOffsetAndLength(...)");
            }
            mThumbnailOffset = offset;
            return this;
        }

        public Builder clearThumbnail() {
            mHasThumbnail = false;
            mThumbnailWidth = 0;
            mThumbnailHeight = 0;
            mThumbnailOffset = 0;
            mThumbnailLength = 0;
            mIsThumbnailCompressed = false;
            return this;
        }

        public Builder setLatLong(float latitude, float longitude) {
            mHasLatLong = true;
            mLatitude = latitude;
            mLongitude = longitude;
            return this;
        }

        public Builder setLatitudeOffsetAndLength(long offset, long length) {
            mHasLatLong = true;
            mLatitudeOffset = offset;
            mLatitudeLength = length;
            return this;
        }

        public Builder setLatitudeOffset(long offset) {
            if (!mHasLatLong) {
                throw new IllegalStateException(
                        "Latitude position in the file must first be "
                                + "set with setLatitudeOffsetAndLength(...)");
            }
            mLatitudeOffset = offset;
            return this;
        }

        public Builder clearLatLong() {
            mHasLatLong = false;
            mLatitude = 0;
            mLongitude = 0;
            return this;
        }

        public Builder setAltitude(float altitude) {
            mAltitude = altitude;
            return this;
        }

        public Builder setMake(@Nullable String make) {
            if (make == null) {
                mHasMake = false;
                mMakeOffset = 0;
                mMakeLength = 0;
            } else {
                mHasMake = true;
                mMake = make;
            }
            return this;
        }

        // TODO: b/270554381 - consider deriving length automatically from `make.length() + 1`
        //  (since the string is null-terminated in the format).
        public Builder setMakeOffsetAndLength(long offset, long length) {
            mHasMake = true;
            mMakeOffset = offset;
            mMakeLength = length;
            return this;
        }

        public Builder setMakeOffset(long offset) {
            if (!mHasMake) {
                throw new IllegalStateException(
                        "Make position in the file must first be set with"
                                + " setMakeOffsetAndLength(...)");
            }
            mMakeOffset = offset;
            return this;
        }

        public Builder setModel(@Nullable String model) {
            mModel = model;
            return this;
        }

        public Builder setAperture(float aperture) {
            mAperture = aperture;
            return this;
        }

        public Builder setDateTimeOriginal(@Nullable String dateTimeOriginal) {
            mDateTimeOriginal = dateTimeOriginal;
            return this;
        }

        public Builder setExposureTime(float exposureTime) {
            mExposureTime = exposureTime;
            return this;
        }

        public Builder setFlash(float flash) {
            mFlash = flash;
            return this;
        }

        public Builder setFocalLength(@Nullable String focalLength) {
            mFocalLength = focalLength;
            return this;
        }

        public Builder setGpsAltitude(@Nullable String gpsAltitude) {
            mGpsAltitude = gpsAltitude;
            return this;
        }

        public Builder setGpsAltitudeRef(@Nullable String gpsAltitudeRef) {
            mGpsAltitudeRef = gpsAltitudeRef;
            return this;
        }

        public Builder setGpsDatestamp(@Nullable String gpsDatestamp) {
            mGpsDatestamp = gpsDatestamp;
            return this;
        }

        public Builder setGpsLatitude(@Nullable String gpsLatitude) {
            mGpsLatitude = gpsLatitude;
            return this;
        }

        public Builder setGpsLatitudeRef(@Nullable String gpsLatitudeRef) {
            mGpsLatitudeRef = gpsLatitudeRef;
            return this;
        }

        public Builder setGpsLongitude(@Nullable String gpsLongitude) {
            mGpsLongitude = gpsLongitude;
            return this;
        }

        public Builder setGpsLongitudeRef(@Nullable String gpsLongitudeRef) {
            mGpsLongitudeRef = gpsLongitudeRef;
            return this;
        }

        public Builder setGpsProcessingMethod(@Nullable String gpsProcessingMethod) {
            mGpsProcessingMethod = gpsProcessingMethod;
            return this;
        }

        public Builder setGpsTimestamp(@Nullable String gpsTimestamp) {
            mGpsTimestamp = gpsTimestamp;
            return this;
        }

        public Builder setImageSize(int imageWidth, int imageLength) {
            mImageWidth = imageWidth;
            mImageLength = imageLength;
            return this;
        }

        public Builder setIso(@Nullable String iso) {
            mIso = iso;
            return this;
        }

        public Builder setOrientation(int orientation) {
            mOrientation = orientation;
            return this;
        }

        public Builder setWhiteBalance(int whiteBalance) {
            mWhiteBalance = whiteBalance;
            return this;
        }

        public Builder setXmpOffsetAndLength(int offset, int length) {
            mHasXmp = true;
            mXmpOffset = offset;
            mXmpLength = length;
            return this;
        }

        public Builder setXmpOffset(int offset) {
            if (!mHasXmp) {
                throw new IllegalStateException(
                        "XMP position in the file must first be set with"
                                + " setXmpOffsetAndLength(...)");
            }
            mXmpOffset = offset;
            return this;
        }

        public Builder clearXmp() {
            mHasXmp = false;
            mXmpOffset = 0;
            mXmpLength = 0;
            return this;
        }

        public Builder setHasExtendedXmp(boolean value) {
            mHasExtendedXmp = value;
            return this;
        }

        public Builder setHasIccProfile(boolean value) {
            mHasIccProfile = value;
            return this;
        }

        public Builder setHasPhotoshopImageResources(boolean value) {
            mHasPhotoshopImageResources = value;
            return this;
        }

        ExpectedAttributes build() {
            return new ExpectedAttributes(this);
        }
    }

    // TODO: b/270554381 - Add nullability annotations below.

    // Thumbnail information.
    private final boolean mHasThumbnail;
    private final int mThumbnailWidth;
    private final int mThumbnailHeight;
    private final boolean mIsThumbnailCompressed;
    // TODO: b/270554381 - Merge these offset and length (and others) into long[] arrays, and
    //  move them down to their own section. This may also allow removing some of the hasXXX
    // fields.
    private final long mThumbnailOffset;
    private final long mThumbnailLength;

    // GPS information.
    private final boolean mHasLatLong;
    // TODO: b/270554381 - Merge this and longitude into a double[]
    private final float mLatitude;
    private final long mLatitudeOffset;
    private final long mLatitudeLength;
    private final float mLongitude;
    private final float mAltitude;

    // Make information
    private final boolean mHasMake;
    private final long mMakeOffset;
    private final long mMakeLength;
    private final String mMake;

    // Values.
    private final String mModel;
    private final float mAperture;
    private final String mDateTimeOriginal;
    private final float mExposureTime;
    private final String mFocalLength;
    // TODO: b/270554381 - Rename these to make them clear they're strings, or original values,
    //  and move them closer to the (computed) latitude/longitude/altitude values. Consider
    //  also having a verification check that they are consistent with latitude/longitude (but
    //  not sure how to reconcile that with "don't duplicate business logic in tests").
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

    // XMP information.
    private final boolean mHasXmp;
    private final long mXmpOffset;
    private final long mXmpLength;

    private final boolean mHasExtendedXmp;
    private final boolean mHasIccProfile;
    private final boolean mHasPhotoshopImageResources;

    private ExpectedAttributes(Builder builder) {
        // TODO: b/270554381 - Re-order these assignments to match the fields above.
        mHasThumbnail = builder.mHasThumbnail;
        mThumbnailOffset = builder.mThumbnailOffset;
        mThumbnailLength = builder.mThumbnailLength;
        mThumbnailWidth = builder.mThumbnailWidth;
        mThumbnailHeight = builder.mThumbnailHeight;
        mIsThumbnailCompressed = builder.mIsThumbnailCompressed;
        mHasLatLong = builder.mHasLatLong;
        mLatitudeOffset = builder.mLatitudeOffset;
        mLatitudeLength = builder.mLatitudeLength;
        mLatitude = builder.mLatitude;
        mLongitude = builder.mLongitude;
        mAltitude = builder.mAltitude;
        mHasMake = builder.mHasMake;
        mMakeOffset = builder.mMakeOffset;
        mMakeLength = builder.mMakeLength;
        mMake = builder.mMake;
        mModel = builder.mModel;
        mAperture = builder.mAperture;
        mDateTimeOriginal = builder.mDateTimeOriginal;
        mExposureTime = builder.mExposureTime;
        mFocalLength = builder.mFocalLength;
        mGpsAltitude = builder.mGpsAltitude;
        mGpsAltitudeRef = builder.mGpsAltitudeRef;
        mGpsDatestamp = builder.mGpsDatestamp;
        mGpsLatitude = builder.mGpsLatitude;
        mGpsLatitudeRef = builder.mGpsLatitudeRef;
        mGpsLongitude = builder.mGpsLongitude;
        mGpsLongitudeRef = builder.mGpsLongitudeRef;
        mGpsProcessingMethod = builder.mGpsProcessingMethod;
        mGpsTimestamp = builder.mGpsTimestamp;
        mImageLength = builder.mImageLength;
        mImageWidth = builder.mImageWidth;
        mIso = builder.mIso;
        mOrientation = builder.mOrientation;
        mHasXmp = builder.mHasXmp;
        mXmpOffset = builder.mXmpOffset;
        mXmpLength = builder.mXmpLength;
        mHasExtendedXmp = builder.mHasExtendedXmp;
        mHasIccProfile = builder.mHasIccProfile;
        mHasPhotoshopImageResources = builder.mHasPhotoshopImageResources;
    }

    public Builder buildUpon() {
        return new Builder(this);
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

    public long getThumbnailOffset() {
        return mThumbnailOffset;
    }

    public long getThumbnailLength() {
        return mThumbnailLength;
    }

    public boolean hasLatLong() {
        return mHasLatLong;
    }

    public float getLatitude() {
        return mLatitude;
    }

    public long getLatitudeOffset() {
        return mLatitudeOffset;
    }

    public long getLatitudeLength() {
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

    public long getMakeOffset() {
        return mMakeOffset;
    }

    public long getMakeLength() {
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

    public boolean hasXmp() {
        return mHasXmp;
    }

    public long getXmpOffset() {
        return mXmpOffset;
    }

    public long getXmpLength() {
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
