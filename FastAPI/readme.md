# FastAPI template

- 본 프로젝트는 fastapi를 fast하게 개발하기 위해 만든 template 프로젝트입니다.

## 시작

### 가상환경 설치

- 프로젝트 최상단 위치에서, 가상환경을 생성합니다.

```bash
python -m venv .venv
```

- 가상환경을 활성화합니다.
- [운영체제별 가상환경 진입 방법](https://docs.python.org/ko/3/library/venv.html#how-venvs-work)

```bash
# ex. windows , powershell
.\.venv\Scripts\Activate.ps1
```

### 가상환경 설치파일 설치

- 가상환경에 진입한 상태에서 아래 명령어를 입력해 프로젝트 기본 의존성 패키지를 설치합니다.

```bash
pip install -r ./requirements.txt
```

- 가상환경에 필요한 라이브러리를 설치하였다면, 아래 명령을 통해 가상환경에서 벗어납니다.

```bash
exit
```

- 이후 `.\.venv\Lib\site-packages` 디렉토리에 `app.pth` 폴더를 추가한 후, 프로젝트 최상단 경로를 적습니다.
- 아래 명령을 실행해서 이 작업을 대신할 수 있습니다.

```bash
echo $pwd.Path > .\.venv\Lib\site-packages\app.pth
```

- 마지막으로 ide에서 python interpreter를 .venv로 설정하여 모듈을 찾아갈 수 있도록 합시다.

### 프로젝트 실행

- 프로젝트를 실행하기 위해선 app/main.py를 실행하면 됩니다.

## 프로젝트 구조 설명

```
├─app                          // 프로젝트의 로직이 들어있습니다.
│  ├─models                    // 외부에서 들어오는 DTO와 pydantic 모델 클래스가 있습니다.
│  │  ├─common                 // DTO에 해당하지 않는 pydantic class가 들어있습니다.
│  │  ├─request                // request DTO
│  │  └─response               // response DTO
│  ├─routers                   // controller에 해당합니다.
│  └─services                  // 비지니스로직이 들어있는 service가 들어있는 폴더입니다.
├─common                       // 프로젝트 전체에 통틀어 사용되는 파일들이 들어있습니다.
│  └─exceptions                // 에러 핸들링에 필요한 파일들이 들어있습니다.
├─config                       // 환경변수, 로그 포매팅에 관련된 폴더입니다.
├─log                          // 로그가 기록되는 폴더입니다.
├─notebooks                    // 간단한 함수들을 테스트해볼 수 있는 주피터 노트북이 들어있는 폴더입니다.
└─utils                        // file 저장, 삭제 / log기록에 필요한 유틸이 들어있습니다.
```
