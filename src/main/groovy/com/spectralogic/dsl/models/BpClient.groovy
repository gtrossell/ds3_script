package com.spectralogic.dsl.models

import com.spectralogic.ds3client.Ds3ClientImpl
import com.spectralogic.ds3client.commands.GetBucketResponse
import com.spectralogic.ds3client.commands.GetBucketRequest
import com.spectralogic.ds3client.commands.GetServiceRequest
import com.spectralogic.ds3client.commands.PutBucketRequest

class BpClient extends Ds3ClientImpl {
  
  def BpClient(Ds3ClientImpl ds3Client) {
    super(ds3Client.getNetClient())
  }

  /** @return BpBucket of bucket with given name */
  def bucket(String bucketName) {
    // TODO: if no bucket is found return null and issue warning
    try {
      def response = this.getBucket(new GetBucketRequest(bucketName))
      return new BpBucket(response, this)
    } catch (com.spectralogic.ds3client.networking.FailedRequestException e) {
      println e
      return null
    }
  }

  /** @return the names of the buckets */
  List<String> buckets() {
    def buckets = []
    def response = this.getService(new GetServiceRequest())
    response.getListAllMyBucketsResult().getBuckets().each{ bucket -> 
      buckets << bucket.getName()
    }
    return buckets
  }

  /** @return BpBucket of newly created BP bucket */
  def createBucket(String name, String dataPolicyId="") {
    // TODO: impliment dataPolicyId
    if (this.bucket(name)) {
      println "[Error] Bucket with name '$name' already exists!"
      return null
    }
    this.putBucket(new PutBucketRequest(name))
    bucket(name)
  }

  String toString() {
    def details = this.netClient.getConnectionDetails()
    "endpoint: ${details.getEndpoint()}, " +
    "access_key: ${details.getCredentials().getClientId()}"
  }

}
