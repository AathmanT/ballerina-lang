/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.stdlib.internal.compression;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.internal.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Extern function ballerina.compression:decompressFromByteArray.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "internal",
        functionName = "decompressFromByteArray",
        args = {@Argument(name = "content", type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                @Argument(name = "destDir", type = TypeKind.RECORD, structType = "Path",
                        structPackage = "ballerina/file")},
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class DecompressFromByteArray extends BlockingNativeCallableUnit {
    /**
     * File content as byte array defined.
     */
    private static final int SRC_AS_BYTEARRAY_FIELD_INDEX = 0;
    /**
     * File path of the destination directory.
     */
    private static final int DEST_PATH_FIELD_INDEX = 1;

    /**
     * Decompresses a blob.
     *
     * @param inputStream file content as an inputstream
     * @param outdir      path of the destination folder
     */
    static void decompress(InputStream inputStream, Path outdir, Context context) throws IOException {
        ZipInputStream zin;
        // try {
        zin = new ZipInputStream(inputStream);
        ZipEntry entry;
        String name, dir;
        while ((entry = zin.getNextEntry()) != null) {
            name = entry.getName();
            if (!isDecompressDestinationValid(outdir.resolve(name), outdir)) {
                context.setReturnValues(CompressionUtils.createCompressionError(context,
                        "Arbitrary File Write attack attempted via an archive file. File name: " + entry.getName()));
                break;
            }
            if (entry.isDirectory()) {
                Files.createDirectories(outdir.resolve(name));
                continue;
            }
            dir = getDirectoryPath(name);
            if (dir != null) {
                Files.createDirectories(outdir.resolve(dir));
            }
            extractFile(zin, outdir, name);
        }
        zin.close();
    }

    static ErrorValue decompress(InputStream inputStream, Path outdir) throws IOException {
        ZipInputStream zin;
        ErrorValue error = null;
        // try {
        zin = new ZipInputStream(inputStream);
        ZipEntry entry;
        String name, dir;
        while ((entry = zin.getNextEntry()) != null) {
            name = entry.getName();
            if (!isDecompressDestinationValid(outdir.resolve(name), outdir)) {
                error = CompressionUtils.createCompressionError("Arbitrary File Write attack attempted via an archive" +
                        " " +
                        "file. File name: " + entry.getName());
                break;
            }
            if (entry.isDirectory()) {
                Files.createDirectories(outdir.resolve(name));
                continue;
            }
            dir = getDirectoryPath(name);
            if (dir != null) {
                Files.createDirectories(outdir.resolve(dir));
            }
            extractFile(zin, outdir, name);
        }
        zin.close();
        return error;
    }

    /**
     * Validate if the files being extracted will remain within the expected destination path. This is used to prevent
     * Arbitrary File Write attack attempts (Zip Slip).
     *
     * @param extractedFilePath path of the file after extraction is complete.
     * @param destinationPath expected destination path of the extract operation.
     * @return validation status
     * @throws IOException
     */
    private static boolean isDecompressDestinationValid(Path extractedFilePath, Path destinationPath)
            throws IOException {
        String extractedFileCanonicalPath = extractedFilePath.toFile().getCanonicalPath();
        String intendedDestinationCanonicalPath = destinationPath.toFile().getCanonicalPath();

        if (extractedFileCanonicalPath.startsWith(intendedDestinationCanonicalPath)) {
            return true;
        }
        return false;
    }

    /**
     * Extract files from the zipInputStream.
     *
     * @param in     zipInputStream
     * @param outdir output directory file
     * @param name   name of the file
     */
    private static void extractFile(ZipInputStream in, Path outdir, String name) throws IOException {
        Path resourcePath = outdir.resolve(name);
        Files.copy(in, resourcePath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Get the directory name.
     *
     * @param name name of the directory
     * @return directory name
     */
    private static String getDirectoryPath(String name) {
        if (name != null) {
            /*
             According to the .zip specification: The paths of files inside a zip
             should not contain a drive or device letter, or a leading slash.
             All slashes MUST be forward slashes '/' as opposed to backwards slashes                     *
             https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT
            */
            int s = name.lastIndexOf("/");
            return s == -1 ? null : name.substring(0, s);
        }
        return null;
    }

    @Override
    public void execute(Context context) {
        byte[] content = ((BValueArray) context.getRefArgument(SRC_AS_BYTEARRAY_FIELD_INDEX)).getBytes();
        if (content.length == 0) {
            context.setReturnValues(CompressionUtils.createCompressionError(context, "Length of the byte " +
                    "array is empty"));
        } else {
            InputStream inputStream = new ByteArrayInputStream(content);

            BMap<String, BValue> destPathStruct = (BMap) context.getRefArgument(DEST_PATH_FIELD_INDEX);
            Path destPath = (Path) destPathStruct.getNativeData(Constants.PATH_DEFINITION_NAME);

            if (!destPath.toFile().exists()) {
                context.setReturnValues(CompressionUtils.createCompressionError(context,
                        "Path to place the decompressed file is not available"));
            } else {
                try {
                    decompress(inputStream, destPath, context);
                } catch (IOException e) {
                    context.setReturnValues(CompressionUtils.createCompressionError(context,
                            "Error occurred when decompressing " + e.getMessage()));
                }
            }
        }
    }

    public static Object decompressFromByteArray(Strand strand, ArrayValue contents, String destDir) {
        byte[] content = contents.getBytes();
        if (content.length == 0) {
            return CompressionUtils.createCompressionError("Length of the byte array is empty");
        } else {
            InputStream inputStream = new ByteArrayInputStream(content);

            Path destPath = Paths.get(destDir);

            if (!destPath.toFile().exists()) {
                return CompressionUtils.createCompressionError(
                        "Path to place the decompressed file is not available");
            } else {
                try {
                    return decompress(inputStream, destPath);
                } catch (IOException e) {
                    return CompressionUtils.createCompressionError(
                            "Error occurred when decompressing " + e.getMessage());
                }
            }
        }

    }
}
