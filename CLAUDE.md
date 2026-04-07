# dis-news-service

## Run
```
mvn spring-boot:run          # port 8084, context-path /api
```
DB: MySQL `172.16.0.19:3306/dis_news` (spring.datasource in application.properties)

## Stack
Spring Boot 3.5.6 · Java 21 · Lombok · JPA · Spring Security (JWT) · WebSocket

## Endpoint map
| Controller | Base path | Key endpoints |
|---|---|---|
| AuthController | `/auth` | POST /login, PUT /reset-password, POST /register |
| MainController | `/main` | POST /hourly, GET /getHourlyHistory, GET /getDailySummary, GET /getMarkers |
| ReferenceController | `/ref` | /department/\*, /position/\*, /station/\*, /user/\* |
| TaskController | `/ref/task` | GET /getAll, GET /countPending, POST /save, PUT /updateFulfillment/{id}, DELETE /delete/{id} |
| WorkOrderController | `/ref/work-order` | GET /getAll, GET /countPending, POST /save, PUT /updateStatus/{id}, DELETE /delete/{id} |
| SewageTreatmentController | `/sewage-treatment` | GET /summary, GET /stations, POST /save |
| WaterHourlyController | `/ws` | GET /water-hourly |
| VehicleOrderController | `/ref/vehicle-order` | GET /getByDate, POST /save, POST /bulk-confirm |
| ServertimeController | `/server-time` | GET (returns current server time) |
| NotificationController | `/notifications` | GET, PUT /read-all |

## Key entities

### HourlyWsStation (`hourly_ws_station`)
| Field | Meaning |
|---|---|
| `pipeFm1` | FM-1 (full mode) |
| `pipeFm7` | FM-7 (full) OR FM-2 (transmission mode — stored here) |
| `pipeFm8` | FM-8 (full mode) |
| `firstWorkingCount` | Ажиллаж буй худаг |
| `firstPendingCount` | Бэлтгэлд буй худаг |
| `firstRepairingCount` | Засварт буй худаг |
| `firstPool/secondPool/...` | Усан сан түвшин |

### WorkOrder (`work_order`)
| Field | Meaning |
|---|---|
| `assignedDepartmentId` | Захиалга өгсөн алба (UI: "Захиалагч") |
| `departmentId` | Гүйцэтгэгч алба |
| `status` | 0=хүлээгдэж байна, 1=гүйцэтгэж байна, 2=дууссан |
| `activeFlag` | 1=идэвхтэй, 0=устгасан (soft delete) |

### Task (`tasks`)
| Field | Meaning |
|---|---|
| `assignedPositionName` | Үүрэг болгосон албан тушаал |
| `departmentId` | Хариуцагч алба |
| `positionId` | Хариуцагч албан тушаал |
| `status` | 0=биелэлт ороогүй, 1=биелүүлсэн, 2=буцаасан |
| `activeFlag` | 1=идэвхтэй, 0=устгасан |

## Business rules
- **Shift date**: 08:00–07:59 next day = нэг ээлж. Шилжилтийн цаг 08:00.
- **WorkOrder status=2**: автоматаар `work_news` бүртгэлд ордог + мэдэгдэл илгээнэ.
- **Soft delete**: `activeFlag=0` (WorkOrder, Task, бусад). `findActive()` query нь `activeFlag=1` шүүнэ.
- **UserContext**: `UserContext.getUserId()` — JWT-с авна, `createdBy/updatedBy` автоматаар орно.
- **Server time**: frontend `/server-time` endpoint-с цаг авна. Backend `LocalDateTime.now()` хэрэглэнэ.

## UI ↔ Backend correlation
| UI хэсэг | Endpoint |
|---|---|
| Dashboard KPI (үүрэг) | GET /api/ref/task/countPending |
| Dashboard KPI (ажлын захиалга) | GET /api/ref/work-order/countPending?deptId=... |
| ws-station цагийн бүртгэл | POST /api/main/hourly |
| Хяналтын самбар газрын зураг | GET /api/main/getMarkers |
| Departments лавлах | GET /api/ref/department/getAll |
