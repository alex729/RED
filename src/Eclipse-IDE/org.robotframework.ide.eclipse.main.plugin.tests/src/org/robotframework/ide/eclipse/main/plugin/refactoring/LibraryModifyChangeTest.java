/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Objects;

import org.assertj.core.api.Condition;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ltk.core.refactoring.Change;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.red.junit.ProjectProvider;

public class LibraryModifyChangeTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(LibraryModifyChangeTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.configure();
    }

    @Test
    public void checkChangeName() {
        final ReferencedLibrary libraryToModify = ReferencedLibrary.create(LibraryType.PYTHON, "c", "a/b");
        final ReferencedLibrary modifiedLibrary = ReferencedLibrary.create(LibraryType.PYTHON, "d", "x/y");

        final LibraryModifyChange change = new LibraryModifyChange(projectProvider.getFile("red.xml"),
                libraryToModify, modifiedLibrary);

        assertThat(change.getName()).isEqualTo("The library 'c' (a/b) will be changed to 'd' (x/y)");
        assertThat(change.getModifiedElement()).isSameAs(libraryToModify);
    }

    @Test
    public void libraryIsModified_whenChangeIsPerformed() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final ReferencedLibrary libraryToModify = ReferencedLibrary.create(LibraryType.PYTHON, "c", "a/b");
        config.addReferencedLibrary(libraryToModify);

        final ReferencedLibrary modifiedLibrary = ReferencedLibrary.create(LibraryType.PYTHON, "d", "x/y");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final LibraryModifyChange change = new LibraryModifyChange(projectProvider.getFile("red.xml"),
                libraryToModify, modifiedLibrary, eventBroker);

        change.initializeValidationData(null);
        assertThat(change.isValid(null).isOK()).isTrue();
        final Change undoOperation = change.perform(null);

        assertThat(undoOperation).isInstanceOf(LibraryModifyChange.class);
        assertThat(config.getLibraries()).hasSize(1);
        assertThat(config.getLibraries().get(0))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "d", "x/y")));
        verify(eventBroker, times(1)).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED),
                any(RedProjectConfigEventData.class));

        undoOperation.perform(null);
        assertThat(config.getLibraries()).hasSize(1);
        assertThat(config.getLibraries().get(0))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "c", "a/b")));
    }

    private static Condition<? super ReferencedLibrary> sameFieldsAs(final ReferencedLibrary library) {
        return new Condition<ReferencedLibrary>() {

            @Override
            public boolean matches(final ReferencedLibrary toMatch) {
                return Objects.equals(library.getType(), toMatch.getType())
                        && Objects.equals(library.getName(), toMatch.getName())
                        && Objects.equals(library.getPath(), toMatch.getPath());
            }
        };
    }
}
