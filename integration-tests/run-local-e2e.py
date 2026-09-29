"""Build first, then run with Python 3 + pymongo and local MongoDB on 27017.
Uses three temporary databases and ports 18081-18083. Never uses cloud configuration.
"""
import concurrent.futures
import json
import os
from pathlib import Path
import secrets
import socket
import subprocess
import time
import urllib.error
import urllib.request
import uuid
from pymongo import MongoClient

ROOT = Path(__file__).resolve().parents[1]
RUN = uuid.uuid4().hex
DATABASES = {s: f"ridelink_e2e_{RUN}_{s}" for s in ("account", "driver", "ride")}
PORTS = {"account": 18081, "driver": 18082, "ride": 18083}
MODULES = {"account": "account-service", "driver": "driver-vehicle-service", "ride": "ride-management-service"}
TOKEN = secrets.token_hex(32)
JWT = secrets.token_hex(32)
PROCESSES = {}
LOGS = []
CHECKS = 0
mongo = MongoClient("mongodb://localhost:27017", serverSelectionTimeoutMS=3000, uuidRepresentation="standard")


def request(service, method, path, body=None, token=None, expected=200, internal=False):
    global CHECKS
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    if internal:
        headers["X-Service-Token"] = TOKEN
    req = urllib.request.Request(f"http://127.0.0.1:{PORTS[service]}" + path,
                                 data=None if body is None else json.dumps(body).encode(), headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=12) as response:
            status, data = response.status, response.read()
    except urllib.error.HTTPError as error:
        status, data = error.code, error.read()
    parsed = json.loads(data) if data else None
    if expected is not None:
        assert status == expected, f"{service} {method} {path}: expected {expected}, got {status}; error={parsed.get('error') if isinstance(parsed, dict) else ''}"
        CHECKS += 1
    return parsed if expected is not None else (status, parsed)


def start(service):
    env = os.environ.copy()
    env.update({"SPRING_PROFILES_ACTIVE": "e2e", "SERVER_PORT": str(PORTS[service]),
                "SPRING_DATA_MONGODB_URI": f"mongodb://localhost:27017/{DATABASES[service]}",
                "SPRING_DATA_MONGODB_DATABASE": DATABASES[service],
                "SPRING_DATA_MONGODB_HOST": "localhost", "SPRING_DATA_MONGODB_PORT": "27017",
                "DRIVER_DB_NAME": DATABASES[service], "MONGODB_DATABASE": DATABASES[service],
                "JWT_SECRET": JWT, "RIDELINK_SERVICE_TOKEN": TOKEN, "RIDELINK_SECURITY_ENABLED": "true",
                "ACCOUNT_SERVICE_URL": "http://127.0.0.1:18081", "DRIVER_SERVICE_URL": "http://127.0.0.1:18082",
                "LOGGING_LEVEL_ROOT": "WARN", "DRIVER_LOCATION_MAX_AGE_SECONDS": "300"})
    jar = ROOT / MODULES[service] / "target" / (MODULES[service] + "-1.0.0-SNAPSHOT.jar")
    assert jar.exists(), "Run Maven verify before this script"
    logdir = ROOT / "tmp" / "e2e" / RUN
    logdir.mkdir(parents=True, exist_ok=True)
    log = (logdir / (service + ".log")).open("ab")
    LOGS.append(log)
    PROCESSES[service] = subprocess.Popen(["java", "-jar", str(jar)], cwd=ROOT, env=env,
        stdout=log, stderr=subprocess.STDOUT, creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0))


def ready(service):
    deadline = time.monotonic() + 75
    while time.monotonic() < deadline:
        assert PROCESSES[service].poll() is None, f"{service} exited; see tmp/e2e/{RUN}/{service}.log"
        try:
            request(service, "GET", "/v3/api-docs")
            return
        except (OSError, AssertionError):
            time.sleep(0.5)
    raise AssertionError(service + " did not become ready")


def stop(service):
    process = PROCESSES.pop(service, None)
    if process:
        process.terminate()
        try:
            process.wait(timeout=10)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait(timeout=10)


def account(role, number):
    email = f"e2e-{RUN}-{number}@example.com"
    password = secrets.token_hex(12)
    profile = request("account", "POST", "/api/auth/register/" + role,
                      {"fullName": "Integration Test", "email": email, "password": password, "phone": "+94771234567"}, expected=201)
    login = request("account", "POST", "/api/auth/login", {"email": email, "password": password})
    return profile["id"], login["token"]


