/*
 * This is an example use of the shell that records a script that takes a bucket name as an argument and produces a
 * variable named 'bucket'. The script is saved to the working directory and can be executed from the shell.
 */

/*
spectra> args = ['my_bucket']
===> [my_bucket]
spectra> :r getBucket
===>
[INFO] Started recording script
spectra> bucket = client.getBucket(args[0])
===> name: my_bucket, client: {endpoint: 192.168.202.3:8080, access_key: QWRtaW5pc3RyYXRvcg==}
spectra> :r
===>
[INFO] Saved script to 'C:\Users\Sage\getBucket.groovy'
spectra> bucket = null
===> null
spectra> :e getBucket my_bucket
===>
spectra> assert bucket.name == 'my_bucket'
===> null
spectra>
*/
