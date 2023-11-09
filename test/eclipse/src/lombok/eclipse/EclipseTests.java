package lombok.eclipse;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import lombok.eclipse.cleanup.CleanupTest;
import lombok.eclipse.compile.NoErrorsTest;
import lombok.eclipse.edit.SelectTest;
import lombok.eclipse.refactoring.ExtractInterfaceTest;
import lombok.eclipse.refactoring.RenameTest;
import lombok.eclipse.references.FindReferencesTest;

@RunWith(Suite.class)
@SuiteClasses({ExtractInterfaceTest.class, RenameTest.class, SelectTest.class, CleanupTest.class, FindReferencesTest.class, NoErrorsTest.class})
public class EclipseTests {
	
}
