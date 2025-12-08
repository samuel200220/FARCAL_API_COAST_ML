# Guide Jour 2 - Intégration du Modèle ML

## 📅 Planning du Matin (8h-12h)

### Étape 1: Recevoir les fichiers de P3 (vers 10h)
P3 va vous envoyer 2 fichiers :
- `farcal_model_v1.pkl` - Le modèle Random Forest entraîné
- `feature_columns.pkl` - Liste des features utilisées par le modèle

### Étape 2: Placer les fichiers au bon endroit
```bash
# Mettez les fichiers dans le même dossier que ml_api.py
farcal-api/
├── ml_api.py
├── farcal_model_v1.pkl      ← Fichier de P3
├── feature_columns.pkl       ← Fichier de P3
└── tests_integration.py
```

### Étape 3: Remplacer votre ancien ml_api.py
1. Sauvegardez votre ancien fichier (renommez-le `ml_api_old.py`)
2. Utilisez le nouveau `ml_api.py` que je vous ai préparé (dans l'artifact "ml_api.py - Version Jour 2")
3. Vérifiez que les fichiers `.pkl` sont bien présents

### Étape 4: Lancer l'API avec le modèle
```bash
uvicorn ml_api:app --reload
```

**Vous devriez voir :**
```
✅ Features chargées: ['distance_km', 'heure', 'jour_semaine', ...]
✅ Modèle ML chargé avec succès!
INFO:     Uvicorn running on http://127.0.0.1:8000
```

### Étape 5: Tester manuellement
Ouvrez `http://localhost:8000/docs` et testez `/predict` avec :
```json
{
  "distance_km": 10.0,
  "heure": 18,
  "jour_semaine": 2,
  "etat_route": 3,
  "pluie": 0,
  "embouteillage": 1
}
```

**Vérifiez que :**
- Le prix prédit n'est plus 2000 CFA (prix fixe)
- Le prix est réaliste (entre 1000 et 5000 CFA pour 10 km)
- `"model_used": true` dans la réponse

### Étape 6: Lancer les tests automatiques
```bash
python tests_integration.py
```

Cela va tester 5 cas différents et vous donner un résumé.

---

## 🔧 Fonctionnalités de la nouvelle API

### 1. Chargement automatique du modèle
L'API charge le modèle au démarrage. Si le fichier `.pkl` n'existe pas, elle fonctionne en "mode dégradé" avec un calcul simple.

### 2. Nouvelle route : `/model-info`
```bash
curl http://localhost:8000/model-info
```
Retourne les informations sur le modèle chargé.

### 3. Nouvelle route : `/reload-model`
```bash
curl -X POST http://localhost:8000/reload-model
```
Recharge le modèle sans redémarrer l'API (utile si P3 vous donne une version améliorée).

### 4. Validation renforcée
- Distance doit être > 0 et < 100 km
- Les prédictions sont limitées entre 500 et 50,000 CFA
- Gestion des erreurs améliorée

### 5. Mode dégradé automatique
Si le modèle n'est pas trouvé, l'API utilise un calcul simple :
```
Prix = 500 + (distance × 150) + ajustements
```

---

## 🧪 Tests à faire (matin)

### Test 1: Sans modèle (mode dégradé)
1. Renommez temporairement `farcal_model_v1.pkl` en `_farcal_model_v1.pkl`
2. Relancez l'API
3. Testez `/predict` → devrait fonctionner avec calcul simple
4. Vérifiez `"model_used": false` dans la réponse

### Test 2: Avec modèle
1. Remettez le bon nom du fichier
2. Relancez l'API
3. Testez `/predict` → devrait utiliser le modèle ML
4. Vérifiez `"model_used": true` dans la réponse

### Test 3: 5 cas différents
Testez avec le script `tests_integration.py` ou manuellement :
1. Courte distance (5 km) + bonne route
2. Moyenne distance (15 km) + embouteillage + pluie
3. Longue distance (30 km) + mauvaise route
4. Distance négative (doit échouer avec erreur 400)
5. Heure de pointe (7h ou 18h) vs heure creuse (14h)

---

## 📝 À documenter dans `tests_integration.txt`

Créez un fichier texte avec :
```
TESTS D'INTÉGRATION - Jour 2
Date: [date]
Heure: [heure]

=== CONFIGURATION ===
- Modèle chargé: Oui/Non
- Type de modèle: RandomForestRegressor
- Nombre de features: 6

=== TEST 1: Courte distance ===
Input: 5 km, 8h, Lundi, Route bonne, Pas de pluie
Résultat: 1,850 CFA
Status: ✅ RÉUSSI

=== TEST 2: Moyenne distance ===
Input: 15 km, 18h, Mercredi, Route moyenne, Pluie + Embouteillage
Résultat: 3,420 CFA
Status: ✅ RÉUSSI

[... etc pour les 5 tests ...]

=== RÉSUMÉ ===
Tests réussis: 5/5
Temps de réponse moyen: 0.15 secondes
Conclusion: API prête pour production
```

---

## ⚠️ Si quelque chose ne fonctionne pas

### Problème: "FileNotFoundError: farcal_model_v1.pkl"
**Solution:** 
- Vérifiez que le fichier est dans le même dossier que `ml_api.py`
- Vérifiez le nom exact du fichier (sensible à la casse)

### Problème: "ModuleNotFoundError: No module named 'joblib'"
**Solution:**
```bash
pip install joblib pandas numpy scikit-learn
```

### Problème: Prédictions bizarres (prix négatifs ou très élevés)
**Solution:**
- Vérifiez que les features sont dans le bon ordre
- Contactez P3 pour vérifier le modèle
- L'API limite automatiquement entre 500 et 50,000 CFA

### Problème: API ne démarre pas
**Solution:**
```bash
# Vérifiez si le port 8000 est déjà utilisé
lsof -i :8000

# Utilisez un autre port
uvicorn ml_api:app --reload --port 8001
```

---

## 🎯 Checklist avant 12h

- [ ] Modèle `.pkl` reçu de P3
- [ ] API relancée avec nouveau code
- [ ] `/model-info` confirme que modèle est chargé
- [ ] 5 tests manuels effectués
- [ ] Tests automatiques lancés (tous passent)
- [ ] `tests_integration.txt` créé avec résultats
- [ ] Prêt pour l'après-midi (tests approfondis)

---

## 🚀 Prochaines étapes (Après-midi)

L'après-midi, vous allez :
1. Tester 20 cas différents (normaux + cas limites)
2. Mesurer les temps de réponse
3. Créer la documentation complète
4. Préparer `requirements.txt`
5. Créer des exemples Postman

Mais pour l'instant, concentrez-vous sur l'intégration du modèle ! 💪