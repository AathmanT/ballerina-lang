/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.io.nativeimpl;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.stdlib.io.events.EventContext;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.stdlib.io.utils.IOUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Writes XML to a given location.
 *
 * @since ballerina-0.970.0-alpha3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "io",
        functionName = "writeXml",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "WritableCharacterChannel",
                structPackage = "ballerina/io"),
        args = {@Argument(name = "content", type = TypeKind.XML)},
        returnType = {@ReturnType(type = TypeKind.ERROR)},
        isPublic = true
)
public class WriteXml {

    public static Object writeXml(Strand strand, ObjectValue characterChannelObj, XMLValue<?> content) {
        NonBlockingCallback callback = new NonBlockingCallback(strand);
        try {
            CharacterChannel characterChannel = (CharacterChannel) characterChannelObj.getNativeData(
                    IOConstants.CHARACTER_CHANNEL_NAME);
            EventContext eventContext = new EventContext(callback);
            IOUtils.writeFull(characterChannel, content.toString(), eventContext);
        } catch (BallerinaException e) {
            callback.setReturnValues(IOUtils.createError(e.getMessage()));
        } finally {
            callback.notifySuccess();
        }
        return null;
    }
}
