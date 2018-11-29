## 配置 MongoDB 副本集群

### 分配管理员,创建副本集
```javascript

rs.initiate(
    {"_id" : "Acmedcare-NewIM-Set",
     "members" : [
        {"_id" : 1, "host" : "192.168.1.226:30001"},
        {"_id" : 2, "host" : "192.168.1.226:30002"},
        {"_id" : 3, "host" : "192.168.1.226:30003", "arbiterOnly" : true}
    ]
});

db.createUser(
  {
    user: "root",
    pwd: "Acmedcare#root",
    roles: [ { role: "userAdminAnyDatabase", db: "admin" }, "readWriteAnyDatabase" ]
  }
)

db.grantRolesToUser("root" , 
    [{
        "role": "dbOwner",
        "db": "admin"
      },
      {
        "role": "root",
        "db": "admin"
      },
      {
        "role": "clusterAdmin",
        "db": "admin"
      },
      {
        "role": "userAdminAnyDatabase",
        "db": "admin"
      },
      {
        "role": "dbAdminAnyDatabase",
        "db": "admin"
      }
]
)

```

### 创建数据库分配权限

```javascript

# 切换数据库
use Acmedcare-NewDB

# 创建用户
db.createUser(
  {
    user: "NewDBAdmin",
    pwd: "Acmedcare#root",
    roles: [ "readWrite", "dbAdmin" ]
  }
)


```


```bash

mongo -host 192.168.1.226 -port 30001 -uroot -pAcmedcare#root --authenticationDatabase admin

```