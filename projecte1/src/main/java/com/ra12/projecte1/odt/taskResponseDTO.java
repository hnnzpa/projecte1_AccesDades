package com.ra12.projecte1.odt;

import java.sql.Timestamp;

public class taskResponseDTO {
    private Long id; 
    private String nomTaska; 
    private int sparks;
    private Timestamp dataLimit;
    private String urlImage;

    public taskResponseDTO(Long id, String nomTaska, int sparks, Timestamp dataLimit){
        this.id = id;
        this.nomTaska = nomTaska;
        this.sparks = sparks;
        this.dataLimit = dataLimit;
    }
    @Override
    public String toString() {
        return "Tasca [id=" + id + ", nomTaska=" + nomTaska + ", sparks=" + sparks + ", urlImage=" + urlImage
                + "]";
    }

    
}
