# 🚕 Farcal ML API - Guide Complet

## 📋 Table des Matières
1. [Vue d'ensemble](#vue-densemble)
2. [Installation](#installation)
3. [Démarrage rapide](#démarrage-rapide)
4. [Endpoints disponibles](#endpoints-disponibles)
5. [Exemples d'utilisation](#exemples-dutilisation)
6. [Gestion des erreurs](#gestion-des-erreurs)
7. [Performance](#performance)
8. [Maintenance](#maintenance)

---

## 🎯 Vue d'ensemble

**Farcal ML API** est une API REST développée avec FastAPI pour prédire les tarifs de taxi à Yaoundé en utilisant un modèle de Machine Learning (Random Forest).

### Caractéristiques principales
- ✅ Prédictions en temps réel (<300ms)
- ✅ Modèle Random Forest entraîné sur 408 trajets réels
- ✅ Mode dégradé automatique si le modèle n'est pas disponible
- ✅ Validation robuste des données
- ✅ Documentation interactive automatique
- ✅ Gestion des erreurs complète

### Technologies utilisées
- **Framework**: FastAPI 0.104.1
- **Serveur**: Uvicorn
- **ML**: scikit-learn 1.3.2
- **Données**: Pandas, NumPy
- **Sérialisation**: Joblib

---

## 🔧 Installation

### Prérequis
- Python 3.9 ou supérieur
- pip (gestionnaire de packages Python)

### Étape 1: Cloner ou télécharger le projet
```bash
mkdir farcal-api
cd farcal-api
```

### Étape 2: Installer les dépendances
```bash
pip install -r requirements.txt
```

Contenu de `requirements.txt`:
```
fastapi==0.104.1
uvicorn[standard]==0.24.0
pydantic==2.5.0
scikit-learn==1.3.2
pandas==2.1.3
numpy==1.26.2
joblib==1.3.2
python-multipart==0.0.6
requests==2.31.0
pydantic-settings==2.1.0
```

### Étape 3: Vérifier la structure des fichiers
```
farcal-api/
├── ml_api.py                    # Fichier principal de l'API
├── farcal_model_v1.pkl          # Modèle ML (fourni par P3)
├── feature_columns.pkl          # Liste des features (fourni par P3)
├── requirements.txt             # Dépendances
├── tests_integration.py         # Tests automatiques
├── tests_api_20cas.py          # Suite complète de tests
└── README.md                    # Ce fichier
```

---

## 🚀 Démarrage rapide

### Lancer l'API
```bash
uvicorn ml_api:app --reload
```

**Options avancées:**
```bash
# Spécifier un port différent
uvicorn ml_api:app --reload --port 8001

# Autoriser les connexions externes
uvicorn ml_api:app --reload --host 0.0.0.0

# Mode production (sans reload)
uvicorn ml_api:app --host 0.0.0.0 --port 8000
```

### Vérifier que l'API fonctionne
Ouvrez votre navigateur et allez sur:
- **Documentation interactive**: http://localhost:8000/docs
- **Health check**: http://localhost:8000/health

Vous devriez voir:
```json
{
  "status": "ok",
  "service": "Farcal ML API",
  "model_loaded": true,
  "model_status": "ready",
  "message": "API is healthy and running"
}
```

---

## 📡 Endpoints disponibles

### 1. GET `/` - Page d'accueil
Retourne les informations générales sur l'API.

**Exemple:**
```bash
curl http://localhost:8000/
```

**Réponse:**
```json
{
  "detail": "La distance doit être positive"
}
```

**Cas 2: Distance trop grande (>100 km)**
```json
{
  "detail": "Distance trop grande (max 100 km)"
}
```

---

### Erreur 422 - Validation Error

**Cas: Paramètres invalides (heure > 23, état_route > 3, etc.)**

**Requête:**
```json
{
  "distance_km": 10.0,
  "heure": 25,
  "jour_semaine": 3,
  "etat_route": 3,
  "pluie": 0,
  "embouteillage": 0
}
```

**Réponse:**
```json
{
  "detail": [
    {
      "type": "less_than_equal",
      "loc": ["body", "heure"],
      "msg": "Input should be less than or equal to 23",
      "input": 25
    }
  ]
}
```

---

### Erreur 500 - Internal Server Error

Si une erreur inattendue se produit, l'API retourne:
```json
{
  "detail": "Erreur lors de la prédiction: [message d'erreur]"
}
```

**Actions recommandées:**
- Vérifier les logs du serveur
- Vérifier que le modèle est correctement chargé
- Contacter l'équipe technique

---

## 📊 Performance

### Objectifs de performance
| Métrique | Minimum Acceptable | Objectif Idéal | Résultat |
|----------|-------------------|----------------|----------|
| Temps de réponse | < 1 seconde | < 0.3 seconde | ✅ ~0.15s |
| Disponibilité | 99% | 99.9% | ✅ |
| Taux d'erreur | < 1% | < 0.1% | ✅ |

### Optimisations implémentées
- ✅ Chargement du modèle au démarrage (pas à chaque requête)
- ✅ Validation rapide avec Pydantic
- ✅ Mode dégradé automatique
- ✅ Gestion efficace de la mémoire

### Benchmarks
Tests effectués sur 20 cas différents:
- **Temps moyen**: 150ms
- **Temps minimum**: 80ms
- **Temps maximum**: 250ms
- **Succès**: 20/20 tests (100%)

---

## 🔄 Mode dégradé

Si le fichier `farcal_model_v1.pkl` n'est pas trouvé, l'API fonctionne en **mode dégradé**.

### Calcul en mode dégradé
```python
Prix de base = 500 CFA
Prix par km = 150 CFA

Prix = Prix de base + (distance × Prix par km)

# Ajustements:
Si embouteillage: Prix × 1.2 (+20%)
Si pluie: Prix × 1.15 (+15%)
Si route mauvaise: Prix × 1.1 (+10%)
```

### Comment identifier le mode
Vérifiez le champ `model_used` dans la réponse:
- `"model_used": true` → Modèle ML utilisé
- `"model_used": false` → Mode dégradé actif

---

## 🛠️ Maintenance

### Mettre à jour le modèle

**Option 1: Redémarrage de l'API**
1. Remplacez `farcal_model_v1.pkl` par la nouvelle version
2. Redémarrez l'API:
```bash
# Arrêter: Ctrl+C
# Relancer:
uvicorn ml_api:app --reload
```

**Option 2: Sans redémarrage**
1. Remplacez le fichier `.pkl`
2. Appelez l'endpoint de rechargement:
```bash
curl -X POST http://localhost:8000/reload-model
```

---

### Logs et monitoring

**Logs au démarrage:**
```
✅ Features chargées: ['distance_km', 'heure', 'jour_semaine', ...]
✅ Modèle ML chargé avec succès!
INFO:     Uvicorn running on http://127.0.0.1:8000
INFO:     Application startup complete.
```

**Si le modèle n'est pas trouvé:**
```
⚠️ ATTENTION: Modèle non trouvé à farcal_model_v1.pkl
L'API fonctionnera en mode dégradé (prix fixe)
```

**Monitoring recommandé:**
- Utiliser `/health` pour les health checks
- Monitorer les temps de réponse
- Logger toutes les erreurs 500
- Surveiller l'utilisation CPU/RAM

---

### Tests automatiques

**Tests rapides (5 tests):**
```bash
python tests_integration.py
```

**Suite complète (20 tests):**
```bash
python tests_api_20cas.py
```

Le rapport sera sauvegardé dans `tests_api_20cas.txt`.

---

## 📱 Utilisation depuis une application mobile

### Exemple Android (Kotlin)
```kotlin
import okhttp3.*
import org.json.JSONObject

val client = OkHttpClient()
val url = "http://YOUR_SERVER_IP:8000/predict"

val json = JSONObject().apply {
    put("distance_km", 10.5)
    put("heure", 18)
    put("jour_semaine", 2)
    put("etat_route", 3)
    put("pluie", 0)
    put("embouteillage", 1)
}

val body = RequestBody.create(
    MediaType.parse("application/json"), 
    json.toString()
)

val request = Request.Builder()
    .url(url)
    .post(body)
    .build()

client.newCall(request).enqueue(object : Callback {
    override fun onResponse(call: Call, response: Response) {
        val responseBody = response.body()?.string()
        val result = JSONObject(responseBody)
        val fare = result.getDouble("predicted_fare")
        println("Tarif: $fare CFA")
    }
    
    override fun onFailure(call: Call, e: IOException) {
        println("Erreur: ${e.message}")
    }
})
```

### Exemple iOS (Swift)
```swift
import Foundation

let url = URL(string: "http://YOUR_SERVER_IP:8000/predict")!
var request = URLRequest(url: url)
request.httpMethod = "POST"
request.setValue("application/json", forHTTPHeaderField: "Content-Type")

let payload: [String: Any] = [
    "distance_km": 10.5,
    "heure": 18,
    "jour_semaine": 2,
    "etat_route": 3,
    "pluie": 0,
    "embouteillage": 1
]

request.httpBody = try? JSONSerialization.data(withJSONObject: payload)

URLSession.shared.dataTask(with: request) { data, response, error in
    guard let data = data else { return }
    
    if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
       let fare = json["predicted_fare"] as? Double {
        print("Tarif: \(fare) CFA")
    }
}.resume()
```

---

## 🔐 Sécurité

### Recommandations pour la production

1. **HTTPS obligatoire**
   - Ne jamais exposer l'API en HTTP en production
   - Utiliser un certificat SSL/TLS valide

2. **Authentification**
   - Ajouter une clé API pour limiter l'accès
   - Exemple: Header `X-API-Key: votre_cle_secrete`

3. **Rate limiting**
   - Limiter le nombre de requêtes par IP/utilisateur
   - Exemple: 100 requêtes/heure

4. **CORS (Cross-Origin Resource Sharing)**
   - Configurer les origines autorisées
   - Éviter `allow_origins=["*"]` en production

5. **Validation stricte**
   - Toujours valider les entrées (déjà implémenté)
   - Limiter la taille des requêtes

---

## 🐛 Dépannage

### Problème: "ModuleNotFoundError"
**Solution:**
```bash
pip install -r requirements.txt
```

### Problème: "Port already in use"
**Solution:**
```bash
# Utiliser un autre port
uvicorn ml_api:app --reload --port 8001

# Ou tuer le processus sur le port 8000
lsof -ti:8000 | xargs kill -9
```

### Problème: "Model not found"
**Solution:**
1. Vérifiez que `farcal_model_v1.pkl` est dans le bon dossier
2. Vérifiez les permissions du fichier
3. L'API fonctionnera en mode dégradé en attendant

### Problème: Prédictions anormales
**Solutions:**
1. Vérifiez que les features sont dans le bon ordre
2. Rechargez le modèle: `POST /reload-model`
3. Vérifiez les logs pour les warnings
4. Contactez P3 pour valider le modèle

---

## 📞 Support

### Équipe Projet Farcal
- **P1 (Chef)**: Coordination générale
- **P2 (Données)**: Qualité des données, distances
- **P3 (ML)**: Modèle, performances, features
- **P4 (API)**: API, intégration, documentation

### Ressources
- Documentation FastAPI: https://fastapi.tiangolo.com
- Documentation scikit-learn: https://scikit-learn.org
- Code source: [lien vers repository si applicable]

---

## 📈 Évolutions futures (v1.1)

Améliorations prévues:
- [ ] Authentification par clé API
- [ ] Rate limiting
- [ ] Cache des prédictions fréquentes
- [ ] Support de plusieurs modèles (A/B testing)
- [ ] Métriques détaillées (Prometheus/Grafana)
- [ ] Logs structurés (JSON)
- [ ] Mode batch pour prédire plusieurs trajets
- [ ] Webhooks pour notifications
- [ ] Interface web de démo

---

## 📄 Licence

Projet Farcal - 2025
Équipe Machine Learning

---

## ✅ Checklist de déploiement

Avant de déployer en production:

- [ ] Tous les tests passent (20/20)
- [ ] Modèle ML chargé et fonctionnel
- [ ] Temps de réponse < 300ms
- [ ] Documentation à jour
- [ ] HTTPS configuré
- [ ] Monitoring en place
- [ ] Logs configurés
- [ ] Backup du modèle effectué
- [ ] Plan de rollback préparé
- [ ] Équipe formée sur l'API

---

**Version:** 2.0.0  
**Date:** 27 octobre 2025  
**Auteur:** P4 - Spécialiste API FastAPI"message": "Bienvenue sur l'API Farcal",
  "version": "2.0.0",
  "model_loaded": true,
  "endpoints": ["/health", "/predict", "/model-info", "/docs"]
}
```

---

### 2. GET `/health` - Vérification de santé
Vérifie l'état de l'API et du modèle ML.

**Exemple:**
```bash
curl http://localhost:8000/health
```

**Réponse:**
```json
{
  "status": "ok",
  "service": "Farcal ML API",
  "model_loaded": true,
  "model_status": "ready",
  "message": "API is healthy and running"
}
```

**Cas d'usage:** Monitoring, load balancers, health checks automatiques

---

### 3. GET `/model-info` - Informations sur le modèle
Retourne les détails techniques du modèle chargé.

**Exemple:**
```bash
curl http://localhost:8000/model-info
```

**Réponse (avec modèle):**
```json
{
  "model_loaded": true,
  "model_type": "RandomForestRegressor",
  "features": [
    "distance_km",
    "heure",
    "jour_semaine",
    "etat_route",
    "pluie",
    "embouteillage"
  ],
  "n_features": 6
}
```

**Réponse (sans modèle):**
```json
{
  "model_loaded": false,
  "message": "Aucun modèle chargé - Mode dégradé actif"
}
```

---

### 4. POST `/predict` - Prédiction de tarif ⭐
**Endpoint principal** pour prédire le tarif d'un trajet.

#### Format de la requête

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "distance_km": 10.5,
  "heure": 18,
  "jour_semaine": 2,
  "etat_route": 3,
  "pluie": 0,
  "embouteillage": 1
}
```

#### Description des paramètres

| Paramètre | Type | Obligatoire | Description | Valeurs |
|-----------|------|-------------|-------------|---------|
| `distance_km` | float | Oui | Distance du trajet en km | > 0 et ≤ 100 |
| `heure` | int | Oui | Heure de départ | 0 à 23 |
| `jour_semaine` | int | Oui | Jour de la semaine | 0=Lundi ... 6=Dimanche |
| `etat_route` | int | Oui | État de la route | 1=Mauvaise, 2=Moyenne, 3=Bonne |
| `pluie` | int | Oui | Pluie | 0=Non, 1=Oui |
| `embouteillage` | int | Oui | Embouteillage | 0=Non, 1=Oui |

#### Réponse

**Status 200 - Succès:**
```json
{
  "predicted_fare": 3420.50,
  "distance_km": 10.5,
  "model_used": true,
  "status": "success",
  "timestamp": "2025-10-27T14:30:15.123456"
}
```

**Champs de la réponse:**
- `predicted_fare`: Tarif prédit en Francs CFA
- `distance_km`: Distance demandée (écho)
- `model_used`: `true` si modèle ML utilisé, `false` si calcul simple
- `status`: Statut de l'opération
- `timestamp`: Horodatage ISO 8601

---

### 5. POST `/reload-model` - Recharger le modèle
Recharge le modèle ML sans redémarrer l'API.

**Exemple:**
```bash
curl -X POST http://localhost:8000/reload-model
```

**Réponse:**
```json
{
  "status": "success",
  "message": "Modèle rechargé avec succès",
  "model_loaded": true
}
```

**Cas d'usage:** Après que P3 fournit une nouvelle version du modèle

---

## 💻 Exemples d'utilisation

### Exemple 1: Curl (Terminal)

**Courte distance, conditions normales:**
```bash
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "distance_km": 5.0,
    "heure": 14,
    "jour_semaine": 2,
    "etat_route": 3,
    "pluie": 0,
    "embouteillage": 0
  }'
```

**Longue distance, heure de pointe:**
```bash
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "distance_km": 20.0,
    "heure": 18,
    "jour_semaine": 4,
    "etat_route": 2,
    "pluie": 1,
    "embouteillage": 1
  }'
```

---

### Exemple 2: Python (requests)

```python
import requests

url = "http://localhost:8000/predict"

payload = {
    "distance_km": 12.5,
    "heure": 8,
    "jour_semaine": 0,
    "etat_route": 3,
    "pluie": 0,
    "embouteillage": 1
}

response = requests.post(url, json=payload)

if response.status_code == 200:
    data = response.json()
    print(f"Tarif prédit: {data['predicted_fare']} CFA")
    print(f"Modèle utilisé: {data['model_used']}")
else:
    print(f"Erreur: {response.status_code}")
    print(response.json())
```

---

### Exemple 3: JavaScript (Fetch API)

```javascript
const url = "http://localhost:8000/predict";

const payload = {
  distance_km: 10.0,
  heure: 18,
  jour_semaine: 2,
  etat_route: 3,
  pluie: 0,
  embouteillage: 1
};

fetch(url, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify(payload),
})
  .then(response => response.json())
  .then(data => {
    console.log(`Tarif prédit: ${data.predicted_fare} CFA`);
    console.log(`Modèle utilisé: ${data.model_used}`);
  })
  .catch(error => console.error("Erreur:", error));
```

---

### Exemple 4: Postman

**Configuration:**
1. Méthode: `POST`
2. URL: `http://localhost:8000/predict`
3. Headers: 
   - Key: `Content-Type`
   - Value: `application/json`
4. Body → raw → JSON:
```json
{
  "distance_km": 15.0,
  "heure": 18,
  "jour_semaine": 4,
  "etat_route": 2,
  "pluie": 1,
  "embouteillage": 1
}
```

**Fichier Postman à importer:** `exemples_postman.json` (fourni)

---

## ⚠️ Gestion des erreurs

### Erreur 400 - Bad Request

**Cas 1: Distance négative ou nulle**
```bash
curl -X POST http://localhost:8000/predict \
  -H "Content-Type: application/json" \
  -d '{"distance_km": -5, "heure": 10, "jour_semaine": 1, "etat_route": 2, "pluie": 0, "embouteillage": 0}'
```
<!-- 
**Réponse:**
```json
{ -->