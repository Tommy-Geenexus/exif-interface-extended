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

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import static com.tomg.exifinterfaceextended.ExifInterfaceExtendedUtils.copy;
import static com.tomg.exifinterfaceextended.test.R.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.util.Pair;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SmallTest;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Test {@link ExifInterfaceExtended}.
 */
// TODO: Add NEF test file from CTS after reducing file size in order to test uncompressed thumbnail
// image.
@RunWith(AndroidJUnit4.class)
public class ExifInterfaceExtendedTest {
    private static final String TAG = ExifInterfaceExtended.class.getSimpleName();
    private static final boolean VERBOSE = false;  // lots of logging
    private static final double DIFFERENCE_TOLERANCE = .001;
    private static final boolean ENABLE_STRICT_MODE_FOR_UNBUFFERED_IO = true;

    @Rule
    public GrantPermissionRule mRuntimePermissionRule =
            GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final String JPEG_WITH_EXIF_BYTE_ORDER_II = "jpeg_with_exif_byte_order_ii.jpg";
    private static final String JPEG_WITH_EXIF_BYTE_ORDER_MM = "jpeg_with_exif_byte_order_mm.jpg";
    private static final String DNG_WITH_EXIF_WITH_XMP = "dng_with_exif_with_xmp.dng";
    private static final String JPEG_WITH_EXIF_WITH_XMP = "jpeg_with_exif_with_xmp.jpg";
    private static final String PNG_WITH_EXIF_BYTE_ORDER_II = "png_with_exif_byte_order_ii.png";
    private static final String PNG_WITHOUT_EXIF = "png_without_exif.png";
    private static final String WEBP_WITH_EXIF = "webp_with_exif.webp";
    private static final String WEBP_WITHOUT_EXIF_WITH_ANIM_DATA =
            "webp_with_anim_without_exif.webp";
    private static final String WEBP_WITHOUT_EXIF = "webp_without_exif.webp";
    private static final String WEBP_WITHOUT_EXIF_WITH_LOSSLESS_ENCODING =
            "webp_lossless_without_exif.webp";
    private static final String JPEG_WITH_DATETIME_TAG_PRIMARY_FORMAT =
            "jpeg_with_datetime_tag_primary_format.jpg";
    private static final String JPEG_WITH_DATETIME_TAG_SECONDARY_FORMAT =
            "jpeg_with_datetime_tag_secondary_format.jpg";
    private static final String HEIF_WITH_EXIF = "heif_with_exif.heic";
    private static final String JPEG_WITH_EXIF_WITH_PHOTOSHOP_WITH_XMP =
            "jpeg_with_exif_with_photoshop_with_xmp.jpg";
    private static final String JPEG_WITH_ICC_WITH_EXIF_WITH_EXTENDED_XMP =
            "jpeg_with_icc_with_exif_with_extended_xmp.jpg";
    private static final String WEBP_WITH_ICC_WITH_EXIF_WITH_XMP =
            "webp_with_icc_with_exif_with_xmp.webp";
    private static final int[] IMAGE_RESOURCES = new int[] {
            raw.jpeg_with_exif_byte_order_ii,
            raw.jpeg_with_exif_byte_order_mm,
            raw.dng_with_exif_with_xmp,
            raw.jpeg_with_exif_with_xmp,
            raw.png_with_exif_byte_order_ii,
            raw.png_without_exif,
            raw.webp_with_exif,
            raw.webp_with_anim_without_exif,
            raw.webp_without_exif,
            raw.webp_lossless_without_exif,
            raw.jpeg_with_datetime_tag_primary_format,
            raw.jpeg_with_datetime_tag_secondary_format,
            raw.heif_with_exif,
            raw.jpeg_with_exif_with_photoshop_with_xmp,
            raw.jpeg_with_icc_with_exif_with_extended_xmp,
            raw.webp_with_icc_with_exif_with_xmp,
    };
    private static final String[] IMAGE_FILENAMES = new String[] {
            JPEG_WITH_EXIF_BYTE_ORDER_II, JPEG_WITH_EXIF_BYTE_ORDER_MM, DNG_WITH_EXIF_WITH_XMP,
            JPEG_WITH_EXIF_WITH_XMP, PNG_WITH_EXIF_BYTE_ORDER_II, PNG_WITHOUT_EXIF,
            WEBP_WITH_EXIF, WEBP_WITHOUT_EXIF_WITH_ANIM_DATA, WEBP_WITHOUT_EXIF,
            WEBP_WITHOUT_EXIF_WITH_LOSSLESS_ENCODING, JPEG_WITH_DATETIME_TAG_PRIMARY_FORMAT,
            JPEG_WITH_DATETIME_TAG_SECONDARY_FORMAT, HEIF_WITH_EXIF,
            JPEG_WITH_EXIF_WITH_PHOTOSHOP_WITH_XMP, JPEG_WITH_ICC_WITH_EXIF_WITH_EXTENDED_XMP,
            WEBP_WITH_ICC_WITH_EXIF_WITH_XMP
    };

