const LOGTAG = 'AcmedcareWss'
const WSS_NAME = 'schedule-sys'

export const COMMANDS = {
  'AUTH': 0x30000,
  'REGISTER': 0x30001,
  'HEARTBEAT': 0x30003,
  'MESSAGE': 0x30004
}

function debug (msg) {
  console.log(LOGTAG, msg)
}

function generateEvent (s, args) {
  var evt = document.createEvent('CustomEvent')
  evt.initCustomEvent(s, false, false, args)
  return evt
}

let reconnectAttempts = 0
let readyState = WebSocket.CONNECTING
this.CONNECTING = WebSocket.CONNECTING
this.OPEN = WebSocket.OPEN
this.CLOSING = WebSocket.CLOSING
this.CLOSED = WebSocket.CLOSED

var eventTarget = document.createElement('div')

eventTarget.addEventListener('open', function (event) {
  AcmedcareWss.prototype.onopen(event)
})

eventTarget.addEventListener('close', function (event) {
  if (AcmedcareWss.options.heartbeat) {
    clearInterval(heartbeatTimer)
    heartbeatRunning = false
  }
  AcmedcareWss.prototype.onclose(event)
})

eventTarget.addEventListener('connecting', function (event) {
  AcmedcareWss.prototype.onconnecting(event)
})

eventTarget.addEventListener('message', function (event) {
  let result = JSON.parse(event.data)
  console.log('Rev: ' + event.data)
  if (result.bizCode === COMMANDS.HEARTBEAT) {
    // heartbeat response
    console.log('heartbeat response.')
    return
  }

  // auth response
  if (result.bizCode === COMMANDS.AUTH) {
    AcmedcareWss.prototype.authCallback(result.code === 0, result.data)
    return
  }

  // register response message
  if (result.bizCode === COMMANDS.REGISTER) {
    if (result.code !== 0) {
      // clear request
      defaultRequest = {}
      AcmedcareWss.prototype.registerClientCallback(false)
    } else {
      // startup heartbeat
      if (!heartbeatRunning) {
        newHeartbeat()
      }
      AcmedcareWss.prototype.registerClientCallback(true)
    }
    return
  }

  if (result.bizCode === COMMANDS.MESSAGE) {
    AcmedcareWss.prototype.bizMessage(result.message)
    return
  }

  AcmedcareWss.prototype.onmessage(event)
})
eventTarget.addEventListener('error', function (event) {
  AcmedcareWss.prototype.onerror(event)
})

var defaultOptions = {
  /** Whether this instance should log debug messages. */
  debug: false,

  /** Whether or not the websocket should attempt to connect immediately upon instantiation. */
  automaticOpen: true,

  /** The number of milliseconds to delay before attempting to reconnect. */
  reconnectInterval: 10000,

  /** The maximum number of milliseconds to delay a reconnection attempt. */
  maxReconnectInterval: 30000,

  /** The rate of increase of the reconnect delay. Allows reconnect attempts to back off when problems persist. */
  reconnectDecay: 1.5,

  /** The maximum time in milliseconds to wait for a connection to succeed before closing and retrying. */
  timeoutInterval: 2000,

  /** The maximum number of reconnection attempts to make. Unlimited if null. */
  maxReconnectAttempts: null,

  /** heartbeat flag */
  heartbeat: false,

  /** heartbeat period */
  heartbeatInterval: 30000
}

let forcedClose = false
let timedOut = false
let defaultRequest = {}
let heartbeatRunning = false
let heartbeatTimer

function newHeartbeat () {
  heartbeatRunning = true
  heartbeatTimer = setInterval(function () {

    if (readyState !== WebSocket.OPEN) {
      clearInterval(heartbeatTimer)
    }

    let request = {}
    request.bizCode = COMMANDS.HEARTBEAT
    request.orgId = defaultRequest.orgId
    request.parentOrgId = defaultRequest.parentOrgId
    request.areaNo = defaultRequest.areaNo
    request.passportId = defaultRequest.passportId

    AcmedcareWss.wssClient.send(JSON.stringify(request))
    console.log('info: heartbeat.')
  }, AcmedcareWss.options.heartbeatInterval)
}

