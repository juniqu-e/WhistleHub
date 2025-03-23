pipeline {
    agent any

    // 원격 서버 접속 정보와 작업 디렉토리 (Whistlehub 폴더)
    environment {
        ssh = credentials('ssh')
        hostDomain = credentials('hostDomain')
        remoteServer = "${ssh_usr}@${hostDomain}"
        remoteDir = "/home/${ssh_usr}/Whistlehub"
    }

    stages {
        stage('test ssh') {
          steps {        
                sshagent (credentials: ['ssh']) {
                  sh """
                      ssh -o StrictHostKeyChecking=no ${remoteServer}
                          cd ${remoteDir}
                          touch test.txt
                  """
                }
            }
        }
        // stage('Copy Environment Files') {
        //   steps {        
        //         sshagent (credentials: ['ssh']) {
        //           withCredentials([file(credentialsId: 'dockerEnv', variable: 'dockerEnv')]) {
        //                 sh """
        //                     echo "Copying .env file to remote server..."
        //                     ssh -o StrictHostKeyChecking=no ${remoteServer}
        //                         cd ${remoteDir}
        //                         echo $dockerEnv > .env
        //                 """
        //             }
        //         }
        //     }

        // }
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
