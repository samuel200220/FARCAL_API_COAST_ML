// PredictionController.java
package com.yaounde.taxi.controller;

import com.yaounde.taxi.dto.FeaturesRequest;
import com.yaounde.taxi.dto.PredictionResponse;
import com.yaounde.taxi.service.TaxiPredictionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/")
public class PredictionController {
    
    @Autowired
    private TaxiPredictionService predictionService;
    
    @GetMapping
    public Map<String, String> root() {
        return Map.of(
            "message", "API Yaoundé v2 opérationnelle",
            "docs", "/swagger-ui.html"
        );
    }
    
    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> predict(
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
    
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "OK",
            "model", "loaded"
        );
    }
}
