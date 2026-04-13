# Financial Dashboard

## Visão Geral

Sistema financeiro distribuído desenvolvido com foco em boas práticas de engenharia de software, arquitetura de microserviços e arquitetura orientada a eventos.

O objetivo é disponibilizar uma API backend capaz de gerenciar contas e transações financeiras, processar eventos em tempo real e fornecer dados agregados para uma dashboard financeira, simulando um cenário real do segmento financeiro.

Projeto desenvolvido para fins de estudo avançado e portfólio profissional, demonstrando domínio de Java, Spring, mensageria, containers, Kubernetes e Cloud.

---

## Arquitetura

O sistema é composto por cinco microserviços independentes, comunicando-se via HTTP (REST) e eventos assíncronos (Kafka).

```
[Cliente]
    |
    v
[API Gateway :8080]
    |
    +---> auth-service        :8081  (PostgreSQL)
    +---> account-service     :8082  (PostgreSQL)
    +---> transaction-service :8083  (PostgreSQL)
                  |
                  v
              [Kafka - topico: transactions]
                  |
                  v
          analytics-service   :8084  (MongoDB)
```

### Caracteristicas principais

- Arquitetura de microservicos
- Comunicacao sincrona via REST (Spring Cloud Gateway + OpenFeign)
- Comunicacao assincrona via Apache Kafka
- Banco de dados dedicado por servico
- API Gateway como ponto unico de entrada com validacao JWT centralizada

---

## Microservicos

### auth-service

Responsavel pela autenticacao e autorizacao.

- Cadastro de usuarios
- Login com emissao de JWT (HMAC SHA-256, expiracao de 1 hora)
- Validacao de tokens

Banco: PostgreSQL (`auth_db`, porta 5432)

---

### account-service

Responsavel pelo gerenciamento de contas financeiras.

- Criar contas (CHECKING / SAVINGS)
- Consultar saldo e detalhes
- Credito e debito com lock pessimista
- API interna protegida por `X-Internal-Key`

Banco: PostgreSQL (`account_db`, porta 5433)

---

### transaction-service

Responsavel pelo registro das transacoes financeiras.

- Criar transacoes (CREDIT, DEBIT, TRANSFER)
- Validacao de regras de negocio e ownership
- Publicacao de eventos no Kafka (`TransactionCreatedEvent`)

Banco: PostgreSQL (`transaction_db`, porta 5434)

Categorias disponiveis: `SALARY`, `GIFT`, `INVESTMENT_RETURN`, `OTHER_INCOME`, `FOOD`, `CLOTHING`, `TRANSPORT`, `HEALTH`, `ENTERTAINMENT`, `EDUCATION`, `BILLS`, `OTHER_EXPENSE`, `TRANSFER`

---

### analytics-service

Responsavel pelo processamento e agregacao de dados financeiros.

- Consome eventos do Kafka
- Agrega dados por periodo e categoria via MongoTemplate
- Fornece endpoints de consulta para dashboards

Banco: MongoDB (`analytics_db`, porta 27017)

---

### api-gateway

Ponto unico de entrada para o sistema.

- Roteamento de requisicoes para os microservicos
- Validacao centralizada de JWT
- Protecao de rotas publicas e privadas

---

## Endpoints REST

Todos os endpoints sao acessados via API Gateway (`http://localhost:8080`).

### Autenticacao (`/api/auth`) — publico

| Metodo | Endpoint             | Descricao           |
|--------|----------------------|---------------------|
| POST   | /api/auth/register   | Registrar usuario   |
| POST   | /api/auth/login      | Login, retorna JWT  |

### Contas (`/api/accounts`) — requer JWT

| Metodo | Endpoint                              | Descricao                  |
|--------|---------------------------------------|----------------------------|
| GET    | /api/accounts                         | Listar contas do usuario   |
| POST   | /api/accounts/create-account          | Criar conta                |
| GET    | /api/accounts/{accountNumber}         | Detalhes da conta          |
| DELETE | /api/accounts/{accountNumber}         | Deletar conta              |

### Contas — uso interno (requer `X-Internal-Key`)

| Metodo | Endpoint                                        | Descricao          |
|--------|-------------------------------------------------|--------------------|
| GET    | /api/accounts/internal/{accountNumber}          | Consultar conta    |
| POST   | /api/accounts/internal/{accountNumber}/credit   | Creditar valor     |
| POST   | /api/accounts/internal/{accountNumber}/debit    | Debitar valor      |

