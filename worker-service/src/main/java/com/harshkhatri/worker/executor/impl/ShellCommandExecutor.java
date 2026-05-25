package com.harshkhatri.worker.executor.impl;

import com.harshkhatri.worker.enums.JobType;
import com.harshkhatri.worker.executor.JobExecutor;
import com.harshkhatri.worker.model.JobTriggerPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ShellCommandExecutor implements JobExecutor {

    @Override
    public String execute(JobTriggerPayload payload) throws Exception {
        Map<String, Object> config = payload.jobConfig();

        String command = (String) config.get("command");
        String workingDir = (String) config.getOrDefault("workingDir", "/tmp");

        log.info("Executing SHELL job={} command={}", payload.jobId(), command);

        ProcessBuilder processBuilder = new ProcessBuilder()
                .command("sh", "-c", command)
                .directory(new java.io.File(workingDir))
                .redirectErrorStream(true);

        Process process = processBuilder.start();

        boolean finished = process.waitFor(
                payload.timeoutSeconds(), TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Shell command timed out after "
                    + payload.timeoutSeconds() + " seconds");
        }

        String output = new BufferedReader(
                new InputStreamReader(process.getInputStream()))
                .lines()
                .collect(Collectors.joining("\n"));

        int exitCode = process.exitValue();
        log.info("SHELL job={} exited with code={}", payload.jobId(), exitCode);

        if (exitCode != 0) {
            throw new RuntimeException("Shell command failed with exit code "
                    + exitCode + ": " + output);
        }

        return output;
    }

    @Override
    public JobType supports() {
        return JobType.SHELL_COMMAND;
    }
}
