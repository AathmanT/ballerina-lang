import ballerina/io;

public type Person record {
    string name;
    int age;
};

Person p = {name:"Ballerina",age:34};

function writeProtoDataToFile(string path) {
    io:WritableByteChannel channel = io:openWritableFile(path);
    io:WritableProtoChannel pch = new io:WritableProtoChannel(channel);

    var tagLdResponse = pch.writeTag(1,"LD");
    var strResponse = pch.writeString(p.name);

    var tagIntResponse = pch.writeTag(2,"VARINT");
    var intResponse = pch.writeInt(p.age);

    var closeResult = channel.close();
}

function readProtoDataFile(string path) returns boolean|error {
    io:ReadableByteChannel channel = io:openReadableFile(path);
    io:ReadableProtoChannel pch = new io:ReadableProtoChannel(channel);
    int fieldNumber =check pch.readTag();
    match pch.readString(){
        string personName =>{
            io:println(personName);
            if(personName != "Ballerina"){
                var clostResult = pch.close();
                return false;
            }
        }
        error err =>{
            return err;
        }
        () =>{
            return {message:"Error occurred while reading name"};
        }
    }
    fieldNumber =check pch.readTag();
    match pch.readInt(){
        int personAge =>{
            io:println(personAge);
            if(personAge != p.age){
                var closeResult = pch.close();
                return false;
            }
        }
        error err =>{
            return err;
        }
        () =>{
            return {message:"Error occurred while reading name"};
        }
    }
    var closeResult = pch.close();
    return true;
}
