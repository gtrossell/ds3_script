package com.spectralogic.dsl.models

import com.spectralogic.dsl.exceptions.BpException
import com.spectralogic.ds3client.commands.GetBucketResponse
import com.spectralogic.ds3client.commands.GetBucketRequest
import com.spectralogic.ds3client.commands.GetServiceRequest
import com.spectralogic.ds3client.commands.PutBucketRequest
import com.spectralogic.ds3client.Ds3ClientImpl
import com.spectralogic.ds3client.networking.FailedRequestException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Represents a BlackPearl Client */
class BpClient extends Ds3ClientImpl {
  private final static logger = LoggerFactory.getLogger(BpClient.class)

  BpClient(Ds3ClientImpl ds3Client) { // just net client
    super(ds3Client.getNetClient())
  }

  /** @return BpBucket of bucket with given name */
  BpBucket bucket(String bucketName) {
    try {
      def response = this.getBucket(new GetBucketRequest(bucketName))
      return new BpBucket(response, this)
    } catch (FailedRequestException e) {
      throw new BpException(e)
    }
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
