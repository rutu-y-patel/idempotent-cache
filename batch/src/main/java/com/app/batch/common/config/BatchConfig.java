package com.app.batch.common.config;

import com.app.batch.common.constant.BatchConstant;
import com.app.batch.common.listener.FileMovingJobListener;
import com.app.batch.model.EmploymentEventRecord;
import com.app.batch.model.EmploymentEventWithContext;
import com.ms.idempotentcache.cache.IdempotencyCacheService;
import com.ms.idempotentcache.context.ContextIdProvider;
import com.ms.idempotentcache.record.RecordKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    /**
     * Creates a MultiResourceItemReader for EmploymentEventWithContext, reading all CSV files
     * from the specified input directory pattern.
     *
     * @param cacheService The idempotency cache service to use for duplicate checking.
     * @return Configured MultiResourceItemReader
     * @throws IOException If an I/O error occurs
     */
    @Bean
    public MultiResourceItemReader<EmploymentEventWithContext> multiFileReader(IdempotencyCacheService cacheService) throws IOException {
        String resourcePattern = System.getProperty(BatchConstant.INPUT_RESOURCE_DIRECTORY);
        if (resourcePattern == null || resourcePattern.trim().isEmpty()) {
            log.error("Missing required VM argument: -Dinput.resource.directory");
            throw new IllegalArgumentException("Missing required VM argument: -Dinput.resource.directory");
        }

        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(resourcePattern);
        if (resources == null || resources.length == 0) {
            log.warn("No input resources found for pattern: {}", resourcePattern);
        } else {
            log.info("Found resources: {}", Arrays.toString(resources));
        }

        MultiResourceItemReader<EmploymentEventWithContext> multiReader = new MultiResourceItemReader<>();
        multiReader.setResources(resources);

        // Set up line mapping for the file
        ContextAwareFlatFileItemReader delegate = new ContextAwareFlatFileItemReader();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(BatchConstant.SSN, BatchConstant.PLAN, BatchConstant.CLIENT_ID, BatchConstant.EMPLOYMENT_EVENT);

        BeanWrapperFieldSetMapper<EmploymentEventRecord> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(EmploymentEventRecord.class);

        DefaultLineMapper<EmploymentEventRecord> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        delegate.setLineMapper(lineMapper);
        delegate.setLinesToSkip(BatchConstant.LINES_TO_SKIP); // skip header
        delegate.setCacheService(cacheService);
        multiReader.setDelegate(delegate);

        return multiReader;
    }

    // RecordKeyProvider bean for EmploymentEventRecord
    @Bean
    public RecordKeyProvider<EmploymentEventRecord> recordKeyProvider() {
        return record -> String.join(":",
                safe(record.getSsn()),
                safe(record.getPlan()),
                safe(record.getClientId()),
                safe(record.getEmploymentEvent())
        );
    }

    // ContextIdProvider bean for String (file name)
    @Bean
    public ContextIdProvider<String> contextIdProvider() {
        return fileName -> String.format(BatchConstant.EMPLOYMENT_EVENTS_S, safe(fileName));
    }


    // ItemProcessor that filters out already-processed records for idempotency.
    @Bean
    public ItemProcessor<EmploymentEventWithContext, EmploymentEventWithContext> processor(
            IdempotencyCacheService cache,
            RecordKeyProvider<EmploymentEventRecord> recordKeyProvider,
            ContextIdProvider<String> contextIdProvider) {
        return recordWithCtx -> {
            String contextId = contextIdProvider.getContextId(recordWithCtx.getFileName());
            EmploymentEventRecord record = recordWithCtx.getRecord();
            String key = recordKeyProvider.getKey(record);
            log.debug("Processing record in context '{}' with key '{}'", contextId, key);
            //Thread.sleep(1000);
            if (cache.isProcessed(contextId, key)) {
                log.info("Skipping duplicate: '{}' in context '{}'", key, contextId);
                return null; // skip duplicate
            }
            cache.markProcessed(contextId, key);
            log.info("Marked processed: '{}' in context '{}'", key, contextId);
            return recordWithCtx;
        };
    }

    // ItemWriter that logs processed records and registers each file for post-processing move.
    @Bean
    public ItemWriter<EmploymentEventWithContext> writer(FileMovingJobListener fileMovingJobListener) {
        return items -> {
            Set<String> filesInThisChunk = new HashSet<>();
            for (EmploymentEventWithContext item : items) {
                EmploymentEventRecord record = item.getRecord();
                String fileName = item.getFileName();
                log.info("Processed: ClientId: {} PlanId: {} SSN: {} File: {}",
                        record.getClientId(), record.getPlan(), record.getSsn(), fileName);
                filesInThisChunk.add(fileName);
            }
            filesInThisChunk.forEach(fileMovingJobListener::addFile);
        };
    }


    @Bean
    public Job employmentEventsJob(JobRepository jobRepository, Step step1, FileMovingJobListener fileMovingJobListener) {
        return new JobBuilder(BatchConstant.EMPLOYMENT_EVENTS_JOB, jobRepository)
                .start(step1)
                .listener(fileMovingJobListener)
                .build();
    }

    @Bean
    public FileMovingJobListener fileMovingJobListener(IdempotencyCacheService cacheService, ContextIdProvider<String> contextIdProvider) {
        String inputDir = System.getProperty(BatchConstant.INPUT_DIRECTORY);
        String processedDir = System.getProperty(BatchConstant.PROCESSED_DIRECTORY);
        return new FileMovingJobListener(inputDir, processedDir, cacheService, contextIdProvider);
    }

    @Bean
    public Step step(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      MultiResourceItemReader<EmploymentEventWithContext> multiFileReader,
                      ItemProcessor<EmploymentEventWithContext, EmploymentEventWithContext> processor,
                      ItemWriter<EmploymentEventWithContext> writer) {
        int chunkSize = Integer.valueOf(System.getProperty(BatchConstant.BATCH_CHUNK_SIZE, BatchConstant.CHUNK_SIZE));
        return new StepBuilder(BatchConstant.STEP, jobRepository)
                .<EmploymentEventWithContext, EmploymentEventWithContext>chunk(chunkSize, transactionManager)
                .reader(multiFileReader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    /**
     * Ensures null values are safely handled (so you never get "null" in your key).
     */
    private String safe(String value) {
        return value == null ? "" : value;
    }

}