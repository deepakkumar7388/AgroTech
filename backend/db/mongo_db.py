import os
from datetime import datetime
from dotenv import load_dotenv

load_dotenv()

MONGO_URI     = os.getenv("MONGO_URI", "")
MONGO_DB_NAME = os.getenv("MONGO_DB_NAME", "agrotechDatabase")

# ── Graceful init: don't crash if MONGO_URI is missing/placeholder ────────────
_PLACEHOLDER_URIS = {"", "mongodb+srv://<username>:<password>@<cluster>.mongodb.net/<dbname>"}

db                  = None
users_col           = None
reports_col         = None
stress_analysis_col = None
devices_col         = None
iot_data_col        = None

try:
    if MONGO_URI and MONGO_URI not in _PLACEHOLDER_URIS:
        from pymongo import MongoClient
        client  = MongoClient(MONGO_URI, serverSelectionTimeoutMS=5000)
        db      = client.get_database(MONGO_DB_NAME)
        users_col           = db.get_collection("users")
        reports_col         = db.get_collection("reports")
        stress_analysis_col = db.get_collection("stress_analysis")
        devices_col         = db.get_collection("devices")
        iot_data_col        = db.get_collection("iot_readings")
        
        # Ensure Indexes
        users_col.create_index("mobile_number", unique=True, sparse=True)
        devices_col.create_index("device_id", unique=True, sparse=True)
        iot_data_col.create_index([("user_id", 1), ("timestamp", -1)])
        
        print("INFO  [MongoDB]: Connected to Atlas successfully.")
    else:
        print("WARN  [MongoDB]: No valid MONGO_URI. Running in MOCK mode (No DB persistence).")
except Exception as e:
    print(f"ERROR [MongoDB]: Connection failed: {e}")

# ─────────────────────────────────────────────────────────────
# 👤 USER HELPERS
# ─────────────────────────────────────────────────────────────

def get_user_by_email(email: str):
    if users_col is None: return None
    return users_col.find_one({"email": email})

def get_user_by_mobile(mobile_number: str):
    if users_col is None: return None
    return users_col.find_one({"mobile_number": mobile_number})

def get_user_by_id(user_id: str):
    if users_col is None: return None
    from bson.objectid import ObjectId
    try:
        return users_col.find_one({"_id": ObjectId(user_id)})
    except:
        return None

def create_user(user_data: dict):
    if users_col is None:
        class MockResult: inserted_id = "mock_id_123"
        return MockResult()
    return users_col.insert_one(user_data)

def list_all_users():
    if users_col is None: return []
    return list(users_col.find({}, {"password": 0}))

# ─────────────────────────────────────────────────────────────
# 🔌 DEVICE MAPPING HELPERS
# ─────────────────────────────────────────────────────────────

def get_device(device_id: str):
    if devices_col is None: return None
    return devices_col.find_one({"device_id": device_id})

def get_device_by_user(user_id: str):
    if devices_col is None: return None
    return devices_col.find_one({"user_id": user_id})

def map_device_to_user(device_id: str, user_id: str, farmer_name: str):
    if devices_col is None: return
    
    # Check if already owned by someone else
    existing = get_device(device_id)
    if existing and existing.get("user_id") and existing["user_id"] != user_id:
        raise ValueError(f"Device '{device_id}' is already registered to another farmer.")

    devices_col.update_one(
        {"device_id": device_id},
        {"$set": {
            "user_id":     user_id,
            "farmer_name": farmer_name,
            "mapped_at":   datetime.utcnow()
        }},
        upsert=True
    )

def unmap_device(device_id: str):
    if devices_col is None: return
    devices_col.update_one(
        {"device_id": device_id},
        {"$set": {"user_id": None, "farmer_name": "", "mapped_at": None}}
    )

def list_all_devices():
    if devices_col is None: return []
    return list(devices_col.find())

# ─────────────────────────────────────────────────────────────
# 📈 IOT DATA HELPERS
# ─────────────────────────────────────────────────────────────

def save_iot_reading(user_id: str, device_id: str, data: dict):
    if iot_data_col is None: return
    doc = {
        "user_id":   user_id,
        "device_id": device_id,
        "reading":   data,
        "timestamp": datetime.utcnow()
    }
    iot_data_col.insert_one(doc)

def get_iot_history_for_user(user_id: str, limit: int = 20):
    if iot_data_col is None: return []
    cursor = iot_data_col.find({"user_id": user_id}).sort("timestamp", -1).limit(limit)
    history = []
    for doc in cursor:
        r = doc.get("reading", {})
        r["timestamp"] = doc["timestamp"].strftime("%Y-%m-%d %H:%M:%S")
        history.append(r)
    return history

# ─────────────────────────────────────────────────────────────
# 📝 REPORT HELPERS
# ─────────────────────────────────────────────────────────────

def save_report(report_data: dict):
    if reports_col is None:
        print(f"DEBUG [MOCK]: Saved report of type {report_data.get('type')}")
        return
    reports_col.insert_one(report_data)

def save_stress_analysis(analysis_data: dict):
    if stress_analysis_col is None:
        print(f"DEBUG [MOCK]: Saved stress analysis for {analysis_data.get('label')}")
        return
    stress_analysis_col.insert_one(analysis_data)
