# Distributed-Key-Value-Store (분산 키-값 저장소)
## 개요

이 프로젝트는 대규모 서비스를 운영하는데 중요한 요소인 분산 시스템을 학습하기 위해 진행했습니다.
분산 키/값 저장소를 개발하면서 PACELC 이론, 안정 해시, 데이터 일관성 및 장애 회복성 등 분산 시스템과 관련된 다양한 개념을 이해하려고 노력했습니다.

## 분산 시스템이란?

분산 시스템은 서로 다른 머신들에 위치하는 독립된 컴포넌트들의 집합입니다. 각 컴포넌트들은 메시지를 통해 의사소통하며 공동의 목표를 달성합니다.</br>
분산 시스템은 하나의 엔드포인트를 갖고 있는 것처럼 나타내어집니다. 내부적으로는 여러 노드가 같은 목적을 수행함으로써 특정 노드에서 장애가 발생하더라도 전체 서비스의 가용성에는 영향을 받지 않습니다.

## 분산 키-값 저장소(Distributed Key-Value Store)

키-값 저장소는 메모리에 카-값 쌍을 저장하기 위해 설계된 NoSQL 데이터베이스입니다.

키는 고유 식별자 역할을 하며, 일반 텍스트 또는 해시 값일 수 있습니다. 성능상의 이유로 키는 짧을수록 좋으며, 값은 문자열, 객체, 리스트 등 여러 값을 저장할 수 있습니다.
이 프로젝트에서는 키-값 데이터 형식을 편의를 위해 JSON 형식으로 설정했습니다.

분산 키-값 저장소는 키-값 쌍을 여러 서버에 분산시켜 저장하는 방식입니다.

이에 앞서 프로젝트의 컨셉은 플랫폼 서비스에서 외부 협력사로부터 제공된 데이터를 정제하여 캐시한다고 가정합니다.

# 분산 시스템의 기본 이론
## CAP 이론

CAP 이론은 분산 시스템이 세 가지 속성인 일관성, 가용성, 파티션 감내성 모두 만족하는 설계는 불가능하다는 이론입니다.

- 일관성(Consistency): 모든 노드는 동일한 데이터를 가져야한다.
- 가용성(Availability): 일부 노드에 장애가 발생하더라도 정상 응답을 받을 수 있어야 한다.
- 파티션 감내(Partition tolerance): 네트워크 단절(장애)가 발생하더라도 시스템은 정상 동작해야 한다.

## PACELC 이론

PACELC는 CAP 이론의 `파티션 감내는 필수로 고려해야 한다.`, `네트워크 장애가 아닌 상황을 설명하지 못한다.`라는 한계를 보완하기 위해 나왔습니다.

PACELC는 네트워크 단절(P) 상황에서 가용성(A)과 일관성(C)을 고려해야 하며, 그 외(E, else) 상황에서 지연 시간(L)과 일관성(C)를 고려해야 한다고 말합니다.

![PACELC](images/PACELC.png)

