/**********************************/
/* test                           */
/*--------------------------------*/
/* Spectra BlackPearl DSL script  */
/*                                */
/* Created on 06-13-2018          */
/**********************************/
import java.io.File
import groovy.io.FileType

public List bucket(String bucketName)
{
        def bucketContents = []

        bucket = client.getBucket(bucketName)
        println bucket //shows the bucket
        if (bucket.exists == true)
        {
                allObjs = bucket.getAllObjects().iterator()

                while (allObjs)
                {
                        objs = allObjs.take(1).collect().toString()
                        bucketContents << objs.replace("name: ", "").replace("[", "").replace("]", "")
                }
                //println bucketContents //debug
                return bucketContents
        }
        else
        {
                println "Bucket doesn't exist"
                return void
        }
}

public void readFromFile ()
{
        new File("./file.txt").eachLine
        {
                line -> dirArray << line
                //println dirArray
        }
}

/* ------  MAIN ------ */
def bucketContent = []
def listFiles


        bucketContent = bucket('test_bucket')
        //println bucketContent //debug

        listFiles = {File dir -> dir.listFiles().collect { File file -> file.isFile() ? file : listFiles(file)}.flatten()}
        listFiles = listFiles(new File('./')).toString().replace("./", "")
        println listFiles //debug

        if (bucketContent.containsAll(listFiles) == false)
        {
                println "Working"
        }
