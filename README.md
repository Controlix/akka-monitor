# Commands

## Scrape _Prometheus_ metrics
curl localhost:8080/prometheus

## Start a monitor
curl -H "Content-Type: application/json" -d '{"url": "http://google.be?q=world", "method": "get"}' localhost:8080/monitor/start

## Stop a monitor
curl -H "Content-Type: application/json" -d '{"id": "$a"}' localhost:8080/monitor/stop