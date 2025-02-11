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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.runtime;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test processing invocation context.
 */
public class InvocationContextTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/invocation-context.bal");
    }

    @Test(description = "Test case for accessing invocationId from invocation context")
    public void testInvocationId() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testInvocationId");
        Assert.assertTrue(returns[0] instanceof BString);
    }

    @Test(description = "Test case for processing userId in authentication context")
    public void testUserId() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUserId");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for processing username in authentication context")
    public void testUsername() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUsername");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for processing user claims in authentication context")
    public void testUserClaims() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUserClaims");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for processing allowedScopes in authentication context")
    public void testAllowedScopes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAllowedScopes");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for processing authType in authentication context")
    public void testAuthType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthType");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for processing authToken in authentication context")
    public void testAuthToken() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAuthToken");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test case for custom attributes in Invocation Context")
    public void testCustomAttributes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAttributes");
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }
}
