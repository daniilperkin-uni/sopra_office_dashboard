# Basis-Image verwenden
FROM mariadb:latest

# Nur den MariaDB-Client installieren.
RUN apt-get update && \
    apt-get install -y mariadb-client && \
    rm -rf /var/lib/apt/lists/*