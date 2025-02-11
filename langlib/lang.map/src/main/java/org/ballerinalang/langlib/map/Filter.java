/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.map;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.langlib.map.util.MapLibUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.jvm.MapUtils.createOpNotSupportedError;

/**
 * Native implementation of lang.map:filter(map&lt;Type&gt;, function).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.map", functionName = "filter",
        args = {@Argument(name = "m", type = TypeKind.MAP), @Argument(name = "func", type = TypeKind.FUNCTION)},
        returnType = {@ReturnType(type = TypeKind.MAP)},
        isPublic = true
)
public class Filter {

    public static MapValue filter(Strand strand, MapValue<?, ?> m, FPValue<Object, Boolean> func) {
        BType mapType = m.getType();
        BType newMapType;
        switch (mapType.getTag()) {
            case TypeTags.MAP_TAG:
                newMapType = mapType;
                break;
            case TypeTags.RECORD_TYPE_TAG:
                BType newConstraint = MapLibUtils.getCommonTypeForRecordField((BRecordType) mapType);
                newMapType = new BMapType(newConstraint);
                break;
            default:
                throw createOpNotSupportedError(mapType, "filter()");
        }
        MapValue newMap = new MapValueImpl(newMapType);

        m.forEach((key, value) -> {
            if (func.apply(new Object[]{strand, value, true})) {
                newMap.put(key, value);
            }
        });

        return newMap;
    }
}
