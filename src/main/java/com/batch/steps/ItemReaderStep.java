package com.batch.steps;

import com.batch.entities.Person;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import java.io.FileReader;
import java.io.Reader;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Slf4j // lombok annotation for logging
public class ItemReaderStep implements Tasklet {

    @Autowired
    private ResourceLoader resourceLoader; // ResourceLoader es para cargar archivos


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info("-----------> Inicio del paso de LECTURA <-----------");

        Reader reader = new FileReader(resourceLoader.getResource("classpath:files/destination/persons.csv").getFile());
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .build();

        CSVReader csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(parser)
                .withSkipLines(1)
                .build();

        List<Person> personList = new ArrayList<>();
        String[] actualLine;

        while ((actualLine = csvReader.readNext()) != null){
            Person person = new Person();
            person.setName(actualLine[0]);
            person.setLastName(actualLine[1]);
            person.setAge(Integer.parseInt(actualLine[2]));
            personList.add(person);
        }

        csvReader.close();
        reader.close();

        log.info("-----------> Fin del paso de LECTURA <-----------");

        //este es el contexto de ejecución del job donde se pueden compartir datos entre los pasos
        chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("personList", personList);


        return RepeatStatus.FINISHED;
    }
}
