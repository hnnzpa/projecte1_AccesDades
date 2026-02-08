package com.ra12.projecte1.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ra12.projecte1.logs.TaskLogs;
import com.ra12.projecte1.model.Task;
import com.ra12.projecte1.odt.taskRequestDTO;
import com.ra12.projecte1.repository.TaskRepository;



@Service
public class TaskService {

    private static final Path PATH_DIR = Paths.get("private");

    @Autowired
    TaskRepository repo;
    TaskLogs taskLogs;

    public String[] createTask(taskRequestDTO taskDTO){
        Task task = new Task();
        task.setNomTaska(taskDTO.getNomTaska());
        task.setSparks(taskDTO.getSparks());
        task.setDataLimit(taskDTO.getDataLimit());
        
        try {
            int result = repo.createTask(task);
            if (result > 0) {
                return new String[]{"ok", "Taska s'ha creat correctament"};
            } else {
                return new String[]{"e", "No s'ha pogut crear la taska"};
            }
        } catch (Exception e) {
            return new String[]{"e", e.getMessage()};
        }
    }

    // funció per afegir el url d'on es troba la imatge de la taska
    public String[] addImage(Long id, MultipartFile image){
        Task existeix = idExisteix(id);
        if (existeix == null){ // comprobem que la taska amb el id existeixi
            taskLogs.logError("TaskService", "idExisteix", "Usuari no trobat", null);
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
                    taskLogs.logError("TaskService", "addImage", "L'imatge no s'ha actualitzat", null); // guardem l'error a logs
                    return new String[] {"e", "No s'ha pogut actualitzar l'imatge"}; // informa
                }else { 
                    taskLogs.logInfo("TaskService", "addImage", "La url de la imatge s'ha actualitzat");
                    return new String[] {"ok", "Imatge actualitzada correctament"};
                }
            }catch (Exception e){ // en cas de que es produeixi un error que no hem previst
                taskLogs.logError("TaskService", "addImage", "Error de carpetes i files", e);
                return new String[] {"e", e.getMessage()};
            }
        }
    }

    public int updateTask(Long taskId, Task task) {
        taskLogs.logInfo("UserService", "updateUser", "Accedint a updateUser amb id: " + userId);
        
        try{
            int result = repo.updateTaskById(taskId, task);
            if(result > 0){
                taskLogs.logInfo("UserService", "updateUser", 
                    "Usuari amb id " + taskId + " actualitzat correctament");
            }else{
                taskLogs.logError("UserService", "updateUser", 
                    "Usuari amb id " + taskId + " no trobat per actualitzar", null);
            }
            return result;
        }catch(Exception e){
            taskLogs.logError("UserService", "updateUser", "Error actualitzant usuari", e);
            return 0;
        }
    }

    // funció per comprobar si la taska amb el id existeix a la base de dades.
    public Task idExisteix(Long id){
        Task task = repo.findTaskById(id) != null ? repo.findTaskById(id) : null;
        return task;
    }

}
