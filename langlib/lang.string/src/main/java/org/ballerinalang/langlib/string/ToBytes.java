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
package org.ballerinalang.langlib.string;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Convert String to byte array.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.string",
        functionName = "toBytes",
        args = {@Argument(name = "string", type = TypeKind.STRING),
                @Argument(name = "encoding", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.BYTE)},
        isPublic = true
)
public class ToBytes extends BlockingNativeCallableUnit {

    public void execute(Context ctx) {
        try {
            String string = ctx.getStringArgument(0);
            String encoding = ctx.getStringArgument(1);
            byte[] bytes = string.getBytes(encoding);
            BValueArray byteArray = new BValueArray(bytes);
            ctx.setReturnValues(byteArray);
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaException(BallerinaErrorReasons.STRING_OPERATION_ERROR,
                                         "Unsupported Encoding " + e.getMessage());
        }
    }

    public static ArrayValue toBytes(Strand strand, String value) {

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        return new ArrayValue(bytes);
    }
}
