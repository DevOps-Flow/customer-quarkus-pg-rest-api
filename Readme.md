# Customer Quarkus PG REST API

API REST de clientes com Quarkus 3.x, Java 21 (LTS), PostgreSQL, Liquibase, SOLID, Clean Architecture, DTOs, logs JSON, build nativo com GraalVM e manifestos Kubernetes.


## Integração contínua (GitHub Actions)

O workflow em `.github/workflows/maven.yml` neste repositório garante que o JDK seja configurado com `actions/setup-java`, e — para runners self-hosted — baixa a versão mais recente do Apache Maven diretamente dos mirrors oficiais e adiciona seu `bin` ao PATH antes de executar o build.

Por que isso foi feito:
- Em runners self-hosted (por exemplo ARC-System no Kubernetes) o Maven pode não estar instalado por padrão. Baixar a versão oficial garante consistência.

Alternativas recomendadas:
- Usar Maven Wrapper (`mvnw`) — recomendo adicioná-lo ao repositório para builds reproduzíveis sem depender do ambiente do runner.
- Executar o job dentro de um container que já tenha Maven e Java (por exemplo `maven:3.9.6-jdk-21`) — essa é a opção mais previsível em ambientes com Docker.

***

````
- **12-Factor**: config via env (DB_JDBC_URL, DB_USERNAME, DB_PASSWORD, PORT).
- **Java 21 LTS + GraalVM**: build nativo opcional para startup e footprint baixos.
- **Quarkus + Hibernate ORM (Panache)**: persistência limpa.
- **Liquibase**: versionamento de schema.
- **SOLID/Clean Architecture**: Resource → Service Interface → Service Impl → Repository → Domain.
- **DTOs**: validação com Bean Validation; nunca expor entidade diretamente.
- **Tratamento de exceções**: mappers centralizados, respostas JSON.
- **Logs JSON**: prontos para observabilidade.
- **Kubernetes**: namespace, service account, RBAC mínimo, ConfigMap/Secret, Deployment, Service e Ingress.

## Requisitos

- Java 21
- Maven 3.9+
- Docker (para imagem nativa)
- Kubernetes com Ingress (opcional)

## Build & Execução

### Desenvolvimento

```bash
# Modo desenvolvimento (hot reload)
mvn quarkus:dev

# Ou usando variáveis de ambiente
export $(cat .env | xargs)
mvn quarkus:dev
```

### Build JVM

```bash
# Build padrão JVM
mvn clean package

# Executar JAR
java -jar target/customer-quarkus-pg-rest-api-1.0.0.jar
```

### Build Nativo (GraalVM)

```bash
# Build nativo (requer GraalVM ou Docker)
mvn clean package -Pnative -DskipTests

# Executar binário nativo
./target/customer-quarkus-pg-rest-api-1.0.0-runner
```

## Docker

### Imagem JVM

```bash
# Criar Dockerfile padrão (não incluído)
docker build -t customer-api .
docker run -p 8080:8080 customer-api
```

### Imagem Nativa

```bash
# 1. Build nativo local primeiro
mvn clean package -Pnative -DskipTests

# 2. Criar imagem Docker nativa
docker build -f Dockerfile.native -t customer-api-native .

# 3. Executar container nativo
docker run -p 8080:8080 customer-api-native

# Com variáveis de ambiente
docker run -p 8080:8080 \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=senha123 \
  -e DB_JDBC_URL=jdbc:postgresql://host.docker.internal:5432/customer_db \
  customer-api-native
```

**Vantagens da imagem nativa:**

- Startup ultra-rápido (~0.1s vs ~3s JVM)
- Menor consumo de memória
- Tamanho otimizado (~284MB)
- Ideal para containers e microserviços

## Configuração

### Variáveis de Ambiente

Copie o arquivo de exemplo e configure suas credenciais:

```bash
cp .env.example .env
# Edite o arquivo .env com suas configurações
```

### Gerando Dados de Teste

```bash
# Gerar 1000 registros fictícios
./generate-customers.sh

# Personalizar quantidade e URL
./generate-customers.sh http://localhost:8080 500
```

## Endpoints

### API REST

Base URL: `http://localhost:8080/api/v1/customers` (desenvolvimento) ou `http://customer.lab-safer.com/api/v1/customers` (produção)

- `POST /api/v1/customers` - Criar cliente
- `GET /api/v1/customers` - Listar clientes (paginado)
- `GET /api/v1/customers/{id}` - Buscar cliente por ID
- `PUT /api/v1/customers/{id}` - Atualizar cliente
- `DELETE /api/v1/customers/{id}` - Deletar cliente

### Health Checks

Endpoints de monitoramento para Kubernetes e observabilidade:

- `GET /q/health` - Status geral da aplicação
- `GET /q/health/ready` - Readiness probe (pronto para receber tráfego)
- `GET /q/health/live` - Liveness probe (aplicação funcionando)

**Exemplo de resposta:**

```json
{
    "status": "UP",
    "checks": [
        {
            "name": "Database connections health check",
            "status": "UP",
            "data": {
                "<default>": "UP"
            }
        }
    ]
}
```
