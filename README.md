# ShopHub E-Commerce

ShopHub is a Spring Boot e-commerce application for browsing a product catalog, managing a shopping bag and wishlist, and administering products. It includes a responsive Thymeleaf storefront, account registration and sign-in, and a REST API for catalog and wishlist operations.

![Java 25](https://img.shields.io/badge/Java-25-orange)
![Spring Boot 4.1.2](https://img.shields.io/badge/Spring%20Boot-4.1.2-brightgreen)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue)
[![Java CI with Maven](https://github.com/hendisantika/spring-boot-ecommerce/actions/workflows/maven.yml/badge.svg)](https://github.com/hendisantika/spring-boot-ecommerce/actions/workflows/maven.yml)

## Application screenshots

The following screenshots show the running ShopHub application.

### Storefront

![ShopHub home page](working_images/Screenshot%202026-11-01%20141305.png)

### Product collection

![ShopHub product collection](working_images/Screenshot%202026-11-01%20141327.png)

### Shopping bag

![ShopHub shopping bag](working_images/Screenshot%202026-11-01%20141240.png)

### Sign in

![ShopHub sign-in page](working_images/Screenshot%202026-11-01%20141138.png)

## Features

- Browse, search, and filter products by brand, category, and price.
- View product details, prices, discounts, and availability.
- Add products to a session-based shopping bag and update quantities.
- Save products to a wishlist.
- Register a customer account and sign in using Spring Security.
- Manage catalog products from the administrator area.
- Use catalog and wishlist REST endpoints.
- Start the MySQL database and phpMyAdmin with Docker Compose.
- Automatically seed sample users and a small product catalog on first startup.

## Technology stack

- **Language:** Java 25
- **Application framework:** Spring Boot 4.1.2
- **Web UI:** Thymeleaf, HTML, CSS, and JavaScript
- **Security:** Spring Security with BCrypt password encoding
- **Persistence:** Spring Data JPA and Hibernate
- **Database:** MySQL
- **Build:** Maven
- **Local services:** Docker Compose
- **Tests:** JUnit 5, Spring Boot Test, and Testcontainers

## Requirements

- Java Development Kit (JDK) 25
- Docker Desktop or Docker Engine with Docker Compose
- Git

The project includes the Maven Wrapper, so a separate Maven installation is not required.

## Getting started

1. Clone the repository and enter the project directory:

   ```bash
   git clone https://github.com/hendisantika/spring-boot-ecommerce.git
   cd spring-boot-ecommerce
   ```

2. Start the database and phpMyAdmin:

   ```bash
   docker compose up -d
   ```

   MySQL is published on port `3308`; phpMyAdmin is available at [http://localhost:8082](http://localhost:8082).

3. Run the application.

   **Windows PowerShell:**

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   **macOS/Linux:**

   ```bash
   ./mvnw spring-boot:run
   ```

4. Open [http://localhost:8080](http://localhost:8080). Sign in or create an account at `/register`.

On its first run, the application creates sample accounts and seeds five products with their categories and local product images. The storefront requires sign-in; visitors are redirected to the sign-in page.

## Demo accounts

The application seeds the following accounts if they do not already exist:

| Role | Username | Password |
|---|---|---|
| Administrator | `admin` | `admin123` |
| Customer | `user` | `user123` |

These are **development-only credentials**. Change or remove them before deploying the application or making it accessible to other users.

## Configuration

The default database connection is configured in `src/main/resources/application.properties`; the local MySQL service and its database settings are defined in `compose.yml`. The database is exposed on host port `3308` to avoid the default MySQL port. Update both configurations together if you change the database port or credentials.

The Compose file uses a named Docker volume to persist MySQL data between restarts. To stop the services without deleting the data:

```bash
docker compose down
```

For a complete local reset, remove the Compose-managed database volume only after confirming that its data is no longer needed.

## REST API

The catalog API is rooted at `/api`:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/brands` | List catalog brands |
| `GET` | `/api/brands/{brand}/products` | List products for a brand |
| `GET` | `/api/products` | List products; supports `search`, `brand`, `categoryId`, and `maxPrice` filters |
| `GET` | `/api/products/{id}` | Get a product |
| `POST` | `/api/products` | Create a product |
| `PUT` | `/api/products/{id}` | Update a product |
| `DELETE` | `/api/products/{id}` | Delete a product |

The session wishlist API is rooted at `/api/wishlist`:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/wishlist` | Get the current session's wishlist |
| `POST` | `/api/wishlist/items` | Add an item using a JSON `productId` |
| `DELETE` | `/api/wishlist/items/{itemId}` | Remove a wishlist item |
| `DELETE` | `/api/wishlist` | Clear the wishlist |

Catalog `GET` endpoints are public. Creating, updating, and deleting products require an administrator account. Wishlist endpoints require an authenticated session. Include a valid CSRF token with state-changing requests made through the session-based security setup.

Product JSON uses the fields `name`, `description`, `quantity`, `price`, `brand`, `discount`, `categoryIds`, and `image`. The `image` field accepts Base64-encoded image bytes; a `data:image/...;base64,` prefix is also supported.

## Running tests

Run the test suite with:

**Windows PowerShell:**

```powershell
.\mvnw.cmd test
```

**macOS/Linux:**

```bash
./mvnw test
```

The database integration tests use Testcontainers to start MySQL, so Docker must be running.

## Project layout

```text
src/
├── main/
│   ├── java/com/hendisantika/   # Application, controllers, services, repositories, and models
│   └── resources/
│       ├── static/              # CSS, JavaScript, and product images
│       ├── templates/           # Thymeleaf pages and fragments
│       └── application.properties
└── test/                        # Unit and integration tests
working_images/                  # Screenshots of the running application
compose.yml                      # Local MySQL and phpMyAdmin services
```

## License

No license file is currently included in this repository. Add a license before redistributing the project.
