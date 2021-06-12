#!/usr/bin/bash

#region
curl -XPUT 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/region?pretty' \
-u "${ES_USER}:${ES_PASSWORD}" \
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

#truck
curl -XPUT 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/truck?pretty' \
-H 'Content-Type: application/json' \
-u "${ES_USER}:${ES_PASSWORD}" \
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
       },
       "userId": {
         "type": "keyword"
       },
       "foods": {
         "type": "nested",
         "properties": {
           "id": {
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
           "imageUrl": {
             "type": "keyword"
           }
         }
       },
       "ratings": {
         "type": "nested",
         "properties": {
           "id": {
             "type": "keyword"
           },
           "userId": {
             "type": "keyword"
           },
           "star": {
             "type": "byte"
           },
           "comment": {
             "type": "keyword"
           },
           "createdDate": {
             "type": "date",
             "format": "yyyy-MM-dd HH:mm:ss"
           },
           "updatedDate": {
             "type": "date",
             "format": "yyyy-MM-dd HH:mm:ss"
           }
         }
       }
    }
  }
}'


#user
curl -XPUT 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/user?pretty' \
-u "${ES_USER}:${ES_PASSWORD}" \
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
      "role": {
        "type": "keyword"
      },
      "nickName": {
        "type": "keyword"
      },
      "favorites": {
        "type": "nested"
      }
    }
  }
}'

