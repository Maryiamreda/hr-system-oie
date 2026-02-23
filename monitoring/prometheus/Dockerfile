FROM prom/prometheus:v2.44.0
COPY prometheus.yml /etc/prometheus/prometheus.yml
CMD ["--config.file=/etc/prometheus/prometheus.yml", "--web.listen-address=:8080"]