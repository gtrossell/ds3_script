package com.spectralogic.dsl.models

import com.spectralogic.dsl.exceptions.BpException
import com.spectralogic.ds3client.commands.DeleteObjectRequest
import com.spectralogic.ds3client.commands.DeleteObjectResponse
import com.spectralogic.ds3client.commands.GetBucketResponse
import com.spectralogic.ds3client.commands.GetObjectRequest
import com.spectralogic.ds3client.commands.spectrads3.GetBulkJobSpectraS3Request
import com.spectralogic.ds3client.Ds3ClientImpl
import com.spectralogic.ds3client.models.bulk.Ds3Object
import com.spectralogic.ds3client.models.Contents
import com.spectralogic.ds3client.models.User

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

/** Represents a BlackPearl Object */
class BpObject extends Ds3Object {
  private final BpBucket bucket
  private final Ds3ClientImpl client
  private final User owner
  final Map metadata

  BpObject(Contents contents, BpBucket bucket, Ds3ClientImpl client) {
    this.name = contents.getKey()
    this.size = contents.getSize()
    this.owner = contents.getOwner()
    this.bucket = bucket
    this.client = client
    this.metadata = [
      name:       this.name, 
      size:       this.size, 
      owner:      this.owner,
      bucketName: this.bucket.name
    ]
  }

  /** 
   * Deletes the object from the BP 
   * @return true if object was deleted 
   */
  void delete() {
    def deleteResponse = client.deleteObject(new DeleteObjectRequest(bucket.name, name))
    bucket.reload()
  }

  /** 
   * Write object to given directory.
   * It will create the directory path if it does not exist
   * @param path  directory to write object to
   * @return true if the write was successful
   */
  void writeTo(String pathStr) {
    def path = Paths.get(pathStr)

    if (!Files.exists(path)) Files.createDirectory(path)
    
    /* convert BpObject to Ds3Object */
    def objectList = [new Ds3Object(this.getName())]
    def bulkRequest = new GetBulkJobSpectraS3Request(bucket.name, objectList)
    def bulkResponse = this.client.getBulkJobSpectraS3(bulkRequest)
    
    def list = bulkResponse.getMasterObjectList()
    for (objects in list.getObjects()) {
      for (obj in objects.getObjects()) {
        def channel = FileChannel.open(
          path.resolve(obj.getName()),
          StandardOpenOption.WRITE,
          StandardOpenOption.CREATE
        )
        
        channel.position(obj.getOffset());
        
        client.getObject(new GetObjectRequest(
          bucket.name,
          obj.getName(),
          channel,
          list.getJobId().toString(),
          obj.getOffset()
        ))
      }
    }
  }

}
