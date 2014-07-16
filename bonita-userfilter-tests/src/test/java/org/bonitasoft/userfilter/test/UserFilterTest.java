package org.bonitasoft.userfilter.test;

import org.bonitasoft.engine.BonitaSuiteRunner;
import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.TestsInitializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(BonitaSuiteRunner.class)
@Initializer(TestsInitializer.class)
@SuiteClasses({
        SingleUserFilterTest.class,
        UserManagerFilterTest.class,
        ProcessInitiatorUserFilterTest.class,
        ProcessinitiatorManagerUserFilterTest.class,
        SameTaskUserFilterTest.class,
        CustomUserInfoUserFilterIT.class
        })
public class UserFilterTest {

}
