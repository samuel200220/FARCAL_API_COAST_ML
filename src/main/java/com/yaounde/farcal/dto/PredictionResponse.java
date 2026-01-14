// PredictionResponse.java
package com.yaounde.farcal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(name = "PredictionResponse", description = "Réponse de prédiction du prix du trajet")
public class PredictionResponse {

    @Schema(
            description = "Prix estimé en Francs CFA",
            example = "750"
    )
    private int prixEstimeFcfa;

    @Schema(
            description = "Plage de prix estimée (intervalle de confiance)",
            example = "675 - 862 FCFA"
    )
    private String prixEstimeRange;

    @Schema(
            description = "Message de statut de la prédiction",
            example = "Prédiction réussie",
            allowableValues = {
                    "Prédiction réussie",
                    "⚠️ Certains lieux peuvent être inconnus → prix approximatif",
                    "Erreur lors de la prédiction"
            }
    )
    private String message;

    @Schema(
            description = "État de la connaissance des lieux",
            example = "Tout connu",
            allowableValues = {
                    "Tout connu",
                    "⚠️ Certains lieux peuvent être inconnus → prix approximatif",
                    "Lieux inconnus - vérifiez les identifiants OSM"
            }
    )
    private String lieuxConnus;
}