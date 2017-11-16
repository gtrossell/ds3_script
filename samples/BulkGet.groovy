
bucketName = "my_bucket"
objectNames = ["object1.txt", "object2.txt", "objects/"]
destinationPath = "/path/to/destination/"

client.bucket(bucketName).getBulk(objectNames, destinationPath)
