// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/http;
import ballerina/io;

listener http:WebSocketListener socketListener = new(21001);

@http:WebSocketServiceConfig {
    path: "/"
}
service isOpen on socketListener {

    resource function onText(http:WebSocketCaller caller, string text) {
        http:WebSocketError? err = caller->close(timeoutInSecs = 0);
        io:println("In onText isOpen " + caller.isOpen.toString());
    }

    resource function onClose(http:WebSocketCaller caller, int code, string reason) {
        io:println("In onClose isOpen " + caller.isOpen.toString());
    }

    resource function onError(http:WebSocketCaller caller, error err) {
        io:println("In onError isOpen " + caller.isOpen.toString());
    }
}
