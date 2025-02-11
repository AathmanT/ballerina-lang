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
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.wso2.ballerinalang.util.Lists;

import static org.ballerinalang.jvm.values.utils.ArrayUtils.add;
import static org.ballerinalang.langlib.map.util.MapLibUtils.getFieldType;

/**
 * Native implementation of lang.map:get(map&lt;Type&gt;, string).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.map",
        functionName = "entries",
        args = {@Argument(name = "m", type = TypeKind.MAP)},
        returnType = {@ReturnType(type = TypeKind.MAP)},
        isPublic = true
)
public class Entries {

    private static int refType = -1;

    public static MapValue<?, ?> entries(Strand strand, MapValue<?, ?> m) {
        BType newFieldType = getFieldType(m.getType(), "entries()");
        BTupleType entryType = new BTupleType(Lists.of(BTypes.typeString, newFieldType));
        BMapType entryMapConstraint = new BMapType(entryType);
        MapValue<Object, ArrayValue> entries = new MapValueImpl<>(entryMapConstraint);

        m.forEach((key, value) -> {
            ArrayValue entryTuple = new ArrayValue(entryType);
            add(entryTuple, refType, 0, key);
            add(entryTuple, refType, 1, value);
            entries.put(key, entryTuple);
        });

        return entries;
    }
}
