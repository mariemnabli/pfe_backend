# API Examples

Base URL: `http://localhost:8080`

Use header on secured endpoints:

```http
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

## 1. Create a customer group

`POST /api/customer-groups`

Roles: `VENTE`, `DSI`

```json
{
  "name": "Entreprise Acme",
  "groupType": "ENTERPRISE",
  "status": "ACTIVE"
}
```

## 2. Add a customer to a group

`POST /api/customer-groups/1/members/12?memberRole=OWNER&primaryMember=true`

Roles: `VENTE`, `DSI`

Body: empty

## 3. Create an individual customer

`POST /api/clients/json`

Roles: `VENTE`

```json
{
  "nom": "Ali",
  "prenom": "Mansour",
  "telephone": "21620111222",
  "email": "ali.mansour@example.com",
  "adresse": "10 Rue de Marseille",
  "ville": "Tunis",
  "documentType": 1,
  "cinNumber": "12345678"
}
```

## 4. Create a customer already attached to a group

`POST /api/clients/json`

Roles: `VENTE`

```json
{
  "nom": "Sarra",
  "prenom": "Ben Salem",
  "telephone": "21653111222",
  "email": "sarra.bensalem@example.com",
  "adresse": "Immeuble Lac 2",
  "ville": "Tunis",
  "documentType": 2,
  "passportNumber": "XK009911",
  "customerGroupId": 1
}
```

## 5. Create an individual contract

`POST /api/contrats`

Roles: `VENTE`

```json
{
  "contractType": "INDIVIDUAL",
  "holderType": "CUSTOMER",
  "clientId": 12,
  "offreId": 3,
  "dateDebut": "2026-04-25",
  "dateFin": "2027-04-24"
}
```

## 6. Create an enterprise contract

`POST /api/contrats`

Roles: `VENTE`

```json
{
  "contractType": "ENTERPRISE",
  "holderType": "CUSTOMER_GROUP",
  "customerGroupId": 1,
  "offreId": 4,
  "dateDebut": "2026-04-25",
  "dateFin": "2027-04-24"
}
```

## 7. Create a promotion

`POST /api/promotions`

Roles: `METIER`

```json
{
  "nomPromotion": "Remise entreprise 15%",
  "typeReduction": "POURCENTAGE",
  "valeurReduction": 15,
  "dateDebut": "2026-05-01",
  "dateFin": "2026-12-31",
  "regleEligibilite": "Applicable aux grands comptes",
  "ancienneteMinimale": 3,
  "createurId": 2
}
```

## 8. Assign a promotion to one customer

`POST /api/promotions/5/assignments`

Roles: `VENTE`

```json
{
  "targetType": "CUSTOMER",
  "targetCustomerId": 12,
  "assignedById": 7,
  "assignmentMode": "MANUAL",
  "effectiveStartDate": "2026-05-01",
  "effectiveEndDate": "2026-12-31",
  "inheritedToMembers": false
}
```

The assignment is created with `status = SUSPENDED` and `validationStatus = PENDING`. It becomes `ACTIVE` only after an `EXPLOIT` user validates it.

## 9. Assign a promotion to a customer group

`POST /api/promotions/5/assignments`

Roles: `VENTE`

```json
{
  "targetType": "CUSTOMER_GROUP",
  "targetGroupId": 1,
  "assignedById": 7,
  "assignmentMode": "MANUAL",
  "effectiveStartDate": "2026-05-01",
  "effectiveEndDate": "2026-12-31",
  "inheritedToMembers": true
}
```

## 10. Assign a promotion to a contract

`POST /api/promotions/5/assignments`

Roles: `VENTE`

```json
{
  "targetType": "CONTRACT",
  "targetContractId": 9,
  "assignedById": 7,
  "assignmentMode": "MANUAL",
  "effectiveStartDate": "2026-05-01",
  "effectiveEndDate": "2026-12-31",
  "inheritedToMembers": false
}
```

## 11. Validate a promotion assignment

`PUT /api/promotions/5/assignments/14/valider?validateurId=3`

Roles: `EXPLOIT`

Body: empty

## 12. Reject a promotion assignment

`PUT /api/promotions/5/assignments/14/rejeter?validateurId=3`

Roles: `EXPLOIT`

Body: empty

## 13. Validate and activate a promotion

## 14. List all directory numbers

`GET /api/directory-numbers?page=0&size=10`

Roles: `VENTE`, `EXPLOIT`, `DSI`

## 12.bis Create a directory number

`POST /api/directory-numbers`

Roles: `VENTE`, `EXPLOIT`, `DSI`

Body example:

```json
{
  "numero": 216531234567,
  "status": "ACTIF",
  "contratId": 12
}
```

Rules:

- `numero` is mandatory
- if no contract is provided, the default status is `LIBRE`
- if a contract is provided and `status` is omitted, the default status is `ACTIF`
- `ACTIF` and `DESACTIVE` require `contratId` or `contractId`

## 13. List directory numbers by status

`GET /api/directory-numbers?status=ACTIF&page=0&size=10`

Accepted values for `status`: `LIBRE`, `ACTIF`, `DESACTIVE`

## 14. Import directory numbers from CSV

`POST /api/directory-numbers/upload-csv`

Roles: `VENTE`, `EXPLOIT`, `DSI`

Expected CSV header:

```text
numero
216201234567
216531234567
```

Imported numbers are created with status `LIBRE`.

Optional columns are also supported:

```text
numero,status,dateActivation,dateDesactivation,contratId,contractId
216201234567,LIBRE,,,,
216531234567,ACTIF,2026-05-01,,12,
216541234567,DESACTIVE,2026-04-01,2026-05-10,,CTR-000012
```

Rules:

- `numero` is mandatory
- if `contratId` or `contractId` is provided, the imported directory number is linked to that contract
- if `status` is omitted, it defaults to `LIBRE` without contract, or `ACTIF` with contract
- `ACTIF` and `DESACTIVE` require a linked contract

`PUT /api/promotions/5/valider?validateurId=3`

Roles: `EXPLOIT`

Body: empty

Then:

`PUT /api/promotions/5/activer`

Roles: `EXPLOIT`

Body: empty

## 12. Check promotions applicable to a customer

`GET /api/promotions/customer/12`

Roles: `VENTE`, `EXPLOIT`

## 13. Check promotions applicable to a group

`GET /api/promotions/group/1`

Roles: `VENTE`, `EXPLOIT`

## 14. Subscribe a contract to a promotion

`POST /api/souscriptions/contrat/9/promotion/5`

Roles: `VENTE`

Body: empty

## 15. Eligibility check without side effects

`GET /api/souscriptions/contrat/9/promotion/5/eligibilite`

Roles: `VENTE`

## Postman tips

- Define `{{baseUrl}}` as `http://localhost:8080`
- Define `{{token}}` with a JWT obtained from `/api/auth/login`
- Add `Authorization: Bearer {{token}}` on secured requests
- Use one login per role when testing `DSI`, `METIER`, `EXPLOIT`, `VENTE`

## Current authenticated user

`GET /api/users/me`

Roles: authenticated user

Headers:

- `Authorization: Bearer {{token}}`
