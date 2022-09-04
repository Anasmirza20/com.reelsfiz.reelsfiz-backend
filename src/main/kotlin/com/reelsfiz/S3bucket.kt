package com.reelsfiz

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.*
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.content.writeToFile
import com.reelsfiz.Constants.REELS_BUCKET
import com.reelsfiz.Constants.REGION
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

suspend fun putDataInBucket() {
    val args = arrayOf(REELS_BUCKET, "jkfd", "/Users/anasmirza/Desktop/qwe.png")


    val usage = """
    Usage:
        <bucketName> <key> <objectPath> <savePath> <toBucket>

    Where:
        bucketName - The Amazon S3 bucket to create.
        key - The key to use.
        objectPath - The path where the file is located (for example, C:/AWS/book2.pdf).   
        savePath - The path where the file is saved after it's downloaded (for example, C:/AWS/book2.pdf).     
        toBucket - An Amazon S3 bucket to where an object is copied to (for example, C:/AWS/book2.pdf). 
        """

    val bucketName = args[0]
    val key = args[1]
    val objectPath = args[2]/*    val savePath = args[3]
        val toBucket = args[4]*/

    // Create an Amazon S3 bucket.
//    createBucket(bucketName)

    // Update a local file to the Amazon S3 bucket.
//    putObject(bucketName, key, objectPath)

    /* // Download the object to another local file.
     getObject(bucketName, key, savePath)

     // List all objects located in the Amazon S3 bucket.
     listBucketObs(bucketName)

     // Copy the object to another Amazon S3 bucket
     copyBucketOb(bucketName, key, toBucket)

     // Delete the object from the Amazon S3 bucket.
     deleteBucketObs(bucketName, key)

     // Delete the Amazon S3 bucket.
     deleteBucket(bucketName)*/
    println("All Amazon S3 operations were successfully performed")
}

suspend fun createBucket(bucketName: String) {

    val request = CreateBucketRequest {
        bucket = bucketName
    }

    val s3Client = S3Client { region = REGION }.use { s3 ->
        s3.createBucket(request)
        println("$bucketName is ready")
    }
}

suspend fun putObject(bucketName: String, objectKey: String, objectPath: String, result: (String) -> Unit) {
    val body = File(objectPath).asByteStream()
    val request = PutObjectRequest {
        bucket = bucketName
        key = objectKey
        contentType = "video/mp4"
        body.contentLength?.let { contentLength = it }
        this.body = body
    }

    S3Client { region = REGION }.use { s3 ->
        s3.putObject(request)
        val url = "https://$REELS_BUCKET.s3.$REGION.amazonaws.com/"
        result(url)
    }
}

suspend fun getObject(bucketName: String, keyName: String, path: String, result: (PutObjectResponse) -> Unit) {

    val request = GetObjectRequest {
        key = keyName
        bucket = bucketName
    }

    S3Client { region = REGION }.use { s3 ->
        s3.getObject(request) { resp ->
            val myFile = File(path)
            resp.body?.writeToFile(myFile)
            println("Successfully read $keyName from $bucketName")
        }
    }
}

suspend fun listBucketObs(bucketName: String) {

    val request = ListObjectsRequest {
        bucket = bucketName
    }

    S3Client { region = REGION }.use { s3 ->

        val response = s3.listObjects(request)
        response.contents?.forEach { myObject ->
            println("The name of the key is ${myObject.key}")
            println("The owner is ${myObject.owner}")
        }
    }
}

suspend fun copyBucketOb(fromBucket: String, objectKey: String, toBucket: String) {

    var encodedUrl = ""
    try {
        encodedUrl = URLEncoder.encode("$fromBucket/$objectKey", StandardCharsets.UTF_8.toString())
    } catch (e: UnsupportedEncodingException) {
        println("URL could not be encoded: " + e.message)
    }

    val request = CopyObjectRequest {
        copySource = encodedUrl
        bucket = toBucket
        key = objectKey
    }
    S3Client { region = REGION }.use { s3 ->
        s3.copyObject(request)
    }
}

suspend fun deleteBucketObs(bucketName: String, objectName: String) {

    val objectId = ObjectIdentifier {
        key = objectName
    }

    val delOb = Delete {
        objects = listOf(objectId)
    }

    val request = DeleteObjectsRequest {
        bucket = bucketName
        delete = delOb
    }

    S3Client { region = REGION }.use { s3 ->
        s3.deleteObjects(request)
        println("$objectName was deleted from $bucketName")
    }
}

suspend fun deleteBucket(bucketName: String?) {

    val request = DeleteBucketRequest {
        bucket = bucketName
    }
    S3Client { region = REGION }.use { s3 ->
        s3.deleteBucket(request)
        println("The $bucketName was successfully deleted!")
    }
}