def run():
    mongo.admin.command("ping")
    for port in PORTS.values():
        with socket.socket() as sock:
            sock.bind(("127.0.0.1", port))
    for service in PORTS:
        start(service)
    for service in PORTS:
        ready(service)
    print("Three services started with authentication enabled and isolated local databases.", flush=True)
    driver_account, driver_token = account("driver", 1)
    other_driver, other_token = account("driver", 2)
    passenger, passenger_token = account("passenger", 3)
    other_passenger, stranger = account("passenger", 4)
    request("account", "GET", "/api/accounts/me", token=driver_token)
    request("driver", "POST", "/api/drivers", {}, expected=401)
    request("driver", "POST", "/api/drivers", {"accountId": other_driver,"licenseNumber":"bad","serviceArea":"Colombo"}, driver_token,403)
    profile=request("driver", "POST", "/api/drivers", {"accountId":driver_account,"licenseNumber":"E2E-LICENCE","serviceArea":"Colombo"},driver_token,201)
    driver=profile["id"]
    driver_path="/api/drivers/"+driver
    request("driver","GET",driver_path,token=other_token,expected=403)
    request("driver","GET",driver_path,token="invalid",expected=401)
    request("driver","GET","/internal/drivers/"+driver,token=driver_token,expected=401)
    request("driver","POST",driver_path+"/vehicles",{"plateNumber":"E2E-CAR","make":"Toyota","model":"Aqua","vehicleClass":"CAR","capacity":4},driver_token,201)
    request("driver","PUT",driver_path+"/availability",{"availabilityStatus":"AVAILABLE"},driver_token)
    request("driver","PUT",driver_path+"/location",{"latitude":6.9271,"longitude":79.8612},driver_token)
    search="/api/drivers/eligible?lat=6.9271&lng=79.8612&radius=5"
    assert request("driver","GET",search,token=passenger_token)[0]["driverId"]==driver
    def create(owner=passenger, token=passenger_token, expected=201):
        return request("ride","POST","/api/v1/rides",{"passengerId":owner,"pickupLatitude":6.9271,"pickupLongitude":79.8612,
             "pickupAddress":"Colombo","destinationLatitude":6.92,"destinationLongitude":79.86,"destinationAddress":"Destination"},token,expected)
    create(other_passenger,passenger_token,403)
    ride=create(); path="/api/v1/rides/"+ride["id"]
    request("ride","GET",path,token=stranger,expected=403)
    request("ride","PATCH",path+"/assign-driver/"+driver,token=passenger_token)
    request("ride","PATCH",path+"/assign-driver/"+driver,token=passenger_token)
    assert request("driver","GET",driver_path,token=driver_token)["availabilityStatus"]=="ON_TRIP"
    assert request("driver","GET",search,token=passenger_token)==[]
    request("driver","PUT",driver_path+"/availability",{"availabilityStatus":"AVAILABLE"},driver_token,409)
    request("ride","PATCH",path+"/status",{"status":"ACCEPTED"},passenger_token,403)
    request("ride","PATCH",path+"/status",{"status":"ACCEPTED"},other_token,403)
    for status in ("ACCEPTED","IN_PROGRESS","COMPLETED"):
        request("ride","PATCH",path+"/status",{"status":status},driver_token)
    assert request("driver","GET",driver_path,token=driver_token)["availabilityStatus"]=="AVAILABLE"
    print("PASS: account verification, ownership, assignment, ON_TRIP protection and completion release.",flush=True)

    # Race two different rides against one driver, using real concurrent HTTP requests.
    rides=[create(),create()]
    def assign(r):
        return request("ride","PATCH","/api/v1/rides/"+r["id"]+"/assign-driver/"+driver,token=passenger_token,expected=None)[0]
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        results=list(pool.map(assign,rides))
    assert sorted(results)==[200,409],results
    winner=rides[results.index(200)]
    request("ride","DELETE","/api/v1/rides/"+winner["id"],token=passenger_token)
    assert request("driver","GET",driver_path,token=driver_token)["availabilityStatus"]=="AVAILABLE"
    # Delayed release of the previous ride must not release a new reservation.
    next_ride=create(); next_path="/api/v1/rides/"+next_ride["id"]
    request("ride","PATCH",next_path+"/assign-driver/"+driver,token=passenger_token)
    request("driver","DELETE",f"/internal/drivers/{driver}/reservations/{winner['id']}",internal=True)
    assert request("driver","GET",driver_path,token=driver_token)["availabilityStatus"]=="ON_TRIP"
    print("PASS: concurrent assignments allow one winner; delayed release preserves the next ride.",flush=True)

    # Persist cancellation while Driver is down, restart Ride and Driver, and prove durable recovery.
    stop("driver")
    request("ride","DELETE",next_path,token=passenger_token,expected=503)
    pending=request("ride","GET",next_path,token=passenger_token)
    assert pending["status"]=="CANCELLED" and pending["releasePending"]
    stop("ride"); start("ride"); ready("ride")
    start("driver"); ready("driver")
    deadline=time.monotonic()+25
    while time.monotonic()<deadline:
        if not request("ride","GET",next_path,token=passenger_token)["releasePending"]:
            break
        time.sleep(0.5)
    else:
        raise AssertionError("Release recovery did not finish")
    assert request("driver","GET",driver_path,token=driver_token)["availabilityStatus"]=="AVAILABLE"
    stop("account")
    request("driver","GET",driver_path,token=driver_token,expected=503)
    request("ride","GET",next_path,token=passenger_token,expected=503)
    print("PASS: persisted release recovered across process restarts; Account outage fails closed.",flush=True)
    print(f"PASS: {CHECKS} HTTP status checks plus lifecycle, ownership, concurrency and recovery assertions.",flush=True)


if __name__=="__main__":
    try:
        run()
    finally:
        for name in list(PROCESSES):
            stop(name)
        for log in LOGS:
            log.close()
        for db in DATABASES.values():
            assert db.startswith("ridelink_e2e_"+RUN+"_")
            mongo.drop_database(db)
        mongo.close()
        print("Stopped test processes and dropped only this run's three temporary databases.",flush=True)
