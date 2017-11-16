
bucketName = "my_bucket"
basePath = "/home/user/documents"
fileNames = ["$basePath/doc1.txt", "$basePath/doc2.txt", "$basePath/other_docs/"]
bucketPath = "Documents/"

client.bucket(bucketName).putBulk(fileNames, bucketPath)
