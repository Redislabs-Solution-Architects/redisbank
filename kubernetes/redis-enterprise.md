# Running on Kubernetes as a Deployment with Redis Enterprise

As a more generic Kubernetes strategy, a Kubernetes Deployment is used to create the pods for RedisBank. The RedisBank Docker image created by [Dockerfile](./Dockerfile) is pushed to a repository. The image is then referenced in the Deployment. Note the dependency on an already deployed Redis Enterprise database using the [Redis Enterprise Operator](https://docs.redis.com/latest/kubernetes/).

➡ This shows using GCP and GKE; the pattern will work anywhere

➡ It is left as an exercise to the reader to have a Kubernetes cluster and all the appropriate CLIs necessary

➡ The order of things is important. RedisBank will not start if the database hasn't been created.

## Create a Docker image for a GKE Deployment

Create the Docker image and push it to your favorite repository. In this example we'll use Google Container Repository (GCR). First you'll need to add the OAuth credentials to Docker with

```sh
gcloud auth configure-docker gcr.io
```

and then push the image with

```sh
docker build -t redisbank .
docker tag redisbank gcr.io/YOUR-GCP-PROJECT/redisbank
docker push gcr.io/YOUR-GCP-PROJECT/redisbank
```

## Set up a GKE Cluster

➡ It is left as an excercise to the reader to setup GKE.

## Add the Redis Enterprise Operator to the GKE cluster

Please see [Deploy Redis Enterprise Software on Kubernetes](https://docs.redis.com/latest/kubernetes/deployment/quick-start/)

## Create the Redis Enterprise Cluster

A Redis Enterprise cluster is declared with the RedisEnterpriseCluster (REC) resource. Feel free to create the YAML yourself or use [this one](https://raw.githubusercontent.com/andresrinivasan/redis-enterprise-k8s-custom-resources/master/getting-started/rec.yaml) which is ready to go.

```sh
kubectl apply -f https://raw.githubusercontent.com/andresrinivasan/redis-enterprise-k8s-custom-resources/master/getting-started/rec.yaml
```

## Create the Redis Enterprise Database

A Redis Enterprise database is declared with the RedisEnterpriseDatabase (REDB) resource. As part of this resource we also need to specify the modules (Search and Time Series in this case) needed by the RedisBank app. The supplied REDB creates a 1 GB single shard HA database.

```sh
kubectl apply -f https://raw.githubusercontent.com/andresrinivasan/redisbank/blob/work/kubernetes/redis-enterprise-database.yaml
```

## Start the RedisBank App

The provided deployment has everything you need EXCEPT the name of the Docker image for the app. Please update spec.image in [redisbank-enterprise-deployment.yaml](redisbank-enterprise-deployment.yaml) and replace `YOUR-IMAGE-URL` with, wait for it, the URL of your image. Then add the deployment to your Kubernetes cluster with

```sh
kubectl apply -f kubernetes/redisbank-enterprise-deployment.yaml
```

## Troubleshooting

### RedisBank Pod Does Not Start

* Check the logs with `kubectl deployment.apps/redisbank-deployment`
  * If you see *standard_init_linux.go:207: exec user process caused "exec format error"*, the referenced image architecture is different from the container architecture. This can happen if you built the image on your ARM64 Mac and are hosting your Kubernetes cluster on AMD VMs.

### RedisBank Pod Exits or Keeps Restarting

* Check if the Kubernetes cluster has enough resources for the app

* Verify the REDB is started
  * Verify the REDB StatefulSet has completely started
  * Use `kubectl exec -it pod/rec-demo-0 -- rladmin status extra all | fgrep -i health` to verify the REC health is *OK*  

* Check the app logs with `kubectl deployment.apps/redisbank-deployment`. If you see a stack dump, most likely the Search and/or Time Series modules did not load. Verify the versions enumerated in [redis-enterprise-database.yaml](./redis-enterprise-database.yaml) match what is distributed with the version of Redis Enterprise you're using. Edit the REDB resource to match the versions.

### Redis Enterprise StatefulSet Does Not Start or Keeps Restarting

* Check if the Kubernetes cluster has enough resources for Redis Enterprise
