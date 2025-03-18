# 중계서버( AWS ) 접속 방법 공유 - 2024-02-20
## 1. AWS 서버 정보
```
DNS : ec2-54-180-63-231.ap-northeast-2.compute.amazonaws.com
IP : 54.180.63.231
```
## 2. 고객사 자사 AWS 서버 사용 정보
```
DNS : ec2-54-180-63-231.ap-northeast-2.compute.amazonaws.com
IP : 54.180.63.231
Port : 운영 - 10000 ~ 19999 / 개발 - 20000 ~ 29999
예) 운영 : [54.180.63.231](http://ec2-54-180-63-231.ap-northeast-2.compute.amazonaws.co):10005 / 개발 : [54.180.63.231](http://ec2-54-180-63-231.ap-northeast-2.compute.amazonaws.com):20005
HappyTalk IP 대역
운영
Outbound		        110.165.20.41
Webhook Outbound		175.45.193.114 / 175.106.99.156
개발
Outbound		        175.106.105.107
Webhook Outbound		175.106.98.159

AWS 인바운드 설정 필요 시 보안 그룹(salesforce-security-group)에서 등록 가능


HLI : 운영 - 10000 / 개발 20000
```

## 3. AWS Console 접속 ( Console )
```
### Console URL - https://d-9b6708399b.awsapps.com/start/
### ID/PW : jongtae.kim1 / Kjongtae1!
```
## 4. Bash 접속 ( Terminal )
```
1. c/workspaces/Salesforce/AWS 에서 Git Bash 실행 
 - 해당 폴더에 첨부된 Salesforce.pem 파일이 있어야 함
2. $ ssh -i "Salesforce.pem" ec2-user@ec2-54-180-63-231.ap-northeast-2.compute.amazonaws.com
```
 

# 배포 절차 - 2024-02-22

# 1. Docker로 실행하기 ( 강력 추천 )
## 1. Local에서 jar 파일 생성
### [🔗참조 Site 바로가기]( https://velog.io/@xeonu/SpringBoot-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-docker%EB%A1%9C-%EB%B0%B0%ED%8F%AC%ED%95%98%EA%B8%B0 )
## 📌 개요 : 로컬(개발환경)에서 jar 파일로 build 한 후 서버(AWS)에서 Docker Image 생성 후 실행 합니다.
### - 각 고개사 마다 작업하여 도커로 실행( 고객사별 Port 번호가 다름 )
### - Docker 실행 시 Docker 컨테이너 내부 포트(예: 8080)는 동일하게 유지할 수 있지만, 호스트에서 포워딩할 포트는 다르게 지정해야합니다.
```
    1. 개발 환경에서 배포하고 하는 업체에 대한 정보 설정
        * OrgAccess.properties 수정
        * src/main/resources/application.yml 파일에서 사용하고자 하는 Port 변경
    2. jar 파일 생성
        $ ./gradlew clean bootJar
    3. jar 파일 이동 및 이름변경
        $ # (예시임) mv build/libs/happytalk-0.0.1-SNAPSHOT.jar happytalk-고객사명
        $ mv build/libs/happytalk-0.0.1-SNAPSHOT.jar happytalk-cream.jar
    4. Dockerfile 변경
        * 2번째 라인의 ARG JAR_FILE=*.jar ➡️ ARG JAR_FILE=happytalk-고객사명.jar 로 변경
    5. 서버로 전송 ( Git 이용, main branch 작업 가정 )
        $ git add * 
        $ git commit -m "happytalk-고객사명.jar 생성"
        $ git push
    6. 서버에서 docker 배포 작업 수행
        $ # 1. AWS 서버 접속
        $ git pull
        $ # 2. docker Image 생성 
        $ # sudo docker build -t happytalk-고객사명 .
        $ sudo docker build -t happytalk-cream .
        $ # 3. 도커 컨테이너 생성 및 실행 
        $ # 예) sudo docker run --name 고객사명 -i -t -p 포트번호:8080 happytalk-고객사명 &
        $ sudo docker run --name cream -i -t -p 8082:8080 happytalk-cream &
        $ # 4. 확인
        $ sudo docker ps
        $ curl -l http://localhost:8082/happytalk/test
        $ # 위의 curl 명령 실행 결과 index 가 나오면 정상 설치 된것임
```

# 2. 현재 배포 방법 ( 2024년 3월 21일 부터 작업하지 마세요. )
## 1. git 동기화
```
$ cd ~/CREAM_Happytalk_Gateway/
$ git pull
```
## 2. 배포 ( gradle )
```
$ cd ~/CREAM_Happytalk_Gateway/
$ ./gradlew clean bootRun
```
