## Remoting WebSocket Client


### 如何使用

- 引入脚本

```javascript

<script src="wss.js" type="text/javascript"></script>
```


- 初始化使用

```javascript

<script>
    // 当前接入WebServer 的系统名称[固定]
    let wssName = 'schedule-sys';

    // Known params
    // 登录返回的通行证登录票据
    let accessToken = 'eyJhbGciOiJSUzI1NiJ9.eyJfaWQiOiJkY2I1OGE3MTU4MmI0MDYwYTcyZTAzMGI1ZjE5N2Q4ZCIsImRhdCI6Ik4vQmtqTkJBelh0Y04rZDdKRExrVU5OOWNXU2JQWDlId29hV0RYN1B1UElzZ1BSMlNvbS9JK09kWWpWK0hJS0pwWG9ja2Vvb1o3eVZ4a0YydnZweDJtTHA1YVJrOE5FanZrZyszbU8rZXczNmpoaEFkQ1YvVFhhTWNKQ1lqZDhCd1YrMW13T1pVdjJPVzhGZ2tPOERKVmo5bWhKeDMxZ0tIMUdPdmowanA4ST0iLCJpYXQiOjE1NDI3MDM2ODY2NDUsImV4cCI6MTU0MzMxMDY2MDY0NSwiYXVkIjpudWxsfQ.Mm4dLLeKEexAm_v6MdUulGXaAntwdDYgsoh663lolJ89VMm8ZQn_Ejn3rCxKxgJ2AdDnv-rG15LNMxPUWurVm8C6QMO-OYfJsq2jyKg2TafYUKFCsr73BKvc4PMXIt4TRLT1jqdV817_NRGfP-Ksqjb33nHskkP1-uZw5VEF1Fo-hHXSs5lw_ASWs6OpoNIx6U706hNvTauObmueU_yt3LI7KZ5YpmM8azWt7zTEApGGVHBeZ6Pt59PTOp1lXUS1XZ6GyoA2kqx0gb9rFmJKulihmiwwaHqWcFSWyeSNuWAJQh6qQesvA4pybmoeMD93VaUVYVCOyP4-Afa-Gbb97Q';
    let areaNo = 320500;  // 当前系统的区域编号
    let orgId = 3910249034228736; // 当前机构的编号
    let parentOrgId = null; // 父机构编号  (可为空)
    let passportId = 3837142362366976; // 通行证 ID
    let subOrgId = 3910249034228735;  // 推送消息是指定接受的分站 ID

    // web socket instance
    let ws;

    function connect() {
        // 从主服务拉取 WebSocket 服务器地址[多个]
        const http = new XMLHttpRequest();
        const wssQueryUrl = 'http://10.0.0.9:13110/master/available-wss-servers?wssName=' + wssName;
        http.open('GET', wssQueryUrl);
        http.send();
        http.onreadystatechange = function() {
            if (this.readyState === 4 && this.status === 200) {
                let servers = JSON.parse(http.responseText);
                if (servers.length > 0) {
                    // 随机选择负载策略
                    let index = Math.floor(Math.random() * servers.length);
                    let server = servers[index];
                    console.log('选择服务器 : ' + JSON.stringify(server));
                    doConnect('ws://' + server.wssHost + ':' + server.wssPort + '/' + wssName);
                } else {
                    console.log('警告:无可用的WebSocket服务器地址~');
                }
            }
        };
    }

    function doConnect(wssServer) {
        // Connect
        ws = new AcmedcareWss(wssServer, null, {
            debug: true,
            reconnectInterval: 3000,
            heartbeat: true, // 开启心跳
            heartbeatInterval: 10000,
        });

        // Add Open Listener
        ws.addEventListener('open', function(event) {
            ws.auth(accessToken,
                function(success, message) {
                    console.log('授权校验返回值: ' + success);
                    if (typeof message !== 'undefined') {
                        console.log('错误提示信息: ' + message);
                    }

                    // 登录票据校验通过,注册客户端基本信息
                    if (success) {
                        ws.registerClient(areaNo, orgId, passportId, parentOrgId, function(success) {

                            // 注册返回结果(成功,失败) , TODO 根据具体情况具体处理
                            console.log('Register-Callback:' + success);

                        });
                    }
                });
        });
    }

    // DEMO: 拉取在线的分站机构方法示例
    function pullSubOrg() {
        ws.pullOnlineSubOrgs(areaNo, orgId, passportId, function(data) {
            console.log('在线子机构列表 : ' + data);
        });
    }

    // DEMO: 主站推送急救单消息到指定分站
    function pushOrder() {
        ws.pushOrder(document.getElementById('pushOrderInput').value, subOrgId);
    }
</script>

```

### 当前版本功能

- [x] 拉取在线的分站机构方法
- [x] 主站推送急救单消息到指定分站
- [ ] 单聊群聊