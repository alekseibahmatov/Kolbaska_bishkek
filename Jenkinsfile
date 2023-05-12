pipeline {
    agent { label 'Slavik' }
    stages {
        stage('Stop stack') {
            steps {
                sh 'docker compose down'
            }
        }
        stage('Clear docker') {
            steps {
                sh 'docker system prune -af'
            }
        }
        stage('Delete files') {
            steps {
                sh 'rm docker-compose.yml'
                sh 'rm Dockerfile'
            }
        }
        stage('Copy new files') {
            steps {
                sh 'cp /var/jenkins/infra/docker_compose.yml docker-compose.yml'
                sh 'cp /var/jenkins/infra/Dockerfile Dockerfile'
                sh 'cp /var/jenkins/infra/.env_backend .env_backend'
                sh 'cp /var/jenkins/infra/.env_database .env_database'
            }
        }
        stage('Build and start stack') {
            steps {
                sh 'docker network create stack-network'
                sh 'docker run --env-file .env_database --network stack-network --name db -d mysql:5.7'
                sh 'docker build . -t stack-app'
                sh 'docker run --env-file .env_backend --network stack-network --name app -d stack-app'
                cleanWs()
            }
        }
    }
}