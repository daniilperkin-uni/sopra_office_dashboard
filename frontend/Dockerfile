# STAGE 1: Build das Frontend
# Dieser Stage nutzt eine Node.js-Umgebung, um die Vue.js-Anwendung zu kompilieren.
# Das Ergebnis ist ein optimierter Satz statischer Dateien (HTML, CSS, JS) im 'dist'-Ordner.
FROM node:20-alpine AS build

# Setzt das Arbeitsverzeichnis innerhalb des Containers auf /app.
WORKDIR /app

# Kopiere nur die package.json und package-lock.json, um das Docker-Layer-Caching zu nutzen.
# Docker erstellt einen neuen Layer nur, wenn sich diese Dateien ändern.
# Dies beschleunigt nachfolgende Builds erheblich, da 'npm install' nicht jedes Mal ausgeführt wird.
COPY frontend/package*.json ./
RUN npm install

# Kopiere den restlichen Quellcode des Frontends in das Arbeitsverzeichnis.
# Dies geschieht nach 'npm install', um eine Invalidierung des Caching-Layers bei Codeänderungen zu vermeiden.
COPY frontend/ .

# Ermöglicht das Abschneiden des Namens (10 letzen Zeichen bleiben) für einer ansehentliche Darstellung bei Anonymisierung   
ARG VITE_ANONYMIZE_NAMES=true

# Führt den Build-Skript aus, das in package.json definiert ist (typischerweise 'npm run build').
# Dieser Befehl kompiliert die Vue.js-Anwendung und legt die fertigen Dateien im Ordner /app/dist ab.
RUN npm run build --debug

# STAGE 2: Serviere die gebauten Dateien mit Nginx
# Dieser Stage erstellt das finale, schlankere Produktionsimage.
# Er verwendet Nginx, einen leistungsstarken und leichtgewichtigen Webserver, um die statischen Dateien auszuliefern.
FROM nginx:stable-alpine

# Kopiert eine benutzerdefinierte Nginx-Konfigurationsdatei in den Container.
# Dies ermöglicht es, das Verhalten von Nginx (z.B. Routing, Kompression) an die Anforderungen der Vue.js-App anzupassen.
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Kopiert die gebauten Dateien aus dem 'build'-Stage in das Standard-Web-Verzeichnis von Nginx.
# '--from=build' bezieht sich auf den oben definierten Build-Stage.
# Dies ist der Kern des Multi-Stage-Builds: nur die fertigen Artefakte werden in das finale Image übernommen.
COPY --from=build /app/dist /usr/share/nginx/html

# Macht Port 80 für den Container zugänglich, damit der Webserver von außen erreicht werden kann.
EXPOSE 80

# Startet den Nginx-Server im Vordergrund, wenn der Container läuft.
# 'daemon off;' ist notwendig, damit Nginx nicht im Hintergrund läuft und der Container sofort beendet wird.
CMD ["nginx", "-g", "daemon off;"]