    private static final String TEST_TEMP_FILE_NAME = "testImage";
    private static final String JPEG_TEST = "test.jpg";
    private static final String PNG_TEST = "test.png";
    private static final String WEBP_TEST = "test.webp";
    private static final double DELTA = 1e-8;
    // We translate double to rational in a 1/10000 precision.
    private static final double RATIONAL_DELTA = 0.0001;
    private static final int TEST_LAT_LONG_VALUES_ARRAY_LENGTH = 8;
    private static final int TEST_NUMBER_OF_CORRUPTED_IMAGE_STREAMS = 30;
    private static final double[] TEST_LATITUDE_VALID_VALUES = new double[]
            {0, 45, 90, -60, 0.00000001, -89.999999999, 14.2465923626, -68.3434534737};
    private static final double[] TEST_LONGITUDE_VALID_VALUES = new double[]
            {0, -45, 90, -120, 180, 0.00000001, -179.99999999999, -58.57834236352};
    private static final double[] TEST_LATITUDE_INVALID_VALUES = new double[]
            {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 90.0000000001,
                    263.34763236326, -1e5, 347.32525, -176.346347754};
    private static final double[] TEST_LONGITUDE_INVALID_VALUES = new double[]
            {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 180.0000000001,
                    263.34763236326, -1e10, 347.325252623, -4000.346323236};
    private static final double[] TEST_ALTITUDE_VALUES = new double[]
            {0, -2000, 10000, -355.99999999999, 18.02038};
    private static final int[][] TEST_ROTATION_STATE_MACHINE = {
            {ExifInterfaceExtended.ORIENTATION_UNDEFINED, -90,
                    ExifInterfaceExtended.ORIENTATION_UNDEFINED},
            {ExifInterfaceExtended.ORIENTATION_UNDEFINED, 0,
                    ExifInterfaceExtended.ORIENTATION_UNDEFINED},
            {ExifInterfaceExtended.ORIENTATION_UNDEFINED, 90,
                    ExifInterfaceExtended.ORIENTATION_UNDEFINED},
            {ExifInterfaceExtended.ORIENTATION_UNDEFINED, 180,
                    ExifInterfaceExtended.ORIENTATION_UNDEFINED},
            {ExifInterfaceExtended.ORIENTATION_UNDEFINED, 270,
                    ExifInterfaceExtended.ORIENTATION_UNDEFINED},
            {ExifInterfaceExtended.ORIENTATION_UNDEFINED, 540,
                    ExifInterfaceExtended.ORIENTATION_UNDEFINED},
            {ExifInterfaceExtended.ORIENTATION_NORMAL, -90,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_270},
            {ExifInterfaceExtended.ORIENTATION_NORMAL, 0,
                    ExifInterfaceExtended.ORIENTATION_NORMAL},
            {ExifInterfaceExtended.ORIENTATION_NORMAL, 90,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_90},
            {ExifInterfaceExtended.ORIENTATION_NORMAL, 180,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_180},
            {ExifInterfaceExtended.ORIENTATION_NORMAL, 270,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_270},
            {ExifInterfaceExtended.ORIENTATION_NORMAL, 540,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_180},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_90, -90,
                    ExifInterfaceExtended.ORIENTATION_NORMAL},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_90, 0,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_90},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_90, 90,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_180},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_90, 180,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_270},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_90, 270,
                    ExifInterfaceExtended.ORIENTATION_NORMAL},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_90, 540,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_270},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_180, -90,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_90},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_180, 0,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_180},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_180, 90,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_270},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_180, 180,
                    ExifInterfaceExtended.ORIENTATION_NORMAL},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_180, 270,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_90},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_180, 540,
                    ExifInterfaceExtended.ORIENTATION_NORMAL},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_270, -90,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_180},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_270, 0,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_270},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_270, 90,
                    ExifInterfaceExtended.ORIENTATION_NORMAL},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_270, 180,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_90},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_270, 270,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_180},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_270, 540,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_90},
            {ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL, -90,
                    ExifInterfaceExtended.ORIENTATION_TRANSVERSE},
            {ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL, 0,
                    ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL},
            {ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL, 90,
                    ExifInterfaceExtended.ORIENTATION_TRANSPOSE},
            {ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL, 180,
                    ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL},
            {ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL, 270,
                    ExifInterfaceExtended.ORIENTATION_TRANSVERSE},
            {ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL, 540,
                    ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL},
            {ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL, -90,
                    ExifInterfaceExtended.ORIENTATION_TRANSPOSE},
            {ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL, 0,
                    ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL},
            {ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL, 90,
                    ExifInterfaceExtended.ORIENTATION_TRANSVERSE},
            {ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL, 180,
                    ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL},
            {ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL, 270,
                    ExifInterfaceExtended.ORIENTATION_TRANSPOSE},
            {ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL, 540,
                    ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL},
            {ExifInterfaceExtended.ORIENTATION_TRANSPOSE, -90,
                    ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL},
            {ExifInterfaceExtended.ORIENTATION_TRANSPOSE, 0,
                    ExifInterfaceExtended.ORIENTATION_TRANSPOSE},
            {ExifInterfaceExtended.ORIENTATION_TRANSPOSE, 90,
                    ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL},
            {ExifInterfaceExtended.ORIENTATION_TRANSPOSE, 180,
                    ExifInterfaceExtended.ORIENTATION_TRANSVERSE},
            {ExifInterfaceExtended.ORIENTATION_TRANSPOSE, 270,
                    ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL},
            {ExifInterfaceExtended.ORIENTATION_TRANSPOSE, 540,
                    ExifInterfaceExtended.ORIENTATION_TRANSVERSE},
            {ExifInterfaceExtended.ORIENTATION_TRANSVERSE, -90,
                    ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL},
            {ExifInterfaceExtended.ORIENTATION_TRANSVERSE, 0,
                    ExifInterfaceExtended.ORIENTATION_TRANSVERSE},
            {ExifInterfaceExtended.ORIENTATION_TRANSVERSE, 90,
                    ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL},
            {ExifInterfaceExtended.ORIENTATION_TRANSVERSE, 180,
                    ExifInterfaceExtended.ORIENTATION_TRANSPOSE},
            {ExifInterfaceExtended.ORIENTATION_TRANSVERSE, 270,
                    ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL},
            {ExifInterfaceExtended.ORIENTATION_TRANSVERSE, 540,
                    ExifInterfaceExtended.ORIENTATION_TRANSPOSE},
    };
    private static final int[][] TEST_FLIP_VERTICALLY_STATE_MACHINE = {
            {ExifInterfaceExtended.ORIENTATION_UNDEFINED,
                    ExifInterfaceExtended.ORIENTATION_UNDEFINED},
            {ExifInterfaceExtended.ORIENTATION_NORMAL,
                    ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_90,
                    ExifInterfaceExtended.ORIENTATION_TRANSVERSE},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_180,
                    ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_270,
                    ExifInterfaceExtended.ORIENTATION_TRANSPOSE},
            {ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL,
                    ExifInterfaceExtended.ORIENTATION_NORMAL},
            {ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_180},
            {ExifInterfaceExtended.ORIENTATION_TRANSPOSE,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_270},
            {ExifInterfaceExtended.ORIENTATION_TRANSVERSE,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_90}
    };
    private static final int[][] TEST_FLIP_HORIZONTALLY_STATE_MACHINE = {
            {ExifInterfaceExtended.ORIENTATION_UNDEFINED,
                    ExifInterfaceExtended.ORIENTATION_UNDEFINED},
            {ExifInterfaceExtended.ORIENTATION_NORMAL,
                    ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_90,
                    ExifInterfaceExtended.ORIENTATION_TRANSPOSE},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_180,
                    ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL},
            {ExifInterfaceExtended.ORIENTATION_ROTATE_270,
                    ExifInterfaceExtended.ORIENTATION_TRANSVERSE},
            {ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_180},
            {ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL,
                    ExifInterfaceExtended.ORIENTATION_NORMAL},
            {ExifInterfaceExtended.ORIENTATION_TRANSPOSE,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_90},
            {ExifInterfaceExtended.ORIENTATION_TRANSVERSE,
                    ExifInterfaceExtended.ORIENTATION_ROTATE_270}
    };
    private static final HashMap<Integer, Pair<Boolean, Integer>> FLIP_STATE_AND_ROTATION_DEGREES =
            new HashMap<>();
    static {
        FLIP_STATE_AND_ROTATION_DEGREES.put(
                ExifInterfaceExtended.ORIENTATION_UNDEFINED, new Pair<>(false, 0));
        FLIP_STATE_AND_ROTATION_DEGREES.put(
                ExifInterfaceExtended.ORIENTATION_NORMAL, new Pair<>(false, 0));
        FLIP_STATE_AND_ROTATION_DEGREES.put(
                ExifInterfaceExtended.ORIENTATION_ROTATE_90, new Pair<>(false, 90));
        FLIP_STATE_AND_ROTATION_DEGREES.put(
                ExifInterfaceExtended.ORIENTATION_ROTATE_180, new Pair<>(false, 180));
        FLIP_STATE_AND_ROTATION_DEGREES.put(
                ExifInterfaceExtended.ORIENTATION_ROTATE_270, new Pair<>(false, 270));
        FLIP_STATE_AND_ROTATION_DEGREES.put(
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL, new Pair<>(true, 0));
        FLIP_STATE_AND_ROTATION_DEGREES.put(
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE, new Pair<>(true, 90));
        FLIP_STATE_AND_ROTATION_DEGREES.put(
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL, new Pair<>(true, 180));
        FLIP_STATE_AND_ROTATION_DEGREES.put(
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE, new Pair<>(true, 270));
    }

    private static final String[] EXIF_TAGS = {
            ExifInterfaceExtended.TAG_MAKE,
            ExifInterfaceExtended.TAG_MODEL,
            ExifInterfaceExtended.TAG_F_NUMBER,
            ExifInterfaceExtended.TAG_DATETIME_ORIGINAL,
            ExifInterfaceExtended.TAG_EXPOSURE_TIME,
            ExifInterfaceExtended.TAG_FLASH,
            ExifInterfaceExtended.TAG_FOCAL_LENGTH,
            ExifInterfaceExtended.TAG_GPS_ALTITUDE,
            ExifInterfaceExtended.TAG_GPS_ALTITUDE_REF,
            ExifInterfaceExtended.TAG_GPS_DATESTAMP,
            ExifInterfaceExtended.TAG_GPS_LATITUDE,
            ExifInterfaceExtended.TAG_GPS_LATITUDE_REF,
            ExifInterfaceExtended.TAG_GPS_LONGITUDE,
            ExifInterfaceExtended.TAG_GPS_LONGITUDE_REF,
            ExifInterfaceExtended.TAG_GPS_PROCESSING_METHOD,
            ExifInterfaceExtended.TAG_GPS_TIMESTAMP,
            ExifInterfaceExtended.TAG_IMAGE_LENGTH,
            ExifInterfaceExtended.TAG_IMAGE_WIDTH,
            ExifInterfaceExtended.TAG_PHOTOGRAPHIC_SENSITIVITY,
            ExifInterfaceExtended.TAG_ORIENTATION,
            ExifInterfaceExtended.TAG_WHITE_BALANCE
    };

    @Before
    public void setUp() throws Exception {
        if (ENABLE_STRICT_MODE_FOR_UNBUFFERED_IO &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectUnbufferedIo()
                    .penaltyDeath()
                    .build());
        }

        for (int i = 0; i < IMAGE_RESOURCES.length; ++i) {
            File file = getFileFromExternalDir(IMAGE_FILENAMES[i]);
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                inputStream = getApplicationContext()
                        .getResources().openRawResource(IMAGE_RESOURCES[i]);
                outputStream = new FileOutputStream(file);
                copy(inputStream, outputStream);
            } finally {
                closeQuietly(inputStream);
                closeQuietly(outputStream);
            }
        }
    }

    @After
    public void tearDown() {
        for (int i = 0; i < IMAGE_RESOURCES.length; ++i) {
            File imageFile = getFileFromExternalDir(IMAGE_FILENAMES[i]);
            if (imageFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                imageFile.delete();
            }
        }
    }

    @Test
    @LargeTest
    public void testJpegFiles() throws Throwable {
        readFromFilesWithExif(JPEG_WITH_EXIF_BYTE_ORDER_II, array.jpeg_with_exif_byte_order_ii);
        writeToFilesWithExif(JPEG_WITH_EXIF_BYTE_ORDER_II, array.jpeg_with_exif_byte_order_ii);

        readFromFilesWithExif(JPEG_WITH_EXIF_BYTE_ORDER_MM, array.jpeg_with_exif_byte_order_mm);
        writeToFilesWithExif(JPEG_WITH_EXIF_BYTE_ORDER_MM, array.jpeg_with_exif_byte_order_mm);

        readFromFilesWithExif(JPEG_WITH_EXIF_WITH_XMP, array.jpeg_with_exif_with_xmp);
        writeToFilesWithExif(JPEG_WITH_EXIF_WITH_XMP, array.jpeg_with_exif_with_xmp);

        readFromFilesWithExif(JPEG_WITH_EXIF_WITH_PHOTOSHOP_WITH_XMP,
                array.jpeg_with_exif_with_photoshop_with_xmp);
        readFromFilesWithExif(JPEG_WITH_ICC_WITH_EXIF_WITH_EXTENDED_XMP,
                array.jpeg_with_icc_with_exif_with_extended_xmp);

        writeToFilesWithoutMetadata(JPEG_WITH_EXIF_BYTE_ORDER_II, JPEG_TEST, true, false);
        writeToFilesWithoutMetadata(JPEG_WITH_EXIF_BYTE_ORDER_II, JPEG_TEST, true, true);

        writeToFilesWithoutMetadata(JPEG_WITH_EXIF_BYTE_ORDER_MM, JPEG_TEST, true, false);
        writeToFilesWithoutMetadata(JPEG_WITH_EXIF_BYTE_ORDER_MM, JPEG_TEST, true, true);

        writeToFilesWithoutMetadata(JPEG_WITH_EXIF_WITH_XMP, JPEG_TEST, true, false);
        writeToFilesWithoutMetadata(JPEG_WITH_EXIF_WITH_XMP, JPEG_TEST, true, true);

        writeToFilesWithoutMetadata(JPEG_WITH_EXIF_WITH_PHOTOSHOP_WITH_XMP, JPEG_TEST, true, false);
        writeToFilesWithoutMetadata(JPEG_WITH_EXIF_WITH_PHOTOSHOP_WITH_XMP, JPEG_TEST, true, true);

        writeToFilesWithoutMetadata(JPEG_WITH_ICC_WITH_EXIF_WITH_EXTENDED_XMP, JPEG_TEST, true,
                false);
        writeToFilesWithoutMetadata(JPEG_WITH_ICC_WITH_EXIF_WITH_EXTENDED_XMP, JPEG_TEST, true,
                true);
    }

    @Test
    @LargeTest
    public void testDngFiles() throws Throwable {
        readFromFilesWithExif(DNG_WITH_EXIF_WITH_XMP, array.dng_with_exif_with_xmp);
    }

    @Test
    @LargeTest
    public void testPngFiles() throws Throwable {
        readFromFilesWithExif(PNG_WITH_EXIF_BYTE_ORDER_II, array.png_with_exif_byte_order_ii);

        writeToFilesWithoutExif(PNG_WITHOUT_EXIF);

        writeToFilesWithoutMetadata(PNG_WITH_EXIF_BYTE_ORDER_II, PNG_TEST, true, false);
        writeToFilesWithoutMetadata(PNG_WITH_EXIF_BYTE_ORDER_II, PNG_TEST, true, true);

        writeToFilesWithoutMetadata(PNG_WITHOUT_EXIF, PNG_TEST, false, false);
        writeToFilesWithoutMetadata(PNG_WITHOUT_EXIF, PNG_TEST, false, true);
    }

    @Test
    @LargeTest
    public void testStandaloneData() throws Throwable {
        readFromStandaloneDataWithExif(JPEG_WITH_EXIF_BYTE_ORDER_II,
                array.standalone_data_with_exif_byte_order_ii);
        readFromStandaloneDataWithExif(JPEG_WITH_EXIF_BYTE_ORDER_MM,
                array.standalone_data_with_exif_byte_order_mm);
    }

    @Test
    @LargeTest
    public void testWebpFiles() throws Throwable {
        readFromFilesWithExif(WEBP_WITH_EXIF, array.webp_with_exif);
        writeToFilesWithExif(WEBP_WITH_EXIF, array.webp_with_exif);
        readFromFilesWithExif(WEBP_WITH_ICC_WITH_EXIF_WITH_XMP,
                array.webp_with_icc_with_exif_with_xmp);
        writeToFilesWithExif(WEBP_WITH_ICC_WITH_EXIF_WITH_XMP,
                array.webp_with_icc_with_exif_with_xmp);

        writeToFilesWithoutExif(WEBP_WITHOUT_EXIF_WITH_ANIM_DATA);
        writeToFilesWithoutExif(WEBP_WITHOUT_EXIF);
        writeToFilesWithoutExif(WEBP_WITHOUT_EXIF_WITH_LOSSLESS_ENCODING);

        writeToFilesWithoutMetadata(WEBP_WITH_EXIF, WEBP_TEST, true, false);
        writeToFilesWithoutMetadata(WEBP_WITH_EXIF, WEBP_TEST, true, true);

        writeToFilesWithoutMetadata(WEBP_WITH_ICC_WITH_EXIF_WITH_XMP, WEBP_TEST, true, false);
        writeToFilesWithoutMetadata(WEBP_WITH_ICC_WITH_EXIF_WITH_XMP, WEBP_TEST, true, true);

        writeToFilesWithoutMetadata(WEBP_WITHOUT_EXIF_WITH_ANIM_DATA, WEBP_TEST, false, false);
        writeToFilesWithoutMetadata(WEBP_WITHOUT_EXIF_WITH_ANIM_DATA, WEBP_TEST, false, true);

        writeToFilesWithoutMetadata(WEBP_WITHOUT_EXIF, WEBP_TEST, false, false);
        writeToFilesWithoutMetadata(WEBP_WITHOUT_EXIF, WEBP_TEST, false, true);

        writeToFilesWithoutMetadata(WEBP_WITHOUT_EXIF_WITH_LOSSLESS_ENCODING, WEBP_TEST, false,
                false);
        writeToFilesWithoutMetadata(WEBP_WITHOUT_EXIF_WITH_LOSSLESS_ENCODING, WEBP_TEST, false,
                true);
    }

    /**
     * Support for retrieving EXIF from HEIF was added in SDK 28.
     */
    @Test
    @LargeTest
    public void testHeifFile() throws Throwable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Reading XMP data from HEIF was added in SDK 31.
            readFromFilesWithExif(HEIF_WITH_EXIF,
                    Build.VERSION.SDK_INT >= 31
                            ? array.heif_with_exif_31_and_above
                            : array.heif_with_exif);
        } else {
            // Make sure that an exception is not thrown and that image length/width tag values
            // return default values, not the actual values.
            File imageFile = getFileFromExternalDir(HEIF_WITH_EXIF);
            ExifInterfaceExtended exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
            String defaultTagValue = "0";
            assertEquals(defaultTagValue,
                    exif.getAttribute(ExifInterfaceExtended.TAG_IMAGE_LENGTH));
            assertEquals(defaultTagValue, exif.getAttribute(ExifInterfaceExtended.TAG_IMAGE_WIDTH));
        }
    }

    @Test
    @LargeTest
    public void testDoNotFailOnCorruptedImage() {
        // ExifInterface shouldn't raise any exceptions except an IOException when unable to open
        // a file, even with a corrupted image. Generates randomly corrupted image stream for
        // testing. Uses Epoch date count as random seed so that we can reproduce a broken test.
        long seed = System.currentTimeMillis() / (86400 * 1000);
        Log.d(TAG, "testDoNotFailOnCorruptedImage random seed: " + seed);
        Random random = new Random(seed);
        byte[] bytes = new byte[8096];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        for (int i = 0; i < TEST_NUMBER_OF_CORRUPTED_IMAGE_STREAMS; i++) {
            buffer.clear();
            random.nextBytes(bytes);
            if (isNotRandomlyCorrupted(random)) {
                buffer.put(ExifInterfaceExtended.JPEG_SIGNATURE);
            }
            if (isNotRandomlyCorrupted(random)) {
                buffer.put(ExifInterfaceExtended.MARKER_APP1);
            }
            buffer.putShort((short) (random.nextInt(100) + 300));
            if (isNotRandomlyCorrupted(random)) {
                buffer.put(ExifInterfaceExtended.IDENTIFIER_EXIF_APP1);
            }
            if (isNotRandomlyCorrupted(random)) {
                buffer.putShort(ExifInterfaceExtended.BYTE_ALIGN_MM);
            }
            if (isNotRandomlyCorrupted(random)) {
                buffer.put((byte) 0);
                buffer.put(ExifInterfaceExtended.START_CODE);
            }
            buffer.putInt(8);

            // Primary Tags
            int numberOfDirectory = random.nextInt(8) + 1;
            if (isNotRandomlyCorrupted(random)) {
                buffer.putShort((short) numberOfDirectory);
            }
            for (int j = 0; j < numberOfDirectory; j++) {
                generateRandomExifTag(buffer, ExifInterfaceExtended.IFD_TYPE_PRIMARY, random);
            }
            if (isNotRandomlyCorrupted(random)) {
                buffer.putInt(buffer.position() - 8);
            }

            // Thumbnail Tags
            numberOfDirectory = random.nextInt(8) + 1;
            if (isNotRandomlyCorrupted(random)) {
                buffer.putShort((short) numberOfDirectory);
            }
            for (int j = 0; j < numberOfDirectory; j++) {
                generateRandomExifTag(buffer, ExifInterfaceExtended.IFD_TYPE_THUMBNAIL, random);
            }
            if (isNotRandomlyCorrupted(random)) {
                buffer.putInt(buffer.position() - 8);
            }

            // Preview Tags
            numberOfDirectory = random.nextInt(8) + 1;
            if (isNotRandomlyCorrupted(random)) {
                buffer.putShort((short) numberOfDirectory);
            }
            for (int j = 0; j < numberOfDirectory; j++) {
                generateRandomExifTag(buffer, ExifInterfaceExtended.IFD_TYPE_PREVIEW, random);
            }
            if (isNotRandomlyCorrupted(random)) {
                buffer.putInt(buffer.position() - 8);
            }

            if (isNotRandomlyCorrupted(random)) {
                buffer.put(ExifInterfaceExtended.MARKER);
            }
            if (isNotRandomlyCorrupted(random)) {
                buffer.put(ExifInterfaceExtended.MARKER_EOI);
            }

            try {
                new ExifInterfaceExtended(new ByteArrayInputStream(bytes));
                // Always success
            } catch (IOException e) {
                fail("Should not reach here!");
            }
        }
    }

    @Test
    @SmallTest
    public void testSetGpsInfo() throws IOException {
        final String provider = "ExifInterfaceTest";
        final long timestamp = System.currentTimeMillis();
        final float speedInMeterPerSec = 36.627533f;
        Location location = new Location(provider);
        location.setLatitude(TEST_LATITUDE_VALID_VALUES[TEST_LATITUDE_VALID_VALUES.length - 1]);
        location.setLongitude(TEST_LONGITUDE_VALID_VALUES[TEST_LONGITUDE_VALID_VALUES.length - 1]);
        location.setAltitude(TEST_ALTITUDE_VALUES[TEST_ALTITUDE_VALUES.length - 1]);
        location.setSpeed(speedInMeterPerSec);
        location.setTime(timestamp);
        ExifInterfaceExtended exif = createTestExifInterface();
        exif.setGpsInfo(location);

        double[] latLong = exif.getLatLong();
        assertNotNull(latLong);
        assertEquals(TEST_LATITUDE_VALID_VALUES[TEST_LATITUDE_VALID_VALUES.length - 1],
                latLong[0], DELTA);
        assertEquals(TEST_LONGITUDE_VALID_VALUES[TEST_LONGITUDE_VALID_VALUES.length - 1],
                latLong[1], DELTA);
        assertEquals(TEST_ALTITUDE_VALUES[TEST_ALTITUDE_VALUES.length - 1], exif.getAltitude(0),
                RATIONAL_DELTA);
        assertEquals("K", exif.getAttribute(ExifInterfaceExtended.TAG_GPS_SPEED_REF));
        assertEquals(speedInMeterPerSec,
                exif.getAttributeDouble(ExifInterfaceExtended.TAG_GPS_SPEED, 0.0) * 1000 /
                        TimeUnit.HOURS.toSeconds(1), RATIONAL_DELTA);
        assertEquals(provider, exif.getAttribute(ExifInterfaceExtended.TAG_GPS_PROCESSING_METHOD));
        assertNotNull(exif.getGpsDateTime());
        // GPS time's precision is secs.
        assertEquals(TimeUnit.MILLISECONDS.toSeconds(timestamp),
                TimeUnit.MILLISECONDS.toSeconds(exif.getGpsDateTime()));
    }

    @Test
    @SmallTest
    public void testSetLatLong_withValidValues() throws IOException {
        for (int i = 0; i < TEST_LAT_LONG_VALUES_ARRAY_LENGTH; i++) {
            ExifInterfaceExtended exif = createTestExifInterface();
            exif.setLatLong(TEST_LATITUDE_VALID_VALUES[i], TEST_LONGITUDE_VALID_VALUES[i]);

            double[] latLong = exif.getLatLong();
            assertNotNull(latLong);
            assertEquals(TEST_LATITUDE_VALID_VALUES[i], latLong[0], DELTA);
            assertEquals(TEST_LONGITUDE_VALID_VALUES[i], latLong[1], DELTA);
        }
    }

    @Test
    @SmallTest
    public void testSetLatLong_withInvalidLatitude() throws IOException {
        for (int i = 0; i < TEST_LAT_LONG_VALUES_ARRAY_LENGTH; i++) {
            ExifInterfaceExtended exif = createTestExifInterface();
            try {
                exif.setLatLong(TEST_LATITUDE_INVALID_VALUES[i], TEST_LONGITUDE_VALID_VALUES[i]);
                fail();
            } catch (IllegalArgumentException e) {
                // expected
            }
            assertNull(exif.getLatLong());
            assertLatLongValuesAreNotSet(exif);
        }
    }

    @Test
    @SmallTest
    public void testSetLatLong_withInvalidLongitude() throws IOException {
        for (int i = 0; i < TEST_LAT_LONG_VALUES_ARRAY_LENGTH; i++) {
            ExifInterfaceExtended exif = createTestExifInterface();
            try {
                exif.setLatLong(TEST_LATITUDE_VALID_VALUES[i], TEST_LONGITUDE_INVALID_VALUES[i]);
                fail();
            } catch (IllegalArgumentException e) {
                // expected
            }
            assertNull(exif.getLatLong());
            assertLatLongValuesAreNotSet(exif);
        }
    }

    @Test
    @SmallTest
    public void testSetAltitude() throws IOException {
        for (double testAltitudeValue : TEST_ALTITUDE_VALUES) {
            ExifInterfaceExtended exif = createTestExifInterface();
            exif.setAltitude(testAltitudeValue);
            assertEquals(testAltitudeValue, exif.getAltitude(Double.NaN), RATIONAL_DELTA);
        }
    }

    /**
     * JPEG_WITH_DATETIME_TAG_PRIMARY_FORMAT contains the following tags:
     *   TAG_DATETIME, TAG_DATETIME_ORIGINAL, TAG_DATETIME_DIGITIZED = "2016:01:29 18:32:27"
     *   TAG_OFFSET_TIME, TAG_OFFSET_TIME_ORIGINAL, TAG_OFFSET_TIME_DIGITIZED = "100000"
     *   TAG_DATETIME, TAG_DATETIME_ORIGINAL, TAG_DATETIME_DIGITIZED = "+09:00"
     */
    @Test
    @SmallTest
    public void testGetSetDateTime() throws IOException {
        final long expectedGetDatetimeValue =
                1454027547000L /* TAG_DATETIME value ("2016:01:29 18:32:27") converted to msec */
                + 100L /* TAG_SUBSEC_TIME value ("100000") converted to msec */
                + 32400000L /* TAG_OFFSET_TIME value ("+09:00") converted to msec */;
        // GPS datetime does not support subsec precision
        final long expectedGetGpsDatetimeValue =
                1454027547000L /* TAG_DATETIME value ("2016:01:29 18:32:27") converted to msec */
                + 32400000L /* TAG_OFFSET_TIME value ("+09:00") converted to msec */;
        final String expectedDatetimeOffsetStringValue = "+09:00";

        File imageFile = getFileFromExternalDir(JPEG_WITH_DATETIME_TAG_PRIMARY_FORMAT);
        ExifInterfaceExtended exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        // Test getting datetime values
        assertNotNull(exif.getDateTime());
        assertNotNull(exif.getDateTimeOriginal());
        assertNotNull(exif.getDateTimeDigitized());
        assertNotNull(exif.getGpsDateTime());
        assertEquals(expectedGetDatetimeValue, (long) exif.getDateTime());
        assertEquals(expectedGetDatetimeValue, (long) exif.getDateTimeOriginal());
        assertEquals(expectedGetDatetimeValue, (long) exif.getDateTimeDigitized());
        assertEquals(expectedGetGpsDatetimeValue, (long) exif.getGpsDateTime());
        assertEquals(expectedDatetimeOffsetStringValue,
                exif.getAttribute(ExifInterfaceExtended.TAG_OFFSET_TIME));
        assertEquals(expectedDatetimeOffsetStringValue,
                exif.getAttribute(ExifInterfaceExtended.TAG_OFFSET_TIME_ORIGINAL));
        assertEquals(expectedDatetimeOffsetStringValue,
                exif.getAttribute(ExifInterfaceExtended.TAG_OFFSET_TIME_DIGITIZED));

        // Test setting datetime values
        final long currentTimeStamp = System.currentTimeMillis();
        final long expectedDatetimeOffsetLongValue = 32400000L;
        exif.setDateTime(currentTimeStamp);
        exif.saveAttributes();
        exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        assertNotNull(exif.getDateTime());
        assertEquals(currentTimeStamp - expectedDatetimeOffsetLongValue, (long) exif.getDateTime());

        // Test that setting null throws NPE
        try {
            //noinspection ConstantConditions
            exif.setDateTime(null);
            fail();
        } catch (NullPointerException e) {
            // Expected
        }

        // Test that setting negative value throws IAE
        try {
            exif.setDateTime(-1L);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    /**
     * Test whether ExifInterface can correctly get and set datetime value for a secondary format:
     * Primary format example: 2020:01:01 00:00:00
     * Secondary format example: 2020-01-01 00:00:00
     *
     * Getting a datetime tag value with the secondary format should work for both
     * {@link ExifInterfaceExtended#getAttribute(String)} and
     * {@link ExifInterfaceExtended#getDateTime()}.
     * Setting a datetime tag value with the secondary format with
     * {@link ExifInterfaceExtended#setAttribute(String, String)} should automatically convert it to
     * the primary format.
     *
     * JPEG_WITH_DATETIME_TAG_SECONDARY_FORMAT contains the following tags:
     *   TAG_DATETIME, TAG_DATETIME_ORIGINAL, TAG_DATETIME_DIGITIZED = "2016:01:29 18:32:27"
     *   TAG_OFFSET_TIME, TAG_OFFSET_TIME_ORIGINAL, TAG_OFFSET_TIME_DIGITIZED = "100000"
     *   TAG_DATETIME, TAG_DATETIME_ORIGINAL, TAG_DATETIME_DIGITIZED = "+09:00"
     */
    @Test
    @SmallTest
    public void testGetSetDateTimeForSecondaryFormat() throws Exception {
        // Test getting datetime values
        final long expectedGetDatetimeValue =
                1454027547000L /* TAG_DATETIME value ("2016:01:29 18:32:27") converted to msec */
                + 100L /* TAG_SUBSEC_TIME value ("100000") converted to msec */
                + 32400000L /* TAG_OFFSET_TIME value ("+09:00") converted to msec */;
        final String expectedDateTimeStringValue = "2016-01-29 18:32:27";

        File imageFile = getFileFromExternalDir(JPEG_WITH_DATETIME_TAG_SECONDARY_FORMAT);
        ExifInterfaceExtended exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        assertEquals(expectedDateTimeStringValue,
                exif.getAttribute(ExifInterfaceExtended.TAG_DATETIME));
        assertNotNull(exif.getDateTime());
        assertEquals(expectedGetDatetimeValue, (long) exif.getDateTime());

        // Test setting datetime value: check that secondary format value is modified correctly
        // when it is saved.
        final long newDateTimeLongValue =
                1577772000000L /* TAG_DATETIME value ("2020-01-01 00:00:00") converted to msec */
                + 100L /* TAG_SUBSEC_TIME value ("100000") converted to msec */
                + 32400000L /* TAG_OFFSET_TIME value ("+09:00") converted to msec */;
        final String newDateTimeStringValue = "2020-01-01 00:00:00";
        final String modifiedNewDateTimeStringValue = "2020:01:01 00:00:00";

        exif.setAttribute(ExifInterfaceExtended.TAG_DATETIME, newDateTimeStringValue);
        exif.saveAttributes();
        assertEquals(modifiedNewDateTimeStringValue,
                exif.getAttribute(ExifInterfaceExtended.TAG_DATETIME));
        assertEquals(newDateTimeLongValue, (long) exif.getDateTime());
    }

    @Test
    @LargeTest
    public void testAddDefaultValuesForCompatibility() throws Exception {
        File imageFile = getFileFromExternalDir(JPEG_WITH_DATETIME_TAG_PRIMARY_FORMAT);
        ExifInterfaceExtended exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());

        // 1. Check that the TAG_DATETIME value is not overwritten by TAG_DATETIME_ORIGINAL's value
        // when TAG_DATETIME value exists.
        final String dateTimeValue = "2017:02:02 22:22:22";
        final String dateTimeOriginalValue = "2017:01:01 11:11:11";
        exif.setAttribute(ExifInterfaceExtended.TAG_DATETIME, dateTimeValue);
        exif.setAttribute(ExifInterfaceExtended.TAG_DATETIME_ORIGINAL, dateTimeOriginalValue);
        exif.saveAttributes();
        exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        assertEquals(dateTimeValue, exif.getAttribute(ExifInterfaceExtended.TAG_DATETIME));
        assertEquals(dateTimeOriginalValue,
                exif.getAttribute(ExifInterfaceExtended.TAG_DATETIME_ORIGINAL));

        // 2. Check that when TAG_DATETIME has no value, it is set to TAG_DATETIME_ORIGINAL's value.
        exif.setAttribute(ExifInterfaceExtended.TAG_DATETIME, null);
        exif.saveAttributes();
        exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        assertEquals(dateTimeOriginalValue, exif.getAttribute(ExifInterfaceExtended.TAG_DATETIME));
    }

    @Test
    @LargeTest
    public void testSubsec() throws IOException {
        File imageFile = getFileFromExternalDir(JPEG_WITH_DATETIME_TAG_PRIMARY_FORMAT);
        ExifInterfaceExtended exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());

        // Set initial value to 0
        exif.setAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME, /* 0ms */ "000");
        exif.saveAttributes();
        assertEquals("000", exif.getAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME));
        assertNotNull(exif.getDateTime());
        long currentDateTimeValue = exif.getDateTime();

        // Test that single and double-digit values are set properly.
        // Note that since SubSecTime tag records fractions of a second, a single-digit value
        // should be counted as the first decimal value, which is why "1" becomes 100ms and "11"
        // becomes 110ms.
        String oneDigitSubSec = "1";
        exif.setAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME, oneDigitSubSec);
        exif.saveAttributes();
        assertEquals(currentDateTimeValue + 100, (long) exif.getDateTime());
        assertEquals(oneDigitSubSec, exif.getAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME));

        String twoDigitSubSec1 = "01";
        exif.setAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME, twoDigitSubSec1);
        exif.saveAttributes();
        assertEquals(currentDateTimeValue + 10, (long) exif.getDateTime());
        assertEquals(twoDigitSubSec1, exif.getAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME));

        String twoDigitSubSec2 = "11";
        exif.setAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME, twoDigitSubSec2);
        exif.saveAttributes();
        assertEquals(currentDateTimeValue + 110, (long) exif.getDateTime());
        assertEquals(twoDigitSubSec2, exif.getAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME));

        // Test that 3-digit values are set properly.
        String hundredMs = "100";
        exif.setAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME, hundredMs);
        exif.saveAttributes();
        assertEquals(currentDateTimeValue + 100, (long) exif.getDateTime());
        assertEquals(hundredMs, exif.getAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME));

        // Test that values starting with zero are also supported.
        String oneMsStartingWithZeroes = "001";
        exif.setAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME, oneMsStartingWithZeroes);
        exif.saveAttributes();
        assertEquals(currentDateTimeValue + 1, (long) exif.getDateTime());
        assertEquals(oneMsStartingWithZeroes,
                exif.getAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME));

        String tenMsStartingWithZero = "010";
        exif.setAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME, tenMsStartingWithZero);
        exif.saveAttributes();
        assertEquals(currentDateTimeValue + 10, (long) exif.getDateTime());
        assertEquals(tenMsStartingWithZero,
                exif.getAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME));

        // Test that values with more than three digits are set properly. getAttribute() should
        // return the whole string, but getDateTime() should only add the first three digits
        // because it supports only up to 1/1000th of a second.
        String fourDigitSubSec = "1234";
        exif.setAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME, fourDigitSubSec);
        exif.saveAttributes();
        assertEquals(currentDateTimeValue + 123, (long) exif.getDateTime());
        assertEquals(fourDigitSubSec, exif.getAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME));

        String fiveDigitSubSec = "23456";
        exif.setAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME, fiveDigitSubSec);
        exif.saveAttributes();
        assertEquals(currentDateTimeValue + 234, (long) exif.getDateTime());
        assertEquals(fiveDigitSubSec, exif.getAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME));

        String sixDigitSubSec = "345678";
        exif.setAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME, sixDigitSubSec);
        exif.saveAttributes();
        assertEquals(currentDateTimeValue + 345, (long) exif.getDateTime());
        assertEquals(sixDigitSubSec, exif.getAttribute(ExifInterfaceExtended.TAG_SUBSEC_TIME));
    }

    @Test
    @LargeTest
    public void testRotation() throws IOException {
        File imageFile = getFileFromExternalDir(JPEG_WITH_EXIF_BYTE_ORDER_II);
        ExifInterfaceExtended exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());

        int num;
        // Test flip vertically.
        for (num = 0; num < TEST_FLIP_VERTICALLY_STATE_MACHINE.length; num++) {
            exif.setAttribute(ExifInterfaceExtended.TAG_ORIENTATION,
                    Integer.toString(TEST_FLIP_VERTICALLY_STATE_MACHINE[num][0]));
            exif.flipVertically();
            exif.saveAttributes();
            exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
            assertIntTag(exif, ExifInterfaceExtended.TAG_ORIENTATION,
                    TEST_FLIP_VERTICALLY_STATE_MACHINE[num][1]);

        }

        // Test flip horizontally.
        for (num = 0; num < TEST_FLIP_VERTICALLY_STATE_MACHINE.length; num++) {
            exif.setAttribute(ExifInterfaceExtended.TAG_ORIENTATION,
                    Integer.toString(TEST_FLIP_HORIZONTALLY_STATE_MACHINE[num][0]));
            exif.flipHorizontally();
            exif.saveAttributes();
            exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
            assertIntTag(exif, ExifInterfaceExtended.TAG_ORIENTATION,
                    TEST_FLIP_HORIZONTALLY_STATE_MACHINE[num][1]);

        }

        // Test rotate by degrees
        exif.setAttribute(ExifInterfaceExtended.TAG_ORIENTATION,
                Integer.toString(ExifInterfaceExtended.ORIENTATION_NORMAL));
        try {
            exif.rotate(108);
            fail("Rotate with 108 degree should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Success
        }

        for (num = 0; num < TEST_ROTATION_STATE_MACHINE.length; num++) {
            exif.setAttribute(ExifInterfaceExtended.TAG_ORIENTATION,
                    Integer.toString(TEST_ROTATION_STATE_MACHINE[num][0]));
            exif.rotate(TEST_ROTATION_STATE_MACHINE[num][1]);
            exif.saveAttributes();
            exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
            assertIntTag(exif, ExifInterfaceExtended.TAG_ORIENTATION,
                    TEST_ROTATION_STATE_MACHINE[num][2]);
        }

        // Test get flip state and rotation degrees.
        for (Integer key : FLIP_STATE_AND_ROTATION_DEGREES.keySet()) {
            exif.setAttribute(ExifInterfaceExtended.TAG_ORIENTATION, key.toString());
            exif.saveAttributes();
            exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
            Pair<Boolean, Integer> p = FLIP_STATE_AND_ROTATION_DEGREES.get(key);
            assertNotNull(p);
            assertEquals(p.first, exif.isFlipped());
            assertEquals((long) p.second, exif.getRotationDegrees());
        }

        // Test reset the rotation.
        exif.setAttribute(ExifInterfaceExtended.TAG_ORIENTATION,
                Integer.toString(ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL));
        exif.resetOrientation();
        exif.saveAttributes();
        exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        assertIntTag(exif, ExifInterfaceExtended.TAG_ORIENTATION,
                ExifInterfaceExtended.ORIENTATION_NORMAL);

    }

    @Test
    @SmallTest
    public void testInterchangeabilityBetweenTwoIsoSpeedTags() throws IOException {
        // Tests that two tags TAG_ISO_SPEED_RATINGS and TAG_PHOTOGRAPHIC_SENSITIVITY can be used
        // interchangeably.
        @SuppressWarnings("deprecation")
        final String oldTag = ExifInterfaceExtended.TAG_ISO_SPEED_RATINGS;
        final String newTag = ExifInterfaceExtended.TAG_PHOTOGRAPHIC_SENSITIVITY;
        final String isoValue = "50";

        ExifInterfaceExtended exif = createTestExifInterface();
        exif.setAttribute(oldTag, isoValue);
        assertEquals(isoValue, exif.getAttribute(oldTag));
        assertEquals(isoValue, exif.getAttribute(newTag));

        exif = createTestExifInterface();
        exif.setAttribute(newTag, isoValue);
        assertEquals(isoValue, exif.getAttribute(oldTag));
        assertEquals(isoValue, exif.getAttribute(newTag));
    }

    private void printExifTagsAndValues(String fileName, ExifInterfaceExtended exifInterface) {
        // Prints thumbnail information.
        if (exifInterface.hasThumbnail()) {
            byte[] thumbnailBytes = exifInterface.getThumbnailBytes();
            if (thumbnailBytes != null) {
                Log.v(TAG, fileName + " Thumbnail size = " + thumbnailBytes.length);
                Bitmap bitmap = exifInterface.getThumbnailBitmap();
                if (bitmap == null) {
                    Log.e(TAG, fileName + " Corrupted thumbnail!");
                } else {
                    Log.v(TAG, fileName + " Thumbnail size: " + bitmap.getWidth() + ", "
                            + bitmap.getHeight());
                }
            } else {
                Log.e(TAG, fileName + " Unexpected result: No thumbnails were found. "
                        + "A thumbnail is expected.");
            }
        } else {
            if (exifInterface.getThumbnailBytes() != null) {
                Log.e(TAG, fileName + " Unexpected result: A thumbnail was found. "
                        + "No thumbnail is expected.");
            } else {
                Log.v(TAG, fileName + " No thumbnail");
            }
        }

        // Prints GPS information.
        Log.v(TAG, fileName + " Altitude = " + exifInterface.getAltitude(.0));

        double[] latLong = exifInterface.getLatLong();
        if (latLong != null) {
            Log.v(TAG, fileName + " Latitude = " + latLong[0]);
            Log.v(TAG, fileName + " Longitude = " + latLong[1]);
        } else {
            Log.v(TAG, fileName + " No latlong data");
        }

        // Prints values.
        for (String tagKey : EXIF_TAGS) {
            String tagValue = exifInterface.getAttribute(tagKey);
            Log.v(TAG, fileName + " Key{" + tagKey + "} = '" + tagValue + "'");
        }
    }

    private void assertIntTag(ExifInterfaceExtended exifInterface, String tag,
                              int expectedValue) {
        int intValue = exifInterface.getAttributeInt(tag, 0);
        assertEquals(expectedValue, intValue);
    }

    private void assertFloatTag(ExifInterfaceExtended exifInterface, String tag,
                                float expectedValue) {
        double doubleValue = exifInterface.getAttributeDouble(tag, 0.0);
        assertEquals(expectedValue, doubleValue, DIFFERENCE_TOLERANCE);
    }

    private void assertStringTag(ExifInterfaceExtended exifInterface, String tag,
                                 String expectedValue) {
        String stringValue = exifInterface.getAttribute(tag);
        if (stringValue != null) {
            stringValue = stringValue.trim();
        }
        stringValue = ("".equals(stringValue)) ? null : stringValue;

        assertEquals(expectedValue, stringValue);
    }

    private void compareWithExpectedValue(ExifInterfaceExtended exifInterface,
                                          ExpectedValue expectedValue, String verboseTag,
                                          boolean assertRanges) {
        if (VERBOSE) {
            printExifTagsAndValues(verboseTag, exifInterface);
        }
        // Checks a thumbnail image.
        assertEquals(expectedValue.hasThumbnail(), exifInterface.hasThumbnail());
        if (expectedValue.hasThumbnail()) {
            assertNotNull(exifInterface.getThumbnailRange());
            if (assertRanges) {
                final long[] thumbnailRange = exifInterface.getThumbnailRange();
                assertEquals(expectedValue.getThumbnailOffset(), thumbnailRange[0]);
                assertEquals(expectedValue.getThumbnailLength(), thumbnailRange[1]);
            }
            testThumbnail(expectedValue, exifInterface);
        } else {
            assertNull(exifInterface.getThumbnailRange());
            assertNull(exifInterface.getThumbnail());
        }

        // Checks GPS information.
        double[] latLong = exifInterface.getLatLong();
        assertEquals(expectedValue.hasLatLong(), latLong != null);
        if (expectedValue.hasLatLong()) {
            assertNotNull(exifInterface.getAttributeRange(ExifInterfaceExtended.TAG_GPS_LATITUDE));
            if (assertRanges) {
                final long[] latitudeRange = exifInterface
                        .getAttributeRange(ExifInterfaceExtended.TAG_GPS_LATITUDE);
                assertNotNull(latitudeRange);
                assertEquals(expectedValue.getLatitudeOffset(), latitudeRange[0]);
                assertEquals(expectedValue.getLatitudeLength(), latitudeRange[1]);
            }
            assertNotNull(latLong);
            assertEquals(expectedValue.getLatitude(), latLong[0], DIFFERENCE_TOLERANCE);
            assertEquals(expectedValue.getLongitude(), latLong[1], DIFFERENCE_TOLERANCE);
            assertTrue(exifInterface.hasAttribute(ExifInterfaceExtended.TAG_GPS_LATITUDE));
            assertTrue(exifInterface.hasAttribute(ExifInterfaceExtended.TAG_GPS_LONGITUDE));
        } else {
            assertNull(exifInterface.getAttributeRange(ExifInterfaceExtended.TAG_GPS_LATITUDE));
            assertFalse(exifInterface.hasAttribute(ExifInterfaceExtended.TAG_GPS_LATITUDE));
            assertFalse(exifInterface.hasAttribute(ExifInterfaceExtended.TAG_GPS_LONGITUDE));
        }
        assertEquals(expectedValue.getAltitude(), exifInterface.getAltitude(.0),
                DIFFERENCE_TOLERANCE);

        // Checks Make information.
        String make = exifInterface.getAttribute(ExifInterfaceExtended.TAG_MAKE);
        assertEquals(expectedValue.hasMake(), make != null);
        if (expectedValue.hasMake()) {
            assertNotNull(exifInterface.getAttributeRange(ExifInterfaceExtended.TAG_MAKE));
            if (assertRanges) {
                final long[] makeRange = exifInterface
                        .getAttributeRange(ExifInterfaceExtended.TAG_MAKE);
                assertNotNull(makeRange);
                assertEquals(expectedValue.getMakeOffset(), makeRange[0]);
                assertEquals(expectedValue.getMakeLength(), makeRange[1]);
            }
            assertEquals(expectedValue.getMake(), make);
        } else {
            assertNull(exifInterface.getAttributeRange(ExifInterfaceExtended.TAG_MAKE));
            assertFalse(exifInterface.hasAttribute(ExifInterfaceExtended.TAG_MAKE));
        }

        // Checks values.
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_MAKE, expectedValue.getMake());
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_MODEL, expectedValue.getModel());
        assertFloatTag(exifInterface, ExifInterfaceExtended.TAG_F_NUMBER,
                expectedValue.getAperture());
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_DATETIME_ORIGINAL,
                expectedValue.getDateTimeOriginal());
        assertFloatTag(exifInterface, ExifInterfaceExtended.TAG_EXPOSURE_TIME,
                expectedValue.getExposureTime());
        assertFloatTag(exifInterface, ExifInterfaceExtended.TAG_FLASH, expectedValue.getFlash());
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_FOCAL_LENGTH,
                expectedValue.getFocalLength());
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_GPS_ALTITUDE,
                expectedValue.getGpsAltitude());
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_GPS_ALTITUDE_REF,
                expectedValue.getGpsAltitudeRef());
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_GPS_DATESTAMP,
                expectedValue.getGpsDatestamp());
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_GPS_LATITUDE,
                expectedValue.getGpsLatitude());
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_GPS_LATITUDE_REF,
                expectedValue.getGpsLatitudeRef());
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_GPS_LONGITUDE,
                expectedValue.getGpsLongitude());
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_GPS_LONGITUDE_REF,
                expectedValue.getGpsLongitudeRef());
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_GPS_PROCESSING_METHOD,
                expectedValue.getGpsProcessingMethod());
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_GPS_TIMESTAMP,
                expectedValue.getGpsTimestamp());
        assertIntTag(exifInterface, ExifInterfaceExtended.TAG_IMAGE_LENGTH,
                expectedValue.getImageLength());
        assertIntTag(exifInterface, ExifInterfaceExtended.TAG_IMAGE_WIDTH,
                expectedValue.getImageWidth());
        assertStringTag(exifInterface, ExifInterfaceExtended.TAG_PHOTOGRAPHIC_SENSITIVITY,
                expectedValue.getIso());
        assertIntTag(exifInterface, ExifInterfaceExtended.TAG_ORIENTATION,
                expectedValue.getOrientation());
        assertIntTag(exifInterface, ExifInterfaceExtended.TAG_WHITE_BALANCE,
                expectedValue.getWhiteBalance());

        if (expectedValue.hasXmp()) {
            assertNotNull(exifInterface.getAttributeRange(ExifInterfaceExtended.TAG_XMP));
            if (assertRanges) {
                final long[] xmpRange =
                        exifInterface.getAttributeRange(ExifInterfaceExtended.TAG_XMP);
                assertNotNull(xmpRange);
                assertEquals(expectedValue.getXmpOffset(), xmpRange[0]);
                assertEquals(expectedValue.getXmpLength(), xmpRange[1]);
            }
            final String xmp =
                    new String(exifInterface.getAttributeBytes(ExifInterfaceExtended.TAG_XMP),
                    StandardCharsets.UTF_8);
            // We're only interested in confirming that we were able to extract
            // valid XMP data, which must always include this XML tag; a full
            // XMP parser is beyond the scope of ExifInterface. See XMP
            // Specification Part 1, Section C.2.2 for additional details.
            if (!xmp.contains("<rdf:RDF")) {
                fail("Invalid XMP: " + xmp);
            }
        } else {
            assertNull(exifInterface.getAttributeRange(ExifInterfaceExtended.TAG_XMP));
        }
        assertEquals(exifInterface.hasExtendedXmp(), expectedValue.hasExtendedXmp());
        assertEquals(exifInterface.hasIccProfile(), expectedValue.hasIccProfile());
        assertEquals(exifInterface.hasPhotoshopImageResources(),
                expectedValue.hasPhotoshopImageResources());
    }

    private void readFromStandaloneDataWithExif(String fileName, int typedArrayResourceId)
            throws IOException {
        ExpectedValue expectedValue = new ExpectedValue(
                getApplicationContext().getResources().obtainTypedArray(typedArrayResourceId));

        File imageFile = getFileFromExternalDir(fileName);
        String verboseTag = imageFile.getName();

        FileInputStream fis = new FileInputStream(imageFile);
        // Skip the following marker bytes (0xff, 0xd8, 0xff, 0xe1)
        if (fis.skip(4) != 4) {
            throw new IOException();
        }
        // Read the value of the length of the exif data
        short length = readShort(fis);
        byte[] exifBytes = new byte[length];
        if (fis.read(exifBytes) != exifBytes.length) {
            throw new IOException();
        }

        ByteArrayInputStream bin = new ByteArrayInputStream(exifBytes);
        ExifInterfaceExtended exifInterface =
                new ExifInterfaceExtended(bin, ExifInterfaceExtended.STREAM_TYPE_EXIF_DATA_ONLY);
        compareWithExpectedValue(exifInterface, expectedValue, verboseTag, true);
    }

    private void testExifInterfaceCommon(String fileName, ExpectedValue expectedValue)
            throws IOException {
        File imageFile = getFileFromExternalDir(fileName);
        String verboseTag = imageFile.getName();

        // Creates via file.
        ExifInterfaceExtended exifInterface = new ExifInterfaceExtended(imageFile);
        assertNotNull(exifInterface);
        compareWithExpectedValue(exifInterface, expectedValue, verboseTag, true);

        // Creates via path.
        exifInterface = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        assertNotNull(exifInterface);
        compareWithExpectedValue(exifInterface, expectedValue, verboseTag, true);

        InputStream in = null;
        // Creates via InputStream.
        try {
            in = new BufferedInputStream(new FileInputStream(imageFile.getAbsolutePath()));
            exifInterface = new ExifInterfaceExtended(in);
            compareWithExpectedValue(exifInterface, expectedValue, verboseTag, true);
        } finally {
            closeQuietly(in);
        }

        // Creates via FileDescriptor.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            FileDescriptor fd = null;
            try {
                fd = Os.open(imageFile.getAbsolutePath(), OsConstants.O_RDONLY,
                        OsConstants.S_IRWXU);
                exifInterface = new ExifInterfaceExtended(fd);
                compareWithExpectedValue(exifInterface, expectedValue, verboseTag, true);
            } catch (Exception e) {
                throw new IOException("Failed to open file descriptor", e);
            } finally {
                closeQuietly(fd);
            }
        }
    }

    private void testExifInterfaceRange(String fileName, ExpectedValue expectedValue)
            throws IOException {
        File imageFile = getFileFromExternalDir(fileName);

        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(imageFile.getAbsolutePath()));
            if (expectedValue.hasThumbnail()) {
                if (in.skip(expectedValue.getThumbnailOffset()) !=
                        expectedValue.getThumbnailOffset()) {
                    throw new IOException();
                }
                byte[] thumbnailBytes = new byte[expectedValue.getThumbnailLength()];
                if (in.read(thumbnailBytes) != expectedValue.getThumbnailLength()) {
                    throw new IOException("Failed to read the expected thumbnail length");
                }
                // TODO: Need a way to check uncompressed thumbnail file
                Bitmap thumbnailBitmap = BitmapFactory.decodeByteArray(thumbnailBytes, 0,
                        thumbnailBytes.length);
                assertNotNull(thumbnailBitmap);
                assertEquals(expectedValue.getThumbnailWidth(), thumbnailBitmap.getWidth());
                assertEquals(expectedValue.getThumbnailHeight(), thumbnailBitmap.getHeight());
            }

            // TODO: Creating a new input stream is a temporary
            //  workaround for BufferedInputStream#mark/reset not working properly for
            //  LG_G4_ISO_800_DNG. Need to investigate cause.
            in = new BufferedInputStream(new FileInputStream(imageFile.getAbsolutePath()));
            if (expectedValue.hasMake()) {
                if (in.skip(expectedValue.getMakeOffset()) != expectedValue.getMakeOffset()) {
                    throw new IOException();
                }
                byte[] makeBytes = new byte[expectedValue.getMakeLength()];
                if (in.read(makeBytes) != expectedValue.getMakeLength()) {
                    throw new IOException("Failed to read the expected make length");
                }
                String makeString = new String(makeBytes);
                // Remove null bytes
                makeString = makeString.replaceAll("\u0000.*", "");
                assertEquals(expectedValue.getMake(), makeString);
            }

            in = new BufferedInputStream(new FileInputStream(imageFile.getAbsolutePath()));
            if (expectedValue.hasXmp()) {
                if (in.skip(expectedValue.getXmpOffset()) != expectedValue.getXmpOffset()) {
                    throw new IOException();
                }
                byte[] identifierBytes = new byte[expectedValue.getXmpLength()];
                if (in.read(identifierBytes) != expectedValue.getXmpLength()) {
                    throw new IOException("Failed to read the expected xmp length");
                }
                final String identifier = new String(identifierBytes, StandardCharsets.UTF_8);
                final String xmpIdentifier = "<?xpacket begin=";
                final String extendedXmpIdentifier = "<x:xmpmeta xmlns:x=";
                assertTrue(identifier.startsWith(xmpIdentifier) ||
                        identifier.startsWith(extendedXmpIdentifier));
            }
            // TODO: Add code for retrieving raw latitude data using offset and length
        } finally {
            closeQuietly(in);
        }
    }

    private void writeToFilesWithExif(String fileName, int typedArrayResourceId)
            throws IOException {
        ExpectedValue expectedValue = new ExpectedValue(
                getApplicationContext().getResources().obtainTypedArray(typedArrayResourceId));

        File srcFile = getFileFromExternalDir(fileName);
        File imageFile = clone(srcFile);
        String verboseTag = imageFile.getName();

        ExifInterfaceExtended exifInterface =
                new ExifInterfaceExtended(imageFile.getAbsolutePath());
        exifInterface.saveAttributes();
        exifInterface = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        compareWithExpectedValue(exifInterface, expectedValue, verboseTag, false);
        assertBitmapsEquivalent(srcFile, imageFile);
        assertSecondSaveProducesSameSizeFile(imageFile);

        // Test for modifying one attribute.
        exifInterface = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        String backupValue = exifInterface.getAttribute(ExifInterfaceExtended.TAG_MAKE);
        exifInterface.setAttribute(ExifInterfaceExtended.TAG_MAKE, "abc");
        exifInterface.saveAttributes();
        // Check if thumbnail offset and length are properly updated without parsing the data again.
        if (expectedValue.hasThumbnail()) {
            testThumbnail(expectedValue, exifInterface);
        }
        assertEquals("abc", exifInterface.getAttribute(ExifInterfaceExtended.TAG_MAKE));
        // Check if thumbnail bytes can be retrieved from the new thumbnail range.
        if (expectedValue.hasThumbnail()) {
            testThumbnail(expectedValue, exifInterface);
        }

        // Restore the backup value.
        exifInterface.setAttribute(ExifInterfaceExtended.TAG_MAKE, backupValue);
        exifInterface.saveAttributes();
        exifInterface = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        compareWithExpectedValue(exifInterface, expectedValue, verboseTag, false);

        // Creates via FileDescriptor.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            FileDescriptor fd = null;
            try {
                fd = Os.open(imageFile.getAbsolutePath(), OsConstants.O_RDWR, OsConstants.S_IRWXU);
                exifInterface = new ExifInterfaceExtended(fd);
                exifInterface.setAttribute(ExifInterfaceExtended.TAG_MAKE, "abc");
                exifInterface.saveAttributes();
                assertEquals("abc", exifInterface.getAttribute(ExifInterfaceExtended.TAG_MAKE));
            } catch (Exception e) {
                throw new IOException("Failed to open file descriptor", e);
            } finally {
                closeQuietly(fd);
            }
        }
    }

    private void readFromFilesWithExif(String fileName, int typedArrayResourceId)
            throws IOException {
        ExpectedValue expectedValue = new ExpectedValue(
                getApplicationContext().getResources().obtainTypedArray(typedArrayResourceId));

        // Test for reading from external data storage.
        testExifInterfaceCommon(fileName, expectedValue);

        // Test for checking expected range by retrieving raw data with given offset and length.
        testExifInterfaceRange(fileName, expectedValue);
    }

    private void writeToFilesWithoutExif(String fileName) throws IOException {
        File srcFile = getFileFromExternalDir(fileName);
        File imageFile = clone(srcFile);

        ExifInterfaceExtended exifInterface =
                new ExifInterfaceExtended(imageFile.getAbsolutePath());
        exifInterface.setAttribute(ExifInterfaceExtended.TAG_MAKE, "abc");
        exifInterface.saveAttributes();

        assertBitmapsEquivalent(srcFile, imageFile);
        exifInterface = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        String make = exifInterface.getAttribute(ExifInterfaceExtended.TAG_MAKE);
        assertEquals("abc", make);

        assertSecondSaveProducesSameSizeFile(imageFile);
    }

    private void writeToFilesWithoutMetadata(
            String fileName,
            String fileOutName,
            boolean hasMetadata,
            boolean preserveOrientation
    ) throws IOException {
        File source = getFileFromExternalDir(fileName);
        File sink = getFileFromExternalDir(fileOutName);
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(sink);

        try {
            final ExifInterfaceExtended sourceExifInterface =
                    new ExifInterfaceExtended(source.getAbsolutePath());
            if (hasMetadata) {
                assertTrue(sourceExifInterface.hasAttributes(false));
            }
            String orientation =
                    sourceExifInterface.getAttribute(ExifInterfaceExtended.TAG_ORIENTATION);
            sourceExifInterface.saveExclusive(in, out, preserveOrientation);
            final ExifInterfaceExtended sinkExifInterface =
                    new ExifInterfaceExtended(sink.getAbsolutePath());
            assertFalse(sinkExifInterface.hasIccProfile());
            assertFalse(sinkExifInterface.hasXmp());
            assertFalse(sinkExifInterface.hasExtendedXmp());
            assertFalse(sinkExifInterface.hasPhotoshopImageResources());
            if (preserveOrientation) {
                for (String tag : EXIF_TAGS) {
                    String attribute = sinkExifInterface.getAttribute(tag);
                    switch (tag) {
                        case ExifInterfaceExtended.TAG_IMAGE_WIDTH:
                        case ExifInterfaceExtended.TAG_IMAGE_LENGTH:
                            // Ignore
                            break;
                        case ExifInterfaceExtended.TAG_LIGHT_SOURCE:
                            assertEquals("0", attribute);
                            break;
                        case ExifInterfaceExtended.TAG_ORIENTATION:
                            assertEquals(orientation, attribute);
                            break;
                        default:
                            assertNull(attribute);
                            break;

                    }
                }
            } else {
                assertFalse(sinkExifInterface.hasAttributes(true));
            }
        } finally {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    private void testThumbnail(ExpectedValue expectedValue, ExifInterfaceExtended exifInterface) {
        byte[] thumbnail = exifInterface.getThumbnail();
        assertNotNull(thumbnail);
        Bitmap thumbnailBitmap = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
        assertNotNull(thumbnailBitmap);
        assertEquals(expectedValue.getThumbnailWidth(), thumbnailBitmap.getWidth());
        assertEquals(expectedValue.getThumbnailHeight(), thumbnailBitmap.getHeight());
    }

    private void generateRandomExifTag(ByteBuffer buffer, int ifdType, Random random) {
        ExifTag[] tagGroup = ExifInterfaceExtended.EXIF_TAGS[ifdType];
        ExifTag tag = tagGroup[random.nextInt(tagGroup.length)];
        if (isNotRandomlyCorrupted(random)) {
            buffer.putShort((short) tag.getNumber());
        }
        int dataFormat = random.nextInt(ExifInterfaceExtended.IFD_FORMAT_NAMES.length);
        if (isNotRandomlyCorrupted(random)) {
            buffer.putShort((short) dataFormat);
        }
        buffer.putInt(1);
        int dataLength = ExifInterfaceExtended.IFD_FORMAT_BYTES_PER_FORMAT[dataFormat];
        if (dataLength > 4) {
            buffer.putShort((short) random.nextInt(8096 - dataLength));
            buffer.position(buffer.position() + 2);
        } else {
            buffer.position(buffer.position() + 4);
        }
    }

    private boolean isNotRandomlyCorrupted(Random random) {
        // Corrupts somewhere in a possibility of 1/500.
        return random.nextInt(500) != 0;
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    private void closeQuietly(FileDescriptor fd) {
        if (fd != null) {
            try {
                Os.close(fd);
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    private void assertLatLongValuesAreNotSet(ExifInterfaceExtended exif) {
        assertNull(exif.getAttribute(ExifInterfaceExtended.TAG_GPS_LATITUDE));
        assertNull(exif.getAttribute(ExifInterfaceExtended.TAG_GPS_LATITUDE_REF));
        assertNull(exif.getAttribute(ExifInterfaceExtended.TAG_GPS_LONGITUDE));
        assertNull(exif.getAttribute(ExifInterfaceExtended.TAG_GPS_LONGITUDE_REF));
    }

    private ExifInterfaceExtended createTestExifInterface() throws IOException {
        File image = File.createTempFile(TEST_TEMP_FILE_NAME, ".jpg");
        image.deleteOnExit();
        return new ExifInterfaceExtended(image.getAbsolutePath());
    }

    private short readShort(InputStream is) throws IOException {
        int ch1 = is.read();
        int ch2 = is.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (short) ((ch1 << 8) + (ch2));
    }

    private File getFileFromExternalDir(String fileName) {
        return new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                fileName);
    }

    /**
     * Asserts that {@code expectedImageFile} and {@code actualImageFile} can be decoded by
     * {@link BitmapFactory} and the results have the same width, height and MIME type.
     *
     * <p>This does not check the image itself for similarity/equality.
     */
    private void assertBitmapsEquivalent(File expectedImageFile, File actualImageFile) {
        BitmapFactory.Options expectedOptions = new BitmapFactory.Options();
        Bitmap expectedBitmap = decodeBitmap(expectedImageFile, expectedOptions);
        BitmapFactory.Options actualOptions = new BitmapFactory.Options();
        Bitmap actualBitmap = decodeBitmap(actualImageFile, actualOptions);

        assertEquals(expectedOptions.outWidth, actualOptions.outWidth);
        assertEquals(expectedOptions.outHeight, actualOptions.outHeight);
        assertEquals(expectedOptions.outMimeType, actualOptions.outMimeType);
        assertEquals(expectedBitmap.getWidth(), actualBitmap.getWidth());
        assertEquals(expectedBitmap.getHeight(), actualBitmap.getHeight());
    }

    /**
     * Equivalent to {@link BitmapFactory#decodeFile(String, BitmapFactory.Options)} but uses a
     * {@link BufferedInputStream} to avoid violating
     * {@link StrictMode.ThreadPolicy.Builder#detectUnbufferedIo()}.
     */
    private static Bitmap decodeBitmap(File file, BitmapFactory.Options options) {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            return BitmapFactory.decodeStream(inputStream, /* outPadding= */ null, options);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Asserts that saving the file the second time (without modifying any attributes) produces
     * exactly the same length file as the first save. The first save (with no modifications) is
     * expected to (possibly) change the file length because {@link ExifInterfaceExtended} may move/reformat
     * the Exif block within the file, but the second save should not make further modifications.
     */
    private void assertSecondSaveProducesSameSizeFile(File imageFileAfterOneSave)
            throws IOException {
        File imageFileAfterTwoSaves = clone(imageFileAfterOneSave);
        ExifInterfaceExtended exifInterface = new ExifInterfaceExtended(imageFileAfterTwoSaves.getAbsolutePath());
        exifInterface.saveAttributes();
        if (imageFileAfterOneSave.getAbsolutePath().endsWith(".png")
                || imageFileAfterOneSave.getAbsolutePath().endsWith(".webp")) {
            // PNG and (some) WebP files are (surprisingly) modified between the first and second
            // save (b/249097443), so we check the difference between second and third save instead.
            File imageFileAfterThreeSaves = clone(imageFileAfterTwoSaves);
            exifInterface = new ExifInterfaceExtended(imageFileAfterThreeSaves.getAbsolutePath());
            exifInterface.saveAttributes();
            assertEquals(imageFileAfterTwoSaves.length(), imageFileAfterThreeSaves.length());
        } else {
            assertEquals(imageFileAfterOneSave.length(), imageFileAfterTwoSaves.length());
        }
    }

    private File clone(File original) throws IOException {
        File cloned =
                File.createTempFile("tmp_", System.nanoTime() + "_" + original.getName());
        try (FileInputStream inputStream = new FileInputStream(original);
             FileOutputStream outputStream = new FileOutputStream(cloned)) {
            copy(inputStream, outputStream);
        }
        return cloned;
    }
}
