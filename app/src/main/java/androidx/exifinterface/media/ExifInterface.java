package androidx.exifinterface.media;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.github.tommygeenexus.exifinterfaceextended.ExifInterfaceExtended;

/**
 * This is a thin Wrapper around io.github.tommygeenexus.exifinterfaceextended.ExifInterfaceExtended
 * so that it can be uses in any android app that uses original androidx.exifinterface:exifinterface:...
 * ( <a href="https://developer.android.com/reference/androidx/exifinterface/media/ExifInterface">androidx.exifinterface</a> )
 *
 * just by replacing the gradle dependency from
 *
 * dependencies {
 *     implementation "androidx.exifinterface:exifinterface:x.y.z"
 * }
 *
 * to
 *
 * dependencies {
 *     implementation "io.github.tommy-geenexus:exif-interface-extended:a.b.c"
 * }
 *
 */
public class ExifInterface extends ExifInterfaceExtended {
    /**
     * Reads Exif tags from the specified image file.
     *
     * @param file the file of the image data
     * @throws NullPointerException if file is null
     * @throws IOException          if an I/O error occurs while retrieving file descriptor via
     *                              {@link FileInputStream#getFD()}.
     */
    public ExifInterface(@NonNull File file) throws IOException {
        super(file);
    }

    /**
     * Reads Exif tags from the specified image file.
     *
     * @param filename the name of the file of the image data
     * @throws NullPointerException if file name is null
     * @throws IOException          if an I/O error occurs while retrieving file descriptor via
     *                              {@link FileInputStream#getFD()}.
     */
    public ExifInterface(@NonNull String filename) throws IOException {
        super(filename);
    }

    /**
     * Reads Exif tags from the specified image file descriptor. Attribute mutation is supported
     * for writable and seekable file descriptors only. This constructor will not rewind the offset
     * of the given file descriptor. Developers should close the file descriptor after use.
     *
     * @param fileDescriptor the file descriptor of the image data
     * @throws NullPointerException if file descriptor is null
     * @throws IOException          if an error occurs while duplicating the file descriptor.
     */
    public ExifInterface(@NonNull FileDescriptor fileDescriptor) throws IOException {
        super(fileDescriptor);
    }

    /**
     * Reads Exif tags from the specified image input stream. Attribute mutation is not supported
     * for input streams. The given input stream will proceed from its current position. Developers
     * should close the input stream after use. This constructor is not intended to be used with
     * an input stream that performs any networking operations.
     *
     * @param inputStream the input stream that contains the image data
     * @throws NullPointerException if the input stream is null
     */
    public ExifInterface(@NonNull InputStream inputStream) throws IOException {
        super(inputStream);
    }

    /**
     * Reads Exif tags from the specified image input stream based on the stream type. Attribute
     * mutation is not supported for input streams. The given input stream will proceed from its
     * current position. Developers should close the input stream after use. This constructor is not
     * intended to be used with an input stream that performs any networking operations.
     *
     * @param inputStream the input stream that contains the image data
     * @param streamType  the type of input stream
     * @throws NullPointerException if the input stream is null
     * @throws IOException          if an I/O error occurs while retrieving file descriptor via
     *                              {@link FileInputStream#getFD()}.
     */
    public ExifInterface(@NonNull InputStream inputStream, int streamType) throws IOException {
        super(inputStream, streamType);
    }
}
