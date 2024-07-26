package com.batch.config;

import com.batch.steps.ItemDescompressStep;
import com.batch.steps.ItemProcessorStep;
import com.batch.steps.ItemReaderStep;
import com.batch.steps.ItemWriterStep;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing //habilitar el procesamiento de lotes
public class BatchConfiguration {


    public JobBuilder jobBuilder;

    public StepBuilder stepBuilder;


    @Bean
    public ItemDescompressStep itemDescompressStep() {
        return new ItemDescompressStep();
    }

    @Bean
    public ItemReaderStep itemReaderStep() {
        return new ItemReaderStep();
    }

    @Bean
    public ItemProcessorStep itemProcessorStep() {
        return new ItemProcessorStep();
    }

    @Bean
    public ItemWriterStep itemWriterStep() {
        return new ItemWriterStep();
    }

    //definir pasos en orden primero descompress
    @Bean
    public Step descompressFileStep(JobRepository jobRepository, Tasklet itemDescompressStep, PlatformTransactionManager transactionManager){
        return new StepBuilder("descompressFileStep", jobRepository)
                .tasklet(itemDescompressStep, transactionManager)
                .build();
    }

    //definir pasos en orden segundo reader
    @Bean
    public Step readDataStep(JobRepository jobRepository, Tasklet itemReaderStep, PlatformTransactionManager transactionManager){
        return new StepBuilder("readDataStep", jobRepository)
                .tasklet(itemReaderStep, transactionManager)
                .build();
    }

    //definir pasos en orden tercero processor
    @Bean
    public Step processDataStep(JobRepository jobRepository, Tasklet itemProcessorStep, PlatformTransactionManager transactionManager){
        return new StepBuilder("processDataStep", jobRepository)
                .tasklet(itemProcessorStep, transactionManager)
                .build();
    }

    //definir pasos en orden cuarto writer
    @Bean
    public Step writeDataStep(JobRepository jobRepository, Tasklet itemWriterStep, PlatformTransactionManager transactionManager){
        return new StepBuilder("writeDataStep", jobRepository)
                .tasklet(itemWriterStep, transactionManager)
                .build();
    }

    //definir el job
    @Bean
    public Job readCSVJob(JobRepository jobRepository,
                          Step descompressFileStep,
                          Step readDataStep,
                          Step processDataStep,
                          Step writeDataStep){
        return new JobBuilder("readCSVJob", jobRepository)
                .start(descompressFileStep)
                .next(readDataStep)
                .next(processDataStep)
                .next(writeDataStep)
                .build();
    }



}
