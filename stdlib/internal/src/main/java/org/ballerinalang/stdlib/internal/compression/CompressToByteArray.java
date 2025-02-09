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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.internal.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Extern function ballerina.compression:compressToByteArray.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "internal",
        functionName = "compressToByteArray",
        args = {@Argument(name = "dirPath", type = TypeKind.RECORD, structType = "Path",
                structPackage = "ballerina/file")},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.BYTE),
                        @ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class CompressToByteArray extends BlockingNativeCallableUnit {
    /**
     * File path defined in ballerina.compression.
     */
    private static final int SRC_PATH_FIELD_INDEX = 0;

    /**
     * Get byte array of a compressed file.
     *
     * @param dirPath directory path to be compressed.
     */
    private static byte[] compressToBlob(Path dirPath) throws IOException {
        OutputStream os = Compress.compressFiles(dirPath, new ByteArrayOutputStream());
        ByteArrayOutputStream bos = (ByteArrayOutputStream) os;
        return bos.toByteArray();
    }

    @Override
    public void execute(Context context) {
        BValueArray readBytes;
        BMap<String, BValue> dirPathStruct = (BMap) context.getRefArgument(SRC_PATH_FIELD_INDEX);
        Path dirPath = (Path) dirPathStruct.getNativeData(Constants.PATH_DEFINITION_NAME);
        if (!dirPath.toFile().exists()) {
            context.setReturnValues(CompressionUtils.createCompressionError(context, "Path of the folder to be " +
                    "compressed is not available: " + dirPath));
        } else {
            try {
                byte[] compressedBytes = compressToBlob(dirPath);
                readBytes = new BValueArray(compressedBytes);
                context.setReturnValues(readBytes);
            } catch (IOException e) {
                context.setReturnValues(CompressionUtils.createCompressionError(context,
                        "Error occurred when compressing "
                                + e.getMessage()));
            }
        }
    }

    public static Object compressToByteArray(Strand strand, String dirObj) {
        Path dirPath = Paths.get(dirObj);
        if (!dirPath.toFile().exists()) {
            return CompressionUtils.createCompressionError("Path of the folder to be " +
                    "compressed is not available: " + dirPath);
        } else {
            try {
                return new ArrayValue(compressToBlob(dirPath));
            } catch (IOException e) {
                return CompressionUtils.createCompressionError("Error occurred when compressing "
                                + e.getMessage());
            }
        }
    }
}
