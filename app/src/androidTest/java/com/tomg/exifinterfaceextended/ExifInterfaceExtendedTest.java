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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.StrictMode;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SmallTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
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
    private static final String WEBP_WITHOUT_EXIF_WITH_ANIM_DATA =
            "webp_with_anim_without_exif.webp";
    private static final String JPEG_TEST = "test.jpg";
    private static final String PNG_TEST = "test.png";
    private static final String WEBP_TEST = "test.webp";
    private static final double DELTA = 1e-8;
    // We translate double to rational in a 1/10000 precision.
    private static final double RATIONAL_DELTA = 0.0001;

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

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        if (ENABLE_STRICT_MODE_FOR_UNBUFFERED_IO &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectUnbufferedIo()
                    .penaltyDeath()
                    .build());
        }
    }

    @Test
    @LargeTest
    public void testJpegWithExifIntelByteOrder() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_exif_byte_order_ii,
                "jpeg_with_exif_byte_order_ii.jpg"
        );
        readFromFilesWithExif(imageFile, ExpectedAttributes.JPEG_WITH_EXIF_BYTE_ORDER_II);
        writeToFilesWithExif(imageFile, ExpectedAttributes.JPEG_WITH_EXIF_BYTE_ORDER_II);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(JPEG_TEST), true, false);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(JPEG_TEST), true, true);    }

    @Test
    @LargeTest
    public void testJpegWithExifMotorolaByteOrder() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_exif_byte_order_mm,
                "jpeg_with_exif_byte_order_mm.jpg"
        );
        readFromFilesWithExif(imageFile, ExpectedAttributes.JPEG_WITH_EXIF_BYTE_ORDER_MM);
        writeToFilesWithExif(imageFile, ExpectedAttributes.JPEG_WITH_EXIF_BYTE_ORDER_MM);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(JPEG_TEST), true, false);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(JPEG_TEST), true, true);
    }

    @Test
    @LargeTest
    public void testJpegWithExifAndXmp() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_exif_with_xmp,
                "jpeg_with_exif_with_xmp.jpg"
        );
        readFromFilesWithExif(imageFile, ExpectedAttributes.JPEG_WITH_EXIF_WITH_XMP);
        writeToFilesWithExif(imageFile, ExpectedAttributes.JPEG_WITH_EXIF_WITH_XMP);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(JPEG_TEST), true, false);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(JPEG_TEST), true, true);
    }

    @Test
    @LargeTest
    public void testJpegWithExifAndExtendedXmp() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_icc_with_exif_with_extended_xmp,
                "jpeg_with_icc_with_exif_with_extended_xmp.jpg"
        );
        readFromFilesWithExif(imageFile, ExpectedAttributes.JPEG_WITH_ICC_WITH_EXIF_WITH_EXTENDED_XMP);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(JPEG_TEST), true, false);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(JPEG_TEST), true, true);
    }

    @Test
    @LargeTest
    public void testJpegWithExifAndPhotoshop() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_exif_with_photoshop_with_xmp,
                "jpeg_with_exif_with_photoshop_with_xmp.jpg"
        );
        readFromFilesWithExif(imageFile, ExpectedAttributes.JPEG_WITH_EXIF_WITH_PHOTSHOP_WITH_XMP);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(JPEG_TEST), true, false);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(JPEG_TEST), true, true);
    }

    // https://issuetracker.google.com/264729367
    @Test
    @LargeTest
    public void testJpegWithInvalidOffset() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_exif_invalid_offset,
                "jpeg_with_exif_invalid_offset.jpg"
        );
        readFromFilesWithExif(imageFile, ExpectedAttributes.JPEG_WITH_EXIF_INVALID_OFFSET);
        writeToFilesWithExif(imageFile, ExpectedAttributes.JPEG_WITH_EXIF_INVALID_OFFSET);
    }

    // https://issuetracker.google.com/263747161
    @Test
    @LargeTest
    public void testJpegWithFullApp1Segment() throws Throwable {
        File srcFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_exif_full_app1_segment,
                "jpeg_with_exif_full_app1_segment.jpg"
        );
        File imageFile = clone(srcFile);
        ExifInterfaceExtended exifInterface = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        // Add a really long string that makes the Exif data too large for the JPEG APP1 segment.
        char[] longStringChars = new char[500];
        Arrays.fill(longStringChars, 'a');
        String longString = new String(longStringChars);
        exifInterface.setAttribute(ExifInterfaceExtended.TAG_MAKE, longString);

        IOException expected = assertThrows(IOException.class,
                exifInterface::saveAttributes);
        assertThat(expected)
                .hasCauseThat()
                .hasMessageThat()
                .contains("exceeds the max size of a JPEG APP1 segment");
        assertBitmapsEquivalent(srcFile, imageFile);
    }

    @Test
    @LargeTest
    public void testDngWithExifAndXmp() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.dng_with_exif_with_xmp,
                "dng_with_exif_with_xmp.dng"
        );
        readFromFilesWithExif(imageFile, ExpectedAttributes.DNG_WITH_EXIF_WITH_XMP);
    }

    @Test
    @LargeTest
    public void testPngWithExif() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.png_with_exif_byte_order_ii,
                "png_with_exif_byte_order_ii.png"
        );
        readFromFilesWithExif(imageFile, ExpectedAttributes.PNG_WITH_EXIF_BYTE_ORDER_II);
        writeToFilesWithExif(imageFile, ExpectedAttributes.PNG_WITH_EXIF_BYTE_ORDER_II);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(PNG_TEST), true, false);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(PNG_TEST), true, true);
    }

    @Test
    @LargeTest
    public void testPngWithoutExif() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.png_with_exif_byte_order_ii,
                "png_without_exif.png"
        );
        writeToFilesWithoutExif(imageFile);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(PNG_TEST), false, false);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(PNG_TEST), false, true);
    }

    @Test
    @LargeTest
    public void testStandaloneData_jpegIntelByteOrder() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_exif_byte_order_ii,
                "jpeg_with_exif_byte_order_ii.jpg"
        );
        readFromStandaloneDataWithExif(
                imageFile, ExpectedAttributes.JPEG_WITH_EXIF_BYTE_ORDER_II_STANDALONE);
    }

    @Test
    @LargeTest
    public void testStandaloneData_jpegMotorolaByteOrder() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_exif_byte_order_mm,
                "jpeg_with_exif_byte_order_mm.jpg"
        );
        readFromStandaloneDataWithExif(
                imageFile, ExpectedAttributes.JPEG_WITH_EXIF_BYTE_ORDER_MM_STANDALONE);
    }

    @Test
    @LargeTest
    public void testWebpWithExif() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.webp_with_exif,
                "webp_with_exif.jpg"
        );
        readFromFilesWithExif(imageFile, ExpectedAttributes.WEBP_WITH_EXIF);
        writeToFilesWithExif(imageFile, ExpectedAttributes.WEBP_WITH_EXIF);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(WEBP_TEST), true, false);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(WEBP_TEST), true, true);
    }

    @Test
    @LargeTest
    public void testWebpWithExifAndXmp() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.webp_with_icc_with_exif_with_xmp,
                "webp_with_icc_with_exif_with_xmp.webp"
        );
        readFromFilesWithExif(imageFile, ExpectedAttributes.WEBP_WITH_ICC_WITH_EXIF_WITH_XMP);
        writeToFilesWithExif(imageFile, ExpectedAttributes.WEBP_WITH_ICC_WITH_EXIF_WITH_XMP);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(WEBP_TEST), true, false);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(WEBP_TEST), true, true);
    }

    // https://issuetracker.google.com/281638358
    @Test
    @LargeTest
    public void testWebpWithExifApp1() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.invalid_webp_with_jpeg_app1_marker,
                "invalid_webp_with_jpeg_app1_marker.webp"
        );
        readFromFilesWithExif(imageFile, ExpectedAttributes.INVALID_WEBP_WITH_JPEG_APP1_MARKER);
        writeToFilesWithExif(imageFile, ExpectedAttributes.INVALID_WEBP_WITH_JPEG_APP1_MARKER);
    }

    @Test
    @LargeTest
    public void testWebpWithoutExif() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.webp_without_exif,
                "webp_without_exif.webp"
        );
        writeToFilesWithoutExif(imageFile);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(WEBP_TEST), false, false);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(WEBP_TEST), false, true);
    }

    @Test
    @LargeTest
    public void testWebpWithoutExifWithAnimData() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.webp_with_anim_without_exif,
                WEBP_WITHOUT_EXIF_WITH_ANIM_DATA
        );
        writeToFilesWithoutExif(imageFile);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(WEBP_TEST), false, false);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(WEBP_TEST), false, true);
    }

    @Test
    @LargeTest
    public void testWebpWithoutExifWithLosslessEncoding() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.webp_lossless_without_exif,
                "webp_lossless_without_exif.webp"
        );
        writeToFilesWithoutExif(imageFile);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(WEBP_TEST), false, false);
        writeToFilesWithoutMetadata(imageFile, resolveImageFile(WEBP_TEST), false, true);
    }

    @Test
    @LargeTest
    public void testWebpWithoutExifWithLosslessEncodingAndAlpha() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.webp_lossless_alpha_without_exif,
                "webp_lossless_alpha_without_exif.webp"
        );
        writeToFilesWithoutExif(imageFile);
    }

    /**
     * Support for retrieving EXIF from HEIF was added in SDK 28.
     */
    @Test
    @LargeTest
    public void testHeifFile() throws Throwable {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.heif_with_exif,
                "heif_with_exif.heic"
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Reading XMP data from HEIF was added in SDK 31.
            readFromFilesWithExif(
                    imageFile,
                    Build.VERSION.SDK_INT >= 31
                            ? ExpectedAttributes.HEIF_WITH_EXIF_API_31_AND_ABOVE
                            : ExpectedAttributes.HEIF_WITH_EXIF_BELOW_API_31);
        } else {
            // Make sure that an exception is not thrown and that image length/width tag values
            // return default values, not the actual values.
            ExifInterfaceExtended exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
            String defaultTagValue = "0";
            assertEquals(defaultTagValue,
                    exif.getAttribute(ExifInterfaceExtended.TAG_IMAGE_LENGTH));
            assertEquals(defaultTagValue, exif.getAttribute(ExifInterfaceExtended.TAG_IMAGE_WIDTH));
        }
    }

    @Test
    @SmallTest
    public void testDoNotFailOnCorruptedImage() throws Throwable {
        Random random = new Random(/* seed= */ 0);
        byte[] bytes = new byte[8096];
        random.nextBytes(bytes);
        // Overwrite the start of the random bytes with some JPEG-like data, so it starts like a
        // plausible image with EXIF data.
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put(ExifInterfaceExtended.JPEG_SIGNATURE);
        buffer.put(ExifInterfaceExtended.MARKER_APP1);
        buffer.putShort((short) 350);
        buffer.put(ExifInterfaceExtended.IDENTIFIER_EXIF_APP1);
        buffer.putShort(ExifInterfaceExtended.BYTE_ALIGN_MM);
        buffer.put((byte) 0);
        buffer.put(ExifInterfaceExtended.START_CODE);
        buffer.putInt(8);
        // Number of primary tag directories
        buffer.putShort((short) 1);
        // Corruption starts here

        ExifInterfaceExtended exifInterface = new ExifInterfaceExtended(new ByteArrayInputStream(bytes));
        exifInterface.getAttribute(ExifInterfaceExtended.TAG_ARTIST);
        // Test will fail if the ExifInterface constructor or getter throw an exception.
    }

    @Test
    @SmallTest
    public void testSetGpsInfo() throws IOException {
        final String provider = "ExifInterfaceTest";
        final long timestamp = 1689328448000L; // 2023-07-14T09:54:32.000Z
        final float speedInMeterPerSec = 36.627533f;
        double latitudeDegrees = -68.3434534737;
        double longitudeDegrees = -58.57834236352;
        double altitudeMeters = 18.02038;
        Location location = new Location(provider);
        location.setLatitude(latitudeDegrees);
        location.setLongitude(longitudeDegrees);
        location.setAltitude(altitudeMeters);
        location.setSpeed(speedInMeterPerSec);
        location.setTime(timestamp);
        ExifInterfaceExtended exif = createTestExifInterface();
        exif.setGpsInfo(location);

        double[] latLong = exif.getLatLong();
        assertThat(latLong)
                .usingTolerance(DELTA)
                .containsExactly(latitudeDegrees, longitudeDegrees)
                .inOrder();
        assertThat(exif.getAltitude(0)).isWithin(RATIONAL_DELTA).of(altitudeMeters);
        assertThat(exif.getAttribute(ExifInterfaceExtended.TAG_GPS_SPEED_REF))
                .isEqualTo("K");
        float speedInKmph = speedInMeterPerSec * TimeUnit.HOURS.toSeconds(1) / 1000;
        assertThat(exif.getAttributeDouble(ExifInterfaceExtended.TAG_GPS_SPEED, 0.0))
                .isWithin(RATIONAL_DELTA)
                .of(speedInKmph);
        assertThat(exif.getAttribute(ExifInterfaceExtended.TAG_GPS_PROCESSING_METHOD))
                .isEqualTo(provider);
        // GPS time's precision is secs.
        assertThat(TimeUnit.MILLISECONDS.toSeconds(exif.getGpsDateTime()))
                .isEqualTo(TimeUnit.MILLISECONDS.toSeconds(timestamp));
    }

    @Test
    @SmallTest
    public void testSetLatLong_withValidValues() throws IOException {
        testSetLatLong(0d, 0d);
        testSetLatLong(45d, -45d);
        testSetLatLong(90d, 90d);
        testSetLatLong(-60d, -120d);
        testSetLatLong(0.00000001, 180d);
        testSetLatLong(89.999999999, 0.00000001);
        testSetLatLong(14.2465923626, -179.99999999999);
        testSetLatLong(-68.3434534737, -58.57834236352);
    }

    private void testSetLatLong(double latitude, double longitude) throws IOException {
        ExifInterfaceExtended exif = createTestExifInterface();
        exif.setLatLong(latitude, longitude);
        assertThat(exif.getLatLong())
                .usingTolerance(DELTA)
                .containsExactly(latitude, longitude)
                .inOrder();
    }

    @Test
    @SmallTest
    public void testSetLatLong_withInvalidLatitude() throws IOException {
        double longitude = -5.003427;
        assertLatLongInvalidAndNotPersisted(Double.NaN, longitude);
        assertLatLongInvalidAndNotPersisted(Double.POSITIVE_INFINITY, longitude);
        assertLatLongInvalidAndNotPersisted(Double.NEGATIVE_INFINITY, longitude);
        assertLatLongInvalidAndNotPersisted(90.0000000001, longitude);
        assertLatLongInvalidAndNotPersisted(263.34763236326, longitude);
        assertLatLongInvalidAndNotPersisted(-1e5, longitude);
        assertLatLongInvalidAndNotPersisted(347.32525, longitude);
        assertLatLongInvalidAndNotPersisted(-176.346347754, longitude);
    }

    @Test
    @SmallTest
    public void testSetLatLong_withInvalidLongitude() throws IOException {
        double latitude = 56.796626;
        assertLatLongInvalidAndNotPersisted(latitude, Double.NaN);
        assertLatLongInvalidAndNotPersisted(latitude, Double.POSITIVE_INFINITY);
        assertLatLongInvalidAndNotPersisted(latitude, Double.NEGATIVE_INFINITY);
        assertLatLongInvalidAndNotPersisted(latitude, 180.0000000001);
        assertLatLongInvalidAndNotPersisted(latitude, 263.34763236326);
        assertLatLongInvalidAndNotPersisted(latitude, -1e10);
        assertLatLongInvalidAndNotPersisted(latitude, 347.325252623);
        assertLatLongInvalidAndNotPersisted(latitude, -4000.346323236);
    }

    /**
     * Passes the parameters to {@link ExifInterfaceExtended#setLatLong} and asserts it throws
     * an exception because one or both are invalid, and then all the corresponding getters still
     * return 'unset' values.
     */
    private void assertLatLongInvalidAndNotPersisted(double latitude, double longitude)
            throws IOException {
        ExifInterfaceExtended exif = createTestExifInterface();
        assertThrows(IllegalArgumentException.class, () -> exif.setLatLong(latitude, longitude));
        assertThat(exif.getLatLong()).isNull();
        assertThat(exif.getAttribute(ExifInterfaceExtended.TAG_GPS_LATITUDE)).isNull();
        assertThat(exif.getAttribute(ExifInterfaceExtended.TAG_GPS_LATITUDE_REF)).isNull();
        assertThat(exif.getAttribute(ExifInterfaceExtended.TAG_GPS_LONGITUDE)).isNull();
        assertThat(exif.getAttribute(ExifInterfaceExtended.TAG_GPS_LONGITUDE_REF)).isNull();
    }

    @Test
    @SmallTest
    public void testSetAltitude() throws IOException {
        testSetAltitudeInternal(0);
        testSetAltitudeInternal(-2000);
        testSetAltitudeInternal(10000);
        testSetAltitudeInternal(-355.99999999999);
        testSetAltitudeInternal(18.02038);
    }

    private void testSetAltitudeInternal(double altitude) throws IOException {
        ExifInterfaceExtended exif = createTestExifInterface();
        exif.setAltitude(altitude);
        assertThat(exif.getAltitude(Double.NaN)).isWithin(RATIONAL_DELTA).of(altitude);
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

        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_datetime_tag_primary_format,
                "jpeg_with_datetime_tag_primary_format.jpg"
        );
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
        final long newTimestamp = 1689328448000L; // 2023-07-14T09:54:32.000Z
        final long expectedDatetimeOffsetLongValue = 32400000L;
        exif.setDateTime(newTimestamp);
        exif.saveAttributes();
        exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        assertNotNull(exif.getDateTime());
        assertEquals(newTimestamp - expectedDatetimeOffsetLongValue, (long) exif.getDateTime());

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
     * <p>
     * Getting a datetime tag value with the secondary format should work for both
     * {@link ExifInterfaceExtended#getAttribute(String)} and
     * {@link ExifInterfaceExtended#getDateTime()}.
     * Setting a datetime tag value with the secondary format with
     * {@link ExifInterfaceExtended#setAttribute(String, String)} should automatically convert it to
     * the primary format.
     * <p>
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

        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_datetime_tag_secondary_format,
                "jpeg_with_datetime_tag_secondary_format.jpg"
        );
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
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_datetime_tag_primary_format,
                "jpeg_with_datetime_tag_primary_format.jpg"
        );
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
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_datetime_tag_primary_format,
                "jpeg_with_datetime_tag_primary_format.jpg"
        );
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
        // Test flip vertically.
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_UNDEFINED,
                ExifInterfaceExtended::flipVertically,
                ExifInterfaceExtended.ORIENTATION_UNDEFINED);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_NORMAL,
                ExifInterfaceExtended::flipVertically,
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_90,
                ExifInterfaceExtended::flipVertically,
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_180,
                ExifInterfaceExtended::flipVertically,
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_270,
                ExifInterfaceExtended::flipVertically,
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL,
                ExifInterfaceExtended::flipVertically,
                ExifInterfaceExtended.ORIENTATION_NORMAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL,
                ExifInterfaceExtended::flipVertically,
                ExifInterfaceExtended.ORIENTATION_ROTATE_180);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE,
                ExifInterfaceExtended::flipVertically,
                ExifInterfaceExtended.ORIENTATION_ROTATE_270);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE,
                ExifInterfaceExtended::flipVertically,
                ExifInterfaceExtended.ORIENTATION_ROTATE_90);

        // Test flip horizontally.
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_UNDEFINED,
                ExifInterfaceExtended::flipHorizontally,
                ExifInterfaceExtended.ORIENTATION_UNDEFINED);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_NORMAL,
                ExifInterfaceExtended::flipHorizontally,
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_90,
                ExifInterfaceExtended::flipHorizontally,
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_180,
                ExifInterfaceExtended::flipHorizontally,
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_270,
                ExifInterfaceExtended::flipHorizontally,
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL,
                ExifInterfaceExtended::flipHorizontally,
                ExifInterfaceExtended.ORIENTATION_ROTATE_180);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL,
                ExifInterfaceExtended::flipHorizontally,
                ExifInterfaceExtended.ORIENTATION_NORMAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE,
                ExifInterfaceExtended::flipHorizontally,
                ExifInterfaceExtended.ORIENTATION_ROTATE_90);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE,
                ExifInterfaceExtended::flipHorizontally,
                ExifInterfaceExtended.ORIENTATION_ROTATE_270);

        // Test rotate by degrees
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_UNDEFINED,
                exifInterface -> exifInterface.rotate(-90),
                ExifInterfaceExtended.ORIENTATION_UNDEFINED);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_UNDEFINED,
                exifInterface -> exifInterface.rotate(0),
                ExifInterfaceExtended.ORIENTATION_UNDEFINED);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_UNDEFINED,
                exifInterface -> exifInterface.rotate(90),
                ExifInterfaceExtended.ORIENTATION_UNDEFINED);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_UNDEFINED,
                exifInterface -> exifInterface.rotate(180),
                ExifInterfaceExtended.ORIENTATION_UNDEFINED);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_UNDEFINED,
                exifInterface -> exifInterface.rotate(270),
                ExifInterfaceExtended.ORIENTATION_UNDEFINED);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_UNDEFINED,
                exifInterface -> exifInterface.rotate(540),
                ExifInterfaceExtended.ORIENTATION_UNDEFINED);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_NORMAL,
                exifInterface -> exifInterface.rotate(-90),
                ExifInterfaceExtended.ORIENTATION_ROTATE_270);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_NORMAL,
                exifInterface -> exifInterface.rotate(0),
                ExifInterfaceExtended.ORIENTATION_NORMAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_NORMAL,
                exifInterface -> exifInterface.rotate(90),
                ExifInterfaceExtended.ORIENTATION_ROTATE_90);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_NORMAL,
                exifInterface -> exifInterface.rotate(180),
                ExifInterfaceExtended.ORIENTATION_ROTATE_180);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_NORMAL,
                exifInterface -> exifInterface.rotate(270),
                ExifInterfaceExtended.ORIENTATION_ROTATE_270);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_NORMAL,
                exifInterface -> exifInterface.rotate(540),
                ExifInterfaceExtended.ORIENTATION_ROTATE_180);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_90,
                exifInterface -> exifInterface.rotate(-90),
                ExifInterfaceExtended.ORIENTATION_NORMAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_90,
                exifInterface -> exifInterface.rotate(0),
                ExifInterfaceExtended.ORIENTATION_ROTATE_90);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_90,
                exifInterface -> exifInterface.rotate(90),
                ExifInterfaceExtended.ORIENTATION_ROTATE_180);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_90,
                exifInterface -> exifInterface.rotate(180),
                ExifInterfaceExtended.ORIENTATION_ROTATE_270);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_90,
                exifInterface -> exifInterface.rotate(270),
                ExifInterfaceExtended.ORIENTATION_NORMAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_90,
                exifInterface -> exifInterface.rotate(540),
                ExifInterfaceExtended.ORIENTATION_ROTATE_270);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_180,
                exifInterface -> exifInterface.rotate(-90),
                ExifInterfaceExtended.ORIENTATION_ROTATE_90);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_180,
                exifInterface -> exifInterface.rotate(0),
                ExifInterfaceExtended.ORIENTATION_ROTATE_180);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_180,
                exifInterface -> exifInterface.rotate(90),
                ExifInterfaceExtended.ORIENTATION_ROTATE_270);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_180,
                exifInterface -> exifInterface.rotate(180),
                ExifInterfaceExtended.ORIENTATION_NORMAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_180,
                exifInterface -> exifInterface.rotate(270),
                ExifInterfaceExtended.ORIENTATION_ROTATE_90);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_180,
                exifInterface -> exifInterface.rotate(540),
                ExifInterfaceExtended.ORIENTATION_NORMAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_270,
                exifInterface -> exifInterface.rotate(-90),
                ExifInterfaceExtended.ORIENTATION_ROTATE_180);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_270,
                exifInterface -> exifInterface.rotate(0),
                ExifInterfaceExtended.ORIENTATION_ROTATE_270);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_270,
                exifInterface -> exifInterface.rotate(90),
                ExifInterfaceExtended.ORIENTATION_NORMAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_270,
                exifInterface -> exifInterface.rotate(180),
                ExifInterfaceExtended.ORIENTATION_ROTATE_90);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_270,
                exifInterface -> exifInterface.rotate(270),
                ExifInterfaceExtended.ORIENTATION_ROTATE_180);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_ROTATE_270,
                exifInterface -> exifInterface.rotate(540),
                ExifInterfaceExtended.ORIENTATION_ROTATE_90);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL,
                exifInterface -> exifInterface.rotate(-90),
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL,
                exifInterface -> exifInterface.rotate(0),
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL,
                exifInterface -> exifInterface.rotate(90),
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL,
                exifInterface -> exifInterface.rotate(180),
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL,
                exifInterface -> exifInterface.rotate(270),
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL,
                exifInterface -> exifInterface.rotate(540),
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL,
                exifInterface -> exifInterface.rotate(-90),
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL,
                exifInterface -> exifInterface.rotate(0),
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL,
                exifInterface -> exifInterface.rotate(90),
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL,
                exifInterface -> exifInterface.rotate(180),
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL,
                exifInterface -> exifInterface.rotate(270),
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL,
                exifInterface -> exifInterface.rotate(540),
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE,
                exifInterface -> exifInterface.rotate(-90),
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE,
                exifInterface -> exifInterface.rotate(0),
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE,
                exifInterface -> exifInterface.rotate(90),
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE,
                exifInterface -> exifInterface.rotate(180),
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE,
                exifInterface -> exifInterface.rotate(270),
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE,
                exifInterface -> exifInterface.rotate(540),
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE,
                exifInterface -> exifInterface.rotate(-90),
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE,
                exifInterface -> exifInterface.rotate(0),
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE,
                exifInterface -> exifInterface.rotate(90),
                ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE,
                exifInterface -> exifInterface.rotate(180),
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE,
                exifInterface -> exifInterface.rotate(270),
                ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL);
        testModifyOrientation(
                ExifInterfaceExtended.ORIENTATION_TRANSVERSE,
                exifInterface -> exifInterface.rotate(540),
                ExifInterfaceExtended.ORIENTATION_TRANSPOSE);
    }

    private void testModifyOrientation(
            int originalOrientation,
            ExifInterfaceExtendedOperation rotationOperation,
            int expectedOrientation)
            throws IOException {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_exif_byte_order_ii,
                "jpeg_with_exif_byte_order_ii.jpg"
        );
        ExifInterfaceExtended exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        exif.setAttribute(
                ExifInterfaceExtended.TAG_ORIENTATION, Integer.toString(originalOrientation));
        rotationOperation.applyTo(exif);
        exif.saveAttributes();
        exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        assertThat(exif.getAttributeInt(ExifInterfaceExtended.TAG_ORIENTATION, 0))
                .isEqualTo(expectedOrientation);
        imageFile.delete();
    }

    @Test
    @LargeTest
    public void testRotation_byDegrees_invalid() throws IOException {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_exif_byte_order_ii,
                "jpeg_with_exif_byte_order_ii.jpg"
        );
        ExifInterfaceExtended exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        assertThrows(IllegalArgumentException.class, () -> exif.rotate(108));
    }

    @Test
    @LargeTest
    public void testRotation_flipState() throws IOException {
        testFlipStateAndRotation(ExifInterfaceExtended.ORIENTATION_UNDEFINED, false, 0);
        testFlipStateAndRotation(ExifInterfaceExtended.ORIENTATION_NORMAL, false, 0);
        testFlipStateAndRotation(ExifInterfaceExtended.ORIENTATION_ROTATE_90, false, 90);
        testFlipStateAndRotation(ExifInterfaceExtended.ORIENTATION_ROTATE_180, false, 180);
        testFlipStateAndRotation(ExifInterfaceExtended.ORIENTATION_ROTATE_270, false, 270);
        testFlipStateAndRotation(ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL, true, 0);
        testFlipStateAndRotation(ExifInterfaceExtended.ORIENTATION_TRANSVERSE, true, 90);
        testFlipStateAndRotation(ExifInterfaceExtended.ORIENTATION_FLIP_VERTICAL, true, 180);
        testFlipStateAndRotation(ExifInterfaceExtended.ORIENTATION_TRANSPOSE, true, 270);
    }

    private void testFlipStateAndRotation(
            int orientation,
            boolean expectedFlipState,
            int expectedDegrees
    ) throws IOException {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_exif_byte_order_ii,
                "jpeg_with_exif_byte_order_ii.jpg"
        );
        ExifInterfaceExtended exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        exif.setAttribute(ExifInterfaceExtended.TAG_ORIENTATION, String.valueOf(orientation));
        exif.saveAttributes();
        exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        assertThat(exif.isFlipped()).isEqualTo(expectedFlipState);
        assertThat(exif.getRotationDegrees()).isEqualTo(expectedDegrees);
        imageFile.delete();
    }

    @Test
    @LargeTest
    public void testResetOrientation() throws IOException {
        File imageFile = copyFromResourceToFile(
                com.tomg.exifinterfaceextended.test.R.raw.jpeg_with_exif_byte_order_ii,
                "jpeg_with_exif_byte_order_ii.jpg"
        );
        ExifInterfaceExtended exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        exif.setAttribute(
                ExifInterfaceExtended.TAG_ORIENTATION,
                Integer.toString(ExifInterfaceExtended.ORIENTATION_FLIP_HORIZONTAL)
        );
        exif.resetOrientation();
        exif.saveAttributes();
        exif = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        assertIntTag(
                exif,
                ExifInterfaceExtended.TAG_ORIENTATION,
                ExifInterfaceExtended.ORIENTATION_NORMAL
        );
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

    private void compareWithExpectedAttributes(
            ExifInterfaceExtended exifInterface,
            ExpectedAttributes expectedAttributes,
            String verboseTag,
            boolean assertRanges
    ) {
        if (VERBOSE) {
            printExifTagsAndValues(verboseTag, exifInterface);
        }
        // Checks a thumbnail image.
        assertEquals(expectedAttributes.hasThumbnail(), exifInterface.hasThumbnail());
        if (expectedAttributes.hasThumbnail()) {
            assertNotNull(exifInterface.getThumbnailRange());
            if (assertRanges) {
                final long[] thumbnailRange = exifInterface.getThumbnailRange();
                assertEquals(expectedAttributes.getThumbnailOffset(), thumbnailRange[0]);
                assertEquals(expectedAttributes.getThumbnailLength(), thumbnailRange[1]);
            }
            testThumbnail(expectedAttributes, exifInterface);
        } else {
            assertNull(exifInterface.getThumbnailRange());
            assertNull(exifInterface.getThumbnail());
        }

        // Checks GPS information.
        double[] latLong = exifInterface.getLatLong();
        assertEquals(expectedAttributes.hasLatLong(), latLong != null);
        if (expectedAttributes.hasLatLong()) {
            assertNotNull(exifInterface.getAttributeRange(ExifInterfaceExtended.TAG_GPS_LATITUDE));
            if (assertRanges) {
                final long[] latitudeRange = exifInterface
                        .getAttributeRange(ExifInterfaceExtended.TAG_GPS_LATITUDE);
                assertNotNull(latitudeRange);
                assertEquals(expectedAttributes.getLatitudeOffset(), latitudeRange[0]);
                assertEquals(expectedAttributes.getLatitudeLength(), latitudeRange[1]);
            }
            assertNotNull(latLong);
            assertEquals(expectedAttributes.getLatitude(), latLong[0], DIFFERENCE_TOLERANCE);
            assertEquals(expectedAttributes.getLongitude(), latLong[1], DIFFERENCE_TOLERANCE);
            assertTrue(exifInterface.hasAttribute(ExifInterfaceExtended.TAG_GPS_LATITUDE));
            assertTrue(exifInterface.hasAttribute(ExifInterfaceExtended.TAG_GPS_LONGITUDE));
        } else {
            assertNull(exifInterface.getAttributeRange(ExifInterfaceExtended.TAG_GPS_LATITUDE));
            assertFalse(exifInterface.hasAttribute(ExifInterfaceExtended.TAG_GPS_LATITUDE));
            assertFalse(exifInterface.hasAttribute(ExifInterfaceExtended.TAG_GPS_LONGITUDE));
        }
        assertEquals(
                expectedAttributes.getAltitude(),
                exifInterface.getAltitude(.0),
                DIFFERENCE_TOLERANCE
        );

        // Checks Make information.
        String make = exifInterface.getAttribute(ExifInterfaceExtended.TAG_MAKE);
        assertEquals(expectedAttributes.hasMake(), make != null);
        if (expectedAttributes.hasMake()) {
            assertNotNull(exifInterface.getAttributeRange(ExifInterfaceExtended.TAG_MAKE));
            if (assertRanges) {
                final long[] makeRange = exifInterface
                        .getAttributeRange(ExifInterfaceExtended.TAG_MAKE);
                assertNotNull(makeRange);
                assertEquals(expectedAttributes.getMakeOffset(), makeRange[0]);
                assertEquals(expectedAttributes.getMakeLength(), makeRange[1]);
            }
            assertEquals(expectedAttributes.getMake(), make);
        } else {
            assertNull(exifInterface.getAttributeRange(ExifInterfaceExtended.TAG_MAKE));
            assertFalse(exifInterface.hasAttribute(ExifInterfaceExtended.TAG_MAKE));
        }

        // Checks values.
        assertStringTag(
                exifInterface, ExifInterfaceExtended.TAG_MAKE, expectedAttributes.getMake());
        assertStringTag(
                exifInterface, ExifInterfaceExtended.TAG_MODEL, expectedAttributes.getModel());
        assertFloatTag(
                exifInterface,
                ExifInterfaceExtended.TAG_F_NUMBER,
                expectedAttributes.getAperture()
        );
        assertStringTag(
                exifInterface,
                ExifInterfaceExtended.TAG_DATETIME_ORIGINAL,
                expectedAttributes.getDateTimeOriginal()
        );
        assertFloatTag(
                exifInterface,
                ExifInterfaceExtended.TAG_EXPOSURE_TIME,
                expectedAttributes.getExposureTime()
        );
        assertStringTag(
                exifInterface,
                ExifInterfaceExtended.TAG_FOCAL_LENGTH,
                expectedAttributes.getFocalLength()
        );
        assertStringTag(
                exifInterface,
                ExifInterfaceExtended.TAG_GPS_ALTITUDE,
                expectedAttributes.getGpsAltitude()
        );
        assertStringTag(
                exifInterface,
                ExifInterfaceExtended.TAG_GPS_ALTITUDE_REF,
                expectedAttributes.getGpsAltitudeRef()
        );
        assertStringTag(
                exifInterface,
                ExifInterfaceExtended.TAG_GPS_DATESTAMP,
                expectedAttributes.getGpsDatestamp()
        );
        assertStringTag(
                exifInterface,
                ExifInterfaceExtended.TAG_GPS_LATITUDE,
                expectedAttributes.getGpsLatitude()
        );
        assertStringTag(
                exifInterface,
                ExifInterfaceExtended.TAG_GPS_LATITUDE_REF,
                expectedAttributes.getGpsLatitudeRef()
        );
        assertStringTag(
                exifInterface,
                ExifInterfaceExtended.TAG_GPS_LONGITUDE,
                expectedAttributes.getGpsLongitude()
        );
        assertStringTag(
                exifInterface,
                ExifInterfaceExtended.TAG_GPS_LONGITUDE_REF,
                expectedAttributes.getGpsLongitudeRef()
        );
        assertStringTag(
                exifInterface,
                ExifInterfaceExtended.TAG_GPS_PROCESSING_METHOD,
                expectedAttributes.getGpsProcessingMethod()
        );
        assertStringTag(
                exifInterface,
                ExifInterfaceExtended.TAG_GPS_TIMESTAMP,
                expectedAttributes.getGpsTimestamp()
        );
        assertIntTag(
                exifInterface,
                ExifInterfaceExtended.TAG_IMAGE_LENGTH,
                expectedAttributes.getImageLength()
        );
        assertIntTag(
                exifInterface,
                ExifInterfaceExtended.TAG_IMAGE_WIDTH,
                expectedAttributes.getImageWidth()
        );
        assertStringTag(
                exifInterface,
                ExifInterfaceExtended.TAG_PHOTOGRAPHIC_SENSITIVITY,
                expectedAttributes.getIso()
        );
        assertIntTag(
                exifInterface,
                ExifInterfaceExtended.TAG_ORIENTATION,
                expectedAttributes.getOrientation()
        );

        if (expectedAttributes.hasXmp()) {
            assertNotNull(exifInterface.getAttributeRange(ExifInterfaceExtended.TAG_XMP));
            if (assertRanges) {
                final long[] xmpRange =
                        exifInterface.getAttributeRange(ExifInterfaceExtended.TAG_XMP);
                assertNotNull(xmpRange);
                assertEquals(expectedAttributes.getXmpOffset(), xmpRange[0]);
                assertEquals(expectedAttributes.getXmpLength(), xmpRange[1]);
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
        assertEquals(exifInterface.hasExtendedXmp(), expectedAttributes.hasExtendedXmp());
        assertEquals(exifInterface.hasIccProfile(), expectedAttributes.hasIccProfile());
        assertEquals(
                exifInterface.hasPhotoshopImageResources(),
                expectedAttributes.hasPhotoshopImageResources()
        );
    }

    private void readFromStandaloneDataWithExif(
            File imageFile,
            ExpectedAttributes expectedAttributes
    ) throws IOException {
        String verboseTag = imageFile.getName();
        byte[] exifBytes;
        try (FileInputStream fis = new FileInputStream(imageFile)) {
            // Skip the following marker bytes (0xff, 0xd8, 0xff, 0xe1)
            ByteStreams.skipFully(fis, 4);
            // Read the value of the length of the exif data
            short length = readShort(fis);
            exifBytes = new byte[length];
            ByteStreams.readFully(fis, exifBytes);
        }

        ByteArrayInputStream bin = new ByteArrayInputStream(exifBytes);
        ExifInterfaceExtended exifInterface =
                new ExifInterfaceExtended(bin, ExifInterfaceExtended.STREAM_TYPE_EXIF_DATA_ONLY);
        compareWithExpectedAttributes(exifInterface, expectedAttributes, verboseTag, true);
    }

    private void testExifInterfaceCommon(
            File imageFile,
            ExpectedAttributes expectedAttributes
    ) throws IOException {
        String verboseTag = imageFile.getName();

        // Creates via file.
        ExifInterfaceExtended exifInterface = new ExifInterfaceExtended(imageFile);
        assertNotNull(exifInterface);
        compareWithExpectedAttributes(exifInterface, expectedAttributes, verboseTag, true);

        // Creates via path.
        exifInterface = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        assertNotNull(exifInterface);
        compareWithExpectedAttributes(exifInterface, expectedAttributes, verboseTag, true);

        // Creates via InputStream.
        try (InputStream in = new BufferedInputStream(
                java.nio.file.Files.newInputStream(Paths.get(imageFile.getAbsolutePath())))
        ) {
            exifInterface = new ExifInterfaceExtended(in);
            compareWithExpectedAttributes(exifInterface, expectedAttributes, verboseTag, true);
        }

        // Creates via FileDescriptor.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            FileDescriptor fd = null;
            try {
                fd = Os.open(imageFile.getAbsolutePath(), OsConstants.O_RDONLY,
                        OsConstants.S_IRWXU);
                exifInterface = new ExifInterfaceExtended(fd);
                compareWithExpectedAttributes(exifInterface, expectedAttributes, verboseTag, true);
            } catch (Exception e) {
                throw new IOException("Failed to open file descriptor", e);
            } finally {
                closeQuietly(fd);
            }
        }
    }

    private void testExifInterfaceRange(
            File imageFile,
            ExpectedAttributes expectedAttributes
    ) throws IOException {
        try (InputStream in = new BufferedInputStream(
                java.nio.file.Files.newInputStream(Paths.get(imageFile.getAbsolutePath())))
        ) {
            if (expectedAttributes.hasThumbnail()) {
                ByteStreams.skipFully(in, expectedAttributes.getThumbnailOffset());
                byte[] thumbnailBytes = new byte[expectedAttributes.getThumbnailLength()];
                ByteStreams.readFully(in, thumbnailBytes);
                // TODO: Need a way to check uncompressed thumbnail file
                Bitmap thumbnailBitmap = BitmapFactory.decodeByteArray(thumbnailBytes, 0,
                        thumbnailBytes.length);
                assertNotNull(thumbnailBitmap);
                assertEquals(expectedAttributes.getThumbnailWidth(), thumbnailBitmap.getWidth());
                assertEquals(expectedAttributes.getThumbnailHeight(), thumbnailBitmap.getHeight());
            }
        }

        // TODO: Creating a new input stream is a temporary
        //  workaround for BufferedInputStream#mark/reset not working properly for
        //  LG_G4_ISO_800_DNG. Need to investigate cause.
        try (InputStream in = new BufferedInputStream(
                java.nio.file.Files.newInputStream(Paths.get(imageFile.getAbsolutePath())))
        ) {
            if (expectedAttributes.hasMake()) {
                ByteStreams.skipFully(in, expectedAttributes.getMakeOffset());
                byte[] makeBytes = new byte[expectedAttributes.getMakeLength()];
                ByteStreams.readFully(in, makeBytes);
                String makeString = new String(makeBytes);
                // Remove null bytes
                makeString = makeString.replaceAll("\u0000.*", "");
                assertEquals(expectedAttributes.getMake(), makeString);
            }
        }

        try (InputStream in = new BufferedInputStream(
                java.nio.file.Files.newInputStream(Paths.get(imageFile.getAbsolutePath())))
        ) {
            if (expectedAttributes.hasXmp()) {
                ByteStreams.skipFully(in, expectedAttributes.getXmpOffset());
                byte[] identifierBytes = new byte[expectedAttributes.getXmpLength()];
                ByteStreams.readFully(in, identifierBytes);
                final String identifier = new String(identifierBytes, StandardCharsets.UTF_8);
                final String xmpIdentifier = "<?xpacket begin=";
                final String extendedXmpIdentifier = "<x:xmpmeta xmlns:x=";
                assertTrue(identifier.startsWith(xmpIdentifier) ||
                        identifier.startsWith(extendedXmpIdentifier));
            }
            // TODO: Add code for retrieving raw latitude data using offset and length
        }
    }

    private void writeToFilesWithExif(
            File srcFile,
            ExpectedAttributes expectedAttributes
    ) throws IOException {
        File imageFile = clone(srcFile);
        String verboseTag = imageFile.getName();

        ExifInterfaceExtended exifInterface =
                new ExifInterfaceExtended(imageFile.getAbsolutePath());
        exifInterface.saveAttributes();
        exifInterface = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        compareWithExpectedAttributes(exifInterface, expectedAttributes, verboseTag, false);
        assertBitmapsEquivalent(srcFile, imageFile);
        assertSecondSaveProducesSameSizeFile(imageFile);

        // Test for modifying one attribute.
        exifInterface = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        String backupValue = exifInterface.getAttribute(ExifInterfaceExtended.TAG_MAKE);
        exifInterface.setAttribute(ExifInterfaceExtended.TAG_MAKE, "abc");
        exifInterface.saveAttributes();
        // Check if thumbnail offset and length are properly updated without parsing the data again.
        if (expectedAttributes.hasThumbnail()) {
            testThumbnail(expectedAttributes, exifInterface);
        }
        assertEquals("abc", exifInterface.getAttribute(ExifInterfaceExtended.TAG_MAKE));
        // Check if thumbnail bytes can be retrieved from the new thumbnail range.
        if (expectedAttributes.hasThumbnail()) {
            testThumbnail(expectedAttributes, exifInterface);
        }

        // Restore the backup value.
        exifInterface.setAttribute(ExifInterfaceExtended.TAG_MAKE, backupValue);
        exifInterface.saveAttributes();
        exifInterface = new ExifInterfaceExtended(imageFile.getAbsolutePath());
        compareWithExpectedAttributes(exifInterface, expectedAttributes, verboseTag, false);

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

    private void readFromFilesWithExif(
            File imageFile,
            ExpectedAttributes expectedAttributes
    ) throws IOException {
        // Test for reading from external data storage.
        testExifInterfaceCommon(imageFile, expectedAttributes);
        // Test for checking expected range by retrieving raw data with given offset and length.
        testExifInterfaceRange(imageFile, expectedAttributes);
    }

    private void writeToFilesWithoutExif(File srcFile) throws IOException {
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
            File source,
            File sink,
            boolean hasMetadata,
            boolean preserveOrientation
    ) throws IOException {
        try (InputStream in = java.nio.file.Files.newInputStream(source.toPath())) {
            try (OutputStream out = java.nio.file.Files.newOutputStream(sink.toPath())) {
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
            }
        }
    }

    private void testThumbnail(
            ExpectedAttributes expectedAttributes,
            ExifInterfaceExtended exifInterface
    ) {
        byte[] thumbnail = exifInterface.getThumbnail();
        assertNotNull(thumbnail);
        Bitmap thumbnailBitmap = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
        assertNotNull(thumbnailBitmap);
        assertEquals(expectedAttributes.getThumbnailWidth(), thumbnailBitmap.getWidth());
        assertEquals(expectedAttributes.getThumbnailHeight(), thumbnailBitmap.getHeight());
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

    private ExifInterfaceExtended createTestExifInterface() throws IOException {
        File originalFile = tempFolder.newFile();
        File jpgFile = new File(originalFile.getAbsolutePath() + ".jpg");
        if (!originalFile.renameTo(jpgFile)) {
            throw new IOException("Rename from " + originalFile + " to " + jpgFile + " failed.");
        }
        return new ExifInterfaceExtended(jpgFile.getAbsolutePath());
    }

    private short readShort(InputStream is) throws IOException {
        int ch1 = is.read();
        int ch2 = is.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (short) ((ch1 << 8) + (ch2));
    }

    /**
     * Asserts that {@code expectedImageFile} and {@code actualImageFile} can be decoded by
     * {@link BitmapFactory} and the results have the same width, height and MIME type.
     *
     * <p>The assertion is skipped if the test is running on an API level where
     * {@link BitmapFactory} is known not to support the image format of {@code expectedImageFile}
     * (as determined by file extension).
     *
     * <p>This does not check the image itself for similarity/equality.
     */
    private void assertBitmapsEquivalent(File expectedImageFile, File actualImageFile) {
        if (Build.VERSION.SDK_INT < 26
                && expectedImageFile.getName().equals(WEBP_WITHOUT_EXIF_WITH_ANIM_DATA)) {
            // BitmapFactory can't parse animated WebP files on API levels before 26: b/259964971
            return;
        }
        BitmapFactory.Options expectedOptions = new BitmapFactory.Options();
        Bitmap expectedBitmap = Objects.requireNonNull(
                decodeBitmap(expectedImageFile, expectedOptions));
        BitmapFactory.Options actualOptions = new BitmapFactory.Options();
        Bitmap actualBitmap = Objects.requireNonNull(decodeBitmap(actualImageFile, actualOptions));

        assertEquals(expectedOptions.outWidth, actualOptions.outWidth);
        assertEquals(expectedOptions.outHeight, actualOptions.outHeight);
        assertEquals(expectedOptions.outMimeType, actualOptions.outMimeType);
        assertEquals(expectedBitmap.getWidth(), actualBitmap.getWidth());
        assertEquals(expectedBitmap.getHeight(), actualBitmap.getHeight());
        assertEquals(expectedBitmap.hasAlpha(), actualBitmap.hasAlpha());
    }

    /**
     * Equivalent to {@link BitmapFactory#decodeFile(String, BitmapFactory.Options)} but uses a
     * {@link BufferedInputStream} to avoid violating
     * {@link StrictMode.ThreadPolicy.Builder#detectUnbufferedIo()}.
     */
    private static Bitmap decodeBitmap(File file, BitmapFactory.Options options) {
        try (BufferedInputStream inputStream = new BufferedInputStream(
                java.nio.file.Files.newInputStream(file.toPath()))
        ) {
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
        Files.copy(original, cloned);
        return cloned;
    }

    private File copyFromResourceToFile(int resourceId, String filename) throws IOException {
        File file = tempFolder.newFile(filename);
        try (InputStream inputStream =
                     getApplicationContext().getResources().openRawResource(resourceId);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            ByteStreams.copy(inputStream, outputStream);
        }
        return file;
    }

    private File resolveImageFile(String fileName) {
        return new File(tempFolder.getRoot(), fileName);
    }

    /**
     * An operation that can be applied to an {@link ExifInterfaceExtended} instance.
     *
     * <p>We would use java.util.Consumer but it's not available before API 24, and there's no Guava
     * equivalent.
     */
    private interface ExifInterfaceExtendedOperation {
        void applyTo(ExifInterfaceExtended exifInterface);
    }
}
