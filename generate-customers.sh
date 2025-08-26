#!/bin/bash

# Script para gerar 1000 registros fictícios de customers
# Uso: ./generate-customers.sh [URL_BASE] [QUANTIDADE]

BASE_URL=${1:-"http://localhost:8080"}
TOTAL_RECORDS=${2:-1000}
API_ENDPOINT="$BASE_URL/api/v1/customers"

# Arrays de dados fictícios
FIRST_NAMES=("João" "Maria" "Pedro" "Ana" "Carlos" "Lucia" "Rafael" "Fernanda" "Bruno" "Camila" 
             "Diego" "Juliana" "Felipe" "Mariana" "Lucas" "Beatriz" "Thiago" "Larissa" "Gabriel" "Amanda"
             "Rodrigo" "Carla" "Mateus" "Patrícia" "André" "Renata" "Vinicius" "Daniela" "Leonardo" "Priscila"
             "Gustavo" "Vanessa" "Eduardo" "Cristina" "Marcelo" "Simone" "Alexandre" "Mônica" "Roberto" "Sandra"
             "Fernando" "Adriana" "Ricardo" "Eliane" "Fabio" "Tatiana" "Sergio" "Claudia" "Antonio" "Silvia")

LAST_NAMES=("Silva" "Santos" "Oliveira" "Souza" "Rodrigues" "Ferreira" "Alves" "Pereira" "Lima" "Gomes"
            "Costa" "Ribeiro" "Martins" "Carvalho" "Almeida" "Lopes" "Soares" "Fernandes" "Vieira" "Barbosa"
            "Rocha" "Dias" "Monteiro" "Cardoso" "Reis" "Araújo" "Castro" "Andrade" "Nascimento" "Correia"
            "Pinto" "Teixeira" "Moreira" "Cunha" "Mendes" "Farias" "Campos" "Freitas" "Cavalcanti" "Miranda"
            "Nunes" "Barros" "Moura" "Ramos" "Azevedo" "Melo" "Machado" "Coelho" "Pires" "Nogueira")

MIDDLE_NAMES=("de" "da" "dos" "das" "do" "" "" "" "" "")

DOMAINS=("gmail.com" "hotmail.com" "yahoo.com.br" "outlook.com" "uol.com.br" "bol.com.br" "terra.com.br")

# Função para gerar telefone brasileiro
generate_phone() {
    local ddd=$((RANDOM % 89 + 11))  # DDD de 11 a 99
    local number=$((RANDOM % 900000000 + 100000000))  # 9 dígitos
    echo "($ddd) 9$number"
}

# Função para gerar email
generate_email() {
    local first_name=$(echo "$1" | tr '[:upper:]' '[:lower:]' | sed 's/ã/a/g; s/ç/c/g; s/õ/o/g; s/á/a/g; s/é/e/g; s/í/i/g; s/ó/o/g; s/ú/u/g')
    local last_name=$(echo "$2" | tr '[:upper:]' '[:lower:]' | sed 's/ã/a/g; s/ç/c/g; s/õ/o/g; s/á/a/g; s/é/e/g; s/í/i/g; s/ó/o/g; s/ú/u/g')
    local domain=${DOMAINS[$((RANDOM % ${#DOMAINS[@]}))]}
    local number=$((RANDOM % 999 + 1))
    echo "${first_name}.${last_name}${number}@${domain}"
}

# Função para fazer o POST
create_customer() {
    local first_name="$1"
    local middle_name="$2"
    local last_name="$3"
    local email="$4"
    local mobile="$5"
    
    local json_data="{
        \"firstName\": \"$first_name\",
        \"middleName\": \"$middle_name\",
        \"lastName\": \"$last_name\",
        \"email\": \"$email\",
        \"mobile\": \"$mobile\"
    }"
    
    curl -s -X POST \
        -H "Content-Type: application/json" \
        -d "$json_data" \
        "$API_ENDPOINT"
}

# Verificar se a API está disponível
echo "Verificando conectividade com a API..."
if ! curl -s --connect-timeout 5 "$BASE_URL/q/health" > /dev/null; then
    echo "❌ Erro: Não foi possível conectar à API em $BASE_URL"
    echo "Certifique-se de que a aplicação está rodando com: mvn quarkus:dev"
    exit 1
fi

echo "✅ API disponível em $BASE_URL"
echo "🚀 Iniciando criação de $TOTAL_RECORDS registros..."
echo ""

# Contadores
SUCCESS_COUNT=0
ERROR_COUNT=0
START_TIME=$(date +%s)

# Loop principal
for i in $(seq 1 $TOTAL_RECORDS); do
    # Selecionar dados aleatórios
    FIRST_NAME=${FIRST_NAMES[$((RANDOM % ${#FIRST_NAMES[@]}))]}
    LAST_NAME=${LAST_NAMES[$((RANDOM % ${#LAST_NAMES[@]}))]}
    MIDDLE_NAME=${MIDDLE_NAMES[$((RANDOM % ${#MIDDLE_NAMES[@]}))]}
    EMAIL=$(generate_email "$FIRST_NAME" "$LAST_NAME")
    MOBILE=$(generate_phone)
    
    # Fazer a requisição
    RESPONSE=$(create_customer "$FIRST_NAME" "$MIDDLE_NAME" "$LAST_NAME" "$EMAIL" "$MOBILE")
    
    # Verificar se foi bem-sucedido (resposta contém "id")
    if echo "$RESPONSE" | grep -q '"id"'; then
        ((SUCCESS_COUNT++))
        echo "✅ [$i/$TOTAL_RECORDS] $FIRST_NAME $LAST_NAME - $EMAIL"
    else
        ((ERROR_COUNT++))
        echo "❌ [$i/$TOTAL_RECORDS] Erro ao criar: $FIRST_NAME $LAST_NAME"
        echo "   Resposta: $RESPONSE"
    fi
    
    # Progress a cada 50 registros
    if [ $((i % 50)) -eq 0 ]; then
        ELAPSED=$(($(date +%s) - START_TIME))
        RATE=$((i / (ELAPSED + 1)))
        echo ""
        echo "📊 Progresso: $i/$TOTAL_RECORDS (${SUCCESS_COUNT} sucessos, ${ERROR_COUNT} erros) - ${RATE} req/s"
        echo ""
    fi
    
    # Pequeno delay para não sobrecarregar
    sleep 0.01
done

# Estatísticas finais
END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))
AVERAGE_RATE=$((TOTAL_RECORDS / (TOTAL_TIME + 1)))

echo ""
echo "🎉 Processo concluído!"
echo "📈 Estatísticas:"
echo "   • Total de registros: $TOTAL_RECORDS"
echo "   • Sucessos: $SUCCESS_COUNT"
echo "   • Erros: $ERROR_COUNT"
echo "   • Tempo total: ${TOTAL_TIME}s"
echo "   • Taxa média: ${AVERAGE_RATE} req/s"
echo ""
echo "🔍 Para verificar os dados criados:"
echo "   curl $BASE_URL/api/v1/customers?size=10"
