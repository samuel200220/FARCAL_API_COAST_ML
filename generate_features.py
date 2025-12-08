import joblib # pyright: ignore[reportMissingImports]
import pandas as pd # pyright: ignore[reportMissingModuleSource]

# Définir les colonnes de features dans le même ordre que dans votre script d'entraînement
# Ces colonnes correspondent à ce qui reste après avoir supprimé les colonnes inutiles

feature_columns = [
    'pluie',
    'etat_route', 
    'heure',
    'jour_semaine',
    'jour_ferie',
    'bagages',
    'routes_larges',
    'routes_travaux',
    'accident',
    'depart_osm',
    'destination_osm',
    'distance_km'
]

# Sauvegarder le fichier
joblib.dump(feature_columns, 'feature_columns.pkl')

print("✅ Fichier 'feature_columns.pkl' créé avec succès!")
print(f"\nFeatures sauvegardées ({len(feature_columns)}) :")
for i, col in enumerate(feature_columns, 1):
    print(f"  {i}. {col}")

# Vérification : recharger pour tester
loaded_features = joblib.load('feature_columns.pkl')
print(f"\n✅ Vérification : {len(loaded_features)} features rechargées correctement")