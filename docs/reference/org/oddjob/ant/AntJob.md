[HOME](../../../README.md)
# ant

Run a series of <a href="http://ant.apache.org">Ant</a>
tasks.



<em>Not all tasks have been tested!</em>

<h3>The Ant Project</h3>
Oddjob creates its own Ant project to use internally this project can be
shared between different AntJob jobs using the 'project' attribute. This
allows `taskdef`s and properties to be defined in one place and shared
in many jobs.

<h3>Property Expansion</h3>

Oddjob component properties can be referenced inside Ant tasks using
the ${id.property} notation. Ant will look up the Oddjob property
before it looks up properties defined with the &lt;property&gt; tag.
The oddjob derived properties of an Ant task aren't
constant. Oddjob variables can change unlike Ant properties.



Note: Ant looks up properties beginning with 'ant.' - Therefore <em>no
component can have an id of 'ant'</em> as the lookup will fail to retrieve the
properties from that component (unless of course the 'ant' component implements all
the properties that Ant requires!).

<h3>ClassLoaders and Task Definitions</h3>


A class path or class loader can be supplied and this will be used for
task definitions and `antlibs`. Name space antlibs will only work if
the antlib is placed in the oj-ant/lib directory. This is
because name space ant libs require the ant lib to be loaded in the same
class loader as Ant.

### Property Summary

