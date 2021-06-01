#!/usr/bin/bash

curl -XDELETE 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/region?pretty'
curl -XDELETE 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/truck?pretty'
curl -XDELETE 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/food?pretty'
curl -XDELETE 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/rating?pretty'
curl -XDELETE 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/favorite?pretty'
curl -XDELETE 'http://ec2-13-209-181-246.ap-northeast-2.compute.amazonaws.com:9200/user?pretty'
