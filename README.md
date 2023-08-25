Carflix App
===
What is Carflix?
---

+ 차 키가 아닌 스마트폰을 이용하여 시동을 걸 수 있게 하는 서비스
+ 앱을 이용하여 차량의 블루투스 모듈과 연결해 시동을 건다.
+ 운전자 및 주차된 위치를 파악할 수 있다.

Carflix 서비스의 목표는 다음과 같다.

 1. 스마트폰과 블루투스로 통신하며 자동차 키의 기능을 모두 수행할 수 있는 차량 모듈을 설계한다.
 2. 서버와 차량 모듈 간 통신을 중재할 수 있는 스마트폰 앱을 설계한다.
 3. 특정 그룹에 속한 사람만 스마트폰을 통해 차량을 제어하거나 차량의 위치 정보를 조회할 수 있도록 한다. 
 4. 그룹의 차량과 회원을 관리하는 웹페이지를 만들고, 권한이 있는 사용자가 이를 이용할 수 있게 한다.

이 레포지토리는 carflix 서비스 구성요소 중 **스마트폰 앱**에 대한 레포지토리이다.

## 기능 설계
### 1. 회원가입 및 로그인

<img src="https://github.com/simjeehoon/src_repository/blob/master/CarflixApp/master/login.png?raw=true" title="로그인" alt="login img"></img><br/>

> + 처음 안드로이드 앱을 실행하면 [그림 8]과 같이 회원가입 혹은 로그인을 선택하는 화면이 나온다. 
> + 회원가입 화면에 진입한 뒤, 올바르게 정보를 입력하고 회원가입 버튼을 누르면 [그림 7]과 같이 데이터베이스에 정보가 갱신된다. 
> + 가입에 성공하였다면 로그인 화면에서 아이디와 비밀번호를 입력하여 로그인할 수 있다.

### 2. 그룹 생성

<img src="https://github.com/simjeehoon/src_repository/blob/master/CarflixApp/master/group.png?raw=true" title="그룹생성" alt="group img"></img><br/>

> - 로그인에 성공하였다면 [그림 9]와 같이 API 서버로부터 전달받은 그룹 목록을 출력한다. 
> - 그룹 생성은 액션 바의 메뉴를 통해 진행할 수 있다. 
> - 그룹은 특성에 따라 소규모, 대규모, 렌트로 구분이 된다. 
> + 소규모는 가족 등 인원이 적은 단체, 대규모는 회사 등 인원이 많은 단체, 렌트는 렌터카 업체를 대상으로 한다. 
> + 구분한 이유는 추후에 기능을 확장하기 위해서이다.
> - 그룹 생성에 필요한 정보를 입력하고 그룹 생성 버튼을 누르면 [그림 10]과 같이 데이터베이스에 정보가 갱신된다. 

### 3. 차량 등록 및 삭제

<img src="https://github.com/simjeehoon/src_repository/blob/master/CarflixApp/master/addcar.png?raw=true" title="차량등록" alt="add car"></img><br/>

> + 그룹 리스트에서 그룹을 선택하면 차량 목록이 나온다. 그룹 대표자는 [그림 11]과 같이 그룹에 차량을 추가하거나 삭제할 수 있다. 
> + 차량을 추가할 경우 안드로이드는 서버로부터 받은 자동차 ID를 차량 모듈에 할당한다. 성공적으로 할당하면 [그림 14]와 같이 차량 정보가 데이터베이스에 갱신된다.
> + 차량을 삭제할 때는 서버로부터 받은 블루투스 MAC 주소를 이용하여 차량과 연결한 뒤 모듈에 저장된 차량 ID를 삭제한다. 
> + 이후에 데이터베이스에서도 해당 레코드를 삭제한다. 만일 모듈 내 ID가 이미 강제로 삭제되었다면 데이터베이스에서 레코드만 지운다.

### 4. 차량 제어

<img src="https://github.com/simjeehoon/src_repository/blob/master/CarflixApp/master/controlcar.png?raw=true" title="차량제어" alt="control car"></img><br/>

> + 안드로이드 앱에서 차량을 선택하게 되면 [그림 15], [그림 16]과 같이 스마트폰에 저장된 본인 인증 수단으로 본인 인증을 진행한다. 
> + 인증 후 [그림 17]처럼 자동차 키에 있는 기능들을 버튼을 눌러서 실행할 수 있다. 트렁크의 경우 모형 구현의 편의성을 위해 열기와 닫기를 구분하였다. [그림 17]의 문 및 트렁크 제어 버튼을 누르면 [그림 3]의 과정을 거친 뒤 [그림 18]처럼 차량을 제어한다.
> + 시동 걸기 버튼을 누르고 시동을 걸게 되면 안드로이드 앱에서 포그라운드 서비스를 생성한다. 서비스는 3초마다 차량 모듈로부터의 응답을 받고, [그림 19]와 같이 위치 정보를 서버로 전송하는 작업을 한다.

### 5. 차량 위치 조회

> * 차량 목록에서 특정 차량의 이용 정보 조회 버튼을 누르면 [그림 20]과 같이 차량 정보가 표시된다. 
> * 이 화면에는 누가 운전하고 있는지, 차량이 어디에 위치하는지에 대한 정보가 담겨있다.

<img src="https://github.com/simjeehoon/src_repository/blob/master/CarflixApp/master/invitecode.png?raw=true" title="초대코드" alt="invite code"></img><br/>

### 6. 그룹 초대 및 그룹 가입
 > * [그림 21]과 같이 대표자 혹은 부매니저가 초대 코드를 생성할 수 있다. 
 > * 그룹에 속하지 않은 사람은 [그림 22]와 같이 초대 코드를 입력하여 그룹에 가입한다.

---
