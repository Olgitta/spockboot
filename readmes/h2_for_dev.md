Yes, for **local development**, it's common and highly recommended to use an **in-memory database like H2**, especially when you don’t want to depend on a running MySQL instance.

---

## ✅ Why H2?

* Zero setup (embedded in the app)
* Fast and lightweight
* Can mimic MySQL behavior (with compatibility mode)
* Works well with Spring Boot

---

## 🔧 Step-by-Step Setup (Switching to H2 for Dev)

### 1. **Add H2 dependency**

If you're using Maven:

```xml
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>runtime</scope>
</dependency>
```

---

### 2. **Configure H2 in `application-dev.yml` (or `.properties`)**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:spockboot;DB_CLOSE_DELAY=-1;MODE=MySQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.H2Dialect
  h2:
    console:
      enabled: true
      path: /h2-console
```

> ☝️ The `MODE=MySQL` flag helps H2 behave similarly to MySQL (e.g., `LIMIT`, case sensitivity, etc.)

---

### 3. **Enable Spring Profile**

Make sure you start the app with the `dev` profile:

```bash
-Dspring.profiles.active=dev
```

Or in `application.yml`:

```yaml
spring:
  profiles:
    active: dev
```

---

### 4. **Access H2 Console**

Once your app is running, go to:
📍 `http://localhost:8080/h2-console`
Set JDBC URL to:

```
jdbc:h2:mem:spockboot
```

---

## ✅ Tips

* Use `ddl-auto: update` for dev, but never in production.
* You can write SQL init scripts under `src/main/resources` (`schema.sql`, `data.sql`).
* Add test-specific data for faster local development.

Yes, Spring Boot **automatically runs `schema.sql` and `data.sql`** on startup **if they are located in `src/main/resources`** — but with some conditions and caveats:

---

## ✅ Default Behavior

If you’re using an **embedded database** (like H2, HSQLDB, or Derby):

* `schema.sql` is run **first** to create the schema.
* `data.sql` is run **after** to insert data.

This happens **automatically** on application startup.

---

## 🔧 Required setup

1. File location:
   Place these files in `src/main/resources`:

    * `schema.sql` – contains DDL (e.g., `CREATE TABLE ...`)
    * `data.sql` – contains DML (e.g., `INSERT INTO ...`)

2. Optional config (if needed):

   ```yaml
   spring:
     sql:
       init:
         mode: always
         platform: h2   # Optional, can help with multiple DBs
   ```

---

## ⚠️ When It Does *Not* Run Automatically

* If you're using a **non-embedded database** (like MySQL or Postgres), Spring Boot disables automatic execution **by default** unless you explicitly configure:

  ```yaml
  spring:
    sql:
      init:
        mode: always
  ```

* If `spring.jpa.hibernate.ddl-auto` is set to `create`, `update`, etc., it may conflict or override `schema.sql`.

---

## ✅ Recommendation

For H2 (or other embedded DBs in dev/test):

* Add `schema.sql` and/or `data.sql`
* Add this to be safe:

```yaml
spring:
  sql:
    init:
      mode: always
```

Here's how to automatically initialize a database in Spring Boot using `schema.sql` and `data.sql`. This works great with H2 (or any JDBC-compatible DB like MySQL, Postgres, etc.).

---

### ✅ Step 1: Place the SQL files

Create these files in `src/main/resources/`:

#### `schema.sql`

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guid BINARY(16) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### `data.sql`

```sql
INSERT INTO users (guid, email, name, password_hash)
VALUES 
    (UNHEX(REPLACE(UUID(), '-', '')), 'alice@example.com', 'Alice', 'hashed-password-1'),
    (UNHEX(REPLACE(UUID(), '-', '')), 'bob@example.com', 'Bob', 'hashed-password-2');
```

---

### ✅ Step 2: Spring Boot will auto-run them

By default, Spring Boot runs these files **in alphabetical order** at startup if:

* You’re using an **embedded database** (like H2, HSQL, Derby), or
* You explicitly configure it to do so.

---

### ✅ Step 3: Configure for H2 (example)

In your `application.yml` (or `.properties`):

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: none # disables Hibernate schema generation
    show-sql: true
```

Access the H2 console at:
[http://localhost:8080/h2-console](http://localhost:8080/h2-console)
Use JDBC URL: `jdbc:h2:mem:testdb`

---

### ⚠️ Notes

* **For MySQL**, you'll also need to add the relevant `spring.datasource.*` entries for URL, username, and password.
* Disable Hibernate DDL auto-generation (`ddl-auto: none`) to avoid schema conflicts with `schema.sql`.

---
