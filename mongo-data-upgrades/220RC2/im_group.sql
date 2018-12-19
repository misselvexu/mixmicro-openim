/*
 Navicat Premium Data Transfer

 Source Server         : DEV-MONGODB
 Source Server Type    : MongoDB
 Source Server Version : 40004
 Source Host           : localhost:27017
 Source Schema         : Acmedcare-NewDB

 Target Server Type    : MongoDB
 Target Server Version : 40004
 File Encoding         : 65001

 Date: 18/12/2018 09:19:43
*/


// ----------------------------
// Collection structure for im_group
// ----------------------------
db.getCollection("im_group").drop();
db.createCollection("im_group");
db.getCollection("im_group").createIndex({
    groupId: NumberInt("1")
}, {
    name: "groupId",
    unique: true
});

// ----------------------------
// Documents of im_group
// ----------------------------
session = db.getMongo().startSession();
session.startTransaction();
db = session.getDatabase("Acmedcare-NewDB");
db.getCollection("im_group").insert([ {
    _id: ObjectId("5bfcd82081a82fb9ad6fa96f"),
    namespace: "DEFAULT",
    groupId: "gid-20181122",
    groupOwner: "misselvexu",
    groupName: "gname-test-group",
    groupBizTag: "G-Z",
    groupExt: "",
    _class: "com.acmedcare.framework.newim.Group"
} ]);
db.getCollection("im_group").insert([ {
    _id: ObjectId("5bff711b1fdc4968865f7eb2"),
    namespace: "DEFAULT",
    groupId: "gid-20181123",
    groupOwner: "misselvexu",
    groupName: "xxwss",
    groupBizTag: "tagss",
    groupExt: "~",
    _class: "com.acmedcare.framework.newim.Group"
} ]);
db.getCollection("im_group").insert([ {
    _id: ObjectId("5bff71ca1fdc4968865f7eb3"),
    namespace: "DEFAULT",
    groupId: "gid-20181123-2",
    groupOwner: "misselvexu",
    groupName: "xxwss",
    groupBizTag: "tagss",
    groupExt: "~",
    _class: "com.acmedcare.framework.newim.Group"
} ]);
db.getCollection("im_group").insert([ {
    _id: ObjectId("5bff72131fdc4968865f7eb4"),
    namespace: "DEFAULT",
    groupId: "gid-20181123-1",
    groupOwner: "misselvexu",
    groupName: "xxwss",
    groupBizTag: "tagss",
    groupExt: "~",
    _class: "com.acmedcare.framework.newim.Group"
} ]);
db.getCollection("im_group").insert([ {
    _id: ObjectId("5c0111ce1fdc4996b133586c"),
    namespace: "DEFAULT",
    groupId: "test-issac",
    groupOwner: "Issac",
    groupName: "test-issac",
    groupStatus: "ENABLED",
    _class: "com.acmedcare.framework.newim.Group"
} ]);
db.getCollection("im_group").insert([ {
    _id: ObjectId("5c0112861fdc4996b133586e"),
    namespace: "DEFAULT",
    groupId: "test-issac-001",
    groupOwner: "Issac",
    groupName: "test-issac-001",
    groupStatus: "ENABLED",
    _class: "com.acmedcare.framework.newim.Group"
} ]);
db.getCollection("im_group").insert([ {
    _id: ObjectId("5c10e2be1fdc4973a16b0af0"),
    namespace: "DEFAULT",
    groupId: "A1234A",
    groupOwner: "1234",
    groupName: "A1234A",
    groupStatus: "ENABLED",
    _class: "com.acmedcare.framework.newim.Group"
} ]);
db.getCollection("im_group").insert([ {
    _id: ObjectId("5c134aab1fdc4973a16b0af1"),
    namespace: "DEFAULT",
    groupId: "A3933065054981120A",
    groupOwner: "3933065054981120",
    groupName: "A3933065054981120A",
    groupStatus: "ENABLED",
    _class: "com.acmedcare.framework.newim.Group"
} ]);
session.commitTransaction(); session.endSession();
