

## 本软件是一个符合ss协议规范的服务端。



## 两种启动模式

	forward ，数据转发，将接收到的数据原封不动的发送到配置指定的ip端口上。

比如我在服务器A上起一个服务器，在服务器B上配置转发，所有访问服务器B的请求都会发送给服务器A。



ss，shadowsocket模式。

支持的加密方式有一下四种：

- Aes128 GCM

- Aes192 GCM

- Aes256 GCM

- Rc4Md5



就是一个java写的shadowsocker服务端，配置好端口，密码，加密方式，启动即可。
>>>>>>> Stashed changes
