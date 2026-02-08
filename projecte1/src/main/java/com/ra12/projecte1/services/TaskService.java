package com.ra12.projecte1.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ra12.projecte1.logs.TaskLogs;
import com.ra12.projecte1.model.Task;
import com.ra12.projecte1.odt.taskRequestDTO;
import com.ra12.projecte1.odt.taskResponseDTO;
import com.ra12.projecte1.repository.TaskRepository;



@Service
public class TaskService {

    private static final Path PATH_DIR = Paths.get("private");

    @Autowired
    TaskRepository repo;
    TaskLogs log;

    public String[] createTask(taskRequestDTO taskDTO){
        Task task = new Task();
        task.setNomTasca(taskDTO.getNomTasca());
        task.setSparks(taskDTO.getSparks());
        task.setDataLimit(taskDTO.getDataLimit());
        
        String msg = log.info("TaskService", "createTask", "Creant una tasca");
        log.writeToFile(msg);

        try {
            int result = repo.createTask(task);
            if (result > 0) {
                log.writeToFile(log.info("TaskService", "createTask", "Usuari creat correctament"));
                return new String[]{"ok", "Tasca s'ha creat correctament"};
            } else {
                log.writeToFile(log.error("TaskService", "createTask", "No s'ha pogut crear l'usuari"));
                return new String[]{"e", "No s'ha pogut crear la tasca"};
            }
        } catch (Exception e) {
            log.writeToFile(log.error("TaskService", "createTask", "No s'ha pogut crear l'usuari"));
            return new String[]{"e", e.getMessage()};
        }
    }

    // funció per afegir el url d'on es troba la imatge de la taska
    public String[] addImage(Long id, MultipartFile image){
        Task existeix = idExisteix(id);
        if (existeix == null){ // comprobem que la taska amb el id existeixi
            log.error("TaskService", "idExisteix", "Usuari no trobat");
            return new String[]{"e", "Usuari no trobat"};
        }else { // en cas de que si existeixi:
            try{
                if (Files.notExists(PATH_DIR)){ // comprova si no existeix la carpeta on es guarden les imatges (important: private)
                    Files.createDirectories(PATH_DIR); // la crea
                }
                String urlImage = image.getOriginalFilename();
                Path urlSencer = PATH_DIR.resolve(urlImage); // la url d'on es guardara la imatge
                Files.copy(image.getInputStream(), urlSencer, StandardCopyOption.REPLACE_EXISTING); // guarga l'imatge, en cas de que ja n'hi hagi una la remplaça
                int resposta = repo.setImagePath(id, urlSencer.toString()); // guardem a la base de dades la url de la imatge

                if (resposta == 0){ // en cas de que s'hagi donat un error (0-no s'ha actualitzat res)
                    log.error("TaskService", "addImage", "L'imatge no s'ha actualitzat"); // guardem l'error a logs
                    return new String[] {"e", "No s'ha pogut actualitzar l'imatge"}; // informa
                }else { 
                    log.info("TaskService", "addImage", "La url de la imatge s'ha actualitzat");
                    return new String[] {"ok", "Imatge actualitzada correctament"};
                }
            }catch (Exception e){ // en cas de que es produeixi un error que no hem previst
                log.error("TaskService", "addImage", "Error de carpetes i files");
                return new String[] {"e", e.getMessage()};
            }
        }
    }

    public int updateTask(Long taskId, Task task) {
        log.info("TaskService", "updateTask", "Accedint a updateTask amb id: " + taskId);
        
        try{
            int result = repo.updateTaskById(taskId, task);
            if(result > 0){
                log.info("TaskService", "updateTask", 
                    "Task amb id " + taskId + " actualitzada correctament");
            }else{
                log.error("TaskService", "updateTask", 
                    "Task amb id " + taskId + " no trobada per actualitzar");
            }
            return result;
        }catch(Exception e){
            log.error("TaskService", "updateTask", "Error actualitzant taska");
            return 0;
        }
    }

    // funció per comprobar si la taska amb el id existeix a la base de dades.
    public Task idExisteix(Long id){
        Task task = repo.findTaskById(id) != null ? repo.findTaskById(id) : null;
        return task;
    }

}
