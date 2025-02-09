package io.jhudson.gogetssm;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;  

@QuarkusMainTest
public class MainIntegrationTest {

    // https://picocli.info/#_exception_exit_codes
    // By default, the execute method returns CommandLine.ExitCode.OK (0) on success,
    // CommandLine.ExitCode.SOFTWARE (1) when an exception occurred in the Runnable, Callable or command method,
    // and CommandLine.ExitCode.USAGE (2) for invalid input.
    // (These are common values according to this StackOverflow answer). This can be customized


    @Test
    @Launch(value = {"--path", "/"}, exitCode = 0)
    public void testAppBase(LaunchResult result) {
    }

    @Test
    @Launch(value = {"--path", "/", "--output", "SHELL"}, exitCode = 0)
    public void testAppBaseFormatShell(LaunchResult result) {
        Assertions.assertEquals(
            "export mypath/to-parameters/key2=value2\n" + 
            "export mypath/to-parameters/key1=value1",
            result.getOutput());
    }

    @Test
    @Launch(value = {"--path", "/", "--output", "TEXT"}, exitCode = 0)
    public void testAppBaseFormatText(LaunchResult result) {
        Assertions.assertEquals(
            "mypath/to-parameters/key2=value2\n"
            + "mypath/to-parameters/key1=value1",
            result.getOutput());
    }

    @Test
    @Launch(value = {"--path", "/", "--output", "JSON"}, exitCode = 0)
    public void testAppBaseFormatJson(LaunchResult result) {
        Assertions.assertEquals("{\"mypath/to-parameters/key2\": \"value2\",\"mypath/to-parameters/key1\": \"value1\"}", result.getOutput());
    }

    @Test
    @Launch(value = {"--output", "foobar"}, exitCode = 2)
    public void testAppWithInvalidOutput(LaunchResult result) {
        // You can also capture and assert the output if needed
    }

    @Test
    @Launch(value = {"--output", "TEMPLATE"}, exitCode = 1)
    public void testAppInvalidTemplateNoTemplatePath(LaunchResult result) {
        System.out.println(result.getOutput());
    }

    @Test
    @Launch(value = {"--output", "TEMPLATE", "--template", "/my/path/to/template"}, exitCode = 1)
    public void testAppValidTemplateWithTemplatePathFileNonExistent(LaunchResult result) {
    }

    @Test
    @Launch(value = {"--output", "TEMPLATE", "--template", "/home/jhudson/sources/jget-ssm-params/test.tpl"}, exitCode = 0)
    public void testAppValidTemplateWithTemplatePathFileExists(LaunchResult result) {
    }
}
