#!/usr/bin/bash

curl -XPUT 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/permission_region?pretty' \
-H 'Content-Type: application/json' \
-d \
'{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
      "regionName": {
        "type": "keyword"
      },
      "regionType": {
        "type": "integer"
      },
      "city": {
        "type": "keyword"
      },
      "town": {
        "type": "keyword"
      },
      "roadAddress": {
        "type": "text"
      },
      "postAddress": {
        "type": "text"
      },
      "geoLocation": {
        "type": "geo_point"
      },
      "capacity": {
        "type": "integer"
      },
      "cost": {
        "type": "keyword"
      },
      "permissionStartDate": {
        "type": "date"
      },
      "permissionEndDate": {
        "type": "date"
      },
      "closedDays": {
        "type": "keyword"
      },
      "weekdayStartTime": {
        "type": "keyword"
      },
      "weekdayEndTime": {
        "type": "keyword"
      },
      "weekendStartTime": {
        "type": "keyword"
      },
      "weekendEndTime": {
        "type": "keyword"
      },
      "restrictedItems": {
        "type": "keyword"
      },
      "agencyName": {
        "type": "keyword"
      },
      "agencyTel": {
        "type": "keyword"
      }
    }
  }
}'

curl -XPUT 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/truck?pretty' \
-H 'Content-Type: application/json' \
-d \
'{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
	  "id": {
		"type": "keyword"
	  },
      "name": {
        "type": "keyword"
      },
      "geoLocation": {
        "type": "geo_point"
      },
      "description": {
        "type": "keyword"
      },
      "opened": {
        "type": "boolean"
      }
    }
  }
}'

curl -XPUT 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/menu?pretty' \
-H 'Content-Type: application/json' \
-d \
'{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
	  "id": {
		"type": "keyword"
	  },
      "truckId": {
        "type": "keyword"
      },
      "name": {
        "type": "keyword"
      },
      "cost": {
        "type": "integer"
      },
      "description": {
        "type": "keyword"
      },
      "picture": {
        "type": "dense_vector",
        "dims": 128
      }
    }
  }
}'

curl -XPUT 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/review?pretty' \
-H 'Content-Type: application/json' \
-d \
'{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
	  "id": {
		"type": "keyword"
	  },
      "userId": {
        "type": "keyword"
      },
      "truckId": {
        "type": "keyword"
      },
      "star": {
        "type": "byte"
      },
      "comment": {
        "type": "keyword"
      }
    }
  }
}'

curl -XPUT 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/favorite?pretty' \
-H 'Content-Type: application/json' \
-d \
'{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
	  "id": {
		"type": "keyword"
	  },
      "userId": {
        "type": "keyword"
      },
      "truckId": {
        "type": "keyword"
      }
    }
  }
}'
