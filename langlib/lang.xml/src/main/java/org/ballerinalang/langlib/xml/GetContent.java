/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langlib.xml;

import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.XMLValueUtil;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Returns the content of a text or processing instruction or comment item.
 *
 * @since 0.90
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.xml",
        functionName = "getContent",
        args = {@Argument(name = "xmlValue", type = TypeKind.XML)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class GetContent {

    public static String getContent(Strand strand, Object xmlVal) {
        XMLValue value = (XMLValue) xmlVal;
        if (IsText.isText(strand, value)) {
            return value.getTextValue();
        } else if (IsProcessingInstruction.isProcessingInstruction(strand, value)) {
            return XMLValueUtil.getPIContent(value);
        } else  if (IsComment.isComment(strand, value)) {
            return XMLValueUtil.getCommentContent(value);
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.XML_FUNC_TYPE_ERROR, "getContent",
                "text|processing instruction|comment");
    }
}
