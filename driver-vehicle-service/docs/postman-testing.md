# Test Driver & Vehicle APIs in Postman

Import `postman/collections/Driver-Vehicle.postman_collection.json` from the repository root.
In Postman select **Import**, choose that file, then open **RideLink - Driver & Vehicle - IT24300246**.
Use **Run collection** to execute all thirteen requests in order, or open each request and press **Send**, starting with 01.
The collection supplies JSON bodies, Content-Type, tests and variables. No environment is required.

`baseUrl` defaults to `http://localhost:8082`. Request 01 creates unique test data and automatically saves `driverId`.
Run request 01 first; later requests use that saved ID. Every full run creates one driver and one vehicle.

| Request | Method and path | Expected status |
| --- | --- | --- |
| Create driver | POST /api/drivers | 201 |
| Get driver | GET /api/drivers/{{driverId}} | 200 |
| Register vehicle | POST /api/drivers/{{driverId}}/vehicles | 201 |
| Set AVAILABLE | PUT /api/drivers/{{driverId}}/availability | 200 |
| Update location | PUT /api/drivers/{{driverId}}/location | 200 |
| Find eligible drivers (05a) | GET /api/drivers/eligible?lat=6.9271&lng=79.8612&radius=5 | 200 |
| Invalid radius (05b) | GET /api/drivers/eligible?lat=6.9271&lng=79.8612&radius=51 | 400 |
| Read location back | GET /api/drivers/{{driverId}} | 200 |
| Invalid location | PUT /api/drivers/{{driverId}}/location | 400 |
| Duplicate vehicle | POST /api/drivers/{{driverId}}/vehicles | 409 |
| Missing driver | GET /api/drivers/000000000000000000000001 | 404 |
| Set OFFLINE | PUT /api/drivers/{{driverId}}/availability | 200 |

The 400/409/404 responses are expected passing negative tests. Search uses a maximum radius of 50 km
and requires a location updated within five minutes, AVAILABLE status and a registered vehicle.
Run in order 01-05, 05a, 05b, 06-10, 10a. If you pause for over five minutes, send 05 again before 05a.
If the sidebar order differs, use the request names to select this order.
These are local development endpoints; authentication is pending.

Request 10a searches again and verifies that this run's OFFLINE driver is excluded.
Other eligible drivers may still appear.

Re-import the updated collection in Postman to use the new requests.

## Start the service again

MongoDB must be running on localhost:27017. From the repository root, run in PowerShell:

```powershell
java -jar driver-vehicle-service/target/driver-vehicle-service-1.0.0-SNAPSHOT.jar --server.port=8082 --spring.data.mongodb.database=driver_db_postman_20260929
```

Do not start a second instance if port 8082 is already in use. The dedicated demo database retains synthetic test data.
If the JAR is absent, build it with `mvn -B -pl driver-vehicle-service -am package` first.

## Optional command-line collection run

```powershell
npx.cmd --yes newman run postman/collections/Driver-Vehicle.postman_collection.json
```

Newman executes the same collection tests, but this is separate from running them in the Postman desktop UI.
