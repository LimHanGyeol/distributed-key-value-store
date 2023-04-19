import requests
import time
import matplotlib.pyplot as plt
import numpy as np

# POST 요청 보내는 함수
def send_post_request(index):
    url = 'http://localhost:8080/put'
    headers = {'Content-Type': 'application/json'}
    data = {'key': 'key{}'.format(index), 'value': 'value{}'.format(index)}
    response = requests.post(url, headers=headers, json=data)
    return response.status_code

# GET 요청 보내는 함수
def send_get_request(index):
    url = 'http://localhost:8080/get?key=key{}'.format(index)
    response = requests.get(url)
    return response.status_code

# 각 요청을 0.5초, 0.2초 딜레이와 함께 n번씩 보내기
post_response_data = []
get_response_data = []
for i in range(1, 151):
    # POST 요청 보내기
    time.sleep(0.5)
    post_response_code = send_post_request(i)
    post_response_data.append((i, post_response_code))

    # GET 요청 보내기
    time.sleep(0.2)
    get_response_code = send_get_request(i)
    get_response_data.append((i, get_response_code))

# 상태 코드 선 그래프로 시각화
post_status_codes = [data[1] for data in post_response_data]
get_status_codes = [data[1] for data in get_response_data]
plt.plot(post_status_codes, label='POST')
plt.plot(get_status_codes, label='GET')
plt.legend()
plt.xlabel('Request Index')
plt.ylabel('Status Code')
plt.yticks([200, 500])
plt.show()
