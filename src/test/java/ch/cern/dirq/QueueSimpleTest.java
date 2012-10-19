package ch.cern.dirq;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import ch.cern.dirq.extra.TestDirq;
import ch.cern.mig.posix.Posix;
import ch.cern.mig.posix.Timeval;
import ch.cern.mig.utils.FileUtils;

/**
 * Unit test for {@link ch.cern.dirq.QueueSimple}.
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2012
 */
public class QueueSimpleTest extends QueueTest {
	public static final String qsPath = dir + "qs/";

	/**
	 * Create the test case.
	 * 
	 * @param name name of the test case
	 */
	public QueueSimpleTest(String name) {
		super(name);
	}

	/**
	 * Test addDir.
	 * 
	 * @throws QueueException
	 */
	public void testAddDir() throws QueueException {
		QueueSimple qs = new QueueSimple(qsPath);
		String dirname = qs._addDir();
		assertEquals(8, dirname.length());
	}

	/**
	 * Test queue creation.
	 * 
	 * @throws QueueException
	 */
	public void testCreation() throws QueueException {
		QueueSimple qs = new QueueSimple(qsPath);
		assertEquals(qsPath, qs.getPath());
		assertTrue(new File(qsPath).isDirectory());
	}

	/**
	 * Test add operation.
	 * 
	 * @throws QueueException
	 */
	public void testAdd() throws QueueException {
		QueueSimple qs = new QueueSimple(qsPath);
		String data = "abc";
		String elem = qs.add(data);
		assertTrue(new File(qsPath + File.separator + elem).exists());
		assertEquals(data, FileUtils.fileRead(qsPath + File.separator + elem));
	}

	/**
	 * Test addPath operation.
	 * 
	 * @throws IOException
	 * @throws QueueException
	 */
	public void testAddPath() throws IOException, QueueException {
		QueueSimple qs = new QueueSimple(qsPath);
		String data = "abc";
		String tmpDir = qsPath + File.separator + "elems";
		Posix.posix.mkdir(tmpDir);
		String tmpName = tmpDir + File.separator + "elem.tmp";
		File tmpFile = new File(tmpName);
		tmpFile.createNewFile();
		FileUtils.writeToFile(tmpFile, data);
		assertTrue(new File(tmpName).exists());
		String newName = qs.addPath(tmpName);
		assertFalse(new File(tmpName).exists());
		assertTrue(new File(qsPath + File.separator + newName).exists());
		// assertEquals(1, new File(tmpDir).listFiles().length);
		assertEquals(data,
				FileUtils.fileRead(qsPath + File.separator + newName));
	}

	/**
	 * Test lock/unlock operations.
	 * 
	 * @throws Exception
	 */
	public void testLockUnlock() throws Exception {
		QueueSimple qs = new QueueSimple(qsPath);
		String data = "abc";
		String elemName = "foobar";
		String elemPath = qsPath + File.separator + elemName;
		FileUtils.writeToFile(elemPath, data);
		assertTrue(qs.lock(elemName));
		assertTrue(new File(elemPath + QueueSimple.LOCKED_SUFFIX).exists());
		qs.unlock(elemName);
	}

	/**
	 * Test get operation.
	 * 
	 * @throws Exception
	 */
	public void testGet() throws Exception {
		QueueSimple qs = new QueueSimple(qsPath);
		String data = "abc";
		String elem = qs.add(data);
		qs.lock(elem);
		assertEquals(data, qs.get(elem));
	}

	/**
	 * Test count operation.
	 * 
	 * @throws Exception
	 */
	public void testCount() throws Exception {
		QueueSimple qs = new QueueSimple(qsPath);
		String data = "abc";
		qs.add(data);
		assertEquals(1, qs.count());
		String inDir = new File(qsPath).listFiles()[0].getPath();
		new File(inDir + File.separator + "foo.bar").createNewFile();
		assertEquals(1, qs.count());
	}

	/**
	 * Test remove operation.
	 * 
	 * @throws Exception
	 */
	public void testRemove() throws Exception {
		QueueSimple qs = new QueueSimple(qsPath);
		String data = "abc";
		for (int i = 0; i < 5; i++) {
			qs.add(data);
		}
		assertEquals(5, qs.count());
		for (String element : qs) {
			qs.lock(element);
			qs.remove(element);
		}
		assertEquals(0, qs.count());
	}

	/**
	 * Test purge basic operation.
	 * 
	 * @throws QueueException
	 */
	public void testPurgeBasic() throws QueueException {
		QueueSimple qs = new QueueSimple(qsPath);
		qs.purge();
		qs.purge(0, 0);
		qs.add("abc");
		assertEquals(1, qs.count());
		qs.purge();
	}

	/**
	 * Test purge one dir operation.
	 * 
	 * @throws Exception
	 */
	public void testPurgeOneDir() throws Exception {
		QueueSimple qs = new QueueSimple(qsPath);
		qs.add("abc");
		assertEquals(1, qs.count());
		String elem = qs.iterator().next();
		qs.lock(elem);
		String elemPathLock = qs.getPath() + File.separator + elem
				+ QueueSimple.LOCKED_SUFFIX;
		assertTrue(new File(elemPathLock).exists());
		Thread.sleep(2000);
		qs.purge(1);
		assertFalse(new File(elemPathLock).exists());
		assertEquals(1, qs.count());
		assertEquals(1, new File(qs.getPath()).listFiles().length);
	}

	/**
	 * Test purge multi dir operation.
	 * 
	 * @throws Exception
	 */
	public void testPurgeMultiDir() throws Exception {
		QueueSimple qs = new QueueSimple(qsPath);
		File qsPath = new File(qs.getPath());
		qs.add("foo");
		assertEquals(1, qs.count());
		assertEquals(1, qsPath.listFiles().length);
		qs.add("bar");
		assertEquals(2, qs.count());
		assertEquals(2, qsPath.listFiles().length);
		qs.purge();
		assertEquals(2, qs.count());

		String elem = qs.iterator().next();
		qs.lock(elem);
		qs.remove(elem);
		assertEquals(1, qs.count());
		qs.purge();
		assertEquals(1, qsPath.listFiles().length);

		qs.add("abc");
		assertEquals(2, qs.count());
		assertEquals(2, qsPath.listFiles().length);
		for (String element : qs) {
			qs.lock(element);
		}
		Iterator<String> it = qs.iterator();
		String elem1 = it.next();
		String lockPath1 = qs.getPath() + File.separator + elem1
				+ QueueSimple.LOCKED_SUFFIX;
		assertTrue(new File(lockPath1).exists());
		long[] backInTime = new long[] {
				(System.currentTimeMillis() / 1000) - 25, 0 };
		Timeval[] timeval = (Timeval[]) new Timeval().toArray(2);
		timeval[0].setTime(backInTime);
		timeval[1].setTime(backInTime);
		Posix.posix.utimes(lockPath1, timeval);
		qs.purge(10);
		assertFalse(new File(lockPath1).exists());

		//assertEquals(2, qs.count());
		String elem2 = it.next();
		String lockPath2 = qs.getPath() + File.separator + elem2
				+ QueueSimple.LOCKED_SUFFIX;
		assertTrue(new File(lockPath2).exists());
	}
	
	/**
	 * Multi test.
	 * 
	 * @throws Exception
	 */
	public void testMulti() throws Exception {
		System.out.println("################ TestDirq simple ################## BEGIN");
		new TestDirq().mainSimple();
		System.out.println("################ TestDirq simple ################## END");
	}
}