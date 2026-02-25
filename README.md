# 📌 Proyecto DSII – Sistema de Gestión y Monitoreo de Dispositivos

## 📖 Descripción del Proyecto

Este proyecto consiste en el desarrollo de un sistema backend en Spring Boot para la gestión y monitoreo de dispositivos dentro de una red.  

El sistema permite:

- Registrar usuarios
- Registrar dispositivos
- Controlar el estado de autorización de dispositivos
- Registrar eventos generados por los dispositivos
- Gestionar métricas de calidad de servicio (QoS)

---

## 🎯 Objetivo

Desarrollar una aplicación estructurada bajo arquitectura en capas que permita aplicar conceptos de:

- Programación orientada a objetos
- Modelo entidad-relación
- API REST
- Control de acceso por roles
- Trabajo colaborativo con Git y GitHub

---

## 🏗️ Arquitectura del Proyecto

El proyecto está organizado en las siguientes capas:

- **modelo** → Entidades del sistema
- **controller** → Endpoints REST
- **servicios** → Lógica de negocio
- **repositorio** → Acceso a base de datos
- **dto** → Transferencia de datos
- **config** → Configuración de seguridad

---

## 🧩 Modelo del Sistema

### Entidades principales:

- Usuario
- Dispositivo
- Evento
- QoSMetrics

### Enumeraciones:

- EstadoAutorizacion
- Protocolo

---

## 🔐 Funcionalidades Principales

- Registro y autenticación de usuarios
- Asociación de dispositivos a usuarios
- Control de autorización de dispositivos
- Registro de eventos por dispositivo
- Registro y consulta de métricas QoS
- Restricción de acceso por roles

---

## 📊 Diagrama de Clases

<img width="826" height="460" alt="image" src="https://github.com/user-attachments/assets/4da91c28-638f-4227-80d0-2a739d444810" />


---

## 🛠️ Tecnologías Utilizadas

- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- Maven
- Git
- GitHub

---

## 👥 Trabajo Colaborativo

El proyecto fue desarrollado en equipo utilizando control de versiones con Git.

Cada integrante realizó commits individuales evidenciando su participación en el desarrollo del sistema.

---

## 🚀 Cómo Ejecutar el Proyecto

1. Clonar el repositorio: git clone https://github.com/lamaravilla18/proyecto-de-clase-dsII/tree/main

2. Abrir el proyecto en un IDE (IntelliJ / VS Code)

3. Ejecutar la clase principal:  SecurityDemoApplication.java

4. Acceder a la API desde: http://localhost:8080

---

## 📌 Estado del Proyecto

Proyecto en desarrollo académico con funcionalidades principales implementadas.


