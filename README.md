# WatchOut
- email address : sksowk156@gmail.com <br />

## Features
WatchOut은 졸음을 방지 서비스입니다.<br />
졸음을 감지하면 경고음을 울리고, 주변 휴식 공간 정보를 제공합니다.<br />
경고음을 커스텀할 수 있고, 졸음 감지 결과를 확인할 수 있습니다.<br />

### Screenshots


## Usage
### 홈
|제목 셀1|제목 셀2|제목 셀3|제목 셀4|
|---|---|---|---|
|내용 1|내용 2|내용 3|내용 4|

<img src="https://github.com/sksowk156/Hellth/assets/110645858/f1aad50a-39e9-4b96-bd3d-87293f840df0" width="216" height="384"/><br />

+ 구글 아이디로 회원 가입 및 로그인 할 수 있습니다.<br />
+ 로그인을 해야만 앱을 사용할 수 있습니다.<br />

### 졸음 감지
|제목 셀1|제목 셀2|제목 셀3|제목 셀4|
|---|---|---|---|
|내용 1|내용 2|내용 3|내용 4|

<img src="https://github.com/sksowk156/Hellth/assets/110645858/a818e82b-6d74-4121-bc00-2ffa42be43ab" width="216" height="384"/>
<img src="https://github.com/sksowk156/Hellth/assets/110645858/05bc494c-73f8-421d-83f0-9909cbd42837" width="216" height="384"/><br />

+ 홈 화면 오른 쪽 상단에 있는 톱니바퀴 버튼을 누르면 로그아웃과 회원 탈퇴를 할 수 있습니다.<br />
+ 회원 탈퇴 시 모든 운동 기록은 삭제 됩니다.<br />
+ 몇몇 기능들은 네트워크가 연결되어 있지 않아도 사용할 수 있습니다.(이전에 로그인한 기록이 있다면)<br />
   + 네트워크가 필요한 기능 : 기준 달리기, 운동 기록 저장<br />
   
### 통계
|제목 셀1|제목 셀2|제목 셀3|제목 셀4|
|---|---|---|---|
|내용 1|내용 2|내용 3|내용 4|


### 설정
|제목 셀1|제목 셀2|제목 셀3|제목 셀4|
|---|---|---|---|
|내용 1|내용 2|내용 3|내용 4|

## Architecture
![그림02](https://github.com/sksowk156/Hellth/assets/110645858/dad4558f-1a29-4091-8efa-a60eddd16cc3)
- **app** : Application, Splash Activity, MainActivity로  있고, Navigation에 Feature 모듈들의 Navigation들이 정의되어 있다. Feature 모듈들을 의존하고 있다.
- **buildSrc** : 모든 모듈들의 dependencies의 버전을 관리하고 있다. 
- **feature** : 모든 feature 모듈들은 서로 독립적(단일 책임) 이다. 오로지 core 모듈에만 종속적이다. ui 요소와 viewmodel을 포함한다.
    - **home** : 첫 화면으로 analyze fragment와 setting fragment를 연결해준다.
    - **analyze** : 졸음 감지 서비스를 제공하는 모듈이다.
    - **setting** : 졸음 감지 옵션 설정을 다루는 모듈이다.
    - **statistic** : 통계 정보를 제공하는 모듈이다.
- **core** : 모듈 간에 공유해야 하는 코드와 공통 dependencies를 관리하는 모듈이다. core 모듈 끼리는 종속 관계가 가능하다. 하지만 feature 모듈과 app 모듈에는 종속하지 않는다.
    - **common** : 모든 모듈에 공통적으로 사용되는 클래스나 변수, 함수 등이 정의되어 있다.
    - **common-ui** : feature 모듈에 사용되는 공통 ui 요소를 관리하는 모듈이다.
    - **model** : 앱 전체적으로 사용되는 데이터 모델이다.
    - **data** : 다양한 feature 모듈에 필요한 데이터를 여러 sources에 요청하거나 업데이트 및 저장을 해준다.
    - **domain** : usecase를 통해 ViewModel들에 사용되는 중복 코드를 단순화하고 제거한다. 그리고 data 모듈의 repository의 데이터를 중간 연산자를 통해 편집한다.
    - **datastore** : datastore를 관리하는 모듈이다.
    - **network** : REST 처리를 통해 네트워크를 요청하고 응답을 처리하는 모듈이다.
    - **database** : 앱 내부 저장소의 data를 관리하는 모듈이다.

### tools
- MLkit : 얼굴 인식 및 특징점 추출
- CameraX : 얼굴 분석
- RxBinding : Event 처리
- FlowBinding : Event 처리
- MediaPlayer : 음악 재생 기능 구현
- ExoPlayer : 음악 시작 구간 설정 기능 구현
- STT : 음성 인식 기능 구현
- TTS : 음성 안내 기능 구현
- Coroutine : 비동기 처리
- Flow : Flow의 중간 연산자를 활용한 비동기 처리
- Retrofit : 공공 데이터 요청
- Room : 음악 정보 저장
- DataStore : 옵션 정보 저장(볼륨, 안내 설정)
- Dagger Hilt : 의존성 주입
- Navigation : 모듈 간 화면 전환
- MpAndroidChart : 졸음 인식 결과 그래프 표현
- LeackCanary : 메모릭 누수 방지
- Location Service : 위치 정보 조회
- TED Permission : 권한 요청 및 확인
  
## Development Environment
- Android Studio Giraffe | 2022.3.1 Patch 1

### Application Version
- minSdkVersion : 26
- targetSdkVersion : 34
- javaVersion : 18
- kotlinVersion : 1.8.20

## License
License 파일 확인
