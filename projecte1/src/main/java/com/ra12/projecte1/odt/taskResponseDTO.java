package com.ra12.projecte1.odt;

import java.sql.Timestamp;

public class taskResponseDTO {
    private Long id; 
    private String nomTaska; 
    private int sparks;
    private Timestamp dataLimit;
    private String urlImage;

    @Override
    public String toString() {
        return "Tasca [id=" + id + ", nomTaska=" + nomTaska + ", sparks=" + sparks + ", urlImage=" + urlImage
                + "]";
    }

    
}
