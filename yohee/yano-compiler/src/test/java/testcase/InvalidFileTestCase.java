package testcase;

import com.fula.fano.file2string;

/**
 * A test case for a non-existent file.
 */
@file2string("src/test/FakeTest.json")
public interface InvalidFileTestCase {

    String test();

}
