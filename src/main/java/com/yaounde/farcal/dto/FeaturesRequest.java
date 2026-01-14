// FeaturesRequest.java
package com.yaounde.taxi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FeaturesRequest {
    
    @NotBlank(message = "Le champ 'pluie' est requis")
    private String pluie;
    
    @NotBlank(message = "Le champ 'etat_route' est requis")
    private String etatRoute;
    
    @NotBlank(message = "Le champ 'heure' est requis")
    private String heure;
    
    @NotBlank(message = "Le champ 'jour_semaine' est requis")
    private String jourSemaine;
    
    @NotBlank(message = "Le champ 'jour_ferie' est requis")
    private String jourFerie;
    
    @NotBlank(message = "Le champ 'bagages' est requis")
    private String bagages;
    
    @NotBlank(message = "Le champ 'routes_larges' est requis")
    private String routesLarges;
    
    @NotBlank(message = "Le champ 'routes_travaux' est requis")
    private String routesTravaux;
    
    @NotBlank(message = "Le champ 'accident' est requis")
    private String accident;
    
    @NotBlank(message = "Le champ 'depart_osm' est requis")
    private String departOsm;
    
    @NotBlank(message = "Le champ 'destination_osm' est requis")
    private String destinationOsm;
    
    @NotNull(message = "Le champ 'distance_km' est requis")
    @Positive(message = "La distance doit être positive")
    private Float distanceKm;
}
