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

package org.ballerinalang.net.http.nativeimpl.request;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.uri.URIUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.ballerinalang.net.http.HttpConstants.QUERY_PARAM_MAP;

/**
 * Get the Query params from HTTP message and return a map.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "getQueryParams",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Request",
                             structPackage = "ballerina/http"),
        returnType = {@ReturnType(type = TypeKind.MAP, elementType = TypeKind.STRING)},
        isPublic = true
)
public class GetQueryParams {
    @SuppressWarnings("unchecked")
    public static MapValue<String, Object> getQueryParams(Strand strand, ObjectValue requestObj) {
        try {
            Object queryParams = requestObj.getNativeData(QUERY_PARAM_MAP);
            if (queryParams != null) {
                return (MapValue<String, Object>) queryParams;
            }
            HttpCarbonMessage httpCarbonMessage = (HttpCarbonMessage) requestObj
                    .getNativeData(HttpConstants.TRANSPORT_MESSAGE);
            BMapType mapType = new BMapType(new BArrayType(BTypes.typeString));
            MapValue<String, Object> params = new MapValueImpl<>(mapType);
            Object rawQueryString = httpCarbonMessage.getProperty(HttpConstants.RAW_QUERY_STR);
            if (rawQueryString != null) {
                URIUtil.populateQueryParamMap((String) rawQueryString, params);
            }
            requestObj.addNativeData(QUERY_PARAM_MAP, params);
            return params;
        } catch (Exception e) {
            throw new BallerinaException("Error while retrieving query param from message: " + e.getMessage());
        }
    }
}
