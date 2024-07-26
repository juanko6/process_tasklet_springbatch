package com.batch.steps;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


@Slf4j
public class ItemDescompressStep implements Tasklet {

    @Autowired
    private ResourceLoader resourceLoader;


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info("-----------> Inicio del paso de DESCOMPRESION <-----------");


        // Se recupera el archivo ZIP que se encuentra en la carpeta resources
        Resource resource = resourceLoader.getResource("classpath:files/person.zip");

        // comprobar si el archivo ZIP existe en la ruta, en macOS se debe evitar los archivos temporales
        if (!resource.exists()) {
            throw new FileNotFoundException("El archivo ZIP no se encuentra en la ruta especificada: " + resource.getURI());
        }
        String filepath = resource.getFile().getAbsolutePath();

        // pasos para descomprimir el archivo ZIP
        //se referencia el archivo ZIP
        ZipFile zipFile = new ZipFile(filepath);

        //crear el archivo de destino
        File destDir = new File(resource.getFile().getParent(), "destination");
        //validar si existe el directorio, si no se crea el directorio
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        // esto toma los archivos que hay en el zip y los toma como entradas
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements()){
            ZipEntry zipEntry = entries.nextElement();
            File file = new File(destDir, zipEntry.getName()); //guardar con el mismo nombre

            // Evitar procesar archivos temporales de macOS
            if (zipEntry.getName().startsWith("__MACOSX") || zipEntry.getName().startsWith("._")) {
                continue;
            }

            //si es un directorio se crea
            if(file.isDirectory()){
                file.mkdirs();
            } else {
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                //se crea el archivo
                FileOutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int length;

                while((length = inputStream.read(buffer)) > 0){
                    outputStream.write(buffer, 0, length);
                }

                outputStream.close();
                inputStream.close();
            }
        }

        zipFile.close();

        log.info("-----------> Fin del paso de DESCOMPRESION <-----------");
        return RepeatStatus.FINISHED;
    }
}
