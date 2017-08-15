package com.spectralogic.dsl.models

import com.spectralogic.ds3client.commands.GetBucketResponse
import com.spectralogic.ds3client.commands.GetBucketRequest
import com.spectralogic.ds3client.commands.GetServiceRequest
import com.spectralogic.ds3client.commands.PutBucketRequest
import com.spectralogic.ds3client.Ds3ClientImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Represents a BlackPearl Client */
class BpClient extends Ds3ClientImpl {
  private final static logger

  BpClient(Ds3ClientImpl ds3Client) { // just net client
    super(ds3Client.getNetClient())
    logger = LoggerFactory.getLogger(BpClient.class)
  }

  /** @return BpBucket of bucket with given name */
  BpBucket bucket(String bucketName) {
    try {
      def response = this.getBucket(new GetBucketRequest(bucketName))
      return new BpBucket(response, this)
    } catch (com.spectralogic.ds3client.networking.FailedRequestException e) {
      logger.error("Failed!", e)
      return null // custom error
    }
  }

  /** @return the names of the buckets */
  List<String> buckets() {
    def buckets = []
    def response = this.getService(new GetServiceRequest())
    response.getListAllMyBucketsResult().getBuckets().each { buckets << it.getName() }
    return buckets
  }

  /** @return newly created BpBucket */
  BpBucket createBucket(String name, String dataPolicyId="") {
    // TODO: implement dataPolicyId
    if (bucket(name)) {
      logger.error("Bucket with name '{}' already exists!", name)
      return null
    }
    putBucket(new PutBucketRequest(name))
    bucket(name)
  }

  String toString() {
    def details = this.netClient.getConnectionDetails()
    "endpoint: ${details.getEndpoint()}, " +
      "access_key: ${details.getCredentials().getClientId()}"
  }

}
