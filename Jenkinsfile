pipeline {
    agent { label 'docker' }

    environment {
        DOCKER_REGISTRY = 'rabtab'
        IMAGE_TAG = "${BUILD_NUMBER}"
        DOCKER_CONFIG = "/Users/rabia/.docker-jenkins" // critical
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/rab-tab/movie-booking-kafka-saga.git'
            }
        }

        stage('Build Maven') {
            steps {
                sh 'mvn -T 1C clean package -DskipTests'
            }
        }

        stage('Build & Push Docker Images') {
            steps {
                sh '''
                  docker login https://index.docker.io/v1/  # will use config.json automatically

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

    post {
        always {
            sh 'docker logout || true'
        }
    }
}
