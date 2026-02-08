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

    public taskResponseDTO readById(long id){

        String msg;
        try {
            Task task = repo.readById(id);
            msg = log.info("TaskService", "readById", "Consultant la tasca");
            log.writeToFile(msg);
            return task.toTaskResponseDTO();

        } catch (Exception e) {
            msg = log.error("TaskService", "readById", "No s'ha pogut consultar la tasca");
            log.writeToFile(msg);
            return new taskResponseDTO();
        }
    }

    public List<taskResponseDTO> readAll(){
        String msg = log.info("TaskService", "readAll", "Consultant tots el usuaris");
        log.writeToFile(msg);
        List<taskResponseDTO> tasksResponse = new ArrayList<>();

        try {
            List<Task> tasks = repo.readAll();
            for (Task task: tasks){
                tasksResponse.add(task.toTaskResponseDTO());
            }
            return tasksResponse;
        } catch (Exception e){
            return tasksResponse;
        }
        
    }

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

    public int createTasks(MultipartFile csv) throws IOException{

        String msg = log.info("TaskService", "createTasks", "Carregant la informació del fitxer " + csv.getName());
        log.writeToFile(msg);

        int comptador = 0;
        int erronis = 0;

        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try(BufferedReader br = new BufferedReader(new InputStreamReader(csv.getInputStream()))){
            String linia;
            int nLinia = 1;
            while((linia = br.readLine())!= null){
                String[] c = linia.split(",");
                try{
                    repo.createTask(new Task(c[0],Integer.parseInt(c[1]),Timestamp.valueOf(c[2]), now, now));
                    comptador++;
                } catch(Exception e){
                    msg = log.error("TaskService", "createTasks",
                        String.format("Error en la línia %d del fitxer. Missatge d'error: %s",nLinia,e));
                    log.writeToFile(msg);
                    erronis++;
                }
                nLinia++;
            }

        } catch (IOException e){
            System.err.println("Error d'accès al fitxer: " + e.getMessage());
           msg = log.error("TasksService", "createTask", "Error de lectura de l'arxiu");
           log.writeToFile(msg);
           return -1;
        }
        String dir = "src/main/resources/private/csv_processed";
        Path directory = Paths.get(dir);
        Path filePath = Paths.get(dir + "/" + csv.getOriginalFilename());
        
        // Guardem el csv
        try{
            Files.createDirectories(directory);
            Files.copy(csv.getInputStream(),filePath);
            
        }
        catch (Exception e){
            System.err.println("No s'ha pogut guardar el csv");
            msg = log.error("TaskService", "createTasks", "No s'ha pogut guardar l'arxiu");
            log.writeToFile(msg);
        }
        msg = log.info("TaskService", "createTasks",
            String.format("S'han guardat correctament %d registres i han donat error %d registres",comptador, erronis));
        log.writeToFile(msg);
        // Retornem registres creats
        return comptador;

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

    //elimina totes les taskes
    public int deleteAll(){
        log.info("TaskService", "deleteAll", "Accedint a deleteAll");
        try{
            //funcio del repositori per eliminar tot de la BBDD
            int result = repo.deleteAll();
            if(result > 0){ // si el resultat és major que 0, significa que s'ha eliminat alguna taska, per tant guardem un log informatiu
                log.info("TaskService", "deleteAll", 
                    "Totes les taskes eliminades correctament");
            }else{
                log.error("TaskService", "deleteAll", 
                    "No s'han trobat taskes per eliminar");
            }
            return result;
        }catch(Exception e){
            log.error("TaskService", "deleteAll", "Error eliminant les taskes");
            return 0;
        }
    }

    // funció per eliminar una taska a partir del id
    public int deleteById(Long id){
        // log per indicar que s'ha accedit a la funció deleteById amb un id concret
        log.info("TaskService", "deleteById", "Accedint a deleteById amb id: " + id);
        try{
            // cridem a la funció del repositori que elimina la taska i guardem el resultat (nombre de registres eliminats)
            int result = repo.deleteById(id);
            if(result > 0){ // si el resultat és major que 0, significa que s'ha eliminat una taska, per tant guardem un log informatiu
                log.info("TaskService", "deleteById", 
                    "Task amb id " + id + " eliminada correctament");
            }else{
                log.error("TaskService", "deleteById", 
                    "Task amb id " + id + " no trobada per eliminar");
            }
            return result;
        }catch(Exception e){
            log.error("TaskService", "deleteById", "Error eliminant taska");
            return 0;
        }
    }

}
