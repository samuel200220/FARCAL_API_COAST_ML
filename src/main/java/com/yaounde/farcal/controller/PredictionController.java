// PredictionController.java
package com.yaounde.farcal.controller;

import com.yaounde.farcal.dto.FeaturesRequest;
import com.yaounde.farcal.dto.PredictionResponse;
import com.yaounde.farcal.service.TaxiPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/")
@Tag(name = "Prediction API", description = "API de prédiction des tarifs de taxi à Yaoundé")
@ApiResponses(value = {
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
})
public class PredictionController {

    @Autowired
    private TaxiPredictionService predictionService;

    @Operation(
            summary = "Page d'accueil de l'API",
            description = "Endpoint racine qui affiche un message de bienvenue et redirige vers la documentation"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Message de bienvenue",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Map.class),
                    examples = @ExampleObject(
                            value = "{\"message\": \"API Yaoundé v2 opérationnelle\", \"docs\": \"/swagger-ui.html\"}"
                    )
            )
    )
    @GetMapping
    public Map<String, String> root() {
        return Map.of(
                "message", "API Yaoundé v2 opérationnelle",
                "docs", "/swagger-ui.html"
        );
    }

    @Operation(
            summary = "Prédire le prix d'un trajet",
            description = "Calcule le prix estimé d'un trajet en taxi à Yaoundé basé sur les caractéristiques fournies"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Prédiction réussie",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PredictionResponse.class),
                            examples = @ExampleObject(
                                    name = "Prédiction réussie",
                                    value = """
                    {
                      "prix": 750,
                      "range": "675 - 862 FCFA",
                      "message": "Prédiction réussie",
                      "lieuxConnus": "Tout connu"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requête invalide - validation échouée",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Validation error",
                                    value = """
                    {
                      "timestamp": "2024-01-15T10:30:00.000Z",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Validation failed",
                      "path": "/predict"
                    }
                    """
                            )
                    )
            )
    })
    @PostMapping(
            value = "/predict",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PredictionResponse> predict(
            @Parameter(
                    description = "Caractéristiques du trajet pour la prédiction",
                    required = true,
                    schema = @Schema(implementation = FeaturesRequest.class)
            )
            @Valid @RequestBody FeaturesRequest features) {
        try {
            float prediction = predictionService.predict(features);
            int prix = Math.round(prediction);

            String lieuxConnus = features.getDepartOsm().contains("unknown") ||
                    features.getDestinationOsm().contains("unknown")
                    ? "⚠️ Certains lieux peuvent être inconnus → prix approximatif"
                    : "Tout connu";

            PredictionResponse response = new PredictionResponse(
                    prix,
                    String.format("%d - %d FCFA", (int)(prix * 0.9), (int)(prix * 1.15)),
                    "Prédiction réussie",
                    lieuxConnus
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "Vérifier l'état de l'API",
            description = "Endpoint de santé pour vérifier si l'API et le modèle sont opérationnels"
    )
    @ApiResponse(
            responseCode = "200",
            description = "API et modèle opérationnels",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Map.class),
                    examples = @ExampleObject(
                            value = "{\"status\": \"OK\", \"model\": \"loaded\"}"
                    )
            )
    )
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "OK",
                "model", "loaded"
        );
    }
}