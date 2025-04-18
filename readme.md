A **high-performance Netty-based socket framework** that simplifies network communication by handling all low-level **encoding/decoding**. Developers can focus purely on **business logic** without dealing with raw bytes or Netty's underlying complexities.

### **Key Features:**
✅ **Automatic Serialization** – Built-in support for common protocols (e.g., Protobuf, JSON).  
✅ **Business-First API** – Just implement your logic; the framework handles the rest.  
✅ **Scalable & Efficient** – Leverages Netty’s event-driven model for high throughput.  
✅ **No Blocking** – Supports non-blocking I/O for improved performance.


### **Example Usage:**


1. Define the Processor to handle the tcp request

```java

@NettyProcessor
public class CompanyProcessor {

    @Autowired
    private final CompanyInfoServiceImpl companyInfoService;

    @NettyMapping(LIST_COMPANY_REQ)
    public NettyResponse<List<CompanyVo>> list(ChannelHandlerContext ctx,  @Valid CompanyListQo qo) {
        List<CompanyVo> companyVos = companyInfoService.companyList(qo);
        return NettyResponse.success(companyVos);
    }
}

```

2. Create a client interface to send requests


```java

@NettyClient
public interface HeartBeatClient {

    /**
     * heartbeat req for sync req
     */
    @NettyMapping(HEARTBEAT_REQ)
    NettyResponse<HeartbeatMessage> sendHeartbeat(Channel channel, HeartbeatMessage param);

    /**
     * heartbeat req for async sync req
     */
    @NettyMapping(HEARTBEAT_REQ)
    Promise<NettyResponse<HeartbeatMessage>> sendAsyncHeartbeat(Channel channel, HeartbeatMessage param);

}


```


**Ideal for:**  
• Rapid development of TCP services  
• Game servers, IoT, RPC frameworks  
• Any app needing reliable, high-speed networking