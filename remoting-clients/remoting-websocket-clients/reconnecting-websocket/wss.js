(function(global, factory) {
    if (typeof define === 'function' && define.amd) {
        define([], factory);
    } else if (typeof module !== 'undefined' && module.exports) {
        module.exports = factory();
    } else {
        global.AcmedcareWss = factory();
    }
})(this, function() {

    if (!('WebSocket' in window)) {
        return;
    }

    function AcmedcareWss(url, protocols, options) {

        // Default settings
        var settings = {

            /** Whether this instance should log debug messages. */
            debug: false,

            /** Whether or not the websocket should attempt to connect immediately upon instantiation. */
            automaticOpen: true,

            /** The number of milliseconds to delay before attempting to reconnect. */
            reconnectInterval: 1000,
            /** The maximum number of milliseconds to delay a reconnection attempt. */
            maxReconnectInterval: 30000,
            /** The rate of increase of the reconnect delay. Allows reconnect attempts to back off when problems persist. */
            reconnectDecay: 1.5,

            /** The maximum time in milliseconds to wait for a connection to succeed before closing and retrying. */
            timeoutInterval: 2000,

            /** The maximum number of reconnection attempts to make. Unlimited if null. */
            maxReconnectAttempts: null,

            /** The binary type, possible values 'blob' or 'arraybuffer', default 'blob'. */
            binaryType: 'blob',

            /** heartbeat flag */
            heartbeat: false,

            /** heartbeat period */
            heartbeatInterval: 30000,
        };
        if (!options) {
            options = {};
        }

        // Overwrite and define settings with options if they exist.
        for (var key in settings) {
            if (typeof options[key] !== 'undefined') {
                this[key] = options[key];
            } else {
                this[key] = settings[key];
            }
        }

        // These should be treated as read-only properties

        /** The URL as resolved by the constructor. This is always an absolute URL. Read only. */
        this.url = url;

        /** The number of attempted reconnects since starting, or the last successful connection. Read only. */
        this.reconnectAttempts = 0;

        /**
         * The current state of the connection.
         * Can be one of: WebSocket.CONNECTING, WebSocket.OPEN, WebSocket.CLOSING, WebSocket.CLOSED
         * Read only.
         */
        this.readyState = WebSocket.CONNECTING;

        /**
         * A string indicating the name of the sub-protocol the server selected; this will be one of
         * the strings specified in the protocols parameter when creating the WebSocket object.
         * Read only.
         */
        this.protocol = null;

        // Private state variables

        var self = this;
        var ws;
        var forcedClose = false;
        var timedOut = false;
        var defaultRequest = {};
        var heartbeatRunning = false;
        var heartbeatTimer;

        this.clearDefaultRequest = function() {
            console.log('info: clear request cache.');
            defaultRequest = {};
        };

        this.getDefaultRequest = function() {
            return defaultRequest;
        };

        this.newHeartbeat = function() {
            heartbeatRunning = true;
            heartbeatTimer = setInterval(function() {
                let request = {};
                request.bizCode = 0x30003;
                request.orgId = defaultRequest.orgId;
                request.parentOrgId = defaultRequest.parentOrgId;
                request.areaNo = defaultRequest.areaNo;
                request.passportId = defaultRequest.passportId;
                ws.send(JSON.stringify(request));
                console.log('info: heartbeat.');
            }, self.heartbeatInterval);
        };

        var eventTarget = document.createElement('div');

        // Wire up "on*" properties as event handlers

        eventTarget.addEventListener('open', function(event) {
            self.onopen(event);
        });
        eventTarget.addEventListener('close', function(event) {
            // TODO shutdown heartbeat
            if (self.heartbeat) {
                console.log('info: shutdown heartbeat.');
                clearInterval(heartbeatTimer);
                heartbeatRunning = false;
            }
            self.onclose(event);
        });
        eventTarget.addEventListener('connecting', function(event) {
            self.onconnecting(event);
        });
        eventTarget.addEventListener('message', function(event) {
            self.onmessage(event);
        });
        eventTarget.addEventListener('error', function(event) {
            self.onerror(event);
        });

        // Expose the API required by EventTarget

        this.addEventListener = eventTarget.addEventListener.bind(eventTarget);
        this.removeEventListener = eventTarget.removeEventListener.bind(eventTarget);
        this.dispatchEvent = eventTarget.dispatchEvent.bind(eventTarget);

        /**
         * This function generates an event that is compatible with standard
         * compliant browsers and IE9 - IE11
         *
         * This will prevent the error:
         * Object doesn't support this action
         *
         * http://stackoverflow.com/questions/19345392/why-arent-my-parameters-getting-passed-through-to-a-dispatched-event/19345563#19345563
         * @param s String The name that the event should use
         * @param args Object an optional object that the event will use
         */
        function generateEvent(s, args) {
            var evt = document.createEvent('CustomEvent');
            evt.initCustomEvent(s, false, false, args);
            return evt;
        }

        this.open = function(reconnectAttempt) {
            ws = new WebSocket(self.url, protocols || []);
            ws.binaryType = this.binaryType;

            if (reconnectAttempt) {
                if (this.maxReconnectAttempts && this.reconnectAttempts > this.maxReconnectAttempts) {
                    return;
                }
            } else {
                eventTarget.dispatchEvent(generateEvent('connecting'));
                this.reconnectAttempts = 0;
            }

            if (self.debug || AcmedcareWss.debugAll) {
                console.debug('AcmedcareWss', 'attempt-connect', self.url);
            }

            var localWs = ws;
            var timeout = setTimeout(function() {
                if (self.debug || AcmedcareWss.debugAll) {
                    console.debug('AcmedcareWss', 'connection-timeout', self.url);
                }
                timedOut = true;
                localWs.close();
                timedOut = false;
            }, self.timeoutInterval);

            ws.onopen = function(event) {
                clearTimeout(timeout);
                if (self.debug || AcmedcareWss.debugAll) {
                    console.debug('AcmedcareWss', 'onopen', self.url);
                }
                self.protocol = ws.protocol;
                self.readyState = WebSocket.OPEN;
                self.reconnectAttempts = 0;
                var e = generateEvent('open');
                e.isReconnect = reconnectAttempt;
                reconnectAttempt = false;
                eventTarget.dispatchEvent(e);
            };

            ws.onclose = function(event) {
                clearTimeout(timeout);
                ws = null;
                if (forcedClose) {
                    self.readyState = WebSocket.CLOSED;
                    eventTarget.dispatchEvent(generateEvent('close'));
                } else {
                    self.readyState = WebSocket.CONNECTING;
                    var e = generateEvent('connecting');
                    e.code = event.code;
                    e.reason = event.reason;
                    e.wasClean = event.wasClean;
                    eventTarget.dispatchEvent(e);
                    if (!reconnectAttempt && !timedOut) {
                        if (self.debug || AcmedcareWss.debugAll) {
                            console.debug('AcmedcareWss', 'onclose', self.url);
                        }
                        eventTarget.dispatchEvent(generateEvent('close'));
                    }

                    var timeout = self.reconnectInterval * Math.pow(self.reconnectDecay, self.reconnectAttempts);
                    setTimeout(function() {
                        self.reconnectAttempts++;
                        self.open(true);
                    }, timeout > self.maxReconnectInterval ? self.maxReconnectInterval : timeout);
                }
            };
            ws.onmessage = function(event) {
                if (self.debug || AcmedcareWss.debugAll) {
                    console.debug('AcmedcareWss', 'onmessage', self.url, event.data);
                }
                var e = generateEvent('message');
                e.data = event.data;
                eventTarget.dispatchEvent(e);
            };
            ws.onerror = function(event) {
                if (self.debug || AcmedcareWss.debugAll) {
                    console.debug('AcmedcareWss', 'onerror', self.url, event);
                }
                eventTarget.dispatchEvent(generateEvent('error'));
            };
        };

        // Whether or not to create a websocket upon instantiation
        if (this.automaticOpen == true) {
            this.open(false);
        }

        // Access Token Auth
        this.auth = function(token, callback) {
            if (ws) {
                if (self.debug || AcmedcareWss.debugAll) {
                    console.debug('AcmedcareWss', 'send', self.url, token);
                }

                if (typeof callback !== 'undefined') {
                    AcmedcareWss.prototype.authCallback = callback;
                }

                // build auth message
                let request = {};
                request.bizCode = 0x30000;
                request.accessToken = token;
                request.wssClientType = 'Normal';

                return ws.send(JSON.stringify(request));
            } else {
                throw 'INVALID_STATE_ERR : Pausing to reconnect websocket';
            }
        };

        // Access Token Auth
        this.pushOrder = function(orderDetail, subOrgId) {
            if (ws) {
                if (self.debug || AcmedcareWss.debugAll) {
                    console.debug('AcmedcareWss', 'send', self.url, orderDetail, subOrgId);
                }

                // build auth message
                let request = {};
                request.bizCode = 0x31002;
                request.orgId = this.getDefaultRequest().orgId;
                request.parentOrgId = this.getDefaultRequest().parentOrgId;
                request.areaNo = this.getDefaultRequest().areaNo;
                request.passportId = this.getDefaultRequest().passportId;
                request.orderDetail = orderDetail;
                request.subOrgId = subOrgId;

                return ws.send(JSON.stringify(request));
            } else {
                throw 'INVALID_STATE_ERR : Pausing to reconnect websocket';
            }
        };

        this.registerClient = function(areaNo, orgId, passportId, parentOrgId, callback) {
            if (ws) {
                if (self.debug || AcmedcareWss.debugAll) {
                    console.debug('AcmedcareWss', 'send', self.url, areaNo, orgId, passportId);
                }

                if (typeof callback !== 'undefined') {
                    AcmedcareWss.prototype.registerClientCallback = callback;
                }

                console.log('begin register client.');
                // build auth message
                let request = {};
                request.bizCode = 0x30001;
                request.orgId = orgId;
                request.parentOrgId = parentOrgId;
                request.areaNo = areaNo;
                request.passportId = passportId;

                // save
                defaultRequest = request;
                return ws.send(JSON.stringify(request));
            } else {
                throw 'INVALID_STATE_ERR : Pausing to reconnect websocket';
            }
        };

        this.pullOnlineSubOrgs = function(areaNo, orgId, passportId, callback) {
            if (ws) {
                if (self.debug || AcmedcareWss.debugAll) {
                    console.debug('AcmedcareWss', 'send', self.url, areaNo, orgId, passportId);
                }

                if (typeof callback !== 'undefined') {
                    AcmedcareWss.prototype.pullOnlineSubOrgsCallback = callback;
                }
                // build auth message
                let pullRequest = {};
                pullRequest.bizCode = 0x31001;
                pullRequest.orgId = orgId;
                pullRequest.areaNo = areaNo;
                pullRequest.passportId = passportId;

                return ws.send(JSON.stringify(pullRequest));
            } else {
                throw 'INVALID_STATE_ERR : Pausing to reconnect websocket';
            }
        };

        /**
         * Transmits data to the server over the WebSocket connection.
         *
         * @param data a text string, ArrayBuffer or Blob to send to the server.
         */
        this.send = function(data) {
            if (ws) {
                if (self.debug || AcmedcareWss.debugAll) {
                    console.debug('AcmedcareWss', 'send', self.url, data);
                }
                return ws.send(data);
            } else {
                throw 'INVALID_STATE_ERR : Pausing to reconnect websocket';
            }
        };

        /**
         * Closes the WebSocket connection or connection attempt, if any.
         * If the connection is already CLOSED, this method does nothing.
         */
        this.close = function(code, reason) {
            // Default CLOSE_NORMAL code
            if (typeof code === 'undefined') {
                code = 1000;
            }
            forcedClose = true;
            if (ws) {
                ws.close(code, reason);
            }
        };

        /**
         * Additional public API method to refresh the connection if still open (close, re-open).
         * For example, if the app suspects bad data / missed heart beats, it can try to refresh.
         */
        this.refresh = function() {
            if (ws) {
                ws.close();
            }
        };
    }

    /**
     * An event listener to be called when the WebSocket connection's readyState changes to OPEN;
     * this indicates that the connection is ready to send and receive data.
     */
    AcmedcareWss.prototype.onopen = function(event) {
    };
    /** An event listener to be called when the WebSocket connection's readyState changes to CLOSED. */
    AcmedcareWss.prototype.onclose = function(event) {
    };
    /** An event listener to be called when a connection begins being attempted. */
    AcmedcareWss.prototype.onconnecting = function(event) {
    };
    /** An event listener to be called when a message is received from the server. */
    AcmedcareWss.prototype.onmessage = function(event) {
        let result = JSON.parse(event.data);
        console.log('Rev: ' + event.data);

        if (result.bizCode === 0x30003) {
            // heartbeat response
            console.log('heartbeat response.');
        }

        // auth response
        if (result.bizCode === 0x30000) {
            this.authCallback(result.code === 0, result.data);
        }

        // register response message
        if (result.bizCode === 0x30001) {
            if (result.code !== 0) {
                // clear request
                this.clearDefaultRequest();
                this.registerClientCallback(false);
            } else {
                // startup heartbeat
                this.newHeartbeat();
                this.registerClientCallback(true);
            }
        }

        // pull online sub orgs response
        if (result.bizCode === 0x31001) {
            this.pullOnlineSubOrgsCallback(result.data);
        }

        // push command message
        if (result.bizCode === 0x31002) {
            this.receiveOrder(result.data);
        }
    };
    /** An event listener to be called when an error occurs. */
    AcmedcareWss.prototype.onerror = function(event) {
    };

    // biz function
    AcmedcareWss.prototype.authCallback = function(success, message) {
    };

    AcmedcareWss.prototype.pullOnlineSubOrgsCallback = function(data) {
    };

    AcmedcareWss.prototype.registerClientCallback = function(data) {
    };

    AcmedcareWss.prototype.receiveOrder = function(data) {
    };


    /**
     * Whether all instances of AcmedcareWss should log debug messages.
     * Setting this to true is the equivalent of setting all instances of AcmedcareWss.debug to true.
     */
    AcmedcareWss.debugAll = false;

    AcmedcareWss.CONNECTING = WebSocket.CONNECTING;
    AcmedcareWss.OPEN = WebSocket.OPEN;
    AcmedcareWss.CLOSING = WebSocket.CLOSING;
    AcmedcareWss.CLOSED = WebSocket.CLOSED;

    return AcmedcareWss;
});

