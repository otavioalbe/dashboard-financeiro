# Financial Dashboard – MVP

## 📌 Visão Geral

Este projeto é um **sistema financeiro distribuído**, desenvolvido com foco em **boas práticas de engenharia de software**, **arquitetura de microserviços** e **arquitetura orientada a eventos**.

O objetivo é disponibilizar uma **API backend** capaz de gerenciar contas e transações financeiras, processar eventos em tempo real e fornecer dados agregados para uma **dashboard financeira**, simulando um cenário real do **segmento financeiro**.

O projeto foi pensado para fins de **estudo avançado e portfólio profissional**, demonstrando domínio de Java, Spring, mensageria, containers, Kubernetes e Cloud.

---

## 🎯 Escopo do MVP

O MVP contempla:

* Autenticação de usuários com JWT
* Gerenciamento de contas financeiras
* Registro de transações (crédito e débito)
* Processamento de eventos financeiros via Kafka
* Geração de dados analíticos agregados
* Exposição de APIs REST seguras

O frontend (dashboard) **não faz parte do escopo do MVP**, sendo opcional e desacoplado do backend.

---

## 🧩 Arquitetura Geral

O sistema é composto por **microserviços independentes**, comunicando-se via HTTP e eventos assíncronos.

### Principais características:

* Arquitetura de microserviços
* Comunicação síncrona via REST
* Comunicação assíncrona via Kafka
* Banco de dados por serviço
* API Gateway como ponto único de entrada

---

## 🧱 Microserviços

### 🔐 auth-service

Responsável pela autenticação e autorização.

**Responsabilidades:**

* Cadastro de usuários
* Login
* Emissão e validação de JWT
* Controle de permissões

**Banco:** SQL (PostgreSQL)

---

### 🏦 account-service

Responsável pelo gerenciamento de contas financeiras.

**Responsabilidades:**

* Criar contas
* Consultar saldo
* Atualizar saldo

**Banco:** SQL (PostgreSQL)

---

### 💸 transaction-service

Responsável pelo registro das transações financeiras.

**Responsabilidades:**

* Criar transações (crédito/débito)
* Validação de regras de negócio
* Publicação de eventos no Kafka

**Banco:** SQL (PostgreSQL)

**Eventos publicados:**

* TransactionCreatedEvent

---

### 📊 analytics-service

Responsável pelo processamento e agregação de dados financeiros.

**Responsabilidades:**

* Consumo de eventos do Kafka
* Agregação de dados por período e categoria
* Fornecer dados para dashboards

**Banco:** NoSQL (MongoDB)

---

### 🌐 api-gateway

Ponto único de entrada para o sistema.

**Responsabilidades:**

* Roteamento de requisições
* Validação de JWT
* Centralização de segurança

---

## ⚙️ Tecnologias Utilizadas

### Backend

* Java 17+
* Spring Boot
* Spring Security
* Spring Data JPA
* Spring Cloud Gateway
* Spring Cloud OpenFeign

### Mensageria

* Apache Kafka

### Banco de Dados

* PostgreSQL
* MongoDB

### Infraestrutura

* Docker
* Docker Compose
* Kubernetes

### Cloud

* Microsoft Azure (AKS / Containers)

### Testes

* JUnit 5
* Mockito
* Testcontainers

---

## 🧪 Qualidade e Boas Práticas

* Clean Code
* Separação de responsabilidades
* DTOs e validações
* Tratamento centralizado de exceções
* Logs estruturados
* Testes unitários e de integração

---

## 📐 Diagramas

### Diagrama de Contexto (C4 – Nível 1)

```text
[Usuário]
    |
    v
[API Gateway]
    |
    +--> auth-service
    +--> account-service
    +--> transaction-service
                 |
                 v
               [Kafka]
                 |
                 v
           analytics-service
```

---

### Diagrama de Containers (C4 – Nível 2)

```text
┌──────────────────┐
│   API Gateway    │
└──────────────────┘
        │
        ▼
┌──────────────────┐     ┌──────────────────┐
│  auth-service    │     │ account-service  │
└──────────────────┘     └──────────────────┘
        │                       │
        ▼                       ▼
┌──────────────────┐     ┌──────────────────┐
│ transaction-svc  │ --> │     Kafka        │
└──────────────────┘     └──────────────────┘
                                │
                                ▼
                        ┌──────────────────┐
                        │ analytics-service│
                        └──────────────────┘
```

---

## 🚀 Próximos Passos (Roadmap)

* Implementação dos microserviços
* Configuração de mensageria Kafka
* Criação de testes automatizados
* Containerização com Docker
* Deploy em Kubernetes
* Publicação em Azure
* (Opcional) Desenvolvimento da dashboard frontend

---

## 👨‍💻 Autor

Projeto desenvolvido para fins educacionais e demonstração técnica, com foco em vagas de desenvolvimento backend e sistemas distribuídos no segmento financeiro.
