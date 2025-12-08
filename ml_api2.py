from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field, validator
from typing import Literal
import pandas as pd
import joblib
import numpy as np

app = FastAPI(
    title="API Prédiction Tarif Taxi Yaoundé",
    description="Prédiction du prix de course en FCFA - Version finale propre",
    version="2.0.0"
)

# Chargement du modèle + préprocesseur (si tu as sauvegardé un Pipeline)
try:
    # model = joblib.load("Gradient Boosting_yaounde.pkl")
    model = joblib.load("Random Forest_yaounde.pkl")
    print("Modèle chargé avec succès")
except Exception as e:
    raise RuntimeError(f"Erreur chargement modèle : {e}")

# Mapping pour conversion automatique 0/1 → oui/non et chiffres → jours
YES_NO_MAP = {"oui": "oui", "non": "non", "0": "non", "1": "oui", 0: "non", 1: "oui"}
JOURS = ["lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche"]

class Features(BaseModel):
    pluie: str = Field(..., description="oui/non ou 0/1")
    etat_route: Literal["bonne", "moyenne", "mauvaise"]
    heure: str = Field(..., description="ex: 14:30 ou 14")
    jour_semaine: str = Field(..., description="lundi à dimanche ou 0-6")
    jour_ferie: str = Field(..., description="oui/non ou 0/1")
    bagages: str = Field(..., description="oui/non ou 0/1")
    routes_larges: str = Field(..., description="oui/non ou 0/1")
    routes_travaux: str = Field(..., description="oui/non ou 0/1")
    accident: str = Field(..., description="oui/non ou 0/1")
    depart_osm: str
    destination_osm: str
    distance_km: float = Field(..., gt=0)

    @validator("pluie", "jour_ferie", "bagages", "routes_larges", "routes_travaux", "accident", pre=True)
    def normalize_yes_no(cls, v):
        return YES_NO_MAP.get(str(v).lower(), "non")

    @validator("jour_semaine", pre=True)
    def normalize_day(cls, v):
        try:
            num = int(v)
            return JOURS[num] if 0 <= num <= 6 else v.lower()
        except:
            return v.lower()

    @validator("heure", pre=True)
    def format_hour(cls, v):
        v = str(v).strip()
        return v if ":" in v else f"{v.zfill(2)}:00"

    class Config:
        json_schema_extra = {
            "example": {
                "pluie": "0", "etat_route": "bonne", "heure": "14", "jour_semaine": "3",
                "jour_ferie": "0", "bagages": "non", "routes_larges": "oui", "routes_travaux": "non",
                "accident": "0", "depart_osm": "Mvan", "destination_osm": "Ngoa-Ekelle", "distance_km": 7.3
            }
        }

@app.get("/")
def root():
    return {"message": "API Yaoundé v2 opérationnelle", "docs": "/docs"}

@app.post("/predict")
def predict(data: Features):
    try:
        df = pd.DataFrame([data.dict()])
        pred = model.predict(df)[0]
        prix = round(float(pred), 0)
        return {
            "prix_estime_fcfa": int(prix),
            "prix_estime_range": f"{int(prix*0.9)} - {int(prix*1.15)} FCFA",
            "message": "Prédiction réussie",
            "lieux_connus": "⚠️ Certains lieux peuvent être inconnus → prix approximatif" if "unknown" in str(df) else "Tout connu"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
def health(): return {"status": "OK", "model": "loaded"}