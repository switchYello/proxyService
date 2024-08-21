## 是什么

本程序是一个使用Java语言编写符合ss协议规范的服务端  
为什么要重复造轮子呢？ 编写此软件主要是为了学习 netty 和 reactor-netty 的用法

## 启动模式

程序支持三种启动模式：

- ss模式，与原生shadowsocker的能力是一样的，支持四种加密算法
- forward模式，存粹的tcp端口转发
- http(s)代理模式，支持配置用户名密码

## shadowsocker的算法

当前支持四种算法，通过配置文件配置

- Aes128 GCM
- Aes192 GCM
- Aes256 GCM
- Rc4Md5

## 配置说明：

配置是yaml格式，jar文件目录下放置一个 `config.yaml`程序启动后会自动识别，可通过日志看配置有没有生效

```yaml
services:
- name: ss服务            # 名称，随意自己认识即可
  mode: ss               # 标识以ss模式启动
  localPort: 30005       # 本地绑定端口
  passWord: 924c5e53af4a # 密码
  encrypt: aes-128-gcm   # 算法，aes-128-gcm，aes-192-gcm，aes-256-gcm，rc4-md5
  enable: true           # 是否启用

- name: 端口转发服务
  mode: forward                # 端口转发模式
  localPort: 9090              # 本地端口，这里的含义是访问服务器的9090端口，会自动端口映射到www.example.com的3306端口上
  serverHost: www.example.com  # 服务端绑定的地址
  serverPort: 3306             # 服务端绑定的端口
  enable: false                # 是否启用

- name: HTTP代理服务    
  mode: http_proxy       # 以http代理模式启动
  localPort: 9999        # 代理绑定的端口
  userName: a            # 用户名
  passWord: a            # 密码
  enable: false          # 是否启用
global:
  client_time_out: 5000  # 全局客户端超时时间 5s
logger:
  level: DEBUG   # DEBUG,INFO,WARN,ERROR,  这里表示将连接信息输出成什么日志级别，程序只会输出info以上的日志，因此配成DEBUG就不会看到日志
  format: SIMPLE # SIMPLE,HEX_DUMP,TEXTUAL 这里是连接的日志级别SIMPLE输出简单的创建关闭等信息，HEX_DEMP会以16禁止输出报文详细信息

```

## 启动方式
程序不占内存，如果oom了可以适当调大点
```shell
nohup java -Xmn20M -Xmx30M -jar proxyService-1.0.jar &
```


