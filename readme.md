

### client:

type|length|host|port
-|-|-|-|-
byte|short|string|short
1|x|www.github.com|443

### client：
type|timeStamp|tokenLength|token|length|host|port
-|-|-|-|-|-|-
byte|long|short|string|short|string|short
2|1573029976101|n|xyxyxy|n|www.github.com|443


### server:
响应|说明
-|-
1|连接成功
2|需要验证
Disconnect|拒绝

