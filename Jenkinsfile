pipeline {
    agent { label 'docker' }

    environment {
        DOCKER_REGISTRY = 'rabtab'
        IMAGE_TAG = "${BUILD_NUMBER}"
        DOCKER_CONFIG = "${WORKSPACE}/.docker"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/rab-tab/movie-booking-kafka-saga.git'
            }
        }

        stage('Build Maven Reactor') {
            steps {
                sh 'mvn -T 1C clean package -DskipTests'
            }
        }

        stage('Prepare Docker Auth') {
            steps {
                withCredentials([string(
                    credentialsId: 'dockerhub-auth',
                    variable: 'DOCKER_AUTH_JSON'
                )]) {
                    sh '''
                      mkdir -p $DOCKER_CONFIG
                      echo "$DOCKER_AUTH_JSON" > $DOCKER_CONFIG/config.json
                    '''
                }
            }
        }

        stage('Build & Push Images') {
            steps {
                sh '''
                  docker build -t $DOCKER_REGISTRY/booking-service:$IMAGE_TAG booking-service
                  docker push $DOCKER_REGISTRY/booking-service:$IMAGE_TAG

                  docker build -t $DOCKER_REGISTRY/seat-inventory-service:$IMAGE_TAG seat-inventory-service
                  docker push $DOCKER_REGISTRY/seat-inventory-service:$IMAGE_TAG

                  docker build -t $DOCKER_REGISTRY/payment-service:$IMAGE_TAG payment-service
                  docker push $DOCKER_REGISTRY/payment-service:$IMAGE_TAG
                '''
            }
        }
    }
}
