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
| RepairCategoryController | `/repair/category` | GET /getAll, POST /save, PUT /update/{id}, DELETE /delete/{id} |
| VehicleRepairController | `/repair/vehicle` | GET /getAll, GET /active, GET /by-vehicle?plate=, POST /save, PUT /update/{id}, PUT /finish/{id}, DELETE /delete/{id} |
| RepairPartController | `/repair/part` | GET /types, GET /getAll?includeInactive=, POST /save, PUT /update/{id}, PUT /restore/{id}, DELETE /delete/{id} |
| RepairWorkerController | `/repair/worker` | GET /specialties, GET /getAll?includeInactive=, POST /save, PUT /update/{id}, PUT /restore/{id}, DELETE /delete/{id} |
| RepairUsageController | `/repair/usage` | GET /{repairId}, POST /part, POST /worker, DELETE /part/{id}, DELETE /worker/{id}, GET /worker-history?from=&to= |
| RepairDocumentController | `/repair/document` | GET /getAll, POST /save, PUT /update/{id}, DELETE /delete/{id} |
| RepairAttendanceController | `/repair/attendance` | GET ?date=, POST ?date= |
| VehicleRepairPhotoController | `/repair/photo` | GET /{repairId}, POST, DELETE /{id} |
| RepairReportController | `/repair/report` | GET /daily, /weekly, /period, /preview (Word .docx) |
| DriverController | `/ref/driver` | GET /getAll?includeInactive=, GET /search?q=, POST /save, PUT /update/{id}, DELETE /delete/{id} (**soft delete**) |
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

### RepairCategory (`repair_category`)
| Field | Meaning |
|---|---|
| `name` | Ангиллын нэр (Их засвар, Урсгал засвар, Техникийн үйлчилгээ, Сервис үйлчилгээ) |
| `code` | Системийн түлхүүр — үндсэн 4 ангилалд л утгатай, UI-аас нэмсэнд NULL. Сервер эзэмшинэ, update-аар өөрчлөгдөхгүй |
| `sortOrder` | Жагсаалтын дараалал |
| `activeFlag` | 1=идэвхтэй, 0=устгасан (soft delete) |

Migration: `db_migration_repair_category.sql` (хүснэгт + үндсэн 4 мөр + цэсний мөр).

### VehicleRepair (`vehicle_repair`)
| Field | Meaning |
|---|---|
| `vehicleId` / `plateNumber` | Машин. Дугаарыг давхар хадгална — `ref/vehicle/delete` нь hard delete тул түүх үлдээхэд хэрэгтэй |
| `repairCategoryId` | `repair_category`-ийн мөр |
| `startDate` / `endDate` | Засварын хугацаа. `endDate` NULL = тодорхойгүй |
| `status` | 0=засварт байна, 1=дууссан |
| `activeFlag` | 1=идэвхтэй, 0=устгасан (soft delete) |

**Дүрэм**: нэг машинд нэг л нээлттэй (status=0) засвар байна — `save()` давхардлыг хориглоно.
`GET /active` нээлттэй бүх бичлэгийг буцаана; frontend `assign-form` үүнийг уншаад захиалгын
хугацаатай давхцаж буй машиныг сонголтоос хасна (давхцал: `start <= өдөр` ба `end IS NULL OR end >= өдөр`).

Migration: `db_migration_vehicle_repair.sql` (`db_migration_repair_category.sql`-ийн дараа).

### Driver (`driver`)
| Field | Meaning |
|---|---|
| `name` (`full_name`) | Овог нэр |
| `licenseCategories` | "A,B,C" хэлбэрээр |
| `activeFlag` | 1=идэвхтэй, 0=устгасан. **Өмнө нь hard delete байсан** — хуваарилалтын түүхэд нэр үлдэх ёстой тул soft delete болгов |

Migration: `db_migration_employee_menu.sql` (active_flag + "Ажилтан" цэсний мөр).

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
