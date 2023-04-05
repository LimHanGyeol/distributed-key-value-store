# distributed-key-value-stsore (분산 키-값 저장소)

## 목적

- 분산 컴퓨팅 환경의 이해

## 컨셉

- 이커머스 서비스에서 협력사의 상품 데이터를 관리하기 위한 Key-Value 저장소
- 해당 서비스는 상품 주문 시 재고 상황을 협력사에서 검수하는 과정이 있다.
  그러므로 가져온 상품을 노출하는 역할만 제공한다.
- CAP - PACELC 이론을 사용하여 PA, EL 상황을 만족하도록 한다.
    - Network Partition 상황에 Availability(가용성) 을 우선한다.
        - 특정 노드에 장애가 발생되더라도 사용자는 서비스를 이용할 수 있어야한다.
    - Else 상황에 Latency(응답시간)를 우선한다.
        - 주문 시 추가 검수 과정이 있어 철저한 정합성 검증에 한계가 있다.
          사용자에게 빠르게 응답하여 원활한 서비스 경험을 제공한다.

## 프로젝트 구조

### Key-Value Proxy Application

- 분산되어있는 Key-Value Application 을 관리하는 Coordination 역할을 한다.
    - Discovery: Config File 로 간략화하여 ZooKeeper 와 etcd 를 대체한다.
- Consistent Hashing(안정 해시) 을 사용하여 요청을 분산처리 한다.
- Key-Value 인입 시 Primary Node 와 Secondary Node 로 데이터를 저장하여 데이터 다중화를 만족한다.
- 데이터 일관성은 W = 1 을 설정하여 빠른 쓰기 연산을 가정한다. HTTP 요청을 통해 정족수 합의 프로토콜을 대체한다.

### Key-Value Store

- Key-Value 를 Memory 에 저장하는 기본적인 기능 구성

### Key-Value Application

- Proxy 를 통해 들어온 Put, Get 요청을 처리한다.
- Redis 를 이용하여 가십 프로토콜(Gossip Protocol) 을 대체한다.
    - 가십 프로토콜은 Application 의 Scale-up과 Scale-out 에 영향을 받으면 안된다.
