package com.example.recruitmentbackend.util;

import java.io.File;
import java.util.List;

public class WordToPdfUtil {

    private static final String SOFFICE_PATH = "C:\\Program Files\\LibreOffice\\program\\soffice.exe";

    public static void convert(String inputPath, String outputDir) throws Exception {
        File inputFile = new File(inputPath);
        File outDir = new File(outputDir);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        List<String> command = List.of(
                SOFFICE_PATH,
                "--headless",
                "--norestore",
                "--convert-to", "pdf",
                "--outdir", outDir.getAbsolutePath(),
                inputFile.getAbsolutePath()
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (var is = process.getInputStream()) {
            is.transferTo(System.out);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("LibreOffice 转换失败，退出码: " + exitCode);
        }
    }
}