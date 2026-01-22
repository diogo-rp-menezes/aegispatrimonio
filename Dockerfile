# Use a imagem oficial do OpenJDK para uma build otimizada
FROM openjdk:21-jdk-slim as build

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo pom.xml para que as dependências possam ser baixadas em cache
COPY pom.xml .

# Copia o código fonte da aplicação
COPY src ./src

# Compila a aplicação e gera o JAR executável
RUN ./mvnw clean package -DskipTests

# Imagem final para a aplicação
FROM openjdk:21-jdk-slim

# Define o diretório de trabalho
WORKDIR /app

# Copia o JAR gerado da etapa de build
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta em que a aplicação Spring Boot será executada
EXPOSE 8080

# Define o comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