### Transacoes (`/api/transactions`) — requer JWT

| Metodo | Endpoint                                         | Descricao                    |
|--------|--------------------------------------------------|------------------------------|
| POST   | /api/transactions                                | Criar transacao              |
| GET    | /api/transactions                                | Listar transacoes do usuario |
| GET    | /api/transactions/account/{accountNumber}        | Listar por conta             |
| GET    | /api/transactions/{transactionId}                | Detalhes da transacao        |

### Analytics (`/api/analytics`) — requer JWT

| Metodo | Endpoint                          | Descricao                              |
|--------|-----------------------------------|----------------------------------------|
| GET    | /api/analytics/summary            | Total de credito, debito e saldo       |
| GET    | /api/analytics/by-category        | Agrupado por categoria e tipo          |
| GET    | /api/analytics/monthly            | Totais por mes (year + month)          |
| GET    | /api/analytics/top-expenses       | Top N maiores despesas (`?limit=5`)    |

---

## Kafka

- **Topico:** `transactions`
- **Producer:** transaction-service publica `TransactionCreatedEvent` ao criar uma transacao
- **Consumer:** analytics-service consome e persiste como `TransactionRecord` no MongoDB

Campos do evento: `transactionId`, `userId`, `accountNumber`, `targetAccountNumber`, `type`, `category`, `description`, `amount`, `createdAt`

---

## Tecnologias

### Backend

- Java 21
- Spring Boot 4.0.1
- Spring Security 7
- Spring Data JPA
- Spring Data MongoDB
- Spring Cloud Gateway 2025.1.0
- Spring Cloud OpenFeign

### Mensageria

- Apache Kafka 4.1.1
- Spring Kafka 4.0.1

### Banco de Dados

- PostgreSQL 15
- MongoDB 7

### Infraestrutura

- Docker
- Docker Compose
- Kubernetes (manifests para Minikube e producao)

### Cloud

- AWS EC2 — Free Tier (Ubuntu, Elastic IP fixo: 52.3.119.116)

### Testes

- JUnit 5
- Mockito
- AssertJ

---

## Testes

29 testes unitarios com Mockito e AssertJ.

| Servico             | Classe de Teste                  | Quantidade |
|---------------------|----------------------------------|------------|
| account-service     | AccountServiceTest               | 12         |
| transaction-service | TransactionOperationServiceTest  | 13         |
| analytics-service   | AnalyticsQueryServiceTest        | 6          |
| analytics-service   | TransactionEventConsumerTest     | 2          |

---

## Como Executar

### Variaveis de Ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
JWT_SECRET=seu_jwt_secret
DB_USERNAME=postgres
DB_PASSWORD=postgres
MONGO_USERNAME=mongo
MONGO_PASSWORD=mongo
INTERNAL_API_KEY=internal-secret-key-change-in-prod
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Desenvolvimento (Docker Compose)

```bash
# Primeira vez ou apos mudanca de codigo
docker compose up --build

# Sem mudanca de codigo
docker compose up
```

A API estara disponivel em `http://localhost:8080`.

### Kubernetes (Minikube)

```bash
# Iniciar o cluster
minikube start

# Build das imagens dentro do Docker do Minikube
.\k8s\build.ps1

# Deploy no cluster
.\k8s\deploy.ps1

# Obter a URL do gateway
minikube service api-gateway -n financial-dashboard --url
```

---

## CI/CD

O deploy em producao e automatizado via **GitHub Actions**.

- Push na branch `main` dispara o workflow `.github/workflows/deploy.yml`
- O workflow acessa a EC2 via SSH e executa `git pull` + `docker compose up -d --build`
- Os containers sobem automaticamente ao ligar a instancia (systemd service)

**Branches:**

| Branch    | Finalidade                        |
|-----------|-----------------------------------|
| `develop` | Desenvolvimento e testes          |
| `main`    | Producao — trigger do deploy auto |

---

## Fluxo de uma Transacao

1. `POST /api/auth/login` — obtencao do JWT
2. `POST /api/transactions` — validacao de ownership, saldo e regras de negocio
3. account-service credita/debita via API interna com lock pessimista
4. Evento publicado no topico `transactions` do Kafka
5. analytics-service consome o evento e persiste no MongoDB
6. `GET /api/analytics/**` — dados agregados disponiveis em tempo real

---

## Autor

Otavio De Carli Albuquerque

Projeto desenvolvido para demonstracao tecnica e portfolio profissional, com foco em backend Java e sistemas distribuidos no segmento financeiro.
