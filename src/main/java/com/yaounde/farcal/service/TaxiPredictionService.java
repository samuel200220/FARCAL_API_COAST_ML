package com.yaounde.farcal.service;

import ai.onnxruntime.*;
import com.yaounde.farcal.dto.FeaturesRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Service
public class TaxiPredictionService {

    private OrtEnvironment env;
    private OrtSession session;

    private static final Map<String, String> YES_NO_MAP = Map.of(
            "oui", "oui",
            "non", "non",
            "0", "non",
            "1", "oui"
    );

    private static final String[] JOURS = {
            "lundi", "mardi", "mercredi", "jeudi",
            "vendredi", "samedi", "dimanche"
    };

    @PostConstruct
    public void init() {
        try {
            env = OrtEnvironment.getEnvironment();

            // 🔹 Charger le modèle depuis le classpath
            InputStream modelStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream(
                            "models/RandomForest_yaounde_target_encoder.onnx"
                    );

            if (modelStream == null) {
                throw new RuntimeException("❌ Modèle ONNX introuvable dans resources/models/");
            }

            // 🔹 Copier vers un vrai fichier temporaire
            Path tempModel = Files.createTempFile("rf-model-", ".onnx");
            Files.copy(modelStream, tempModel, StandardCopyOption.REPLACE_EXISTING);

            // 🔹 Charger ONNX depuis un vrai fichier
            session = env.createSession(
                    tempModel.toAbsolutePath().toString(),
                    new OrtSession.SessionOptions()
            );

            System.out.println("✅ Modèle ONNX chargé avec succès");
            System.out.println("Inputs: " + session.getInputNames());
            System.out.println("Outputs: " + session.getOutputNames());

        } catch (Exception e) {
            throw new RuntimeException("❌ Erreur chargement modèle ONNX", e);
        }
    }

    public float predict(FeaturesRequest features) throws OrtException {

        FeaturesRequest normalized = normalizeFeatures(features);
        Map<String, OnnxTensor> inputs = prepareInputs(normalized);

        try (OrtSession.Result result = session.run(inputs)) {

            float[][] output = (float[][]) result.get(0).getValue();
            return output[0][0];

        } finally {
            for (OnnxTensor tensor : inputs.values()) {
                tensor.close();
            }
        }
    }

    private FeaturesRequest normalizeFeatures(FeaturesRequest features) {
        FeaturesRequest n = new FeaturesRequest();

        n.setPluie(normalizeYesNo(features.getPluie()));
        n.setJourFerie(normalizeYesNo(features.getJourFerie()));
        n.setBagages(normalizeYesNo(features.getBagages()));
        n.setRoutesLarges(normalizeYesNo(features.getRoutesLarges()));
        n.setRoutesTravaux(normalizeYesNo(features.getRoutesTravaux()));
        n.setAccident(normalizeYesNo(features.getAccident()));

        n.setJourSemaine(normalizeDay(features.getJourSemaine()));
        n.setHeure(formatHour(features.getHeure()));

        n.setEtatRoute(features.getEtatRoute().toLowerCase());
        n.setDepartOsm(features.getDepartOsm());
        n.setDestinationOsm(features.getDestinationOsm());
        n.setDistanceKm(features.getDistanceKm());

        return n;
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
        return value.contains(":")
                ? value
                : String.format("%02d:00", Integer.parseInt(value));
    }

    private Map<String, OnnxTensor> prepareInputs(FeaturesRequest f)
            throws OrtException {

        Map<String, OnnxTensor> inputs = new HashMap<>();

        inputs.put("pluie", createStringTensor(f.getPluie()));
        inputs.put("etat_route", createStringTensor(f.getEtatRoute()));
        inputs.put("heure", createStringTensor(f.getHeure()));
        inputs.put("jour_semaine", createStringTensor(f.getJourSemaine()));
        inputs.put("jour_ferie", createStringTensor(f.getJourFerie()));
        inputs.put("bagages", createStringTensor(f.getBagages()));
        inputs.put("routes_larges", createStringTensor(f.getRoutesLarges()));
        inputs.put("routes_travaux", createStringTensor(f.getRoutesTravaux()));
        inputs.put("accident", createStringTensor(f.getAccident()));
        inputs.put("depart_osm", createStringTensor(f.getDepartOsm()));
        inputs.put("destination_osm", createStringTensor(f.getDestinationOsm()));
        inputs.put("distance_km", createFloatTensor(f.getDistanceKm()));

        return inputs;
    }

    private OnnxTensor createStringTensor(String value) throws OrtException {
        return OnnxTensor.createTensor(env, new String[][]{{value}});
    }

    private OnnxTensor createFloatTensor(float value) throws OrtException {
        return OnnxTensor.createTensor(env, new float[][]{{value}});
    }

    @PreDestroy
    public void cleanup() throws OrtException {
        if (session != null) session.close();
        if (env != null) env.close();
        System.out.println("🔴 Modèle ONNX déchargé");
    }
}
