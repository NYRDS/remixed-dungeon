package com.nyrds.teavm;

import org.teavm.tooling.TeaVMTool;
import org.teavm.tooling.TeaVMToolLog;
import org.teavm.tooling.sources.DirectorySourceFileProvider;
import org.teavm.diagnostics.ProblemProvider;
import org.teavm.diagnostics.Problem;

import java.io.File;
import java.util.List;

public class TeaVMRunner {
    public static void main(String[] args) throws Exception {
        if (args.length < 6) {
            System.err.println("Usage: TeaVMRunner <mainClass> <targetDir> <targetFileName> <classpath> <sourcePath> <reflectionEnabled>");
            System.exit(1);
        }
        
        String mainClass = args[0];
        File targetDir = new File(args[1]);
        String targetFileName = args[2];
        String classpath = args[3];
        String sourcePath = args[4];
        boolean reflectionEnabled = Boolean.parseBoolean(args[5]);
        
        targetDir.mkdirs();
        
        // Set context classloader to ensure TeaVM can find all classes
        Thread.currentThread().setContextClassLoader(TeaVMRunner.class.getClassLoader());
        
        TeaVMTool tool = new TeaVMTool();
        tool.setMainClass(mainClass);
        tool.setTargetDirectory(targetDir);
        tool.setTargetFileName(targetFileName);
        
        // Add source file provider for main classes
        tool.addSourceFileProvider(new DirectorySourceFileProvider(new File(sourcePath)));
        
        // Enable reflection if requested
        if (reflectionEnabled) {
            tool.getProperties().put("org.teavm.reflect.enableRef", "true");
        }
        
        // Set up logging
        tool.setLog(new TeaVMToolLog() {
            @Override
            public void info(String message) {
                System.out.println(message);
            }
            @Override
            public void debug(String message) {
                System.out.println("DEBUG: " + message);
            }
            @Override
            public void warning(String message) {
                System.out.println("WARN: " + message);
            }
            @Override
            public void error(String message) {
                System.err.println(message);
            }
            @Override
            public void info(String message, Throwable throwable) {
                System.out.println(message);
                if (throwable != null) throwable.printStackTrace();
            }
            @Override
            public void debug(String message, Throwable throwable) {
                System.out.println("DEBUG: " + message);
                if (throwable != null) throwable.printStackTrace();
            }
            @Override
            public void warning(String message, Throwable throwable) {
                System.out.println("WARN: " + message);
                if (throwable != null) throwable.printStackTrace();
            }
            @Override
            public void error(String message, Throwable throwable) {
                System.err.println(message);
                if (throwable != null) throwable.printStackTrace();
            }
        });
        
        // Run the build
        tool.generate();
        
        // Check for problems
        ProblemProvider problemProvider = tool.getProblemProvider();
        List<Problem> problems = problemProvider.getProblems();
        if (!problems.isEmpty()) {
            System.err.println("TeaVM compilation had " + problems.size() + " problems:");
            for (Problem problem : problems) {
                System.err.println("  " + problem.getSeverity() + ": " + problem.getText());
                if (problem.getLocation() != null) {
                    System.err.println("    at " + problem.getLocation());
                }
            }
            System.exit(1);
        }
        
        System.out.println("TeaVM build successful!");
        System.out.println("Output: " + targetDir.getAbsolutePath() + "/" + targetFileName);
    }
}
