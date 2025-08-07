package com.app.batch.common.listener;

import com.ms.idempotentcache.cache.IdempotencyCacheService;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.ms.idempotentcache.context.ContextIdProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class FileMovingJobListener implements JobExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(FileMovingJobListener.class);

    private final String inputDir;
    private final String processedDir;
    private final Set<String> filesToMove = ConcurrentHashMap.newKeySet();
    private final IdempotencyCacheService cacheService;
    private final ContextIdProvider<String> contextIdProvider;

    public FileMovingJobListener(String inputDir, String processedDir, IdempotencyCacheService cacheService, ContextIdProvider<String> contextIdProvider) {
        this.inputDir = inputDir;
        this.processedDir = processedDir;
        this.cacheService = cacheService;
        this.contextIdProvider = contextIdProvider;
    }

    public void addFile(String fileName) {
        filesToMove.add(fileName);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        int moved = 0, failed = 0;
        for (String fileName : filesToMove) {
            String contextId = contextIdProvider.getContextId(fileName);
            try {
                Path source = Paths.get(inputDir, fileName);
                Path target = Paths.get(processedDir, fileName);
                Files.createDirectories(target.getParent());
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                log.info("Moved file '{}' to processed folder: {}", fileName, target);

                cacheService.clearCacheForContext(contextId);
                log.info("Cleared cache for context '{}'", contextId);
                moved++;
            } catch (Exception e) {
                log.error("Failed to move file '{}': {}", fileName, e.getMessage(), e);
                failed++;
            }
        }
        log.info("File move summary: moved={}, failed={}", moved, failed);
    }


}