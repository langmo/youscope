@rem   Running this script will remove all Eclipse project files (folders named
@rem   .settings and files named .classpath) from the repository. Even though we gitignore
@rem   these files, being able to remove them locally comes in handy e.g. when having
@rem   changed the repository/project structure and wanting to reimport the repositroy
@rem   into Eclipse.

DEL /S /Q .classpath .project
forfiles /p ..\..\ /s /m .settings /c "cmd /c rmdir /s /q @path"