이미지 출처: [scylladb](https://www.scylladb.com/glossary/pacelc-theorem/)

네트워크 단절로 인해 특정 노드에 접근할 수 없을 때 일관성을 위해 데이터 반영을 아예 실패하도록 할 수 있고, 가용성을 위해 접근 가능한 노드들에 먼저 데이터를 반영할 수 있습니다.
그리고 모든 노드에 데이터를 반영할 경우 그만큼 응답 시간이 길어질 수 있습니다. 이는 일관성이 고려될수록 시스템의 지연 시간이 길어지는 것을 말합니다.

PACELC를 기반으로 분산 시스템은 `PC/EC`, `PC/EL`, `PA/EC`, `PA/EL`로 분류할 수 있습니다.

PCEC는 네트워크 단절 시 일관성을 고려합니다. 정상 시 일관성을 고려합니다. 이는 데이터의 일관성을 위해 서비스의 지연시간과 가용성을 희생할 수 있습니다.

PCEL은 네트워크 단절 시 일관성을 고려합니다. 정상 시 짧은 지연 시간을 고려합니다.

PAEC는 네트워크 단절 시 가용성을 고려합니다. 정상 시 일관성을 고려합니다.

PAEL은 네트워크 단절 시 가용성을 고려합니다. 정상 시 짧은 지연 시간을 고려합니다.

이 프로젝트는 외부 협력사로부터 상품 상태를 검증하는 요구사항을 가정하여 `PA/EL` 기준을 만족하도록 설계했습니다.

## 데이터 파티션

대규모 트래픽을 수용하기 위해 모든 데이터를 단일 서버에서 처리할 수 없습니다.
단일 서버에서 모든 데이터를 처리할 경우 네트워크 단절 혹은 재해 등의 불특정 장애가 일어날 때 이는 단일 장애점(Single Point Of Failure, SPOF)이 되어 서비스 운영 중지 상황을 초래하기 때문입니다.

따라서 여러 서버에 데이터 저장해야 하는데 이때 고려해야 하는 점은 `데이터를 여러 서버에 고르게 분산하는 것과 각 서버가 추가/제거 될 때 데이터의 이동을 최소화할 수 있는것` 입니다.

이 문제를 해결하기 위해 Coordinator Server가 여러 노드를 조율합니다. 이 서버는 각 노드의 IP 주소를 알고(Service Discovery), 클라이언트로부터 인입되는 키-값 저장 요청과 키-값 조회 요청을 부하 분산합니다.</br>
이에 대한 기성 제품은 Zookeeper, Etcd, Eureka가 있습니다. 이 프로젝트에서는 구현 편의를 위해 Proxy 서버를 구성하고 Service Discovery 역할은 Configuration(yml) 파일에 분산 대상 Application IP를 등록합니다.

## 안정 해시(Consistent Hashing)

대규모 트래픽을 수용하기 위해서는 클라이언트의 요청을 고르게 분산해야 합니다.
안정 해시는 해시 테이블 크기가 조정될 때 평균적으로 `키의 개수/슬롯(노드)`의 개수 키만 재배치하는 기술입니다. 이로인해 수평적 규모 확장을 만족할 수 있습니다.

안정 해시는 해시 링이라는 논리적인 공간을 갖습니다. 이는 해시 공간 양 끝을 동그랗게 접은 링의 형태입니다.  그리고 각 서버를 해시 링 위에 배치합니다.

특정 키의 저장 위치는 해시 링에서 키의 위치를 기반으로 시계 방향으로 탐색하며 만나는 첫 번째 서버입니다.
그렇기 때문에 서버 추가/제거 시에도 서버마다 구간(파티션)이 나뉘어 있기 때문에 일부 키만 재배치하게 됩니다.

안정 해시는 파티션 크기를 균등하게 유지하지 못하는 문제, 키가 균등 분포가 어려운 문제가 있습니다.
이를 해결하기 위해 가상 노드를 이용할 수 있습니다.

가상 노드(Virtual Node)는 실제 물리 노드를 가리키는 가상의 노드입니다. 하나의 물리 노드는 여러 개의 가상 노드를 가질 수 있습니다.
물리 노드와 가상 노드들은 해시 링 위에 배치될 수 있습니다.

가상 노드를 통해 물리 노드는 여러 개의 파티션을 관리할 수 있게 됩니다. 가상 노드의 수가 많아지면 키의 분포는 더욱 균등해져서 데이터가 고르게 분포될 수 있습니다. 이로써 파티션의 크기를 균등하게 유지하지 못하는 문제와 키의 균등 분포 문제를 해결할 수 있습니다.

분산 키 값 저장소 프로젝트에서는 안정 해시를 구현하여 요청을 고르게 분산하도록 했고,  이에 사용되는 해시 함수는 MurmurHash를 채택했습니다.
MurmurHash는 비암호화 해시로 암호화의 안정성보다 성능과 해시 충돌의 최소화를 고려하여 만들어진 함수이기 때문에 선택했습니다.

![consistent_hashing](images/key_value_store_topology.png)

```text
분산 시스템의 데이터 분산 방식
MongoDB(Sharded Cluster - Hashed Sharding): MD5
Kafka: MurmurHash(Key) % Partition Count
Redis Cluster: CRC16 Hash(Key) % Hash Slot(16384)
```

## 데이터 다중화와 데이터 일관성

분산 시스템에서 높은 가용성(High Availability)을 확보하기 위해선 데이터를 N개의 서버에 비동기적으로 다중화 (replication)해야합니다.

안정 해시를 통해 데이터 파니셔닝을 구현하고, 불특정 장애 방지를 위해 데이터를 여러 서버에 저장합니다. 다중화된 데이터는 적절히 동기화를 하여 일관성을 유지해야 합니다.

데이터의 일관성을 보장할 수 있는 방법으로는 정족수 합의 프로토콜이 있습니다.
정족수 합의 프로토콜은 노드의 개수(N), 쓰기 연산의 정족수(W), 읽기 연산의 정족수(R)를 조정하여 분산된 노드들의 일관성 수준을 조절할 수 있습니다.

일관성의 수준은 강한 일관성, 약한 일관성, 최종 일관성이 있습니다.
- 강한 일관성(Strong Consistency): 모든 읽기 연산은 가장 최근에 갱신된 결과를 반환
- 약한 일관성(Weak Consistency): 읽기 연산은 가장 최근에 갱신된 결과를 반환하지 못할 수 있음
- 최종 일관성(Eventual Consistency): 약한 일관성의 형태로, 갱신 결과가 결국에는 모든 노드에 동기화

이 프로젝트에서 정족수 합의 프로토콜은 생략했습니다. 분산 시스템의 큰 흐름을 파악하는 목적에서 정족수 합의 프로토콜 등을 상세히 구현하는 것은 프로젝트의 목적에서 벗어날 수 있다고 생각했습니다.</br>
네트워크 단절 상황에서 구성되는 가용성을 확인해보고자 하기에 Primary 노드와 Secondary 노드에 값을 저장하여 데이터의 다중화와 일관성을 만족하도록 했습니다.

## 장애 감지와 장애 처리

분산 시스템은 네트워크 단절 상황을 피할 수 없는 현상으로 분류합니다.

장애 감지와 처리에 필요한 분산 시스템의 개념은 가십 프로토콜(Gossip Protocol) 입니다.
가십 프로토콜은 P2P 네트워크 환경을 구성하며 노드별로 멤버십 목록을 구성합니다. 멤버는 각 노드의 정보와 Health Check 카운트(Heartbeat Counter)를 갖고 있습니다.
그리고 주기적으로 자신의 박동 수를 올리고 멤버십 목록에 구성된 노드들과 이를 공유합니다.

특정 노드의 Heartbeat Counter가 갱신되지 않으면 그 외의 노드들은 해당 노드에서 장애가 발생한 것으로 판단합니다.

이 프로젝트에서는 Redis를 이용하여 가십 프로토콜을 간략화하여 구성했습니다. Redis를 이용하여 클라이언트로부터 유입되는 HTTP 요청과 장애 감지에 필요한 기능을 분리했습니다.

장애 감지와 장애 처리 구현 시 요구사항의 간소화를 위해 장애 유형은 특정 노드의 일시적인 네트워크 단절로 가정했습니다. 이를 구성하기 위해 3개의 분산 서비스를 구성하고 1개의 서비스를 ShutDown 했습니다.</br>
기대한 상황은 다음과 같습니다. 지속적인 HTTP 요청이 유입될 때 특정 구간에 응답이 실패하고 장애가 발생한 노드를 감지 및 처리하여 점진적으로 HTTP 요청이 성공 응답을 받는 것을 가정합니다. 그와 동시에 HTTP 실패 응답 또한 점진적으로 줄어들길 기대했습니다.

테스트는 python 기반의 벤치마크 툴인 locust를 이용했으며, 이를 통해 내결함성을 측정할 수 있습니다.
테스트 결과는 기대한 시나리오에 부합했습니다.

![fault-tolerance](images/total_requests_per_second_1683978393.png)

### 결론

분산 시스템의 기본 원리와 특징을 이해하고 실제 구현을 통해 분산 시스템의 구성 요소들을 이해할 수 있었습니다.
PACELC, 안정 해시, 정족수 합의 프로토콜 등을 알게 됐으며 이는 Kafka, MongoDB 등 여러 분산 시스템을 이해하는데 큰 도움이 됐습니다.
