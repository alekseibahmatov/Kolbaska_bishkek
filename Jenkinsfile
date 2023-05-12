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
                script {
                    try {
                        sh 'rm docker-compose.yml'
                        sh 'rm Dockerfile'
                        sh 'rm .env'
                    } finally {
                        echo 'Skipping step'
                    }
                }

            }
        }
        stage('Copy new files') {
            steps {
                sh 'cp /var/jenkins/infra/docker_compose.yml docker-compose.yml'
                sh 'cp /var/jenkins/infra/Dockerfile Dockerfile'
                sh 'cp /var/jenkins/infra/.env .env'
            }
        }
        stage('Build and start stack') {
            steps {
                sh 'docker compose --env-file .env up -d'
                cleanWs()
            }
        }
    }
}