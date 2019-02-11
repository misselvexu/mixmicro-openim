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

 Date: 18/12/2018 09:19:54
*/


// ----------------------------
// Collection structure for im_refs_group_member
// ----------------------------
db.getCollection("im_refs_group_member").drop();
db.createCollection("im_refs_group_member");
db.getCollection("im_refs_group_member").createIndex({
    groupId: NumberInt("1"),
    memberId: NumberInt("-1"),
    namespace: NumberInt("1")
}, {
    name: "unique_index_4_group_id_and_member_id_and_namespace",
    unique: true
});

// ----------------------------
// Documents of im_refs_group_member
// ----------------------------
session = db.getMongo().startSession();
session.startTransaction();
db = session.getDatabase("Acmedcare-NewDB");
db.getCollection("im_refs_group_member").insert([ {
    _id: ObjectId("5bfcd85481a82fb9b5312f40"),
    namespace: "DEFAULT",
    groupId: "gid-20181122",
    memberId: "3837142362366976",
    _class: "com.acmedcare.framework.newim.storage.mongo.GroupRepositoryImpl$GroupMemberRef"
} ]);
db.getCollection("im_refs_group_member").insert([ {
    _id: ObjectId("5bfcd87e81a82fb9bb70da1d"),
    namespace: "DEFAULT",
    groupId: "gid-20181122",
    memberId: "3837142362366977",
    _class: "com.acmedcare.framework.newim.storage.mongo.GroupRepositoryImpl$GroupMemberRef"
} ]);
db.getCollection("im_refs_group_member").insert([ {
    _id: ObjectId("5c00d3065142f70c17b9d5d4"),
    namespace: "DEFAULT",
    groupId: "gid-20181122",
    memberId: "1033050009520384",
    _class: "com.acmedcare.framework.newim.storage.mongo.GroupRepositoryImpl$GroupMemberRef"
} ]);
db.getCollection("im_refs_group_member").insert([ {
    _id: ObjectId("5c00d3065142f70c17b9d5d3"),
    namespace: "DEFAULT",
    groupId: "gid-20181122",
    memberId: "1010206815324416",
    _class: "com.acmedcare.framework.newim.storage.mongo.GroupRepositoryImpl$GroupMemberRef"
} ]);
db.getCollection("im_refs_group_member").insert([ {
    _id: ObjectId("5c0111ce1fdc4996b133586d"),
    namespace: "DEFAULT",
    groupId: "test-issac",
    memberId: "1004391139674368",
    _class: "com.acmedcare.framework.newim.storage.mongo.GroupRepositoryImpl$GroupMemberRef"
} ]);
db.getCollection("im_refs_group_member").insert([ {
    _id: ObjectId("5c0113cc1fdc4996b1335871"),
    namespace: "DEFAULT",
    groupId: "test-issac-001",
    memberId: "1004391139674368",
    _class: "com.acmedcare.framework.newim.storage.mongo.GroupRepositoryImpl$GroupMemberRef"
} ]);
db.getCollection("im_refs_group_member").insert([ {
    _id: ObjectId("5c04bc931fdc49098ef509ef"),
    namespace: "DEFAULT",
    groupId: "gid-20181123-1",
    memberId: "1004391331727616 ",
    _class: "com.acmedcare.framework.newim.storage.mongo.GroupRepositoryImpl$GroupMemberRef"
} ]);
db.getCollection("im_refs_group_member").insert([ {
    _id: ObjectId("5c04bc931fdc49098ef509f0"),
    namespace: "DEFAULT",
    groupId: "gid-20181123-1",
    memberId: "1028834856421632",
    _class: "com.acmedcare.framework.newim.storage.mongo.GroupRepositoryImpl$GroupMemberRef"
} ]);
db.getCollection("im_refs_group_member").insert([ {
    _id: ObjectId("5c04e0181fdc49098ef509f1"),
    namespace: "DEFAULT",
    groupId: "gid-20181123-1",
    memberId: "1042855216154880",
    _class: "com.acmedcare.framework.newim.storage.mongo.GroupRepositoryImpl$GroupMemberRef"
} ]);
db.getCollection("im_refs_group_member").insert([ {
    _id: ObjectId("5c04e0181fdc49098ef509f2"),
    namespace: "DEFAULT",
    groupId: "gid-20181123-1",
    memberId: "1042903610231040",
    _class: "com.acmedcare.framework.newim.storage.mongo.GroupRepositoryImpl$GroupMemberRef"
} ]);
db.getCollection("im_refs_group_member").insert([ {
    _id: ObjectId("5c04e0181fdc49098ef509f3"),
    namespace: "DEFAULT",
    groupId: "gid-20181123-1",
    memberId: "1042904188193024",
    _class: "com.acmedcare.framework.newim.storage.mongo.GroupRepositoryImpl$GroupMemberRef"
} ]);
db.getCollection("im_refs_group_member").insert([ {
    _id: ObjectId("5c04e0181fdc49098ef509f4"),
    namespace: "DEFAULT",
    groupId: "gid-20181123-1",
    memberId: "1042851086452992",
    _class: "com.acmedcare.framework.newim.storage.mongo.GroupRepositoryImpl$GroupMemberRef"
} ]);
session.commitTransaction(); session.endSession();
