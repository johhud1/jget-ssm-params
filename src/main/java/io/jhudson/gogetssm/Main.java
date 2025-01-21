package io.jhudson.gogetssm;

import java.util.concurrent.Callable;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "get-ssm-params",
        mixinStandardHelpOptions = true,
        description = "gets parameters from AWS SSM parameter store and outputs them to local environment")
public class Main implements Callable<Integer> {

    @Option(
            names = {"-o", "--output"},
            description = "Format of the output that the SSM parameters will be written in")
    private OutputOption output;

    @Option(names = {"-p", "--path"})
    private String path; // make typed?

    @Option(names = {"-r", "--region"})
    private String region; // make typed?

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GetSsmParams()).execute(args);
        System.exit(exitCode);
    }

    /**
     * retrieve ssm parameters from region at path and store them in a local variable
     * return 0 is successful -1 otherwise
     */
    @Override
    public Integer call() throws Exception {
        // System.out.println("invoked: " + this.toString());

        return 0;
    }

    // uses reflection to prints out the fields of this class
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