function doConnect (servers, reconnectAttempt) {
  // 随机选择负载策略
  let index = Math.floor(Math.random() * servers.length)
  let server = servers[index]
  let wssServerAddress = 'ws://' + server.wssHost + ':' + server.wssPort + '/' + WSS_NAME
  console.info(LOGTAG, '连接服务器:', wssServerAddress)
  AcmedcareWss.wssClient = new WebSocket(wssServerAddress)
  console.log('------------------------------------------------------------------')

  if (reconnectAttempt) {
    if (AcmedcareWss.options.maxReconnectAttempts && reconnectAttempts > AcmedcareWss.options.maxReconnectAttempts) {
      return
    }
  } else {
    eventTarget.dispatchEvent(generateEvent('connecting'))
    reconnectAttempts = 0
  }

  let localWs = AcmedcareWss.wssClient
  let timeout = setTimeout(function () {
    timedOut = true
    localWs.close()
    timedOut = false
  }, AcmedcareWss.options.timeoutInterval)

  AcmedcareWss.wssClient.onopen = function (event) {
    clearTimeout(timeout)
    readyState = WebSocket.OPEN
    reconnectAttempts = 0
    let e = generateEvent('open')
    e.isReconnect = reconnectAttempt
    reconnectAttempt = false
    eventTarget.dispatchEvent(e)
  }

  AcmedcareWss.wssClient.onclose = function (event) {
    clearTimeout(timeout)
    AcmedcareWss.wssClient = null
    if (forcedClose) {
      readyState = WebSocket.CLOSED
      eventTarget.dispatchEvent(generateEvent('close'))
    } else {
      readyState = WebSocket.CONNECTING
      let e = generateEvent('connecting')
      e.code = event.code
      e.reason = event.reason
      e.wasClean = event.wasClean
      eventTarget.dispatchEvent(e)
      if (!reconnectAttempt && !timedOut) {
        eventTarget.dispatchEvent(generateEvent('close'))
      }

      let timeout = AcmedcareWss.options.reconnectInterval * Math.pow(AcmedcareWss.options.reconnectDecay,
        reconnectAttempts)
      setTimeout(function () {
        reconnectAttempts++
        doConnect(AcmedcareWss.wssServers, true)
      }, timeout > AcmedcareWss.options.maxReconnectInterval ? AcmedcareWss.options.maxReconnectInterval
        : timeout)
    }
  }

  AcmedcareWss.wssClient.onmessage = function (event) {
    let e = generateEvent('message')
    e.data = event.data
    eventTarget.dispatchEvent(e)
  }
  AcmedcareWss.wssClient.onerror = function (event) {
    eventTarget.dispatchEvent(generateEvent('error'))
  }
}

class AcmedcareWss {
  constructor (serverAddrs, options) {
    this.instance(serverAddrs, options)
  }

  instance (serverAddrs, options) {
    AcmedcareWss.serverAddrs = serverAddrs

    if (!options) {
      options = {}
    }

    if (typeof options === 'undefined') {
      AcmedcareWss.options = defaultOptions
    } else {
      AcmedcareWss.options = options
    }

    // Overwrite and define settings with options if they exist.
    for (var key in defaultOptions) {
      if (typeof options[key] !== 'undefined') {
        AcmedcareWss.options[key] = options[key]
      } else {
        AcmedcareWss.options[key] = defaultOptions[key]
      }
    }

    console.log(AcmedcareWss.options)

    return this
  }

  /**
   * 注册打开事件监听
   * @param fn 回调函数
   */
  addOpenEventListener (fn) {
    AcmedcareWss.prototype.onopen = fn
  }

  /**
   * 注册链接事件监听
   * @param fn 回调函数
   */
  addConnectingEventListener (fn) {
    AcmedcareWss.prototype.onconnecting = fn
  }

  /**
   * 注册业务消息事件监听
   * @param fn 回调函数
   */
  addBizMessageEventListener (fn) {
    AcmedcareWss.prototype.bizMessage = fn
  }

