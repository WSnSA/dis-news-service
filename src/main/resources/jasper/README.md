# Jasper тайлангийн загварууд (JRXML)

Эдгээр JRXML загваруудыг **тусдаа jasper-service** (REST) рендерлэдэг. Backend нь
`JasperClient`-ээр өгөгдлөө JSON-оор илгээж, PDF байт буцааж авдаг. Загварыг ҮРГЭЛЖ
**КОД**-оор дууддаг (id нь дахин import бүрт өөрчлөгддөг).

Эдгээр файлууд нь **эх хувилбар (source of truth)** — репод хадгална. Өөрчилсний
дараа jasper-service-т уг кодоор **дахин import** хийх шаардлагатай (backend нь
файлыг шууд ачаалдаггүй, jasper-service дээрх хувилбарыг ашигладаг).

| Файл | Jasper КОД | Хаанаас дуудагддаг | Endpoint |
|---|---|---|---|
| `briefing_fulfillment.jrxml` | `BRIEFING_FULFILLMENT` | `BriefingReportService` | `GET /ref/briefing/fulfillment-report/pdf?from=&to=` |

## briefing_fulfillment.jrxml
Шуурхай хурлын үүрэг даалгаврын биелэлтийн тайлан. A4 босоо, захын зай 2/2/3/1.5 см.
- Параметр: `reportTitle, periodLabel, reviewerTitle, reviewerName, compilerTitle, compilerName`
- `rows` (detail) мөрийн field: `no, taskText, dept, deadline, fulfillment, assigner, score`

## Тохиргоо (`application.properties`)
```
jasper.base-url=${JASPER_BASE_URL:http://172.16.0.101:8082}
jasper.client-key=${JASPER_CLIENT_KEY:dis-news-service}
jasper.client-secret=${JASPER_CLIENT_SECRET:}   # env-ээр өгнө
jasper.briefing-fulfillment-template-code=${JASPER_BRIEFING_FULFILLMENT_TEMPLATE_CODE:BRIEFING_FULFILLMENT}
```
