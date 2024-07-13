package com.dns;

import com.utils.ResourceManager;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.HostsFileEntries;
import io.netty.resolver.HostsFileEntriesResolver;
import io.netty.resolver.HostsFileParser;
import io.netty.resolver.ResolvedAddressTypes;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.resolver.dns.DnsServerAddressStreamProvider;
import io.netty.resolver.dns.MultiDnsServerAddressStreamProvider;
import io.netty.resolver.dns.SingletonDnsServerAddressStreamProvider;
import io.netty.util.concurrent.EventExecutor;

import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * 异步dns解析
 */
public class AsnycDns extends AddressResolverGroup {

    public static AsnycDns INSTANCE = new AsnycDns();

    @Override
    protected AddressResolver newResolver(EventExecutor executor) {
        assertTrue(EventLoop.class.isAssignableFrom(executor.getClass()), "异步dns EventExecutor 转 EventLoop 类型不匹配");
        return getResolver0((EventLoop) executor).asAddressResolver();
    }

    private DnsNameResolver getResolver0(EventLoop eventLoop) {
        return new DnsNameResolverBuilder(eventLoop)
                .channelType(NioDatagramChannel.class)
                .maxQueriesPerResolve(8)
                .optResourceEnabled(true)
                .ndots(1)
                .nameServerProvider(pro())
                .resolvedAddressTypes(ResolvedAddressTypes.IPV4_PREFERRED)
                .hostsFileEntriesResolver(new LocalHostResolver())
                .build();
    }

    private static class LocalHostResolver implements HostsFileEntriesResolver {
        //系统默认解析器
        private HostsFileEntriesResolver hostsFileEntriesResolver = HostsFileEntriesResolver.DEFAULT;
        //自己解析
        private final Map<String, Inet4Address> inet4Entries;
        private final Map<String, Inet6Address> inet6Entries;

        {
            HostsFileEntries entries = new HostsFileEntries(Collections.<String, Inet4Address>emptyMap(), Collections.<String, Inet6Address>emptyMap());
            try {
                entries = HostsFileParser.parse(new InputStreamReader(ResourceManager.gerResourceForFile("hosts"), StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
            inet4Entries = entries.inet4Entries();
            inet6Entries = entries.inet6Entries();
        }

        //copy from DefaultHostsFileEntriesResolver.address()
        private InetAddress localResource(String inetHost, ResolvedAddressTypes resolvedAddressTypes) {
            String normalized = inetHost.toLowerCase(Locale.ENGLISH);
            switch (resolvedAddressTypes) {
                case IPV4_ONLY:
                    return inet4Entries.get(normalized);
                case IPV6_ONLY:
                    return inet6Entries.get(normalized);
                case IPV4_PREFERRED:
                    Inet4Address inet4Address = inet4Entries.get(normalized);
                    return inet4Address != null ? inet4Address : inet6Entries.get(normalized);
                case IPV6_PREFERRED:
                    Inet6Address inet6Address = inet6Entries.get(normalized);
                    return inet6Address != null ? inet6Address : inet4Entries.get(normalized);
                default:
                    throw new IllegalArgumentException("Unknown ResolvedAddressTypes " + resolvedAddressTypes);
            }
        }

        @Override
        public InetAddress address(String inetHost, ResolvedAddressTypes resolvedAddressTypes) {
            InetAddress localHostResolve = localResource(inetHost, resolvedAddressTypes);
            return localHostResolve != null ? localHostResolve : hostsFileEntriesResolver.address(inetHost, resolvedAddressTypes);
        }
    }

    private DnsServerAddressStreamProvider pro() {
        SingletonDnsServerAddressStreamProvider s1 = new SingletonDnsServerAddressStreamProvider(new InetSocketAddress("8.8.8.8", 53));
        SingletonDnsServerAddressStreamProvider s2 = new SingletonDnsServerAddressStreamProvider(new InetSocketAddress("114.114.114.114", 53));
        return new MultiDnsServerAddressStreamProvider(s1, s2);
    }

    private void assertTrue(boolean instance, String msg) {
        if (!instance) {
            throw new RuntimeException(msg);
        }
    }

}
