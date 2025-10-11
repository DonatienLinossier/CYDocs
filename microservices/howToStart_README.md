# Spring Boot Microservices with Podman & Podman Compose

This guide explains how to start with the creation of your Spring Boot microservices on Windows using Podman and Podman Compose.

---

## 1️⃣ Install Podman

* Download and install from:
  [Podman Desktop for Windows](https://podman-desktop.io/downloads/windows)
* Verify installation:

```powershell
podman --version
```

---

## 2️⃣ Install Podman Compose

* Ensure **pip** is installed
* Install Podman Compose:

```powershell
pip install podman-compose
```

* Verify:

```powershell
podman-compose version
```

## 3️⃣ Install Java 25 (Optional: No need as compilation can take place in the containers)

* Download and install from:
  [Oracle JDK 25 for Windows](https://www.oracle.com/java/technologies/downloads/#jdk25-windows)
* Update your `JAVA_HOME` environment variable
* Verify installation:

```powershell
java --version
```

---


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

## 5️⃣ Add microservice to podman-compose

1. Just paste :
  ```yaml
    {service_name}:
      build: microservices/{service_name}
      container_name: {service_name}
      depends_on:
        - consul
      environment:
        <<: *common-env
        SPRING_APPLICATION_NAME: {service_name}
      expose:
        - "8080"
      networks:
        - internal-net
  ``` 
  and replace {service_name} by the name of your service.

---

## 6️⃣ Launch the app

From the root of the project, run podman-compose up. You can add the following options :
  '-d' option to run the app detached (Advised)
  '--build' option rebuild microservice with changes (Advised)

---

## 7️⃣ Stop the app

Run podman-compose down.

---

## 8️⃣ Consult running services

Once the app is launch, you can check the health of the microservice on `http://localhost:8500/ui/dc1/services`.

