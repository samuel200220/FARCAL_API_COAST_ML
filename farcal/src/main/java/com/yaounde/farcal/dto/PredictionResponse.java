// PredictionResponse.java
package com.yaounde.taxi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PredictionResponse {
    private int prixEstimeFcfa;
    private String prixEstimeRange;
    private String message;
    private String lieuxConnus;
}
