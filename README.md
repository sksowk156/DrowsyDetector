# WatchOut
![WatchOut - 복사본_1](https://github.com/sksowk156/Hellth/assets/110645858/f698d666-4118-4221-b2d3-27e4cb323b0a)
- email address : sksowk156@gmail.com <br />

## Features
WatchOut은 졸음을 방지 서비스입니다.<br />
졸음을 감지하면 경고음을 울리고, 주변 휴식 공간 정보를 제공합니다.<br />
경고음을 커스텀할 수 있고, 졸음 감지 결과를 확인할 수 있습니다.<br />

### Screenshots
![KakaoTalk_20231220_220504776](https://github.com/sksowk156/Hellth/assets/110645858/3d27a815-3737-492d-8fc4-060ebabec329)

## Usage
### 홈
|<img src="https://github.com/sksowk156/Hellth/assets/110645858/ce3c4b23-8e06-4cab-b47f-75b6148ce55e" width="216" height="384"/><br />|
|---|
|-홈 화면입니다.<br />-졸음 감지, 통계, 설정 메뉴가 있습니다.|

### 졸음 감지
|<img src="https://github.com/sksowk156/Hellth/assets/110645858/a337d7b9-70d7-48d5-8035-1cde4b8195c5" width="216" height="384"/><br />|<img src="https://github.com/sksowk156/Hellth/assets/110645858/cef0efa7-8f41-44fc-95d6-c2f59578f861" width="216" height="384"/><br />|
|---|---|
|1. 카메라를 정면으로 응시해 현재 눈 상태를 설정한다.<br />2. 등록한 눈 상태를 기반으로 실시간 눈 상태를 계산한다.<br />3. 졸음이 감지될 경우<br />>> 경고음을 알린다.(기본 음악 또는 사용자 설정 음악)<br />>> 최단 거리에 있는 휴식 공간의 위치를 음성 안내 서비스로 알려준다.|1. 홈 버튼을 눌러 앱을 floating view로 전환한 뒤 다른 앱과 동시에 사용한다.|
   
### 통계
|<img src="https://github.com/sksowk156/Hellth/assets/110645858/161ca01c-7296-4129-b6eb-6c6f402ce2ff" width="216" height="384"/><br />|
|---|
|1. 졸음 감지 서비스를 사용하고 나서 그 통계 정보를 볼 수 있다.<br />>> 정해진 시간을 주기로 눈을 깜빡인 횟수와 졸음 인식 횟수 정보를 보여준다.|


### 설정
|<img src="https://github.com/sksowk156/Hellth/assets/110645858/5656f052-5009-4005-8a56-bd5eddc47a80" width="216" height="384"/><br />|<img src="https://github.com/sksowk156/Hellth/assets/110645858/886362f1-5013-4746-90e7-203ec2ad87bc" width="216" height="384"/><br />|
|---|---|
|1. 졸음 알림음을 선택할 수 있다.<br />>>기본 알림음 : 3개의 기본 음악이 존재한다.<br />>>사용자 설정 음악 : 사용자 선택하는 음악이 3초간 재생된다.<br />2. 알림음 크기를 조절할 수 있다.<br />>>사이드 버튼으로도 조절 가능<br />3. 휴식 장소 안내를 설정할 수 있다.|1. 음악의 구간을 설정할 수 있다.|

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
[License](https://github.com/sksowk156/WatchOut/blob/main/LICENSE)
