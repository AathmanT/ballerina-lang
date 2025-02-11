/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.crypto.nativeimpl;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.crypto.Constants;
import org.ballerinalang.stdlib.crypto.CryptoUtils;

import java.security.InvalidKeyException;
import java.security.PublicKey;

/**
 * Extern function ballerina.crypto:verifyRsaSha1Signature.
 *
 * @since 0.990.4
 */
@BallerinaFunction(orgName = "ballerina", packageName = "crypto", functionName = "verifyRsaSha1Signature")
public class VerifyRsaSha1Signature {

    public static Object verifyRsaSha1Signature(Strand strand, ArrayValue dataValue, ArrayValue signatureValue,
                                                MapValue<?, ?> publicKey) {
        byte[] data = dataValue.getBytes();
        byte[] signature = signatureValue.getBytes();
        try {
            PublicKey key = (PublicKey) publicKey.getNativeData(Constants.NATIVE_DATA_PUBLIC_KEY);
            return CryptoUtils.verify("SHA1withRSA", key, data, signature);
        } catch (InvalidKeyException e) {
            return CryptoUtils.createCryptoError("Uninitialized public key");
        }
    }
}
