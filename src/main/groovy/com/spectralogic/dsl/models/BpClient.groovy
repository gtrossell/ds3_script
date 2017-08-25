package com.spectralogic.dsl.models

import com.spectralogic.ds3client.networking.NetworkClient
import com.spectralogic.dsl.exceptions.BpException
import com.spectralogic.ds3client.commands.GetBucketRequest
import com.spectralogic.ds3client.commands.GetServiceRequest
import com.spectralogic.ds3client.commands.PutBucketRequest
import com.spectralogic.ds3client.Ds3ClientImpl
import com.spectralogic.ds3client.networking.FailedRequestException

/** Represents a BlackPearl Client */
class BpClient extends Ds3ClientImpl {

  BpClient(NetworkClient netClient) {
    super(netClient)
  }

  /** @return BpBucket of bucket with given name */
  BpBucket bucket(String bucketName) {
    def response = this.getBucket(new GetBucketRequest(bucketName))
    return new BpBucket(response, this)
  }

  /** @return the names of the buckets */
  List<String> buckets() {
    def response = this.getService(new GetServiceRequest())
    return response.getListAllMyBucketsResult().getBuckets().collect { it.getName() }
  }

  /** @return newly created BpBucket */
  BpBucket createBucket(String name, String dataPolicyId="") {
    try {
      putBucket(new PutBucketRequest(name))
      bucket(name)
    } catch (FailedRequestException e) {
      throw new BpException(e)
    }
  }

  String toString() {
    def details = this.netClient.getConnectionDetails()
    "endpoint: ${details.getEndpoint()}, " +
      "access_key: ${details.getCredentials().getClientId()}"
  }

}
