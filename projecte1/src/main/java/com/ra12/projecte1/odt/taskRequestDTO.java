package com.ra12.projecte1.odt;

import java.sql.Timestamp;

import com.ra12.projecte1.model.Task;

public class taskRequestDTO {
    private String nomTaska; 
    private int sparks;
    private Timestamp dataLimit;
    private String urlImage;

    public Task toTask(){
        return new Task(nomTaska, sparks, dataLimit, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), urlImage);
    }

}

