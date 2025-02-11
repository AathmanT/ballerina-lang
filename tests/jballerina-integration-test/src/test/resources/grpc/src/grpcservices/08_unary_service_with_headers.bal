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
// This is server implementation for unary blocking/unblocking scenario
import ballerina/grpc;
import ballerina/io;

// Server endpoint configuration
listener grpc:Listener ep8 = new (9098, {
    host:"localhost"
});

@grpc:ServiceDescriptor {
    descriptor: ROOT_DESCRIPTOR_8,
    descMap: getDescriptorMap8()
}
service HelloWorld101 on ep8 {
    resource function hello(grpc:Caller caller, string name, grpc:Headers headers) {
        io:println("name: " + name);
        string message = "Hello " + name;
        if (!headers.exists("x-id")) {
            error? err = caller->sendError(grpc:ABORTED, "x-id header is missing");
        } else {
            io:println("Request Header: " + (headers.get("x-id") ?: ""));
            headers.removeAll();
            headers.addEntry("x-id", "1234567890");
            headers.addEntry("x-id", "2233445677");
            io:print("Response Headers: ");
            io:println(headers.getAll("x-id"));
        }
        error? err = caller->send(message, headers);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        } else {
            io:println("Server send response : " + message);
        }
        checkpanic caller->complete();
    }
}

const string ROOT_DESCRIPTOR_8 = "0A1348656C6C6F576F726C643130312E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F32540A0D48656C6C6F576F726C6431303112430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565620670726F746F33";
function getDescriptorMap8() returns map<string> {
    return {
        "HelloWorld101.proto":
        "0A1348656C6C6F576F726C643130312E70726F746F120C6772706373657276696365731A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F32540A0D48656C6C6F576F726C6431303112430A0568656C6C6F121C2E676F6F676C652E70726F746F6275662E537472696E6756616C75651A1C2E676F6F676C652E70726F746F6275662E537472696E6756616C7565620670726F746F33"
        ,

        "google/protobuf/wrappers.proto":
        "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F627566221C0A0B446F75626C6556616C7565120D0A0576616C7565180120012801221B0A0A466C6F617456616C7565120D0A0576616C7565180120012802221B0A0A496E74363456616C7565120D0A0576616C7565180120012803221C0A0B55496E74363456616C7565120D0A0576616C7565180120012804221B0A0A496E74333256616C7565120D0A0576616C7565180120012805221C0A0B55496E74333256616C7565120D0A0576616C756518012001280D221A0A09426F6F6C56616C7565120D0A0576616C7565180120012808221C0A0B537472696E6756616C7565120D0A0576616C7565180120012809221B0A0A427974657356616C7565120D0A0576616C756518012001280C427C0A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A2A6769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F7772617070657273F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

    };
}
