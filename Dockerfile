# Usa uma imagem base oficial do OpenJDK 21, otimizada para tamanho (slim).
FROM openjdk:21-slim

# Define o diretório de trabalho dentro do container.
WORKDIR /app

# Copia o arquivo .jar gerado pelo Maven para o diretório de trabalho no container.
# O nome do .jar deve corresponder ao que foi gerado na sua pasta 'target'.
COPY target/aegispatrimonio-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta 8080, que é a porta que nossa aplicação Spring Boot usa.
EXPOSE 8080

# Define o comando que será executado quando o container for iniciado.
ENTRYPOINT ["java", "-jar", "app.jar"]