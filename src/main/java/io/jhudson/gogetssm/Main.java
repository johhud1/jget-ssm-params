package io.jhudson.gogetssm;

import static java.util.stream.Collectors.toMap;

import io.github.verils.gotemplate.Template;
import io.github.verils.gotemplate.TemplateException;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collector;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

@Command(
        name = "get-ssm-params",
        mixinStandardHelpOptions = true,
        description = "gets parameters from AWS SSM parameter store and outputs them to local environment")
public class Main implements Callable<Integer> {

    @Option(
            names = {"-o", "--output"},
            description = "Format of the output that the SSM parameters will be written in",
            defaultValue = "JSON")
    private OutputOption output;

    @Option(
            names = {"-p", "--path"},
            defaultValue = "/")
    private String path;

    @Option(
            names = {"-r", "--region"},
            defaultValue = "us-east-1",
            converter = RegionConverter.class)
    private Region region;

    @Option(
            names = {"-t", "--template"},
            description = "Path to a template file that will be used to render the output")
    private Path templatePath;

    @Inject
    SsmClient ssm;
    // TODO: configure ssm client using parsed command line arguments
    // see
    // https://quarkus.io/guides/picocli#configure-cdi-beans-with-parsed-arguments

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GetSsmParams()).execute(args);
        System.exit(exitCode);
    }

    /**
     * retrieve ssm parameters from region at path and store them in a local
     * variable
     * return 0 is successful -1 otherwise
     */
    @Override
    public Integer call() throws Exception {
        Map<String, String> results =
                ssm.getParametersByPath(generateGetParametersByPathRequest()).parameters().stream()
                        .collect(parametersToMap());
        Log.info("ssm parameters retrieved: " + results);
        renderOut(results);
        return 0;
    }

    private Collector<Parameter, ?, Map<String, String>> parametersToMap() {
        return toMap(p -> p.name().substring(path.length()), Parameter::value);
    }

    private GetParametersByPathRequest generateGetParametersByPathRequest() {
        return GetParametersByPathRequest.builder()
                .path(path)
                .recursive(true)
                .withDecryption(true)
                .build();
    }

    private PutParameterRequest generatePutParameterRequest(String name, String value, boolean secure) {
        return PutParameterRequest.builder()
                .name(path + name)
                .value(value)
                .type(secure ? ParameterType.SECURE_STRING : ParameterType.STRING)
                .overwrite(true)
                .build();
    }

    private GetParameterRequest generateGetParameterRequest(String name) {
        return GetParameterRequest.builder()
                .name(path + name)
                .withDecryption(true)
                .build();
    }

    // uses reflection to prints out the fields of this class
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    private void renderOut(Map<String, String> results) {
        switch (output) {
            case SHELL:
                renderOutShell(results);
                break;
            case JSON:
                renderOutJson(results);
                break;
            case TEXT:
                renderOutText(results);
                break;
            case TEMPLATE:
                renderOutTemplate(results);
                break;
        }
    }

    private void renderOutShell(Map<String, String> results) {
        results.entrySet().forEach(e -> System.out.println("export " + e.getKey() + "=" + e.getValue()));
    }

    private void renderOutJson(Map<String, String> results) {
        System.out.println(results);
    }

    private void renderOutText(Map<String, String> results) {
        results.entrySet().forEach(e -> System.out.println(e.getKey() + "=" + e.getValue()));
    }

    private void renderOutTemplate(Map<String, String> results) {
        // Prepare you template
        Template template = new Template("demo");
        try {
            template.parse(Files.newBufferedReader(templatePath));

            // Execute and print out the result text
            StringWriter writer = new StringWriter();
            template.execute(writer, results);
            System.out.print(writer.toString());
        } catch (TemplateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class RegionConverter implements ITypeConverter<Region> {
        public Region convert(String value) throws Exception {
            return Region.of(value);
        }
    }
}
