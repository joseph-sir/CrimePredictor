"""
CrimePredictor Backend Server
Accepts crime prediction requests from the Android CrimePredictor app
and returns calculated risk predictions, risk score, and confidence.
"""

from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import math

HOST = "0.0.0.0"
PORT = 8000

# Base risk factors for South African provinces (heuristic weights)
PROVINCE_RISK = {
    "gauteng": 0.82,
    "western cape": 0.78,
    "kwazulu-natal": 0.74,
    "eastern cape": 0.65,
    "free state": 0.52,
    "mpumalanga": 0.50,
    "north west": 0.48,
    "limpopo": 0.42,
    "northern cape": 0.38
}

# Base risk factors for crime types
CRIME_RISK = {
    "murder": 0.95,
    "carjacking": 0.88,
    "robbery": 0.82,
    "assault": 0.75,
    "burglary": 0.68,
    "sexual offence": 0.85,
    "drug-related": 0.60,
    "theft": 0.45,
    "fraud": 0.40
}

def calculate_risk(province, crime_type, hour, day_of_week, month, crime_count_30d):
    prov_factor = PROVINCE_RISK.get(province.lower().strip(), 0.50)
    crime_factor = CRIME_RISK.get(crime_type.lower().strip(), 0.50)
    
    # Night hours (20:00 to 04:00) carry higher temporal risk
    if hour >= 21 or hour <= 4:
        hour_factor = 0.85
    elif hour >= 17:
        hour_factor = 0.70
    elif hour >= 12:
        hour_factor = 0.50
    else:
        hour_factor = 0.40

    # Weekend factor (Friday = 4, Saturday = 5, Sunday = 6)
    day_factor = 0.80 if day_of_week in [4, 5, 6] else 0.50

    # Crime count normalization (sigmoid-like scaling around 50 crimes)
    count_factor = 1.0 / (1.0 + math.exp(-(crime_count_30d - 50) / 25.0))

    # Weighted combined risk score
    raw_score = (
        0.30 * prov_factor +
        0.25 * crime_factor +
        0.20 * count_factor +
        0.15 * hour_factor +
        0.10 * day_factor
    )
    risk_score = round(min(max(raw_score, 0.05), 0.98), 3)

    # Risk classification
    prediction = "High Risk" if risk_score >= 0.60 else "Low Risk"

    # Confidence calculation based on data completeness
    confidence = round(0.85 + (0.10 if crime_count_30d > 20 else 0.02), 3)
    confidence = min(confidence, 0.97)

    return prediction, risk_score, confidence

class RequestHandler(BaseHTTPRequestHandler):
    def do_OPTIONS(self):
        # CORS pre-flight support
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()

    def do_GET(self):
        if self.path == "/" or self.path == "/health":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            response = {"status": "online", "service": "CrimePredictor API", "version": "1.0"}
            self.wfile.write(json.dumps(response).encode("utf-8"))
        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        if self.path == "/predict":
            content_length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(content_length).decode("utf-8")
            
            try:
                data = json.loads(body)
                province = data.get("province", "Gauteng")
                crime_type = data.get("crime_type", "Theft")
                hour = int(data.get("hour", 12))
                day_of_week = int(data.get("day_of_week", 0))
                month = int(data.get("month", 1))
                crime_count = int(data.get("crime_count_30d", 50))

                prediction, risk_score, confidence = calculate_risk(
                    province, crime_type, hour, day_of_week, month, crime_count
                )

                response = {
                    "prediction": prediction,
                    "risk_score": risk_score,
                    "confidence": confidence
                }

                self.send_response(200)
                self.send_header("Content-Type", "application/json")
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                self.wfile.write(json.dumps(response).encode("utf-8"))
                print(f"[API] Prediction: {province} | {crime_type} | {prediction} ({risk_score*100:.1f}%)")

            except Exception as e:
                self.send_response(400)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"error": str(e)}).encode("utf-8"))
        else:
            self.send_response(404)
            self.end_headers()

def run():
    server = HTTPServer((HOST, PORT), RequestHandler)
    print(f"==================================================")
    print(f" CrimePredictor Server running on http://localhost:{PORT}")
    print(f" Ready to receive requests from Android Studio App")
    print(f"==================================================")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down server.")
        server.server_close()

if __name__ == "__main__":
    run()
