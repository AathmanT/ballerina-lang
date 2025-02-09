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

import ballerina/log;
import ballerina/http;

final string CUSTOM_HEADER = "X-some-header";

service simpleProxy3 = @http:WebSocketServiceConfig {} service {

    resource function onText(http:WebSocketCaller wsEp, string text) {
        if (text == "custom-headers") {
            var returnVal = wsEp->pushText(<string>wsEp.attributes[CUSTOM_HEADER]);
            if (returnVal is http:WebSocketError) {
                panic <error> returnVal;
            }
        }
    }
};

service simple3 on new http:Listener(21012) {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/custom/header/server",
            upgradeService: simpleProxy3
        }
    }
    resource function websocketProxy(http:Caller httpEp, http:Request req) {
        http:WebSocketCaller wsServiceEp;
        wsServiceEp = httpEp->acceptWebSocketUpgrade({ "X-some-header": "some-header-value" });
        wsServiceEp.attributes[CUSTOM_HEADER] = req.getHeader(CUSTOM_HEADER);
    }
}

