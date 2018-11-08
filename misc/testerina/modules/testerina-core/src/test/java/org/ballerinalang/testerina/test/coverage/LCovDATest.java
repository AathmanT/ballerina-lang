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

package org.ballerinalang.testerina.test.coverage;

import org.ballerinalang.testerina.coverage.LCovDA;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class LCovDATest {

    private LCovDA lCovDA;

    @BeforeTest
    public void beforeSuite() {

        lCovDA = new LCovDA(1, 1, 0);
    }

    @Test
    public void getLineNumberWithSetTest() {

        lCovDA.setLineNumber(2);
        Assert.assertEquals(lCovDA.getLineNumber(), 2);
    }

    @Test
    public void getExeCountWithSetTest() {

        lCovDA.setLineExecutionCount(2);
        Assert.assertEquals(lCovDA.getLineExecutionCount(), 2);
    }

    @Test
    public void getChecksumWithSetTest() {

        lCovDA.setChecksum(1);
        Assert.assertEquals(lCovDA.getChecksum(), 1);
    }

}
