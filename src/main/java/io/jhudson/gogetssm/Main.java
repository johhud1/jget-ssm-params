package io.jhudson.gogetssm;

import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import io.github.verils.gotemplate.Template;
import io.github.verils.gotemplate.TemplateException;
import io.quarkus.logging.Log;
import io.quarkus.runtime.QuarkusApplication;
import io.smallrye.common.constraint.Assert;
import jakarta.inject.Inject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.Parameter;

@Command(
        name = "get-ssm-params",
        mixinStandardHelpOptions = true,
        description = "gets parameters from AWS SSM parameter store and outputs them to local environment",
        versionProvider = ManifestVersionProvider.class)
public class Main implements Runnable, QuarkusApplication  {

    @Option(
            names = {"-o", "--output"},
            description = "Format of the output that the SSM parameters will be written in",
            defaultValue = "JSON")
    private @MonotonicNonNull OutputOption output;

    @Option(
            names = {"-p", "--path"},
            defaultValue = "/")
    private @MonotonicNonNull String path;

    // this is not doing anything; see todo note on the SSM param
    @Option(
            names = {"-r", "--region"},
            defaultValue = "us-east-1",
            converter = RegionConverter.class)
    private @MonotonicNonNull Region region;

    @Option(
            names = {"-t", "--template"},
            description = "Path to a template file that will be used to render the output")
    private @Nullable Path templatePath;

    @Inject
    @MonotonicNonNull SsmClient ssm;
    // TODO: configure ssm client using parsed command line arguments
    // see
    // https://quarkus.io/guides/picocli#configure-cdi-beans-with-parsed-arguments
    @Inject
    CommandLine.IFactory factory; 

    public static void main(String[] args) {
        // this is a just a sample for validating that nullness checker is working
        // @NonNull String testing = null;

        int exitCode = new CommandLine(new Main()).execute(args);
        // TODO: handle exceptions? check what exit code picocli returns in case of
        // exception
        System.exit(exitCode);
    }

    /**
     * retrieve ssm parameters from region at path and store them in a local
     * variable
     * return 0 is successful -1 otherwise
     */
    // @Override
    // public Integer call() {
    //     try {
    //         checkNotNullParam("ouput", output);
    //         checkNotNullParam("path", path);
    //         checkNotNullParam("region", region);
    //         checkNotNullParam("ssm", ssm);

    //         Map<String, String> results =
    //                 ssm.getParametersByPath(generateGetParametersByPathRequest(path)).parameters().stream()
    //                         .collect(parametersToMap(path));
    //         Log.info("ssm parameters retrieved: " + results);
    //         renderOut(output, results);
    //     } catch (Exception e) {
    //         Log.error("error retrieving ssm parameters: " + e.getMessage());
    //         return -1;
    //     }
    //     return 0;
    // }

    @Override
    public void run() {
        // try {
            checkNotNullParam("ouput", output);
            checkNotNullParam("path", path);
            checkNotNullParam("region", region);
            checkNotNullParam("ssm", ssm);

            Map<String, String> results =
                    ssm.getParametersByPath(generateGetParametersByPathRequest(path)).parameters().stream()
                            .collect(parametersToMap(path));
            Log.info("ssm parameters retrieved: " + results);
            renderOut(output, results);
        // } catch (Exception e) {
        //     Log.error("error retrieving ssm parameters: " + e.getMessage());
        //     return -1;
        // }
        // return 0;
    }

    @Override
    public int run(String... args) throws Exception {
        return new CommandLine(this, factory).execute(args);
    }

    private static Collector<Parameter, ?, Map<String, String>> parametersToMap(String path) {
        return toMap(p -> p.name().substring(path.length()), Parameter::value);
    }

    private static GetParametersByPathRequest generateGetParametersByPathRequest(String path) {
        return GetParametersByPathRequest.builder()
                .path(path)
                .recursive(true)
                .withDecryption(true)
                .build();
    }

    // private PutParameterRequest generatePutParameterRequest(String name, String value, boolean secure) {
    //     return PutParameterRequest.builder()
    //             .name(path + name)
    //             .value(value)
    //             .type(secure ? ParameterType.SECURE_STRING : ParameterType.STRING)
    //             .overwrite(true)
    //             .build();
    // }

    // private GetParameterRequest generateGetParameterRequest(String name) {
    //     return GetParameterRequest.builder()
    //             .name(path + name)
    //             .withDecryption(true)
    //             .build();
    // }

    // uses reflection to prints out the fields of this class
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    private void renderOut(OutputOption output, Map<String, String> results) {
        System.out.println(
            switch (output) {
                case SHELL ->  renderOutShell(results);
                case JSON -> renderOutJson(results);
                case TEXT -> renderOutText(results);
                case TEMPLATE -> renderOutTemplate(results);
        });
    }

    //TODO: fix these render out methods
    private String renderOutShell(Map<String, String> results) {
        // StringBuilder sb = new StringBuilder();
        // results.entrySet().forEach(e -> sb.append("export " + e.getKey() + "=" + e.getValue()));
        // return sb.toString();
        return results.entrySet().stream()
            .map((e) -> "export " + e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("\n"));
    }

    private String renderOutJson(Map<String, String> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        results.entrySet().forEach(e -> sb.append("\"" + e.getKey() + "\": \"" + e.getValue() + "\","));
        sb.append("}");
        return sb.toString();
    }

    private String renderOutText(Map<String, String> results) {
        StringBuilder sb = new StringBuilder();
        results.entrySet().forEach(e -> sb.append(e.getKey() + "=" + e.getValue()));
        return sb.toString();
    }

    private String renderOutTemplate(Map<String, String> results) {
        checkNotNullParam("templatePath", templatePath);
        // Prepare you template
        Template template = new Template("demo");
        try {
            template.parse(Files.newBufferedReader(templatePath));

            // Execute and print out the result text
            StringWriter writer = new StringWriter();
            template.execute(writer, results);
            return writer.toString();
        } catch (TemplateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("contracts.postcondition")
    @EnsuresNonNull("#2")
    private void checkNotNullParam(String name, @Nullable Object value) {
        Assert.checkNotNullParam(name, value);
    }

    public static class RegionConverter implements ITypeConverter<Region> {
        public Region convert(String value) throws Exception {
            return Region.of(value);
        }
    }
}
