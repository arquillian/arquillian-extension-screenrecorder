Screen Recorder
==============

Screen Recorder is an extension to Arquillian which provides video record of the running tests. It can also take screenshots of the application before and after the test. 

Maven dependency
----------------

    <groupId>org.jboss.arquillian.extension</groupId>
    <artifactId>arquillian-screen-recorder</artifactId>
    <version>1.0.0.Alpha1-SNAPSHOT</version>

Settings
--------

There are various settings which can be used to alter Screen Recorder behaviour. You should put those settings into *arquillian.xml* file in project which contains tests.

### Example 

    <extension qualifier="screenRecorder">
        <property name="rootFolder">target</property>
        <property name="videoFolder">video</property>
        <property name="videoName">myTestVideo</property>
        <property name="enableScreenshots">false</property>
        <property name="recordingType">TEST</property>
    </extension>

### List of settings

* **rootFolder** - folder which will contain all screenshots and videos, defaults to *target/media*
* **videoFolder** - name of the folder which contains video record(s), it is placed inside root folder. Defaults to video
* **screenshotFolder** - same as videoFolder, defaults to screenshot
* **videoName** - this will be video name (in case you are recording one video for all tests), defaults to record
* **imageFileType** - filetype of produced screenshots, allowed types are PNG, JPG and GIF. Default is PNG
* **enableScreenshots** - no screenshots will be made if this flag is false (default is true)
* **enableVideo** - use this to turn video recording off (default is true)
* **recordingType** - enum value which tells Screen Recorder when to save screenshots and videos 
	* **SUITE** - takes one video for all tests and screenshots before and after each test
	* **TEST** - takes one video for every test and screenshots before and after each test
	* **FAILURE** - take video or screenshots only if test fails (default option)
* **frameRate** - you can adjust frame rate of video by this property. One frame is 1000 / frameRate property. Default is 20, so test video is recorded in 50 frames per second
* **testTimeout** - you can set default timeout for each test, this timeout will be used to stop the video recording in order to save space on hard drive. Defautls to one hour


Also see the class [SystemProperties](https://github.com/qa/arquillian-screen-recorder/blob/master/src/main/java/org/jboss/arquillian/extension/screenRecorder/SystemProperties.java) which contains a list of properties and the [RecorderConfiguration](https://github.com/qa/arquillian-screen-recorder/blob/master/src/main/java/org/jboss/arquillian/extension/screenRecorder/RecorderConfiguration.java) which contains their defaults.

 
Notes
-----

Video files are automatically saved in MP4, I will try to provide more formats in the future. Size of the video can be altered via frameRate property. You can also decide if you want to take one video for every test (so this video is named after the test method) or take one video of whole testsuite (this is the default). Screenshots can be taken before and after each test or after test has been failed. Format for images is PNG, this can be changed to GIF or JPG. 

There is a cleanup after the suite which deletes every unused (empty) folder. Screen Recorder also cleans whole root folder before beginning of the suite.
