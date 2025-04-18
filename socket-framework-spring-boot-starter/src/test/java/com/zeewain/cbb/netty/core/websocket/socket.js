var socket;
if (!window.WebSocket) {
  window.WebSocket = window.MozWebSocket;
}
if (window.WebSocket) {

  // 本地的直接测试
  socket = new WebSocket("ws://192.168.1.98:9600/websocket");
  // 本地开启 https 的测试
  // socket = new WebSocket("wss://webrtc.zeewain.localhost/websocket");
  // 开发环境的网关测试
  // socket = new WebSocket("wss://dev.local.zeewain.com/api/open-platform/websocket");

  // 经过网关的负载均衡测试
  // socket = new WebSocket("ws://192.168.0.55:9600/websocket?upstream=vr-meeting-msg-netty-nacos&meetingId=100");
  // socket = new WebSocket("ws://192.168.0.55:28000/websocket?upstream=65fe2b5c-1c79-4316-bc1a-ca66a4f0ac42&meetingId=100", "custom-header");
  // socket = new WebSocket("ws://localhost:8000/websocket?upstream=vr-meeting-msg-netty-nacos&meetingId=100");

  socket.onmessage = function(event) {
    var ta = document.getElementById('responseText');
    ta.value = ta.value + '\n' + event.data
  };
  socket.onopen = function(event) {
    var ta = document.getElementById('responseText');
    ta.value = "Web Socket opened!";
  };
  socket.onclose = function(event) {
    var ta = document.getElementById('responseText');
    ta.value = ta.value + "Web Socket closed";
  };
} else {
  alert("Your browser does not support Web Socket.");
}

function send(message) {
  if (!window.WebSocket) { return; }
  if (socket.readyState == WebSocket.OPEN) {


    var message = {
        path: 'HEARTBEAT_REQ',
        body: { ping: true}
    }

    socket.send(message2Byte(message));
    // socket.send(message);
  } else {
    alert("The socket is not open.");
  }
}

// 请求序号，全局唯一自增
var seqId = 0;

function appendStr(dataview, idx, str){
    for(var i=0; i<str.length; i++) {
        dataview.setInt8(idx++, str.charCodeAt(i));
    }
}


/*
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 5a 57 41 49 01 00 00 00 31 00 00 00 00 00 01 00 |ZWAI....1.......|
|00000010| 0d 48 45 41 52 54 42 45 41 54 5f 52 45 51 00 00 |.HEARTBEAT_REQ..|
|00000020| 00 00 00 0d 7b 22 70 69 6e 67 22 3a 74 72 75 65 |....{"ping":true|
|00000030| 7d                                              |}               |
+--------+-------------------------------------------------+----------------+
*/
// 参考 https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView#examples
function message2Byte(message) {


    message.id = seqId++;
    var pathLength = message.path.length;

    var body = JSON.stringify(message.body);
    var bodyLength = body.length;

    var totalLength = 16 + 7 + pathLength + bodyLength;


    const buffer = new ArrayBuffer(totalLength);
    const view = new DataView(buffer, 0);

    var idx = 0;

    // 魔数 固定 zwai 4字节
    view.setInt8(idx++, 0x5a);
    view.setInt8(idx++, 0x57);
    view.setInt8(idx++, 0x41);
    view.setInt8(idx++, 0x49);

    // version 1 字节
    view.setInt8(idx++, 0x01);

    // total length 4 字节
    view.setInt32(idx, totalLength);
    idx = idx + 4;

    // 序列号算法，默认0，1个字节
    view.setInt8(idx++, 0x00);
    // 压缩算法，默认0，1个字节
    view.setInt8(idx++, 0x00);

    // 请求序号 4 字节
    view.setInt32(idx, message.id);
    idx = idx + 4;

    // 请求标识， 1字节， 0 表示请求报文
    view.setInt8(idx++, 0x00);

    // 消息报文长度, 1 字节
    view.setInt8(idx++, pathLength);
    appendStr(view, idx, message.path)
    idx += pathLength

    // 消息头部长度, 2 字节， 目前默认为空
    view.setInt16(idx, 0);
    idx += 2;

    // 消息正文长度， 4 字节
    view.setInt32(idx, bodyLength);
    idx += 4;
    appendStr(view, idx, body)
    idx += bodyLength

    return buffer;
}


