package org.oddjob.ant;

import org.junit.jupiter.api.*;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OddjobSrc;
import org.oddjob.OurDirs;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.io.FilesType;
import org.oddjob.oddballs.DirectoryOddballFactory;
import org.oddjob.oddballs.Oddball;
import org.oddjob.oddballs.OddballsDescriptorFactory;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.util.URLClassLoaderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@Tag("IntegrationTest")
public class OddballClassLoaderTest {

	private static final Logger logger = LoggerFactory.getLogger(
			OddballClassLoaderTest.class);
	

	@BeforeEach
	void setUp(TestInfo testInfo) {
		logger.info("---------------  " + testInfo.getDisplayName() + " ---------------");
		logger.info("stdout is " + System.out);
	}
	
    @AfterEach
	void tearDown() {

		logger.info("stdout is " + System.out);
	}
	
    @Test
	void testLoadOddball() throws Exception {
		
		Path oddjobApp = OddjobSrc.oddjobApp();

		FilesType oddjobLib = new FilesType();
		oddjobLib.setFiles(oddjobApp.toString() + "/lib/*.jar");
		
		File[] files = oddjobLib.toFiles();
		assertTrue(files.length > 0);
		
		File[] all = new File[files.length + 1];
		all[0] = OddjobSrc.appJar().toFile();

		System.arraycopy(files, 0, all, 1, files.length);
		
		URLClassLoaderType loaderType = new URLClassLoaderType();
		loaderType.setFiles(all);
		loaderType.setParent(getClass().getClassLoader());
		loaderType.configured();

		Path oddballDir = OurDirs.buildDirPath()
				.resolve("oddball");

		Oddball oddball = DirectoryOddballFactory.from(oddballDir.toFile())
				.createFrom(loaderType.toValue());
	
		ArooaDescriptor descriptor = oddball.getArooaDescriptor();
		
		ArooaClass cl = descriptor.getElementMappings(
				).mappingFor(
						new ArooaElement("ant"), 
						new InstantiationContext(ArooaType.COMPONENT, null));
		
		assertNotNull(cl);
	}

    @Test
	public void testClassLoaderOfTasksCreated() 
	throws Exception {

		OurDirs base = new OurDirs();

		ClassLoader existing = Thread.currentThread().getContextClassLoader();
		
		Thread.currentThread().setContextClassLoader(null);
		
		try {
			Path oddjobApp = OddjobSrc.oddjobApp();
			
			FilesType oddjobLib = new FilesType();
			oddjobLib.setFiles(oddjobApp + "/lib/*.jar");
			
			File[] files = oddjobLib.toFiles();
			assertTrue(files.length > 0);
			
			File[] all = new File[files.length + 2];
			all[0] = OddjobSrc.appJar().toFile();
			// needed for log4j to ensure we don't pick up a test version
			// because System.out will already be capturing log messages from
			// previous tests - we don't want these in our console capture.
			all[1] = base.relative("test/launch");
			System.arraycopy(files, 0, all, 2, files.length);
			
			URLClassLoaderType loaderType = new URLClassLoaderType();
			loaderType.setFiles(all);
			loaderType.setParent(ClassLoader.getPlatformClassLoader());
			loaderType.configured();
			
			ClassLoader loader = loaderType.toValue();
			
			Class<?> ojClass = loader.loadClass(
					Oddjob.class.getName());

			Runnable oddjob = (Runnable) ojClass.getConstructor().newInstance();
	
			Object oddball = loader.loadClass(
					OddballsDescriptorFactory.class.getName())
					.getConstructor().newInstance();
			
			BeanUtilsPropertyAccessor accessor = 
				new BeanUtilsPropertyAccessor();

			File oddballDir = base.relative("target/oddball");

			accessor.setProperty(oddball, "files", 
					new File[] { oddballDir } );
			accessor.setProperty(oddjob, "descriptorFactory", oddball);
					
			accessor.setProperty(oddjob, "file", 
					base.relative("test/files/classloader-test.xml"));
			
			ConsoleCapture console = new ConsoleCapture();
			try (ConsoleCapture.Close ignored = console.captureConsole()) {
				
				oddjob.run();		
			}
			
			console.dump(logger);
						
			String[] lines = console.getLines();
			
			assertEquals(2, lines.length);

			
			assertEquals("     [echo] Oddball ClassLoader", 
					lines[0].substring(0, 31));
			
		}
		finally {
			Thread.currentThread().setContextClassLoader(existing);
		}
	}
	
    @Test
	public void testClassLoaderOfSubProject() 
	throws IOException, ArooaPropertyException, ArooaConversionException {

		OurDirs base = new OurDirs();

		Path oddjobApp = OddjobSrc.oddjobApp();

		FilesType oddjobLib = new FilesType();
		oddjobLib.setFiles(oddjobApp + "/lib/*.jar");
		
		File[] files = oddjobLib.toFiles();
		assertTrue(files.length > 0);
		
		URLClassLoaderType loaderType = new URLClassLoaderType();
		loaderType.setFiles(files);
		loaderType.setParent(getClass().getClassLoader());
		loaderType.configured();
		
		Oddjob oddjob = new Oddjob();
		oddjob.setClassLoader(loaderType.toValue());

		Path oddballDir = OurDirs.buildDirPath().resolve("oddball");

		OddballsDescriptorFactory oddball = 
			new OddballsDescriptorFactory();
		oddball.setFiles(new File[] { oddballDir.toFile() });
		oddjob.setDescriptorFactory(oddball);

		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <ant id='myant'>" +
			"   <output>" +
			"    <identify id='ant-output'>" +
			"     <value>" +
			"      <buffer/>" +
			"     </value>" +
			"    </identify>" +
			"   </output>" +
			"   <tasks>" +
			"    <xml>" +
			"     <tasks>" +
			"      <ant antfile='" + 
						base.relative("test/files/AntCallsOddjobTaskWithTaskDef.xml") + "' " +
						"dir='" + base.relative("test/files") + "'/>" +
			"     </tasks>" +
			"    </xml>" +
			"   </tasks>" +
			"  </ant>" +
			" </job>" +
			"</oddjob>";
			
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close ignored = console.captureConsole()) {
			
			oddjob.run();
		}

		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.dump(logger);
		
		String buffer = new OddjobLookup(oddjob).lookup(
				"ant-output", String.class);
		
		assertTrue(buffer.contains("COMPLETE"));
		
		ClassLoader classLoader = new OddjobLookup(oddjob).lookup(
				"myant.class.classLoader", ClassLoader.class);
		
		
		
		String[] lines = console.getLines();
		
		assertEquals(1, lines.length);
		
		assertEquals(classLoader.toString(), lines[0]);
	}


}
