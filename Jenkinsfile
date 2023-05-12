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
        stage('Build and start stack') {
            steps {
                sh 'docker compose up'
            }
        }
    }
}