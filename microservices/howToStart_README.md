# Spring Boot Microservices with Podman & Podman Compose

This guide explains how to create, build, containerize, and run Spring Boot microservices on Windows using Podman and Podman Compose.

---

## 1️⃣ Install Java 25

* Download and install from:
  [Oracle JDK 25 for Windows](https://www.oracle.com/java/technologies/downloads/#jdk25-windows)
* Update your `JAVA_HOME` environment variable
* Verify installation:

```powershell
java --version
```

---

## 2️⃣ Install Podman

* Download and install from:
  [Podman Desktop for Windows](https://podman-desktop.io/downloads/windows)
* Verify installation:

```powershell
podman --version
```

---

## 3️⃣ Install Podman Compose

* Ensure **pip** is installed
* Install Podman Compose:

```powershell
pip install podman-compose
```

* Verify:

```powershell
podman-compose version
```

---

## 4️⃣ Create a Spring Boot Microservice

1. Copy the `MicroServiceDemo/` folder
2. Rename it with your microservice name
3. Navigate to `{MicroServiceName}/demo/`
4. Run and test locally:

```powershell
./mvnw spring-boot:run
```

---

## 5️⃣ Build the Container Image

1. Move to `{MicroServiceName}/demo/`
2. Package the app:

```powershell
./mvnw clean package
```

3. Move to `{MicroServiceName}/`
4. Build the Podman image:

```powershell
podman build . -t {nameForUrImage}
```

---

## 6️⃣ Run the Container

```powershell
podman run -d -p {port}:8080 {nameForUrImage}
```

* Access your app at: `http://localhost:{port}/`

---

## 7️⃣ Configure Podman Compose

Add your service to `podman-compose.yml`:

```yaml
  {nameForUrService}:
    image: {nameForUrImage}
    container_name: {nameForUrContainer}
    ports:
      - "{port}:8080"
```

> Replace placeholders (`{nameForUrService}`, `{nameForUrImage}`, `{nameForUrContainer}`, `{port}`) with your actual names and ports.

---

## 8️⃣ Run Multiple Microservices

Example `podman-compose.yml` with **two Spring Boot services**:

```yaml
version: "3.9"

services:
  user_management_service:
    image: user_management
    container_name: user_management
    ports:
      - "8081:8080"

  document_management_service:
    image: document_management
    container_name: document_management
    ports:
      - "8082:8080"

```

* Start all services:

```powershell
podman-compose up -d
```

* Verify:

```powershell
podman ps
```

* Access endpoints:

| Service             | URL                                            |
| ------------------- | ---------------------------------------------- |
| user_management     | [http://localhost:8081](http://localhost:8081) |
| document_management | [http://localhost:8082](http://localhost:8082) |

* Stop all services:

```powershell
podman-compose down
```

---
