# raspberrypi5 Custom Layer

## 1. recipe-BSP 프로젝트
- dropbear 패키지 설치(SSH 연결 이용)
- 기존 ttyAMA1 -> ttyAMA0으로 설정

## 2. C어플리케이션 프로젝트
- C 언어로 간단한 실행 파일(`hellokim`)을 작성
- Yocto 빌드 시스템을 통해 rootfs(`/usr/bin/`)에 포함시키는 레시피를 작성