  /**
   * 注册WS消息事件监听
   * @param fn 回调函数
   */
  addMessageEventListener (fn) {
    AcmedcareWss.prototype.onmessage = fn
  }

  /**
   * 注册关闭事件监听
   * @param fn 回调函数
   */
  addCloseEventListener (fn) {
    AcmedcareWss.prototype.onclose = fn
  }

  /**
   * 注册错误事件监听
   * @param fn 回调函数
   */
  addErrorEventListener (fn) {
    AcmedcareWss.prototype.onerror = fn
  }

  /**
   * 初始化方法
   */
  init () {
    debug('init' + AcmedcareWss.serverAddrs + ',' + AcmedcareWss.options.heartbeat + ',' + AcmedcareWss.options.heartbeatInterval)
    // 从主服务拉取 WebSocket 服务器地址[多个]
    const http = new XMLHttpRequest()
    const wssQueryUrl = 'http://192.168.1.227:13110/master/available-wss-servers?wssName=' + WSS_NAME
    http.open('GET', wssQueryUrl)
    http.send()
    http.onreadystatechange = function () {
      if (this.readyState === 4 && this.status === 200) {
        let servers = JSON.parse(http.responseText)
        if (servers.length > 0) {
          AcmedcareWss.wssServers = servers
          doConnect(servers, false)
        } else {
          console.log('警告:无可用的WebSocket服务器地址~')
        }
      }
    }
  }

  /**
   * 注销方法
   * @param code 编码
   * @param reason 原因
   */
  destroy (code, reason) {
    // Default CLOSE_NORMAL code
    if (typeof code === 'undefined') {
      code = 1000
    }
    forcedClose = true

    if (AcmedcareWss.options.heartbeat) {
      clearInterval(heartbeatTimer)
      heartbeatRunning = false
    }

    if (AcmedcareWss.wssClient) {
      AcmedcareWss.wssClient.close(code, reason)
    }
  }

  /**
   * 授权校验函数
   * @param accessToken 登录票据
   * @param callback 回调函数
   */
  auth (accessToken, callback) {
    if (AcmedcareWss.wssClient) {
      if (typeof callback !== 'undefined') {
        AcmedcareWss.prototype.authCallback = callback
      }

      // build auth message
      let request = {}
      request.bizCode = COMMANDS.AUTH
      request.accessToken = accessToken
      request.wssClientType = 'Normal'

      return AcmedcareWss.wssClient.send(JSON.stringify(request))
    } else {
      console.error(LOGTAG, 'Wss client is invalid.')
    }
  }

  /**
   * 注册本机客户端
   * @param areaNo 区域编号
   * @param orgId 机构编码
   * @param passportId 通行证编码
   * @param parentOrgId 父机构编码
   * @param callback 回调函数
   */
  registerClient (areaNo, orgId, passportId, parentOrgId, callback) {
    if (AcmedcareWss.wssClient) {
      if (typeof callback !== 'undefined') {
        AcmedcareWss.prototype.authCallback = callback
      }

      console.log('begin register client.')
      // build auth message
      let request = {}
      request.bizCode = COMMANDS.REGISTER
      request.orgId = orgId
      request.parentOrgId = parentOrgId
      request.areaNo = areaNo
      request.passportId = passportId

      // save
      defaultRequest = request
      return AcmedcareWss.wssClient.send(JSON.stringify(request))
    } else {
      console.error(LOGTAG, 'Wss client is invalid.')
    }
  }
}

AcmedcareWss.prototype.onopen = function (event) {
}
AcmedcareWss.prototype.onconnecting = function (event) {
}
AcmedcareWss.prototype.onmessage = function (event) {
}
AcmedcareWss.prototype.bizMessage = function (event) {
}
AcmedcareWss.prototype.onclose = function (event) {
}
AcmedcareWss.prototype.onerror = function (event) {
}
AcmedcareWss.prototype.authCallback = function (success, data) {
}
AcmedcareWss.prototype.registerClientCallback = function (success, data) {
}

export default AcmedcareWss
