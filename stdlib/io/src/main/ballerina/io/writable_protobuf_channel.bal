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

#WritableProtoChannel represents capabilities to encode bytes based on protobuf specification.
public type WritableProtoChannel object {

    private WritableDataChannel? dc;

    public new(WritableByteChannel byteChannel) {
        dc = new WritableDataChannel(byteChannel, bOrder = BIG_ENDIAN);
    }

    public function writeBool(boolean value) returns error? {
        return dc.writeBool(value);
    }

    public function writeDouble(float value) returns error? {
        return dc.writeFloat64(value);
    }

    public function writeFixed32(int value) returns error? {
        return dc.writeInt32(value);
    }

    public function writeFixed64(int value) returns error? {
        return dc.writeInt64(value);
    }

    public function writeFloat(float value) returns error? {
        return dc.writeFloat32(value);
    }

    public function writeInt(int value) returns error? {
        return dc.writeVarInt(value);
    }

    public function writeString(string value) returns error? {
        byte[] bytes = value.toByteArray(PROTOBUF_STRING_ENCODING);
        int length = lengthof bytes;
        var lengthWriteResult = dc.writeVarInt(length);
        match lengthWriteResult {
            () => {
                var contentWriteResult = dc.writeString(value, PROTOBUF_STRING_ENCODING);
                match lengthWriteResult {
                    () => {
                        return ();
                    }
                    error e => {return e;}
                }
            }
            error e => {return e;}
        }
    }

    public function writeTag(int fieldNumber, WireType wireType) returns error? {
        error? result;
        if (wireType == "VARINT"){
            int tag = (fieldNumber << 3) | 0;
            result = dc.writeVarInt(tag);
        } else if (wireType == "BIT64"){
            int tag = (fieldNumber << 3) | 1;
            result = dc.writeVarInt(tag);
        } else if (wireType == "LD"){
            int tag = (fieldNumber << 3) | 2;
            result = dc.writeVarInt(tag);
        } else if (wireType == "BIT32"){
            int tag = (fieldNumber << 3) | 5;
            result = dc.writeVarInt(tag);
        }
        return result;
    }

    public function close() returns error?|() {
        return dc.close();
    }
};
