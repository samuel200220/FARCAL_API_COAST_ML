// TaxiPredictionService.java
package com.yaounde.taxi.service;

import ai.onnxruntime.*;
import com.yaounde.taxi.dto.FeaturesRequest;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.HashMap;
import java.util.Map;

@Service
public class TaxiPredictionService {
    
    private OrtEnvironment env;
    private OrtSession session;
    
    // Mappings pour normalisation
    private static final Map<String, String> YES_NO_MAP = Map.of(
        "oui", "oui", "non", "non",
        "0", "non", "1", "oui"
    );
    
    private static final String[] JOURS = {
        "lundi", "mardi", "mercredi", "jeudi", 
        "vendredi", "samedi", "dimanche"
    };
    
    @PostConstruct
    public void init() throws OrtException {
        env = OrtEnvironment.getEnvironment();
        
        String modelPath = getClass().getClassLoader()
            .getResource("models/Random Forest_yaounde_target_encoder.onnx")
            .getPath();
        
        session = env.createSession(modelPath, new OrtSession.SessionOptions());
        
        System.out.println("✅ Modèle chargé avec succès");
        System.out.println("Inputs: " + session.getInputNames());
        System.out.println("Outputs: " + session.getOutputNames());
    }
    
    public float predict(FeaturesRequest features) throws OrtException {
        // Normaliser les données
        FeaturesRequest normalized = normalizeFeatures(features);
        
        // Préparer les tensors d'entrée
        Map<String, OnnxTensor> inputs = prepareInputs(normalized);
        
        try {
            // Faire la prédiction
            OrtSession.Result result = session.run(inputs);
            
            // Extraire le résultat
            float[][] output = (float[][]) result.get(0).getValue();
            return output[0][0];
            
        } finally {
            // Nettoyer les tensors
            for (OnnxTensor tensor : inputs.values()) {
                tensor.close();
            }
        }
    }
    
    private FeaturesRequest normalizeFeatures(FeaturesRequest features) {
        FeaturesRequest normalized = new FeaturesRequest();
        
        // Normaliser oui/non
        normalized.setPluie(normalizeYesNo(features.getPluie()));
        normalized.setJourFerie(normalizeYesNo(features.getJourFerie()));
        normalized.setBagages(normalizeYesNo(features.getBagages()));
        normalized.setRoutesLarges(normalizeYesNo(features.getRoutesLarges()));
        normalized.setRoutesTravaux(normalizeYesNo(features.getRoutesTravaux()));
        normalized.setAccident(normalizeYesNo(features.getAccident()));
        
        // Normaliser jour de la semaine
        normalized.setJourSemaine(normalizeDay(features.getJourSemaine()));
        
        // Normaliser heure
        normalized.setHeure(formatHour(features.getHeure()));
        
        // Copier les autres champs
        normalized.setEtatRoute(features.getEtatRoute().toLowerCase());
        normalized.setDepartOsm(features.getDepartOsm());
        normalized.setDestinationOsm(features.getDestinationOsm());
        normalized.setDistanceKm(features.getDistanceKm());
        
        return normalized;
    }
    
    private String normalizeYesNo(String value) {
        return YES_NO_MAP.getOrDefault(value.toLowerCase(), "non");
    }
    
    private String normalizeDay(String value) {
        try {
            int num = Integer.parseInt(value);
            return (num >= 0 && num <= 6) ? JOURS[num] : value.toLowerCase();
        } catch (NumberFormatException e) {
            return value.toLowerCase();
        }
    }
    
    private String formatHour(String value) {
        value = value.trim();
        return value.contains(":") ? value : String.format("%02d:00", 
            Integer.parseInt(value));
    }
    
    private Map<String, OnnxTensor> prepareInputs(FeaturesRequest features) 
            throws OrtException {
        Map<String, OnnxTensor> inputs = new HashMap<>();
        
        // Créer les tensors pour chaque feature
        // Note: adapter selon la structure exacte attendue par votre modèle ONNX
        
        inputs.put("pluie", createStringTensor(features.getPluie()));
        inputs.put("etat_route", createStringTensor(features.getEtatRoute()));
        inputs.put("heure", createStringTensor(features.getHeure()));
        inputs.put("jour_semaine", createStringTensor(features.getJourSemaine()));
        inputs.put("jour_ferie", createStringTensor(features.getJourFerie()));
        inputs.put("bagages", createStringTensor(features.getBagages()));
        inputs.put("routes_larges", createStringTensor(features.getRoutesLarges()));
        inputs.put("routes_travaux", createStringTensor(features.getRoutesTravaux()));
        inputs.put("accident", createStringTensor(features.getAccident()));
        inputs.put("depart_osm", createStringTensor(features.getDepartOsm()));
        inputs.put("destination_osm", createStringTensor(features.getDestinationOsm()));
        inputs.put("distance_km", createFloatTensor(features.getDistanceKm()));
        
        return inputs;
    }
    
    private OnnxTensor createStringTensor(String value) throws OrtException {
        String[][] data = {{value}};
        return OnnxTensor.createTensor(env, data);
    }
    
    private OnnxTensor createFloatTensor(float value) throws OrtException {
        float[][] data = {{value}};
        return OnnxTensor.createTensor(env, data);
    }
    
    @PreDestroy
    public void cleanup() throws OrtException {
        if (session != null) session.close();
        if (env != null) env.close();
        System.out.println("🔴 Modèle déchargé");
    }
}
