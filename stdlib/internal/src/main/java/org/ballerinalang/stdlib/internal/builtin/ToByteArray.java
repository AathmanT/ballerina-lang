/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.internal.builtin;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.UnsupportedEncodingException;

/**
 * Convert String to byte array.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "internal",
        functionName = "toByteArray",
        args = {@Argument(name = "string", type = TypeKind.STRING),
                @Argument(name = "encoding", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.BYTE)},
        isPublic = true
)
public class ToByteArray {

    public static ArrayValue toByteArray(Strand strand, String value, String encoding) {
        StringUtils.checkForNull(value, encoding);
        try {
            byte[] bytes = value.getBytes(encoding);
            return new ArrayValue(bytes);
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException(BallerinaErrorReasons.STRING_OPERATION_ERROR,
                    "Unsupported Encoding " + e.getMessage());
        }
    }
}
