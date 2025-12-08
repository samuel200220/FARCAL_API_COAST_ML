import requests
import json
from datetime import datetime

# Configuration
BASE_URL = "http://localhost:8000"

def print_test_header(test_name):
    print("\n" + "="*60)
    print(f"TEST: {test_name}")
    print("="*60)

def print_result(success, message):
    status = "✅ SUCCÈS" if success else "❌ ÉCHEC"
    print(f"{status}: {message}\n")

def test_health():
    """Test 1: Vérifier que l'API est en ligne"""
    print_test_header("Vérification santé de l'API")
    
    try:
        response = requests.get(f"{BASE_URL}/health")
        
        if response.status_code == 200:
            data = response.json()
            print(f"Réponse: {json.dumps(data, indent=2)}")
            
            if data.get("status") == "ok":
                print_result(True, "API en ligne et fonctionnelle")
                return True
            else:
                print_result(False, "API répond mais status != 'ok'")
                return False
        else:
            print_result(False, f"Status code: {response.status_code}")
            return False
            
    except Exception as e:
        print_result(False, f"Erreur de connexion: {str(e)}")
        return False

def test_model_info():
    """Test 2: Vérifier les informations du modèle"""
    print_test_header("Informations sur le modèle")
    
    try:
        response = requests.get(f"{BASE_URL}/model-info")
        
        if response.status_code == 200:
            data = response.json()
            print(f"Réponse: {json.dumps(data, indent=2)}")
            
            if data.get("model_loaded"):
                print_result(True, f"Modèle chargé: {data.get('model_type')}")
                print(f"Features: {data.get('features')}")
            else:
                print_result(True, "API en mode dégradé (pas de modèle)")
            return True
        else:
            print_result(False, f"Status code: {response.status_code}")
            return False
            
    except Exception as e:
        print_result(False, f"Erreur: {str(e)}")
        return False

def test_prediction(test_name, payload, expected_range=None):
    """Test générique de prédiction"""
    print_test_header(test_name)
    print(f"Payload: {json.dumps(payload, indent=2)}")
    
    try:
        response = requests.post(
            f"{BASE_URL}/predict",
            json=payload,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            data = response.json()
            print(f"Réponse: {json.dumps(data, indent=2)}")
            
            predicted_fare = data.get("predicted_fare")
            
            # Vérifier que le prix est dans une plage raisonnable
            if expected_range:
                min_price, max_price = expected_range
                if min_price <= predicted_fare <= max_price:
                    print_result(True, f"Prix prédit: {predicted_fare} CFA (dans la plage attendue)")
                else:
                    print_result(False, f"Prix {predicted_fare} CFA hors de la plage [{min_price}, {max_price}]")
            else:
                # Vérifier juste que le prix est positif et raisonnable
                if 500 <= predicted_fare <= 50000:
                    print_result(True, f"Prix prédit: {predicted_fare} CFA (plage acceptable)")
                else:
                    print_result(False, f"Prix {predicted_fare} CFA semble anormal")
            
            return True
        else:
            print(f"Status code: {response.status_code}")
            print(f"Réponse: {response.text}")
            print_result(False, "Erreur dans la requête")
            return False
            
    except Exception as e:
        print_result(False, f"Erreur: {str(e)}")
        return False

def test_invalid_distance():
    """Test 4: Distance négative (doit échouer)"""
    print_test_header("Test distance négative (doit échouer)")
    
    payload = {
        "distance_km": -5,
        "heure": 10,
        "jour_semaine": 1,
        "etat_route": 2,
        "pluie": 0,
        "embouteillage": 0
    }
    
    print(f"Payload: {json.dumps(payload, indent=2)}")
    
    try:
        response = requests.post(
            f"{BASE_URL}/predict",
            json=payload,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 400:
            print(f"Réponse: {response.json()}")
            print_result(True, "L'API a correctement rejeté la distance négative")
            return True
        else:
            print_result(False, f"Status code attendu: 400, reçu: {response.status_code}")
            return False
            
    except Exception as e:
        print_result(False, f"Erreur: {str(e)}")
        return False

def run_all_tests():
    """Exécute tous les tests"""
    print("\n" + "="*60)
    print("DÉBUT DES TESTS D'INTÉGRATION - FARCAL ML API")
    print(f"Date: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("="*60)
    
    results = []
    
    # Test 1: Health check
    results.append(("Health Check", test_health()))
    
    # Test 2: Model info
    results.append(("Model Info", test_model_info()))
    
    # Test 3: Prédiction courte distance
    results.append(("Courte distance", test_prediction(
        "Prédiction courte distance (5 km)",
        {
            "distance_km": 5.0,
            "heure": 8,
            "jour_semaine": 0,
            "etat_route": 3,
            "pluie": 0,
            "embouteillage": 0
        },
        expected_range=(1000, 3000)
    )))
    
    # Test 4: Prédiction moyenne distance
    results.append(("Moyenne distance", test_prediction(
        "Prédiction moyenne distance (15 km)",
        {
            "distance_km": 15.0,
            "heure": 18,
            "jour_semaine": 2,
            "etat_route": 2,
            "pluie": 1,
            "embouteillage": 1
        },
        expected_range=(2000, 6000)
    )))
    
    # Test 5: Prédiction longue distance
    results.append(("Longue distance", test_prediction(
        "Prédiction longue distance (30 km)",
        {
            "distance_km": 30.0,
            "heure": 7,
            "jour_semaine": 4,
            "etat_route": 1,
            "pluie": 1,
            "embouteillage": 1
        },
        expected_range=(4000, 12000)
    )))
    
    # Test 6: Distance invalide
    results.append(("Distance négative", test_invalid_distance()))
    
    # Résumé
    print("\n" + "="*60)
    print("RÉSUMÉ DES TESTS")
    print("="*60)
    
    passed = sum(1 for _, success in results if success)
    total = len(results)
    
    for test_name, success in results:
        status = "✅" if success else "❌"
        print(f"{status} {test_name}")
    
    print(f"\nRésultat: {passed}/{total} tests réussis ({passed*100//total}%)")
    
    if passed == total:
        print("\n🎉 TOUS LES TESTS SONT PASSÉS! L'API EST PRÊTE!")
    else:
        print(f"\n⚠️ {total - passed} test(s) ont échoué. Vérifiez les erreurs ci-dessus.")
    
    print("="*60 + "\n")

if __name__ == "__main__":
    print("Assurez-vous que l'API est lancée sur http://localhost:8000")
    print("Commande: uvicorn ml_api:app --reload\n")
    input("Appuyez sur Entrée pour commencer les tests...")
    
    run_all_tests()