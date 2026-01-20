# Spotify Tracker – Deployment (k3s + ingress-nginx + cert-manager + Let’s Encrypt)

Dieses Setup deployt die App + MySQL auf einer einzelnen VM (z.B. GCP).  
Traffic läuft über **ingress-nginx** (NodePort) und wird am Host über **socat** von **:80/:443** auf die NodePorts weitergeleitet.  
Traefik wird deaktiviert/entfernt, damit es keine Port-/Ingress-Konflikte gibt.

---

## Voraussetzungen

- Debian/Ubuntu VM mit öffentlicher IPv4
- DNS A-Record: `your-domain.de` → öffentliche VM-IP
- Cloud-Firewall/Security Group: **TCP 80 und 443 inbound erlaubt**
    - (GCP: Network tags `http-server` + `https-server` oder eigene Firewall-Regel)
- Repo enthält Ordner `k8s/` mit YAMLs:
    - `namespace.yaml`
    - `deployment-mysql.yaml`, `service-mysql.yaml`
    - `deployment-app.yaml`, `service-app.yaml`
    - `configmap-app.yaml`, `secret-app.yaml`
    - `ingress.yaml`
    - `cluster-issuer.yaml` (cert-manager ClusterIssuer für Let’s Encrypt)

> Wichtig: Ingress + Issuer müssen Domain & Email korrekt enthalten.

---

## Quickstart (ein Durchlauf)

> Als User mit sudo-Rechten ausführen.  
> Wenn du `DOMAIN`/`LE_EMAIL` anpasst, muss das auch in `k8s/ingress.yaml` und `k8s/cluster-issuer.yaml` stehen.
> Es sollte zudem auch die `configmap-app.yaml` da dort der Spotify Redirect link drin steht.
> Die `secret-app.yaml` muss geändern werden dort sollten spezielle Spotify Daten eingetragen werden, Passwörter geändern werden etc.

```bash

DOMAIN="your-domain.de"
LE_EMAIL="your-mail@exmaple.de"

# 1) Basis-Pakete
sudo apt-get update
sudo apt-get install -y curl ca-certificates socat

# 2) k3s installieren (Traefik disabled)
curl -sfL https://get.k3s.io | sudo INSTALL_K3S_EXEC="server --disable traefik" sh -

# kubeconfig lesbar machen (damit kubectl ohne sudo geht)
sudo chmod 644 /etc/rancher/k3s/k3s.yaml
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml

# optional: kubectl verfügbar machen
# (k3s bringt kubectl als "k3s kubectl" mit, aber symlink ist praktisch)
sudo ln -sf /usr/local/bin/k3s /usr/local/bin/kubectl

# 3) Falls Traefik bereits existiert: entfernen
kubectl -n kube-system delete deploy traefik 2>/dev/null || true
kubectl -n kube-system delete svc traefik 2>/dev/null || true
sudo rm -f /var/lib/rancher/k3s/server/manifests/*traefik* 2>/dev/null || true
sudo systemctl restart k3s

# 4) ingress-nginx installieren
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.2/deploy/static/provider/cloud/deploy.yaml

# warten bis ingress-nginx ready ist
kubectl -n ingress-nginx rollout status deploy/ingress-nginx-controller --timeout=180s

# 5) cert-manager installieren
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.6/cert-manager.yaml

# warten bis cert-manager ready ist
kubectl -n cert-manager rollout status deploy/cert-manager --timeout=180s
kubectl -n cert-manager rollout status deploy/cert-manager-webhook --timeout=180s
kubectl -n cert-manager rollout status deploy/cert-manager-cainjector --timeout=180s

# 6) Ingress Controller Service auf NodePort sicherstellen (falls nicht already)
kubectl -n ingress-nginx patch svc ingress-nginx-controller -p '{"spec":{"type":"NodePort"}}'

# NodePorts auslesen (Default: 30318/31451)
kubectl -n ingress-nginx get svc ingress-nginx-controller

# 7) Host Ports 80/443 via socat auf die NodePorts forwarden (systemd)
sudo tee /etc/systemd/system/k3s-forward-80.service >/dev/null <<'EOF'
[Unit]
Description=Forward TCP 80 to k3s NodePort 30318 (ingress-nginx)
After=network-online.target
Wants=network-online.target

[Service]
ExecStart=/usr/bin/socat TCP-LISTEN:80,fork,reuseaddr TCP:127.0.0.1:30318
Restart=always
RestartSec=1

[Install]
WantedBy=multi-user.target
EOF

sudo tee /etc/systemd/system/k3s-forward-443.service >/dev/null <<'EOF'
[Unit]
Description=Forward TCP 443 to k3s NodePort 31451 (ingress-nginx)
After=network-online.target
Wants=network-online.target

[Service]
ExecStart=/usr/bin/socat TCP-LISTEN:443,fork,reuseaddr TCP:127.0.0.1:31451
Restart=always
RestartSec=1

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now k3s-forward-80.service k3s-forward-443.service

# prüfen dass 80/443 wirklich lauschen
sudo ss -ltnp | egrep ':80|:443'

# 8) App Stack deployen
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret-app.yaml
kubectl apply -f k8s/configmap-app.yaml

kubectl apply -f k8s/deployment-mysql.yaml
kubectl apply -f k8s/service-mysql.yaml

kubectl apply -f k8s/deployment-app.yaml
kubectl apply -f k8s/service-app.yaml

# Issuer + Ingress (TLS)
kubectl apply -f k8s/cluster-issuer.yaml
kubectl apply -f k8s/ingress.yaml

# 9) Status prüfen
kubectl -n spotify-stats get pods
kubectl -n cert-manager get challenges,orders -A | grep -i "$DOMAIN" || true
kubectl -n spotify-stats get certificate || true
