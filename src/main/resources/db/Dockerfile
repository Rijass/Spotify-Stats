FROM mysql:8.4.2

# 12-Factor: KEINE Credentials oder fixed Config im Image
# Alles kommt per ENV zur Laufzeit

# Healthcheck
HEALTHCHECK --interval=10s --timeout=5s \
    CMD mysqladmin ping -h localhost || exit 1

EXPOSE 3306
