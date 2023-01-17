# Running on Kubernetes as a Deployment with any Redis

* redisbank-configmap.yaml -> contains environment variables for the app that will be injected at deploy time
* redisbank-deployment.yaml -> deployment for the docker image (substitute your own docker hub id)
* redisbank-service.yaml -> exposes the deployment through a load balancer

## Things to keep in mind

When you deploy the service/loadbalancer, put the public IP that's generated for you in the configmap under STOMP_HOST (the JS app uses this to setup the WebSocket connection from your local browser to where the app is running, which defaults to localhost)