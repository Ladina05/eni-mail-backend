# ENI Mail — Backend

Backend REST de la plateforme de messagerie interne ENI, construit avec Java 21, Spring Boot 3.5.16, Spring Security, JWT et PostgreSQL.

## 1. Prérequis

- JDK 21
- Maven 3.6.3 ou supérieur
- PostgreSQL
- IntelliJ IDEA (recommandé)

## 2. Créer la base PostgreSQL

Dans pgAdmin 4, créer une base nommée `eni_mail`, encodage UTF8.

## 3. Configuration

Copier `.env.example` vers `.env` pour conserver tes paramètres de référence. Spring Boot ne charge pas automatiquement ce fichier `.env` sans dépendance supplémentaire : définis les variables dans IntelliJ (Run > Edit Configurations > Environment variables) ou adapte `application.yml`.

Variables indispensables :
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET` (clé Base64 secrète d'au moins 32 octets)
- `APP_ADMIN_EMAIL`
- `APP_ADMIN_PASSWORD`

Origines frontend autorisées par défaut : `http://localhost:5173` et `http://localhost:5174`. En déploiement, définir `FRONTEND_URLS` avec la liste exacte des origines séparées par des virgules (par exemple `https://mail.eni.mg`). `FRONTEND_URL` reste accepté pour une seule origine.

Ne publie jamais les vrais mots de passe ni la clé JWT dans Git.

## 4. Lancer depuis le terminal

À la racine du projet :

```powershell
mvn clean spring-boot:run
```

Compiler :

```powershell
mvn clean package
```

Lancer le JAR :

```powershell
java -jar target/eni-mail-1.0.0.jar
```

## 5. URLs

- API : http://localhost:8080/api
- Swagger UI : http://localhost:8080/swagger-ui.html
- OpenAPI JSON : http://localhost:8080/v3/api-docs
- Health : http://localhost:8080/actuator/health
- WebSocket SockJS : http://localhost:8080/ws

## 6. Authentification

`POST /api/auth/login`

```json
{
  "email": "admin@eni.mg",
  "password": "ChangeThisAdminPassword!"
}
```

Ajouter ensuite l'en-tête :

`Authorization: Bearer <token>`

Le compte administrateur initial est créé uniquement si `APP_ADMIN_EMAIL` et `APP_ADMIN_PASSWORD` sont définis au premier démarrage et si cet e-mail n'existe pas déjà.

## 7. Routes principales

### Authentification
- `POST /api/auth/login`
- `GET /api/auth/me`

### Utilisateurs (création et activation réservées à ADMIN)
- `GET /api/users`
- `GET /api/users?q=nom`
- `POST /api/users`
- `PATCH /api/users/{id}/active?active=true`

### Départements et promotions
- `GET /api/departments`
- `POST /api/departments` (ADMIN)
- `DELETE /api/departments/{id}` (ADMIN)
- `GET /api/promotions`
- `POST /api/promotions` (ADMIN)
- `DELETE /api/promotions/{id}` (ADMIN)

### Groupes
- `GET /api/groups`
- `POST /api/groups`
- `POST /api/groups/{groupId}/members/{userId}`
- `DELETE /api/groups/{groupId}/members/{userId}`
- `GET /api/groups/{groupId}/members`

### Messages
- `POST /api/messages`
- `POST /api/messages/drafts`
- `GET /api/messages/inbox`
- `GET /api/messages/sent`
- `GET /api/messages/drafts`\n- `POST /api/messages/drafts/{id}/send`\n- `DELETE /api/messages/drafts/{id}`\n- `GET /api/messages/archive`\n- `GET /api/messages/trash`
- `GET /api/messages/{id}`
- `PATCH /api/messages/{id}/read`
- `PATCH /api/messages/{id}/unread`
- `PATCH /api/messages/{id}/star`
- `PATCH /api/messages/{id}/archive`
- `PATCH /api/messages/{id}/restore`
- `PATCH /api/messages/{id}/delete`

Exemple d'envoi :

```json
{
  "subject": "Réunion pédagogique",
  "body": "La réunion aura lieu vendredi à 9h.",
  "to": ["etudiant@eni.mg"],
  "cc": ["enseignant@eni.mg"],
  "bcc": []
}
```

Les adresses des destinataires doivent correspondre à des comptes existants dans la base.

### Pièces jointes
- `POST /api/messages/{messageId}/attachments` (multipart, champ `file`)
- `GET /api/messages/{messageId}/attachments/{attachmentId}`

Dans cette première version, les fichiers sont ajoutés aux brouillons uniquement. Les types autorisés sont PDF, DOCX, XLSX, PNG, JPEG et TXT ; taille maximale 10 Mo par fichier.

### Notifications
- `GET /api/notifications`
- `GET /api/notifications/unread-count`
- `PATCH /api/notifications/{id}/read`

## 8. Limites fonctionnelles à connaître

Cette base fournit les fondations du backend et les principaux endpoints. Avant une mise en production, il faudra compléter notamment :
- mise à jour et envoi des brouillons avec leurs pièces jointes ;
- dossiers Corbeille et Archives avec listes dédiées ;
- règles complètes de visibilité CC/CCI ;
- pagination, tri et recherche avancée ;
- contrôle des permissions des groupes ;
- validation institutionnelle des domaines d'adresses ;
- tests d'intégration et audit de sécurité ;
- stockage de fichiers hors du répertoire public et analyse antivirus ;
- configuration WebSocket avec authentification JWT au niveau du handshake ;
- migration de schéma avec Flyway ou Liquibase (au lieu de `ddl-auto: update`).

Les réponses API ne doivent pas être considérées comme une garantie de conformité à une politique de sécurité institutionnelle sans revue dédiée.
