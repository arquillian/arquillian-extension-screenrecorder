Screen Recorder
==============

Screen Recorder is an extension to Arquillian which provides video record of the running tests. It can also take screenshots of the application before and after the test. 

Maven dependency
----------------

    <groupId>org.jboss.arquillian.extension</groupId>
    <artifactId>arquillian-screen-recorder</artifactId>
    <version>1.0</version>

Settings
--------

There are various settings which can be used to alter Screen Recorder behaviour. You should put those settings into *arquillian.xml* file in project which contains tests.

### Example 

    <extension qualifier="screenRecorder">
        <property name="rootFolder">target</property>
        <property name="videoFolder">video</property>
        <property name="videoName">myTestVideo</property>
        <property name="shouldTakeScreenshots">false</property>
    </extension>`

### List of settings

Also see the class [SystemProperties](https://github.com/pmensik/screenRecorder/blob/master/src/main/java/org/jboss/arquillian/extension/screenRecorder/SystemProperties.java) which contains a list of properties and the [RecorderConfiguration](https://github.com/pmensik/screenRecorder/blob/master/src/main/java/org/jboss/arquillian/extension/screenRecorder/RecorderConfiguration.java) which contains their defaults.

 
Notes
-----

Video files are automatically saved in MP4, I will try to provide more formats in the future. Size of the video can be altered via frameRate property. You can also decide if you want to take one video for every test (so this video is named after the test method) or take one video of whole testsuite (this is the default). Format for images is PNG, this can be changed to GIF or JPG.  
