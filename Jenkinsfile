pipeline {
    agent any

    // 원격 서버 접속 정보와 작업 디렉토리 (MyFairy 폴더)
    environment {
        REMOTE_SERVER = credentials('ssh').usernmae + '@' + credentials('hostDomain').host
        REMOTE_DIR    = '/home/' + credentials('ssh').usernmae + '/WhistleHub'
    }

    stages {
        stage('test ssh') {
          steps {        
                sshagent (credentials: ['ssh']) {
                sh """
                    ssh -o StrictHostKeyChecking=no ${REMOTE_SERVER} "pwd"
                """
                }
            }
        }
        stage('test ssh') {
          steps {        
                sshagent (credentials: ['ssh']) {
                sh """
                    ssh -o StrictHostKeyChecking=no ${REMOTE_SERVER}
                    cd ${REMOTE_DIR}
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
