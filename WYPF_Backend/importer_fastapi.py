"""
FastAPI CSV importer for West Yorkshire Crimes CSV
Produces Firestore documents using firebase-admin SDK.

Usage:
1. Place your Firebase service account JSON in the same folder and name it `serviceAccountKey.json`
   (or update the CREDENTIAL_PATH variable).
2. Install requirements:
   pip install fastapi uvicorn firebase-admin python-multipart pandas
3. Run:
   uvicorn importer_fastapi:app --host 0.0.0.0 --port 8000 --reload
4. POST the CSV file to /import-csv as form-data file

This script expects CSV columns:
Crime ID, Month, Reported by, Falls within, Longitude, Latitude, Location,
LSOA code, LSOA name, Crime type, Last outcome category, Context

Each CSV row will be written to Firestore collection 'west_yorkshire_crimes'.
"""

from fastapi import FastAPI, File, UploadFile, HTTPException
import csv, io, os
import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime

CREDENTIAL_PATH = os.path.join(os.getcwd(), 'serviceAccountKey.json')
COLLECTION_NAME = 'west_yorkshire_crimes'

if not os.path.exists(CREDENTIAL_PATH):
    raise SystemExit(f"Firebase service account JSON not found at {CREDENTIAL_PATH}. Please add it before running.")

cred = credentials.Certificate(CREDENTIAL_PATH)
firebase_admin.initialize_app(cred)
db = firestore.client()

app = FastAPI(title='WY Crimes CSV Importer')

def parse_float_safe(x):
    try:
        return float(x)
    except Exception:
        return None

def parse_month_safe(s):
    # Expecting format like '2018-05' or 'YYYY-MM' or full date; attempt parsing to timestamp
    if not s or s.strip()=='' or s.lower()=='nan':
        return None
    try:
        return datetime.strptime(s.strip(), '%Y-%m')
    except Exception:
        # try full date
        try:
            return datetime.fromisoformat(s.strip())
        except Exception:
            return None

@app.post('/import-csv')
async def import_csv(file: UploadFile = File(...)):
    if not file.filename.lower().endswith('.csv'):
        raise HTTPException(status_code=400, detail='Please upload a CSV file.')
    content = await file.read()
    text = content.decode('utf-8', errors='replace')
    reader = csv.DictReader(io.StringIO(text))
    count = 0
    errors = []
    for idx, row in enumerate(reader, start=1):
        try:
            doc = {
                'crime_id': row.get('Crime ID', '').strip(),
                'month': parse_month_safe(row.get('Month', '')),
                'reported_by': row.get('Reported by', '').strip(),
                'falls_within': row.get('Falls within', '').strip(),
                'location_desc': row.get('Location', '').strip(),
                'lsoa_code': row.get('LSOA code', '').strip(),
                'lsoa_name': row.get('LSOA name', '').strip(),
                'crime_type': row.get('Crime type', '').strip(),
                'last_outcome': row.get('Last outcome category', '').strip(),
                'context': row.get('Context', '').strip() if row.get('Context') and str(row.get('Context')).lower()!='nan' else None,
                'longitude': parse_float_safe(row.get('Longitude')),
                'latitude': parse_float_safe(row.get('Latitude')),
                'imported_at': datetime.utcnow()
            }
            # Optionally remove None values or validate coordinates
            # Add document with crime_id as custom id (if unique) else let Firestore generate id
            if doc['crime_id']:
                # Use crime_id as document id for idempotency
                db.collection(COLLECTION_NAME).document(doc['crime_id']).set(doc)
            else:
                db.collection(COLLECTION_NAME).add(doc)
            count += 1
        except Exception as e:
            errors.append({'row': idx, 'error': str(e)})
    return {'imported': count, 'errors': errors}

if __name__ == '__main__':
    import uvicorn
    uvicorn.run('importer_fastapi:app', host='0.0.0.0', port=8000, reload=True)
