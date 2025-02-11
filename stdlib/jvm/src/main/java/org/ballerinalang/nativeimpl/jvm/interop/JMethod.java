/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.nativeimpl.jvm.interop;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/**
 * Represents a java method in this implementation.
 *
 * @since 1.0.0
 */
class JMethod {
    static final JMethod NO_SUCH_METHOD = new JMethod(null, null);

    private JMethodKind kind;
    private Executable method;

    static JMethod build(JMethodKind kind, Executable executable) {
        return new JMethod(kind, executable);
    }

    private JMethod(JMethodKind kind, Executable executable) {
        this.kind = kind;
        this.method = executable;
    }

    String getClassName() {
        return method.getDeclaringClass().getName();
    }

    boolean isDeclaringClassInterface() {
        return this.method.getDeclaringClass().isInterface();
    }

    String getName() {
        if (kind == JMethodKind.CONSTRUCTOR) {
            return "<init>";
        } else {
            return method.getName();
        }
    }

    JMethodKind getKind() {
        return kind;
    }

    String getSignature() {
        if (kind == JMethodKind.CONSTRUCTOR) {
            return JInterop.getMethodSig(void.class, method.getParameterTypes());
        } else {
            return JInterop.getMethodSig(getReturnType(), method.getParameterTypes());
        }
    }

    Class<?>[] getParamTypes() {
        return method.getParameterTypes();
    }

    Class<?> getReturnType() {
        if (kind == JMethodKind.CONSTRUCTOR) {
            return method.getDeclaringClass();
        } else {
            return ((Method) method).getReturnType();
        }
    }
}
