package com.tgu.registry.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

import java.util.Arrays;


@Slf4j
public class WatchZK {

    private CuratorFramework client;

    private ServiceCache cache;

    public WatchZK(CuratorFramework client, ServiceCache cache) {
        this.client = client;
        this.cache = cache;
    }


    // 监听当前节点和子节点的更新、删除、创建
    public void watch2Update(String path) {
        CuratorCache curatorCache = CuratorCache.build(client, "/");
        curatorCache.listenable().addListener(new CuratorCacheListener() {
            // params: 参数类型；节点更新前的状态、数据；节点更新后的状态、数据
            // 创建节点：节点刚被创建，不存在更新前节点，所以第二个参数为null
            // 删除节点：节点被删除，不存在更新后节点，所以第三个参数为null
            // 如果节点创建时没有赋值（只创建了节点），在这种情况下，更新前节点的data为null
            @Override
            public void event(Type type, ChildData childData, ChildData childData1) {
                switch (type.name()) {
                    case "NODE_CREATED":
                        String[] pathList = parsePath(childData1);
                        if (pathList.length <= 2) {
                            break;
                        } else {
                            String serviceName = pathList[1];
                            String address = pathList[2];
                            cache.addService2Cache(serviceName, address);
                        }
//                        log.info("NODE_CREATED >>> {}", new String(childData1.getData()));
                        break;
                    case "NODE_CHANGE":
                        if (childData.getData() != null) {
                            log.info("data before change >>> {}", new String(childData.getData()));
                        } else {
                            log.info("节点第一次赋值");
                        }
                        String[] oldPathList = parsePath(childData);
                        String[] newPathList = parsePath(childData1);
                        cache.replaceServiceAddress(oldPathList[1], oldPathList[2], newPathList[2]);
                        log.info("NODE_CHANGE >>> from {} to {}", new String(childData.getData()), new String(childData1.getData()));
                        break;
                    case "NODE_DELETE":
                        String[] deletePath = parsePath(childData);
                        if (deletePath.length <= 2) {
                            break;
                        } else {
                            String serviceName = deletePath[1];
                            String address = deletePath[2];
                            cache.delete(serviceName, address);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        curatorCache.start();
    }
    private String[] parsePath(ChildData childData) {
        String string = childData.getPath();
        String[] split = string.split("/");
        log.info("parsed path >>> {}", Arrays.toString(split));
        return split;
    }



}
