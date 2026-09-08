# API Parity Gaps vs qs-crm-be

Compared against `D:/Projects/qs-crm/qs-crm-be/internal/adapters/http/router.go` on 2026-09-08.

## Fixed in this pass

- `PATCH /customers/{id}` now maps to customer update. Java also keeps `PUT /customers/{id}` as an extra alias.
- `POST /public/leads` now maps to lead creation.
- `GET /public/captcha` now maps to captcha generation.
- File list/download/delete route coverage was expanded for activity, work order, work order comment, product, and campaign file paths where the generic file service can support it.
- Customer route surface now includes tracking stats, export template, import placeholder, portal status/grant/revoke/reset, and merge.
- ManualHub now accepts `POST /manualhub/documents/{id}/files` and exposes `POST /manualhub/documents/{id}/onlyoffice-forcesave`.
- Customer portal attachment/comment routes now exist and are wired to the generic file service and work-order-comment service.

## Still missing or partial

- Customer import is route-compatible but currently returns a parsed-count placeholder instead of reading Excel like Go.
- Customer portal routes exist, but ownership/customer scoping is not yet equivalent to Go; Java currently delegates directly to work order/comment/file services.
- Customer portal product item, warranty, and service request read endpoints are still stubbed in `CustomerPortalService`.
- Portal grant/revoke/reset routes are route-compatible placeholders; they do not yet create/update linked auth users like Go.
- ManualHub `onlyoffice-forcesave` is route-compatible but returns current `content.file_url`; it does not yet track active OnlyOffice session keys or call CommandService.
- Permission enforcement names differ in places. Java uses a mix of current authorities and unguarded controllers; Go router has per-route permission middleware.
- DTO-level parity was not fully verified for every endpoint. Some Java payload names are already aligned with `@JsonProperty`, but a full field-by-field audit remains needed.

## Notes

- Java has extra endpoints not in Go or named differently, such as `PUT /customers/{id}` and some generic file routes.
- Build/tests pass after the route alias changes.