| Property | Description |
| -------- | ----------- |
| [baseDir](#propertybaseDir) | The base directory. | 
| [classLoader](#propertyclassLoader) | An optional class loader which will be set as Ant's class loader. | 
| [classPath](#propertyclassPath) | A class path to use to create Ants class loader. | 
| [exception](#propertyexception) | How to handle build failure. | 
| [messageLevel](#propertymessageLevel) | The message level for output. one of DEBUG, ERROR, INFO, VERBOSE, WARN. | 
| [name](#propertyname) |  | 
| [output](#propertyoutput) | Where to write the resultant output from ant. | 
| [project](#propertyproject) | A reference to project in another ant job. | 
| [stop](#propertystop) |  | 
| [tasks](#propertytasks) | The ant XML configuration for the tasks. | 
| [version](#propertyversion) | The ant version. | 


### Example Summary

| Title | Description |
| ----- | ----------- |
| [Example 1](#example1) | Running an Ant Echo Task. |
| [Example 2](#example2) | Defining Ant properties in an Ant Job. |
| [Example 3](#example3) | Using Oddjob variables. |
| [Example 4](#example4) | Sharing a project. |
| [Example 5](#example5) | Working with files in Ant. |


### Property Detail
#### baseDir <a name="propertybaseDir"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The base directory. Equivalent to setting the
basedir attribute of an ant project.

#### classLoader <a name="propertyclassLoader"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

An optional class loader which will be set as
Ant's class loader.

#### classPath <a name="propertyclassPath"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A class path to use to create Ants class loader.
This is often more convenient than providing a separate class loader.
If a class loader is also provided then it will be used as the parent
class loader of the class loader created from this path, otherwise
the class loader of this job will be used as the parent.

#### exception <a name="propertyexception"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No, defaults to false.</td></tr>
</table>

How to handle build failure. If true, then a build
failure will in an EXCEPTION state for this job.

#### messageLevel <a name="propertymessageLevel"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

The message level for output. one of
DEBUG, ERROR, INFO, VERBOSE, WARN.

#### name <a name="propertyname"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
</table>



#### output <a name="propertyoutput"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No. By default the output will only be written to 
 the logger.</td></tr>
</table>

Where to write the resultant output from ant.

#### project <a name="propertyproject"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ATTRIBUTE</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>No.</td></tr>
</table>

A reference to project in another ant job.
This allows reference ids to be shared.

#### stop <a name="propertystop"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>



#### tasks <a name="propertytasks"></a>

<table style='font-size:smaller'>
      <tr><td><i>Configured By</i></td><td>ELEMENT</td></tr>
      <tr><td><i>Access</i></td><td>READ_WRITE</td></tr>
      <tr><td><i>Required</i></td><td>Yes.</td></tr>
</table>

The ant XML configuration for the tasks. The
document element for the task definitions is expected to be
&lt;tasks&gt;. Any type of content that is normally contained in
an Ant target is allowed as child elements of &lt;tasks&gt; including
properties and task definitions.

#### version <a name="propertyversion"></a>

<table style='font-size:smaller'>
      <tr><td><i>Access</i></td><td>READ_ONLY</td></tr>
</table>

The ant version.


### Examples
#### Example 1 <a name="example1"></a>

Running an Ant Echo Task. The resultant output is captured in a buffer.

```xml
<oddjob id='this'>
    <job>
        <ant id='an-ant'>
            <output>
                <identify id='result'>
                    <value>
                        <buffer/>
                    </value>
                </identify>
            </output>
            <tasks>
                <xml>
                    <tasks>
                        <echo message='${this.args[0]}'/>
                    </tasks>
                </xml>
            </tasks>
        </ant>
    </job>
</oddjob>
```


#### Example 2 <a name="example2"></a>

Defining Ant properties in an Ant Job.

```xml
<ant>
    <tasks>
        <xml>
            <tasks>
                <property name="test.thing" value="Test"/>
                <echo message='${test.thing}'/>
            </tasks>
        </xml>
    </tasks>
</ant>
```


#### Example 3 <a name="example3"></a>

Using Oddjob variables. Variables and properties defined in Oddjob are
available in the Ant tasks.

```xml
<oddjob id='this'>
    <job>
        <sequential>
            <jobs>
                <variables id='v'>
                    <fruit>
                        <value value='Apples'/>
                    </fruit>
                </variables>
                <ant>
                    <tasks>
                        <xml>
                            <tasks>
                                <taskdef name='result'
                                    classname='org.oddjob.ant.AntJobTest$ResultTask'/>
                                <property name='our.fruit'
                                    value='${v.fruit}'/>
                                <property name='v.fruit' value='Pears'/>
                                <result key='one' result='${our.fruit}'/>
                                <result key='two' result='${v.fruit}'/>
                            </tasks>
                        </xml>
                    </tasks>
                </ant>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


Note that the property defined in Ant does not override that defined
in Oddjob (as per the rules of Ant). the result of both one and two is
'Apples'

#### Example 4 <a name="example4"></a>

Sharing a project.

```xml
<oddjob>
    <job>
        <sequential>
            <jobs>
                <variables id='v'>
                    <fruit>
                        <value value='Apples'/>
                    </fruit>
                </variables>
                <ant id='defs'>
                    <tasks>
                        <xml>
                            <tasks>
                                <taskdef name='result'
                                    classname='org.oddjob.ant.AntJobTest$ResultTask'/>
                                <property name='our.fruit'
                                    value='${v.fruit}'/>
                                <property name='v.fruit' value='Pears'/>
                            </tasks>
                        </xml>
                    </tasks>
                </ant>
                <ant project='${defs.project}'>
                    <tasks>
                        <xml>
                            <tasks>
                                <property name='our.fruit'
                                    value='Pears'/>
                                <result key='three' result='${our.fruit}'/>
                                <result key='four' result='${v.fruit}'/>
                            </tasks>
                        </xml>
                    </tasks>
                </ant>
            </jobs>
        </sequential>
    </job>
</oddjob>
```


The first Ant job declares a task and properties that the second
Ant project can access.

#### Example 5 <a name="example5"></a>

Working with files in Ant.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<oddjob>
    <job>
        <sequential name="Using Ant to Manipulate Files">
            <jobs>
                <properties>
                    <values>
                        <value key="our.test.file.name" value="test.txt"/>
                    </values>
                </properties>
                <ant baseDir="${work.dir}">
                    <tasks>
                        <xml>
                            <tasks>
                                <patternset id="file.test">
                                    <include name="${our.test.file.name}"/>
                                </patternset>
                                <touch file="${our.test.file.name}"/>
                                <copy todir="..">
                                    <fileset dir=".">
                                        <patternset refid="file.test"/>
                                    </fileset>
                                </copy>
                                <delete file="../${our.test.file.name}"/>
                                <delete file="${our.test.file.name}"/>
                                <echo message="Done."/>
                            </tasks>
                        </xml>
                    </tasks>
                </ant>
            </jobs>
        </sequential>
    </job>
</oddjob>
```



-----------------------

<div style='font-size: smaller; text-align: center;'>(c) R Gordon Ltd 2005 - Present</div>
