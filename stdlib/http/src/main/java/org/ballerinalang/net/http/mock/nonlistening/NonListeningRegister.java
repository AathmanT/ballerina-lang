/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.http.mock.nonlistening;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketService;
import org.ballerinalang.net.http.WebSocketServicesRegistry;

import static org.ballerinalang.net.http.HttpConstants.MOCK_LISTENER_ENDPOINT;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "register",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = MOCK_LISTENER_ENDPOINT,
                structPackage = "ballerina.http"),
        args = {@Argument(name = "serviceType", type = TypeKind.TYPEDESC),
                @Argument(name = "annotationData", type = TypeKind.MAP)},
        isPublic = true
)
public class NonListeningRegister extends org.ballerinalang.net.http.serviceendpoint.Register {
    public static void register(Strand strand, ObjectValue serviceEndpoint, ObjectValue service,
                                MapValue annotationData) {
        HTTPServicesRegistry httpServicesRegistry = getHttpServicesRegistry(serviceEndpoint);
        WebSocketServicesRegistry webSocketServicesRegistry = getWebSocketServicesRegistry(serviceEndpoint);

        BType param;
        AttachedFunction[] resourceList = service.getType().getAttachedFunctions();
        if (resourceList.length > 0 && (param = resourceList[0].getParameterType()[0]) != null) {
            String callerType = param.getName();
            if (HttpConstants.HTTP_CALLER_NAME.equals(callerType)) {
                httpServicesRegistry.registerService(service);
            } else if (WebSocketConstants.FULL_WEBSOCKET_CALLER_NAME.equals(callerType)) {
                WebSocketService webSocketService = new WebSocketService(service, strand.scheduler);
                webSocketServicesRegistry.registerService(webSocketService);
            }
        } else {
            httpServicesRegistry.registerService(service);
        }
    }
}
