package com.app.batch.common.config;

import com.app.batch.common.context.FileContextHolder;
import com.app.batch.model.EmploymentEventRecord;
import com.app.batch.model.EmploymentEventWithContext;
import com.ms.idempotentcache.cache.IdempotencyCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;


/**
 * FlatFileItemReader that sets FileContextHolder with the current file name whenever setResource is called.
 */
public class ContextAwareFlatFileItemReader<T>
        extends FlatFileItemReader<T>
        implements ResourceAwareItemReaderItemStream<T> {

    private static final Logger log = LoggerFactory.getLogger(ContextAwareFlatFileItemReader.class);

    private String fileName;
    private IdempotencyCacheService cacheService;
    private Resource resource;

    public void setCacheService(IdempotencyCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void setResource(Resource resource) {
        super.setResource(resource);
        this.fileName = (resource != null) ? resource.getFilename() : null;
        this.resource = resource;
    }

    @Override
    public T read() throws Exception {
        T record = super.read();
        if (record != null && record instanceof EmploymentEventRecord) {
            return (T) new EmploymentEventWithContext((EmploymentEventRecord) record, fileName);
        }
        return record;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public void close() {
        super.close();
        FileContextHolder.clear();
    }
}
