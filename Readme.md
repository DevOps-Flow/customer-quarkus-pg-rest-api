# Customer Quarkus PG REST API

API REST de clientes com Quarkus 3.x, Java 21 (LTS), PostgreSQL, Liquibase, SOLID, Clean Architecture, DTOs, logs JSON, build nativo com GraalVM e manifestos Kubernetes.

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
## HashiCorp Vault (integração no Kubernetes)

Este repositório já inclui manifestos Kubernetes que usam o Vault Agent Injector para injetar segredos no pod da aplicação.

Resumo da configuração presente em `k8s/20-deployment-service.yaml`:

- O `Deployment` possui as seguintes anotações do Vault Agent Injector:
    - `vault.hashicorp.com/agent-inject: "true"` — ativa a injeção do agent.
    - `vault.hashicorp.com/auth-path: "auth/kubernetes"` — caminho de autenticação Kubernetes no Vault.
    - `vault.hashicorp.com/role: "customer-api"` — role do Vault vinculada ao ServiceAccount.
    - `vault.hashicorp.com/agent-inject-secret-db-secrets.properties: "homelab/data/customer-api"` — caminho KV no Vault (KV v2) que contém as credenciais.
    - `vault.hashicorp.com/agent-inject-template-db-secrets.properties` — template que gera o arquivo `db-secrets.properties` com as chaves `quarkus.datasource.username` e `quarkus.datasource.password` lidas de `.Data.data` do segredo (KV v2).

- O `ServiceAccount` usado pelo pod é `customer-api-sa` e o namespace é `customer-api` (veja `k8s/00-namespace-sa-rbac.yaml`).
- O contêiner da aplicação define `QUARKUS_CONFIG_LOCATIONS=/vault/secrets/db-secrets.properties`, portanto o Quarkus irá carregar o arquivo injetado como fonte de configuração.

O que é preciso configurar no Vault (exemplo mínimo)

1. Habilitar o auth Kubernetes (apenas se ainda não estiver habilitado):

```bash
vault auth enable kubernetes
```

2. Configurar o backend Kubernetes (exemplo simplificado):

```bash
# Depende do seu ambiente — normalmente você fornece o token reviewer e o endpoint do kube-apiserver
vault write auth/kubernetes/config \
    token_reviewer_jwt="$(kubectl get secret $(kubectl get serviceaccount customer-api-sa -n customer-api -o jsonpath='{.secrets[0].name}') -n customer-api -o jsonpath='{.data.token}' | base64 --decode)" \
    kubernetes_ca_cert="$(kubectl get secret $(kubectl get serviceaccount customer-api-sa -n customer-api -o jsonpath='{.secrets[0].name}') -n customer-api -o jsonpath='{.data.ca\.crt}' | base64 --decode)" \
    kubernetes_host="https://$(kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}' | sed -e 's~https\\?://~~')"
```

3. Criar uma policy que permita leitura do caminho do segredo (exemplo `customer-api`):

```bash
cat <<EOF | vault policy write customer-api -
path "homelab/data/customer-api" {
    capabilities = ["read"]
}
EOF
```

4. Criar a role Kubernetes que vincula o ServiceAccount `customer-api-sa` ao policy `customer-api`:

```bash
vault write auth/kubernetes/role/customer-api \
    bound_service_account_names=customer-api-sa \
    bound_service_account_namespaces=customer-api \
    policies=customer-api \
    ttl=24h
```

5. Criar o segredo KV (KV v2 neste exemplo) com as credenciais da base de dados:

```bash
vault kv put homelab/data/customer-api db_username=myuser db_password='s3cr3tP@ss'
```

Como funciona na prática

- Quando o pod é criado, o Vault Agent Injector injeta um sidecar e recupera o segredo `homelab/data/customer-api` usando a role `customer-api`.
- O template configurado transforma o segredo em um arquivo de propriedades, por exemplo `/vault/secrets/db-secrets.properties` com linhas:

    quarkus.datasource.username=...
    quarkus.datasource.password=...

- A aplicação lê esse arquivo porque `QUARKUS_CONFIG_LOCATIONS` aponta para ele.

Testes e verificação

- Aplique os manifestos (namespace, service account e deployment):

```bash
kubectl apply -f k8s/00-namespace-sa-rbac.yaml
kubectl apply -f k8s/20-deployment-service.yaml
```

- Verifique os pods e logs:

```bash
kubectl get pods -n customer-api
kubectl logs -n customer-api -l app=customer-api
```

- Para inspecionar o arquivo de segredos injetado (após o pod estar pronto):

```bash
POD=$(kubectl get pod -n customer-api -l app=customer-api -o jsonpath='{.items[0].metadata.name}')
kubectl exec -n customer-api $POD -- cat /vault/secrets/db-secrets.properties
```

Boas práticas e observações de segurança

- Use policies de menor privilégio: conceda apenas `read` ao caminho necessário.
- Proteja o tráfego entre Kubernetes e Vault (TLS) e garanta que o endpoint do Vault seja acessível apenas por quem precisa.
- Rotacione credenciais no Vault quando necessário — o Agent Injector e Quarkus lerão a nova configuração nos próximos mounts/recargas conforme o template/agent configurado.
- Em ambientes offline ou sem acesso público, forneça um mirror interno para os binários/configs ou pré-instale o Vault Agent Injector no cluster.

Se quiser, eu posso adicionar exemplos automáticos para gerar a policy/role/secret via scripts ou adicionar um README separado com etapas de bootstrap do Vault para ambientes de desenvolvimento. Quer que eu adicione isso?

***