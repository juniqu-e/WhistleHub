pipeline {
    agent any

    // 원격 서버 접속 정보와 작업 디렉토리 (MyFairy 폴더)
    environment {
        ssh = credentials('ssh')
        hostDomain = credentials('hostDomain')
    }

    stages {
        stage('test ssh') {
          steps {        
                sshagent (credentials: ['ssh']) {
                sh """
                    ssh -o StrictHostKeyChecking=no ${ssh_usr}@${hostDomain} "pwd"
                """
                }
            }
        }
        stage('touch text file') {
          steps {        
                sshagent (credentials: ['ssh']) {
                sh """
                    ssh -o StrictHostKeyChecking=no ${ssh_usr}@${hostDomain}
                    touch test.txt
                """
                }
            }

        }
    }

    post {
        success {
            echo "Pipeline completed successfully!"
            
        }
        failure {
            echo "Pipeline failed. Please check the logs for details."
            
        }
    }
}
