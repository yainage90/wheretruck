#!/usr/bin/python3

import requests
from datetime import datetime
from elasticsearch import Elasticsearch


url='https://www.data.go.kr/download/15028208/standard.do?dataType=json'
response=requests.get(url)
records=response.json()['records']

def make_doc(obj):
	
	try:
		capacity = obj.get('푸드트럭운영대수') 
		capacity = float(capacity)
	except ValueError:
		capacity = -1

	try:
		regionType = obj.get('장소유형')
	except ValueError:
		retgionType = -1


	roadAddress = obj.get('소재지도로명주소').strip()
	postAddress = obj.get('소재지지번주소').strip()

	if roadAddress:
		tokens = roadAddress.split()
		city = tokens[0]
		town = tokens[1]
	elif postAddress:
		tokens = postAddress.split()
		city = tokens[0]
		town = tokens[1]
	

	doc={
		'regionName': obj.get('허가구역명'),
		'regionType': regionType,
		'city': city,
		'town': town,
		'roadAddress': roadAddress,
		'postAddress': postAddress,
		'geoLocation': {'lon': float(obj.get('경도') or 0.0), 'lat': float(obj.get('위도') or 0.0)},
		'capacity': capacity,
		'cost': obj.get('허가구역사용료'),
		'permissionStartDate': obj.get('허가구역운영시작일자'),
		'permissionEndDate': obj.get('허가구역운영종료일자'),
		'closedDays': obj.get('허가구역휴무일'),
		'weekdayStartTime': obj.get('허가구역평일운영시작시각'),
		'weekdayEndTime': obj.get('허가구역평일운영종료시각'),
		'weekendStartTime': obj.get('허가구역주말운영시작시각'),
		'weekendEndTime': obj.get('허가구역주말운영종료시각'),
		'restrictedItems': obj.get('판매제한품목'),
		'agencyName': obj.get('관리기관명'),
		'agencyTel': obj.get('관리기관전화번호')
	}
	return doc


es = Elasticsearch('http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200')
index='permission_region'

def index_doc(doc):
	es.index(index=index, body=doc)

for obj in records:
	doc=make_doc(obj)
	print(doc)
	index_doc(doc)
