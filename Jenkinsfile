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
                      ssh -o StrictHostKeyChecking=no ${remoteServer} '
                          pwd
                          '
                  """
                }
            }
        }

        stage('git pull') {
          steps {        
                sshagent (credentials: ['ssh']) {
                    sh """
                        echo "git pull"
                        ssh -o StrictHostKeyChecking=no ${remoteServer} '
                            cd ${remoteDir} && \
                            chmod +x askpass.sh && \
                            export GIT_ASKPASS=\$(pwd)/askpass.sh && \
                            git pull
                            '
                    """
                }

                sshagent (credentials: ['ssh']) {
                    sh """
                        echo "Delete askpass file"
                        ssh -o StrictHostKeyChecking=no ${remoteServer} '
                            cd ${remoteDir}
                            rm askpass.sh
                            export GIT_ASKPASS=""
                            '
                    """
                }
            }
        }

        stage('Copy Environment Files') {
          steps {        
                sshagent (credentials: ['ssh']) {
                  withCredentials([file(credentialsId: 'dockerEnv', variable: 'envFile')]) {
                        sh """
                            echo "Copying .env file to remote server..."
                            scp -o StrictHostKeyChecking=no "${envFile}" "${remoteServer}:${remoteDir}/.env"
                        """
                    }
                }

                sshagent (credentials: ['ssh']) {
                  withCredentials([file(credentialsId: 'fastapiEnv', variable: 'envFile')]) {
                        sh """
                            echo "Copying FastAPI.env file to remote server..."
                            scp -o StrictHostKeyChecking=no "${envFile}" "${remoteServer}:${remoteDir}/envs/FastAPI.env"
                        """
                    }
                }
        
                sshagent (credentials: ['ssh']) {
                  withCredentials([file(credentialsId: 'backendEnv', variable: 'envFile')]) {
                        sh """
                            echo "Copying backend.env file to remote server..."
                            scp -o StrictHostKeyChecking=no "${envFile}" "${remoteServer}:${remoteDir}/envs/backend.env"
                        """
                    }
                }

                sshagent (credentials: ['ssh']) {
                  withCredentials([file(credentialsId: 'mysqlEnv', variable: 'envFile')]) {
                        sh """
                            echo "Copying mysql.env file to remote server..."
                            scp -o StrictHostKeyChecking=no "${envFile}" "${remoteServer}:${remoteDir}/envs/mysql.env"
                        """
                    }
                }
            
                sshagent (credentials: ['ssh']) {
                  withCredentials([file(credentialsId: 'neo4jEnv', variable: 'envFile')]) {
                        sh """
                            echo "Copying neo4j.env file to remote server..."
                            scp -o StrictHostKeyChecking=no "${envFile}" "${remoteServer}:${remoteDir}/envs/neo4j.env"
                        """
                    }
                }
            }
        }

        
        stage('docker compose down') {
          steps {        
                sshagent (credentials: ['ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${remoteServer} '
                            cd ${remoteDir}
                            docker compose down
                            docker compose -f docker-compose.db.yml down
                            '
                    """
                }
            }
        }

        stage('docker compose up') {
          steps {        
                sshagent (credentials: ['ssh']) {
                    sh """
                        echo "docker compose db up"
                        ssh -o StrictHostKeyChecking=no ${remoteServer} '
                            cd ${remoteDir}
                            docker compose -f docker-compose.db.yml up -d
                            '
                    """
                }

                sshagent (credentials: ['ssh']) {
                    sh """
                        echo "docker compose up"
                        ssh -o StrictHostKeyChecking=no ${remoteServer} '
                            cd ${remoteDir}
                            docker compose up -d --build
                            '
                    """
                }
            }
        }

        stage('remove env files') {
          steps {        
                sshagent (credentials: ['ssh']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${remoteServer} '
                            cd ${remoteDir}
                            rm .env
                            rm ./envs/*.env
                            '
                    """
                }
            }
        }
    }


    post {
        success {
            echo "Pipeline completed successfully!"
            withCredentials([string(credentialsId: 'discordUrl', variable: 'DISCORD')]) {
                        discordSend description: """
                        ✅ ${currentBuild.displayName} 빌드에 성공했습니다.
                        실행 시간 : ${currentBuild.duration / 1000}s
                        """,
                        link: env.BUILD_URL, result: currentBuild.currentResult, 
                        title: "${env.JOB_NAME} : ${currentBuild.displayName} 성공", 
                        webhookURL: "$DISCORD"
            }
        }
        failure {
            echo "Pipeline failed. Please check the logs for details."
            withCredentials([string(credentialsId: 'discordUrl', variable: 'DISCORD')]) {
                        discordSend description: """
                        ❌ ${currentBuild.displayName} 빌드에 실패했습니다.
                        실행 시간 : ${currentBuild.duration / 1000}s
                        """,
                        link: env.BUILD_URL, result: currentBuild.currentResult, 
                        title: "${env.JOB_NAME} : ${currentBuild.displayName} 실패", 
                        webhookURL: "$DISCORD"
            }
        }
    }
}
