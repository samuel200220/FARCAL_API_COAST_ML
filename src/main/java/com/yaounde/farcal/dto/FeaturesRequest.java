// FeaturesRequest.java
package com.yaounde.farcal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(name = "FeaturesRequest", description = "Requête contenant les caractéristiques du trajet pour la prédiction du prix")
public class FeaturesRequest {

    @Schema(
            description = "Présence de pluie",
            example = "Non",
            allowableValues = {"Oui", "Non", "Pluie légère", "Pluie forte"}
    )
    @NotBlank(message = "Le champ 'pluie' est requis")
    private String pluie;

    @Schema(
            description = "État de la route",
            example = "Bonne",
            allowableValues = {"Bonne", "Moyenne", "Mauvaise", "Très mauvaise"}
    )
    @NotBlank(message = "Le champ 'etat_route' est requis")
    private String etatRoute;

    @Schema(
            description = "Heure du trajet (format 24h)",
            example = "14:30",
            pattern = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$"
    )
    @NotBlank(message = "Le champ 'heure' est requis")
    private String heure;

    @Schema(
            description = "Jour de la semaine",
            example = "Lundi",
            allowableValues = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"}
    )
    @NotBlank(message = "Le champ 'jour_semaine' est requis")
    private String jourSemaine;

    @Schema(
            description = "Jour férié ou non",
            example = "Non",
            allowableValues = {"Oui", "Non"}
    )
    @NotBlank(message = "Le champ 'jour_ferie' est requis")
    private String jourFerie;

    @Schema(
            description = "Présence de bagages",
            example = "Oui",
            allowableValues = {"Oui", "Non", "Beaucoup"}
    )
    @NotBlank(message = "Le champ 'bagages' est requis")
    private String bagages;

    @Schema(
            description = "Routes larges sur le trajet",
            example = "Non",
            allowableValues = {"Oui", "Non", "Partiellement"}
    )
    @NotBlank(message = "Le champ 'routes_larges' est requis")
    private String routesLarges;

    @Schema(
            description = "Routes en travaux sur le trajet",
            example = "Non",
            allowableValues = {"Oui", "Non", "Peu"}
    )
    @NotBlank(message = "Le champ 'routes_travaux' est requis")
    private String routesTravaux;

    @Schema(
            description = "Présence d'accident sur le trajet",
            example = "Non",
            allowableValues = {"Oui", "Non", "Risque élevé"}
    )
    @NotBlank(message = "Le champ 'accident' est requis")
    private String accident;

    @Schema(
            description = "ID OSM du point de départ",
            example = "way:123456789",
            minLength = 1
    )
    @NotBlank(message = "Le champ 'depart_osm' est requis")
    private String departOsm;

    @Schema(
            description = "ID OSM de la destination",
            example = "node:987654321",
            minLength = 1
    )
    @NotBlank(message = "Le champ 'destination_osm' est requis")
    private String destinationOsm;

    @Schema(
            description = "Distance du trajet en kilomètres",
            example = "5.2",
            minimum = "0.1",
            exclusiveMinimum = true
    )
    @NotNull(message = "Le champ 'distance_km' est requis")
    @Positive(message = "La distance doit être positive")
    private Float distanceKm;
}