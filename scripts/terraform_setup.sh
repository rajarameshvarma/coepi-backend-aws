#!/bin/sh

aws s3api create-bucket \
  --bucket $TFSTATE_BUCKET \
  --region us-east-1 \
  --acl private

aws s3api put-bucket-encryption \
  --bucket $TFSTATE_BUCKET \
  --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'
