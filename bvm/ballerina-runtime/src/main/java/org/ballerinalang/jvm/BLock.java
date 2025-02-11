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
package org.ballerinalang.jvm;

import java.util.ArrayDeque;

/**
 * {@code VarLock} represents lock object for variables in jvm.
 *
 * @since 1.0.0
 */
public class BLock {

    private ArrayDeque<Strand> current;

    private ArrayDeque<Strand> waitingForLock;

    public BLock() {
        this.current = new ArrayDeque<>();
        this.waitingForLock = new ArrayDeque<>();
    }

    public synchronized boolean lock(Strand strand) {
        if (isLockFree() || lockedBySameContext(strand)) {
            current.offerLast(strand);
            return true;
        }

        waitingForLock.offerLast(strand);

        // Strand state change
        strand.yield = true;
        strand.blocked = true;
        strand.blockedOnExtern = false;
        return false;
    }

    public synchronized void unlock() {
        //current cannot be empty as unlock cannot be called without lock being called first.
        current.removeLast();
        if (!waitingForLock.isEmpty()) {
            Strand strand = waitingForLock.removeFirst();
            strand.scheduler.unblockStrand(strand);
        }
    }

    private boolean isLockFree() {
        return current.isEmpty();
    }

    private boolean lockedBySameContext(Strand ctx) {
        return current.getLast() == ctx;
    }
}
