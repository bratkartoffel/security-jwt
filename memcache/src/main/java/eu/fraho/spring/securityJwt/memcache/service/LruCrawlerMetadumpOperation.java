package eu.fraho.spring.securityJwt.memcache.service;

import eu.fraho.spring.securityJwt.memcache.dto.LruMetadumpEntry;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationCallback;

public interface LruCrawlerMetadumpOperation extends Operation {
    interface Callback extends OperationCallback {
        void gotMetadump(LruMetadumpEntry entry);
    }
}
