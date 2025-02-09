/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.nativeimpl.response;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpUtil;

/**
 * Create a new entity and associate it with the response.
 *
 * @since 0.970.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "createNewEntity",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Response",
                structPackage = "ballerina/http"),
        args = {@Argument(name = "response", type = TypeKind.OBJECT)},
        returnType = {@ReturnType(type = TypeKind.OBJECT)},
        isPublic = false
)
public class CreateNewEntity {
    public static ObjectValue createNewEntity(Strand strand, ObjectValue responseObj) {
        return HttpUtil.createNewEntity(responseObj);
    }
}
