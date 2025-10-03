# CI/CD Improvements

## 🚀 Melhorias Implementadas

### 1. **Code Coverage com JaCoCo**
- ✅ Plugin JaCoCo configurado no `pom.xml`
- ✅ Relatório de cobertura gerado automaticamente
- ✅ Verificação de cobertura mínima (50%)
- ✅ Upload do relatório como artefato do GitHub Actions

**Como visualizar localmente**:
```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

**Como visualizar no CI**:
1. Acesse a execução do workflow no GitHub Actions
2. Vá em "Artifacts" no final da página
3. Baixe `jacoco-coverage-report`
4. Abra `index.html` no navegador

---

### 2. **Upload de Artefatos**

#### **JAR da Aplicação**
- ✅ JAR compilado disponível para download
- ✅ Retenção de 30 dias
- ✅ Nome: `application-jar`

#### **Relatório de Cobertura**
- ✅ Relatório HTML completo do JaCoCo
- ✅ Retenção de 30 dias
- ✅ Nome: `jacoco-coverage-report`

#### **Resultados dos Testes**
- ✅ Relatórios Surefire (XML/TXT)
- ✅ Upload mesmo se testes falharem (`if: always()`)
- ✅ Retenção de 30 dias
- ✅ Nome: `test-results`

---

### 3. **Build Nativo (GraalVM)**

#### **Job Separado**: `build-native`
- ✅ Executa **apenas em push para master**
- ✅ Depende do job `build-and-test` (só roda se testes passarem)
- ✅ Gera executável nativo otimizado
- ✅ Upload do binário como artefato

#### **Benefícios do Build Nativo**:
- ⚡ Startup ultra-rápido (~0.1s vs ~3s JVM)
- 💾 Menor consumo de memória
- 📦 Executável standalone (não precisa de JVM)
- 🐳 Ideal para containers

**Como usar o executável nativo**:
```bash
# Baixar artefato 'native-executable' do GitHub Actions
chmod +x customer-quarkus-pg-rest-api-1.0.7-runner
./customer-quarkus-pg-rest-api-1.0.7-runner
```

---

## 📊 Estrutura do Workflow

```yaml
jobs:
  build-and-test:           # Job principal
    - Checkout código
    - Setup JDK 21
    - Setup Maven 3.9.9
    - Build e testes
    - Gerar relatório JaCoCo
    - Upload artefatos (JAR, coverage, test results)
  
  build-native:             # Job condicional (apenas master)
    needs: build-and-test
    if: master && push
    - Checkout código
    - Setup JDK 21
    - Setup Maven 3.9.9
    - Build nativo (sem testes)
    - Upload executável nativo
```

---

## 🎯 Configuração JaCoCo

### **Cobertura Mínima**
- **50% de cobertura de linhas** por package
- Build falha se não atingir o mínimo

### **Ajustar cobertura mínima**
Edite `pom.xml`:
```xml
<limit>
  <counter>LINE</counter>
  <value>COVEREDRATIO</value>
  <minimum>0.80</minimum>  <!-- 80% -->
</limit>
```

### **Excluir classes da cobertura**
```xml
<configuration>
  <excludes>
    <exclude>**/config/**</exclude>
    <exclude>**/dto/**</exclude>
  </excludes>
</configuration>
```

---

## 📈 Métricas de Cobertura

O JaCoCo gera métricas detalhadas:

- **Instructions**: Bytecode instructions
- **Branches**: Cobertura de condicionais (if/else)
- **Lines**: Linhas de código
- **Methods**: Métodos executados
- **Classes**: Classes testadas

---

## 🔧 Comandos Úteis

### **Executar testes com cobertura**
```bash
mvn clean test jacoco:report
```

### **Verificar cobertura mínima**
```bash
mvn jacoco:check
```

### **Build completo (JVM)**
```bash
mvn clean package
```

### **Build nativo**
```bash
mvn package -Pnative -DskipTests
```

### **Executar nativo localmente**
```bash
./target/customer-quarkus-pg-rest-api-1.0.7-runner
```

---

## 📦 Artefatos Disponíveis no GitHub Actions

Após cada execução do workflow, os seguintes artefatos ficam disponíveis:

| Artefato | Conteúdo | Quando |
|----------|----------|--------|
| `application-jar` | JAR compilado | Sempre |
| `jacoco-coverage-report` | Relatório HTML de cobertura | Sempre |
| `test-results` | Resultados Surefire (XML/TXT) | Sempre (mesmo com falha) |
| `native-executable` | Binário nativo GraalVM | Apenas push para master |

---

## 🎓 Próximos Passos (Opcionais)

### **1. Integração com SonarQube/SonarCloud**
```yaml
- name: SonarCloud Scan
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
  run: mvn sonar:sonar
```

### **2. Publicar imagem Docker**
```yaml
- name: Build and Push Docker Image
  run: |
    docker build -f Dockerfile.native -t myapp:${{ github.sha }} .
    docker push myapp:${{ github.sha }}
```

### **3. Deploy automático**
```yaml
- name: Deploy to Kubernetes
  run: kubectl apply -f k8s/
```

### **4. Notificações**
```yaml
- name: Notify Slack
  if: failure()
  uses: slackapi/slack-github-action@v1
```

---

## 📝 Notas

- **Retenção de artefatos**: 30 dias (configurável)
- **Build nativo**: ~5-10 minutos (mais lento que JVM)
- **Cache Maven**: Ativado para acelerar builds
- **Testes**: Sempre executados (não pulados)
- **Docker-in-Docker**: Habilitado para Testcontainers
